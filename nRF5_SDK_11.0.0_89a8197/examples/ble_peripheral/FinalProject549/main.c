
/** @file
 *
 * @defgroup ble_sdk_app_template_main main.c
 * @{
 * @ingroup ble_sdk_app_template
 * @brief Template project main file.
 *
 */

#include <stdint.h>
#include <string.h>
#include "nordic_common.h"
#include "nrf.h"
#include "app_error.h"
#include "ble.h"
#include "ble_hci.h"
#include "ble_srv_common.h"
#include "ble_advdata.h"
#include "ble_advertising.h"
#include "ble_conn_params.h"
#include "boards.h"
#include "softdevice_handler.h"
#include "app_timer.h"
#include "device_manager.h"
#include "pstorage.h"
#include "app_trace.h"
#include "bsp.h"
#include "bsp_btn_ble.h"
#include "app_util_platform.h"
#include "nrf_drv_twi.h"
#include "nrf_delay.h"

#include "our_service.h"
#include "device_info_service.h"
#include "temperature_service.h"
#include "flash_operations.h"
#include "SEGGER_RTT.h"
#include "app_uart.h"

/*UART buffer size. */
#define UART_TX_BUF_SIZE 256
#define UART_RX_BUF_SIZE 1

#define IS_SRVC_CHANGED_CHARACT_PRESENT  1                                          /**< Include or not the service_changed characteristic. if not enabled, the server's database cannot be changed for the lifetime of the device*/

#define CENTRAL_LINK_COUNT               0                                          /**<number of central links used by the application. When changing this number remember to adjust the RAM settings*/
#define PERIPHERAL_LINK_COUNT            1                                          /**<number of peripheral links used by the application. When changing this number remember to adjust the RAM settings*/

#define DEVICE_NAME                      "Little_Brother"	                         					/**< Name of device. Will be included in the advertising data. */
#define MANUFACTURER_NAME                "NordicSemiconductor"                      /**< Manufacturer. Will be passed to Device Information Service. */
#define APP_ADV_INTERVAL                 300                                        /**< The advertising interval (in units of 0.625 ms. This value corresponds to 25 ms). */
#define APP_ADV_TIMEOUT_IN_SECONDS       180                                        /**< The advertising timeout in units of seconds. */

#define APP_TIMER_PRESCALER              0                                          /**< Value of the RTC1 PRESCALER register. */
#define APP_TIMER_OP_QUEUE_SIZE          4                                          /**< Size of timer operation queues. */

#define MIN_CONN_INTERVAL                MSEC_TO_UNITS(100, UNIT_1_25_MS)           /**< Minimum acceptable connection interval (0.1 seconds). */
#define MAX_CONN_INTERVAL                MSEC_TO_UNITS(200, UNIT_1_25_MS)           /**< Maximum acceptable connection interval (0.2 second). */
#define SLAVE_LATENCY                    0                                          /**< Slave latency. */
#define CONN_SUP_TIMEOUT                 MSEC_TO_UNITS(4000, UNIT_10_MS)            /**< Connection supervisory timeout (4 seconds). */

#define FIRST_CONN_PARAMS_UPDATE_DELAY   APP_TIMER_TICKS(5000, APP_TIMER_PRESCALER) /**< Time from initiating event (connect or start of notification) to first time sd_ble_gap_conn_param_update is called (5 seconds). */
#define NEXT_CONN_PARAMS_UPDATE_DELAY    APP_TIMER_TICKS(30000, APP_TIMER_PRESCALER)/**< Time between each call to sd_ble_gap_conn_param_update after the first call (30 seconds). */
#define MAX_CONN_PARAMS_UPDATE_COUNT     3                                          /**< Number of attempts before giving up the connection parameter negotiation. */

#define SEC_PARAM_BOND                   1                                          /**< Perform bonding. */
#define SEC_PARAM_MITM                   0                                          /**< Man In The Middle protection not required. */
#define SEC_PARAM_IO_CAPABILITIES        BLE_GAP_IO_CAPS_NONE                       /**< No I/O capabilities. */
#define SEC_PARAM_OOB                    0                                          /**< Out Of Band data not available. */
#define SEC_PARAM_MIN_KEY_SIZE           7                                          /**< Minimum encryption key size. */
#define SEC_PARAM_MAX_KEY_SIZE           16                                         /**< Maximum encryption key size. */
#define TEST_STORAGE_VAL                 0x12345678
#define RAM_LOG_STORAGE                  256

#define DEAD_BEEF                        0xDEADBEEF                            /**< Value used as error code on stack dump, can be used to identify stack location on stack unwind. */
#define ACII_SPACE                       32

static dm_application_instance_t        m_app_handle;                               /**< Application identifier allocated by device manager */

static uint16_t                          m_conn_handle = BLE_CONN_HANDLE_INVALID;   /**< Handle of the current connection. */

//Declare service structures
ble_os_t m_our_service;
ble_dis_t device_info_service;
ble_tss_t temperature_service;

//Declare global flash variables
flash_addresses flash_addr;
bool flash_op = true;
pstorage_handle_t       base_handle;

//log sending globals
uint32_t ram_store_idx = 0;
uint32_t log_send_idx = 0;
bool wrapped = false;
bool SENDING_LOGS = false;
temp_log temp_ram_storage[RAM_LOG_STORAGE];
bool end_log_sent = false;


// Declare an app_timer id + define timer interval and define a timer interval
APP_TIMER_DEF(m_our_char_timer_id);
#define OUR_CHAR_TIMER_INTERVAL     APP_TIMER_TICKS(3000, APP_TIMER_PRESCALER) // 3000 ms intervals
                                   
/**@brief Callback function for asserts in the SoftDevice.
 *
 * @details This function will be called in case of an assert in the SoftDevice.
 *
 * @warning This handler is an example only and does not fit a final product. You need to analyze
 *          how your product is supposed to react in case of Assert.
 * @warning On assert from the SoftDevice, the system can only recover on reset.
 *
 * @param[in] line_num   Line number of the failing ASSERT call.
 * @param[in] file_name  File name of the failing ASSERT call.
 */
void assert_nrf_callback(uint16_t line_num, const uint8_t * p_file_name)
{
    app_error_handler(DEAD_BEEF, line_num, p_file_name);
}

static void power_manage(void);

// flash storage event handler
static void example_cb_handler(pstorage_handle_t  * handle,
                               uint8_t              op_code,
                               uint32_t             result,
                               uint8_t            * p_data,
                               uint32_t             data_len)
{
    flash_op = false;
}

// Timer event handler
static void timer_timeout_handler(void * p_context)
{
    uint32_t err_code;
    int32_t temperature = 0;
    static int32_t previous_temperature = 0;
    static uint32_t time = 0;
    static uint32_t log_id_count = 1;
    
    // Get temperature from on chip sensor
    sd_temp_get(&temperature);
      
    //only store new logs if old ones aren't being sent
    if(!SENDING_LOGS && DEVICE_ID != -1 && SENSORS_ON){
      for(int i = 0; i<SENSOR_NUM; i++){
        if(SENSOR_NAMES[i][0] != ' '){
          temp_ram_storage[ram_store_idx].id = SENSOR_NAMES[i][0];
          temp_ram_storage[ram_store_idx].log_id = log_id_count;
          temp_ram_storage[ram_store_idx].time = time*3;
          temp_ram_storage[ram_store_idx].value = temperature;
          printf("Log Added\n");
        
          log_id_count+=1;
          //update log storage data
          ram_store_idx = (ram_store_idx+1)%RAM_LOG_STORAGE;
          if(ram_store_idx == log_send_idx){
            wrapped = true;
          }
        }
      }

      time+=1;
    }
    //static bool first_connection = true;
//    if(first_connection){
//      first_connection = false;
//      //store log in characteristic
//      log_buff = most_recent_log;
//      //update log_num characteristic
//      num_log_buff+=1;
//    } else {
//      //store logs in ram
//      temp_ram_storage[ram_store_idx] = most_recent_log;
//      ram_store_idx = (ram_store_idx+1)%RAM_LOG_STORAGE;
//      num_log_buff+=1;
//    }
}


/**@brief Function for the Timer initialization.
 *
 * @details Initializes the timer module. This creates and starts application timers.
 */
static void timers_init(void)
{
    // Initialize timer module.
    APP_TIMER_INIT(APP_TIMER_PRESCALER, APP_TIMER_OP_QUEUE_SIZE, false);

    // Initiate timer
    app_timer_create(&m_our_char_timer_id, APP_TIMER_MODE_REPEATED, timer_timeout_handler);
}


/**@brief Function for the GAP initialization.
 *
 * @details This function sets up all the necessary GAP (Generic Access Profile) parameters of the
 *          device including the device name, appearance, and the preferred connection parameters.
 */
static void gap_params_init(void)
{
    uint32_t                err_code;
    ble_gap_conn_params_t   gap_conn_params;
    ble_gap_conn_sec_mode_t sec_mode;

    BLE_GAP_CONN_SEC_MODE_SET_OPEN(&sec_mode);

    err_code = sd_ble_gap_device_name_set(&sec_mode,
                                          (const uint8_t *)DEVICE_NAME,
                                          strlen(DEVICE_NAME));
    APP_ERROR_CHECK(err_code);

    memset(&gap_conn_params, 0, sizeof(gap_conn_params));

    gap_conn_params.min_conn_interval = MIN_CONN_INTERVAL;
    gap_conn_params.max_conn_interval = MAX_CONN_INTERVAL;
    gap_conn_params.slave_latency     = SLAVE_LATENCY;
    gap_conn_params.conn_sup_timeout  = CONN_SUP_TIMEOUT;

    err_code = sd_ble_gap_ppcp_set(&gap_conn_params);
    APP_ERROR_CHECK(err_code);
}

/**@brief Function for initializing services that will be used by the application.
 */
static void services_init(void)
{
    divice_info_service_init(&device_info_service);
    temperature_service_init(&temperature_service);
}


/**@brief Function for handling the Connection Parameters Module.
 *
 * @details This function will be called for all events in the Connection Parameters Module which
 *          are passed to the application.
 *          @note All this function does is to disconnect. This could have been done by simply
 *                setting the disconnect_on_fail config parameter, but instead we use the event
 *                handler mechanism to demonstrate its use.
 *
 * @param[in] p_evt  Event received from the Connection Parameters Module.
 */
static void on_conn_params_evt(ble_conn_params_evt_t * p_evt)
{
    uint32_t err_code;

    if (p_evt->evt_type == BLE_CONN_PARAMS_EVT_FAILED)
    {
        err_code = sd_ble_gap_disconnect(m_conn_handle, BLE_HCI_CONN_INTERVAL_UNACCEPTABLE);
        APP_ERROR_CHECK(err_code);
    }
}


/**@brief Function for handling a Connection Parameters error.
 *
 * @param[in] nrf_error  Error code containing information about what went wrong.
 */
static void conn_params_error_handler(uint32_t nrf_error)
{
    APP_ERROR_HANDLER(nrf_error);
}


/**@brief Function for initializing the Connection Parameters module.
 */
static void conn_params_init(void)
{
    uint32_t               err_code;
    ble_conn_params_init_t cp_init;

    memset(&cp_init, 0, sizeof(cp_init));

    cp_init.p_conn_params                  = NULL;
    cp_init.first_conn_params_update_delay = FIRST_CONN_PARAMS_UPDATE_DELAY;
    cp_init.next_conn_params_update_delay  = NEXT_CONN_PARAMS_UPDATE_DELAY;
    cp_init.max_conn_params_update_count   = MAX_CONN_PARAMS_UPDATE_COUNT;
    cp_init.start_on_notify_cccd_handle    = BLE_GATT_HANDLE_INVALID;
    cp_init.disconnect_on_fail             = false;
    cp_init.evt_handler                    = on_conn_params_evt;
    cp_init.error_handler                  = conn_params_error_handler;

    err_code = ble_conn_params_init(&cp_init);
    APP_ERROR_CHECK(err_code);
}


/**@brief Function for starting timers.
*/
static void application_timers_start(void)
{
    // To update temperature only when in a connection then don't call app_timer_start() here, but in on_ble_evt().
    app_timer_start(m_our_char_timer_id, OUR_CHAR_TIMER_INTERVAL, NULL);
}


/**@brief Function for putting the chip into sleep mode.
 *
 * @note This function will not return.
 */
static void sleep_mode_enter(void)
{
    uint32_t err_code = bsp_indication_set(BSP_INDICATE_IDLE);
    APP_ERROR_CHECK(err_code);

    // Prepare wakeup buttons.
    err_code = bsp_btn_ble_sleep_mode_prepare();
    APP_ERROR_CHECK(err_code);

    // Go to system-off mode (this function will not return; wakeup will cause a reset).
    err_code = sd_power_system_off();
    APP_ERROR_CHECK(err_code);
}


/**@brief Function for handling advertising events.
 *
 * @details This function will be called for advertising events which are passed to the application.
 *
 * @param[in] ble_adv_evt  Advertising event.
 */
static void on_adv_evt(ble_adv_evt_t ble_adv_evt)
{
    uint32_t err_code;

    switch (ble_adv_evt)
    {
        case BLE_ADV_EVT_FAST:
            err_code = bsp_indication_set(BSP_INDICATE_ADVERTISING);
            APP_ERROR_CHECK(err_code);
            break;
        case BLE_ADV_EVT_IDLE:
            sleep_mode_enter();
            break;
        default:
            break;
    }
}

void init_log_send_idx(){
  if(wrapped){
    log_send_idx = (ram_store_idx+2) % RAM_LOG_STORAGE;
    wrapped = false;
  }
}

void send_logs(){
  uint32_t err_code;
  if(log_send_idx == ram_store_idx && !end_log_sent){

    temp_log end_log;
    end_log.id = 0;
    end_log.log_id = 0;
    end_log.value = 0;
    end_log.time = 0;    
    err_code = termperature_characteristic_update(&temperature_service,
                                           end_log);
    end_log_sent = true;
    printf("End Log Sent\n");
    return;
  } else if(log_send_idx == ram_store_idx){
    printf("Wait till disconnection. End Log already sent.\n");
    return;
  }

  SENDING_LOGS = true;
  printf("SENDING LOGS\n");
  while(true){
    //attempt to send log
    temperature_service.conn_handle = m_app_handle;
    
    err_code = termperature_characteristic_update(&temperature_service,
                                           temp_ram_storage[log_send_idx]);
     
    //check if data transfer went through
    if (err_code == BLE_ERROR_NO_TX_PACKETS)
    {  
        printf("Buffer Full\n");
        break;
    }
    else if (err_code != NRF_SUCCESS) 
    {
        APP_ERROR_HANDLER(err_code);
    }
    
    //update send index if data transfer was queued
    log_send_idx = (log_send_idx+1) % RAM_LOG_STORAGE;
    
    printf("Log Sent\n");
    //check if all logs have been sent
    if(log_send_idx == ram_store_idx){
      printf("STOP SENDING LOGS\n");
      SENDING_LOGS = false;
      break;
    }  
    
  }
}

/**@brief Function for handling the Application's BLE Stack events.
 *
 * @param[in] p_ble_evt  Bluetooth stack event.
 */
static void on_ble_evt(ble_evt_t * p_ble_evt)
{
    uint32_t err_code;
    ble_gatts_evt_write_t * p_evt_write;

    switch (p_ble_evt->header.evt_id)
            {
        case BLE_GAP_EVT_CONNECTED:
            printf("BLE_GAP_EVT_CONNECTED\n");
            end_log_sent = false;
            err_code = bsp_indication_set(BSP_INDICATE_CONNECTED);
            APP_ERROR_CHECK(err_code);
            m_conn_handle = p_ble_evt->evt.gap_evt.conn_handle;
            err_code = sd_ble_gatts_sys_attr_set(m_conn_handle, NULL,0,0);
            APP_ERROR_CHECK(err_code);
            break;
        case BLE_GAP_EVT_DISCONNECTED:
            printf("BLE_GAP_EVT_DISCONNECTED\n");
            m_conn_handle = BLE_CONN_HANDLE_INVALID;
            SENDING_LOGS = false;
            printf("STOP SENDING LOGS\n");
            break;
        //send more logs when queue is complete
        case BLE_EVT_TX_COMPLETE:
            printf("Queue Cleared... Send More Logs\n");
            send_logs();
            break;
        case BLE_GATTS_EVT_WRITE:
            p_evt_write = &p_ble_evt->evt.gatts_evt.params.write;
            if(p_evt_write->handle == temperature_service.data_handle.cccd_handle){
              printf("Descriptor Write\n");
              init_log_send_idx();
              send_logs();
            }
            break;
        default:
            // No implementation needed.
            break;
    }
}


/**@brief Function for dispatching a BLE stack event to all modules with a BLE stack event handler.
 *
 * @details This function is called from the BLE Stack event interrupt handler after a BLE stack
 *          event has been received.
 *
 * @param[in] p_ble_evt  Bluetooth stack event.
 */
static void ble_evt_dispatch(ble_evt_t * p_ble_evt)
{
    dm_ble_evt_handler(p_ble_evt);
    ble_conn_params_on_ble_evt(p_ble_evt);
    bsp_btn_ble_on_ble_evt(p_ble_evt);
    on_ble_evt(p_ble_evt);
    ble_advertising_on_ble_evt(p_ble_evt);
    ble_temperature_service_on_ble_evt(&temperature_service, p_ble_evt,temp_ram_storage,RAM_LOG_STORAGE);
    ble_device_info_on_ble_evt(&device_info_service, p_ble_evt);
}


/**@brief Function for dispatching a system event to interested modules.
 *
 * @details This function is called from the System event interrupt handler after a system
 *          event has been received.
 *
 * @param[in] sys_evt  System stack event.
 */
static void sys_evt_dispatch(uint32_t sys_evt)
{
    pstorage_sys_event_handler(sys_evt);
    ble_advertising_on_sys_evt(sys_evt);
}


/**@brief Function for initializing the BLE stack.
 *
 * @details Initializes the SoftDevice and the BLE event interrupt.
 */
static void ble_stack_init(void)
{	
    
    uint32_t err_code;
    
    nrf_clock_lf_cfg_t clock_lf_cfg = NRF_CLOCK_LFCLKSRC;
    
    // Initialize the SoftDevice handler module.
    SOFTDEVICE_HANDLER_INIT(&clock_lf_cfg, NULL);
    
    ble_enable_params_t ble_enable_params;
		memset(&ble_enable_params, 0, sizeof(ble_enable_params_t));
		ble_enable_params.common_enable_params.vs_uuid_count   = 2;
		ble_enable_params.gatts_enable_params.attr_tab_size    = BLE_GATTS_ATTR_TAB_SIZE_DEFAULT;
		ble_enable_params.gatts_enable_params.service_changed  = 0;
    ble_enable_params.gap_enable_params.periph_conn_count  = PERIPHERAL_LINK_COUNT;
    ble_enable_params.gap_enable_params.central_conn_count = CENTRAL_LINK_COUNT;		
    
    //Check the ram settings against the used number of links
    CHECK_RAM_START_ADDR(CENTRAL_LINK_COUNT,PERIPHERAL_LINK_COUNT);
    
    // Enable BLE stack.
    ble_enable_params.gatts_enable_params.service_changed = IS_SRVC_CHANGED_CHARACT_PRESENT;
    err_code = softdevice_enable(&ble_enable_params);
    APP_ERROR_CHECK(err_code);

    // Register with the SoftDevice handler module for BLE events.
    err_code = softdevice_ble_evt_handler_set(ble_evt_dispatch);
    APP_ERROR_CHECK(err_code);

    // Register with the SoftDevice handler module for BLE events.
    err_code = softdevice_sys_evt_handler_set(sys_evt_dispatch);
    APP_ERROR_CHECK(err_code);
}


/**@brief Function for handling events from the BSP module.
 *
 * @param[in]   event   Event generated by button press.
 */
void bsp_event_handler(bsp_event_t event)
{
    uint32_t err_code;
    switch (event)
    {
        case BSP_EVENT_SLEEP:
            //printf("BSP_EVENT_SLEEP\n");
            sleep_mode_enter();
            break;

        case BSP_EVENT_DISCONNECT:
            //printf("BSP_EVENT_DISCONNECT\n");
            err_code = sd_ble_gap_disconnect(m_conn_handle, BLE_HCI_REMOTE_USER_TERMINATED_CONNECTION);
            if (err_code != NRF_ERROR_INVALID_STATE)
            {
                APP_ERROR_CHECK(err_code);
            }
            break;

        case BSP_EVENT_WHITELIST_OFF:
            //printf("BSP_EVENT_WHITELIST_OFF");
            err_code = ble_advertising_restart_without_whitelist();
            if (err_code != NRF_ERROR_INVALID_STATE)
            {
                APP_ERROR_CHECK(err_code);
            }
            break;

        default:
            break;
    }
}


/**@brief Function for handling the Device Manager events.
 *
 * @param[in] p_evt  Data associated to the device manager event.
 */
static uint32_t device_manager_evt_handler(dm_handle_t const * p_handle,
                                           dm_event_t const  * p_event,
                                           ret_code_t        event_result)
{
    APP_ERROR_CHECK(event_result);

#ifdef BLE_DFU_APP_SUPPORT
    if (p_event->event_id == DM_EVT_LINK_SECURED)
    {
        app_context_load(p_handle);
    }
#endif // BLE_DFU_APP_SUPPORT

    return NRF_SUCCESS;
}


/**@brief Function for the Device Manager initialization.
 *
 * @param[in] erase_bonds  Indicates whether bonding information should be cleared from
 *                         persistent storage during initialization of the Device Manager.
 */
static void device_manager_init(bool erase_bonds)
{
    uint32_t               err_code;
    dm_init_param_t        init_param = {.clear_persistent_data = erase_bonds};
    dm_application_param_t register_param;

    // Initialize persistent storage module.
    err_code = pstorage_init();
    APP_ERROR_CHECK(err_code);

    err_code = dm_init(&init_param);
    APP_ERROR_CHECK(err_code);

    memset(&register_param.sec_param, 0, sizeof(ble_gap_sec_params_t));

    register_param.sec_param.bond         = SEC_PARAM_BOND;
    register_param.sec_param.mitm         = SEC_PARAM_MITM;
    register_param.sec_param.io_caps      = SEC_PARAM_IO_CAPABILITIES;
    register_param.sec_param.oob          = SEC_PARAM_OOB;
    register_param.sec_param.min_key_size = SEC_PARAM_MIN_KEY_SIZE;
    register_param.sec_param.max_key_size = SEC_PARAM_MAX_KEY_SIZE;
    register_param.evt_handler            = device_manager_evt_handler;
    register_param.service_type           = DM_PROTOCOL_CNTXT_GATT_SRVR_ID;

    err_code = dm_register(&m_app_handle, &register_param);
    APP_ERROR_CHECK(err_code);
}


/**@brief Function for initializing the Advertising functionality.
 */
static void advertising_init(void)
{
    uint32_t      err_code;
    ble_advdata_t advdata;

    // Build advertising data struct to pass into ble_advertising_init().
    memset(&advdata, 0, sizeof(advdata));

    advdata.name_type               = BLE_ADVDATA_FULL_NAME;
    advdata.flags                   = BLE_GAP_ADV_FLAGS_LE_ONLY_GENERAL_DISC_MODE;

    ble_adv_modes_config_t options = {0};
    options.ble_adv_fast_enabled  = BLE_ADV_FAST_ENABLED;
    options.ble_adv_fast_interval = APP_ADV_INTERVAL;
    options.ble_adv_fast_timeout  = APP_ADV_TIMEOUT_IN_SECONDS;

    // create a scan response packet and include the list of UUIDs 
    ble_uuid_t m_adv_uuids[] = {BLE_UUID_OUR_SERVICE_UUID, BLE_UUID_TYPE_VENDOR_BEGIN};
    
    ble_advdata_t srdata;
    memset(&srdata, 0, sizeof(srdata));
    //srdata.uuids_complete.uuid_cnt = sizeof(m_adv_uuids) / sizeof(m_adv_uuids[0]);
    //srdata.uuids_complete.p_uuids = m_adv_uuids;
		
    err_code = ble_advertising_init(&advdata, &srdata, &options, on_adv_evt, NULL);
    APP_ERROR_CHECK(err_code);
}


/**@brief Function for initializing buttons and leds.
 *
 * @param[out] p_erase_bonds  Will be true if the clear bonding button was pressed to wake the application up.
 */
static void buttons_leds_init(bool * p_erase_bonds)
{
    bsp_event_t startup_event;

    uint32_t err_code = bsp_init(BSP_INIT_LED | BSP_INIT_BUTTONS,
                                 APP_TIMER_TICKS(100, APP_TIMER_PRESCALER), 
                                 bsp_event_handler);
    APP_ERROR_CHECK(err_code);

    err_code = bsp_btn_ble_init(NULL, &startup_event);
    APP_ERROR_CHECK(err_code);

    *p_erase_bonds = (startup_event == BSP_EVENT_CLEAR_BONDING_DATA);
}


/**@brief Function for the Power manager.
 */
static void power_manage(void)
{
    uint32_t err_code = sd_app_evt_wait();
    APP_ERROR_CHECK(err_code);
}

/**
 * @brief UART events handler.
 */
static void uart_events_handler(app_uart_evt_t * p_event)
{
    switch (p_event->evt_type)
    {
        case APP_UART_COMMUNICATION_ERROR:
            APP_ERROR_HANDLER(p_event->data.error_communication);
            break;

        case APP_UART_FIFO_ERROR:
            APP_ERROR_HANDLER(p_event->data.error_code);
            break;

        default:
            break;
    }
}


/**
 * @brief UART initialization.
 * Just the usual way. Nothing special here
 */
static void uart_config(void)
{
    uint32_t                     err_code;
    const app_uart_comm_params_t comm_params =
    {
        RX_PIN_NUMBER,
        TX_PIN_NUMBER,
        RTS_PIN_NUMBER,
        CTS_PIN_NUMBER,
        APP_UART_FLOW_CONTROL_DISABLED,
        false,
        UART_BAUDRATE_BAUDRATE_Baud38400
    };

    APP_UART_FIFO_INIT(&comm_params,
                       UART_RX_BUF_SIZE,
                       UART_TX_BUF_SIZE,
                       uart_events_handler,
                       APP_IRQ_PRIORITY_LOW,
                       err_code);

    APP_ERROR_CHECK(err_code);
}

void flash_init(){
    uint32_t err_code;
    err_code = pstorage_init();
    APP_ERROR_CHECK(err_code);
    
    pstorage_module_param_t param;
    param.block_size  = 1024;
    param.block_count = 1;
    param.cb          = example_cb_handler;
    err_code = pstorage_register(&param, &base_handle);
    APP_ERROR_CHECK(err_code);
}

void device_name_id_store(uint32_t name, uint32_t id){
  //name must be less than 4 bytes
  uint32_t err_code;
  uint32_t dataTest;
  
  //check if data was already written to location
  err_code = pstorage_load(&base_handle, &dataTest, 4, 0);
  while(flash_op){
            
  }
  flash_op = true;
  
  //if data not written to
  if(dataTest != TEST_STORAGE_VAL){
    uint32_t data[3];
    data[0] = TEST_STORAGE_VAL;
    data[1] = name;
    data[2] = id;
    err_code = pstorage_store(&base_handle, data, 12, 0);
    while(flash_op){
          
    }
    flash_op = true;
  }
}

//void device_name_id_load(uint32_t name, uint32_t id){
//  //name must be less than 4 bytes
//  uint32_t err_code;
//  uint32_t dataTest;
//  
//  //check if data was already written to location
//  err_code = pstorage_load(&base_handle, &dataTest, 4, 0);
//  while(flash_op){
//            
//  }
//  flash_op = true;
//  
//  //if data not written to
//  if(dataTest != TEST_STORAGE_VAL){
//    data[0] = TEST_STORAGE_VAL;
//    data[1] = name;
//    data[2] = id;
//    err_code = pstorage_store(&base_handle, data, 12, 0);
//    while(flash_op){
//          
//    }
//    flash_op = true;
//  }
//}

/*Pins to connect shield. */
//#define ARDUINO_I2C_SCL_PIN 7
//#define ARDUINO_I2C_SDA_PIN 30

/**
 * @brief UART initialization.
 */
//void twi_init (void)
//{
//    ret_code_t err_code;
//    
//    const nrf_drv_twi_config_t twi_mma_7660_config = {
//       .scl                = ARDUINO_SCL_PIN,
//       .sda                = ARDUINO_SDA_PIN,
//       .frequency          = NRF_TWI_FREQ_100K,
//       .interrupt_priority = APP_IRQ_PRIORITY_HIGH
//    };
//    
//    err_code = nrf_drv_twi_init(&m_twi_mma_7660, &twi_mma_7660_config, twi_handler, NULL);
//    APP_ERROR_CHECK(err_code);
//    
//    nrf_drv_twi_enable(&m_twi_mma_7660);
//}
//#define MMA7660_REG_MODE    0x07U
//#define ACTIVE_MODE 1u


//nrf_drv_twi_t twi; //= NRF_DRV_TWI_INSTANCE(0);

int main(void)
{   
    uint32_t err_code;
    bool erase_bonds;
//    twi.drv_inst_idx = 0;
//    twi.use_easy_dma = false;
//    twi.reg.p_twi = NULL;
    //initialize app components
    uart_config();
    //uint16_t tx_data = 9;
    
//    err_code = nrf_drv_twi_init(&twi, NULL, NULL,NULL);
//    APP_ERROR_CHECK(err_code);
//    nrf_drv_twi_enable(&twi);

    /* Writing to MMA7660_REG_MODE "1" enables the accelerometer. */
    //uint8_t reg[2] = {MMA7660_REG_MODE, ACTIVE_MODE};

//    err_code = nrf_drv_twi_tx(&twi, (0x98U >> 1), reg, sizeof(reg), false);  
//    APP_ERROR_CHECK(err_code);
     
//    while(true){
//      err_code = nrf_drv_twi_rx(&twi, (0x98U >> 1), &tx_data, sizeof(tx_data));
//      APP_ERROR_CHECK(err_code);
//      printf("%u\n",tx_data);
//      nrf_delay_ms(1000);
//    }
    
    
    
    
    timers_init();
    buttons_leds_init(&erase_bonds);
    ble_stack_init();
    //flash_init();
    device_manager_init(erase_bonds);
    gap_params_init();
    services_init();
    advertising_init();
    conn_params_init();
    app_timer_start(m_our_char_timer_id, OUR_CHAR_TIMER_INTERVAL, NULL);
    


    // Start execution.
    application_timers_start();
    err_code = ble_advertising_start(BLE_ADV_MODE_FAST);
    APP_ERROR_CHECK(err_code);
    
    uint32_t old_logs[RAM_LOG_STORAGE];
    bool logs_in_flash = false;

    // Enter main loop.
    for (;;)
    {
      
       


      if(false){
        //check if flash storage necessary
        if(ram_store_idx == RAM_LOG_STORAGE){
          //printf("Storing Flash\n");
          //turn off timer
          //err_code = app_timer_stop(m_our_char_timer_id);
          //APP_ERROR_CHECK(err_code);
          //turn off adv
          //err_code = sd_ble_gap_adv_stop();
          //APP_ERROR_CHECK(err_code);
          // Request to write RAM_LOG_STORAGE*bytes/elem bytes to block at an offset of 0 bytes.
          flash_op = true;
          err_code = pstorage_store(&base_handle, temp_ram_storage, 16, 0);
          APP_ERROR_CHECK(err_code);
          logs_in_flash = true;
          ram_store_idx = 0;
          while(flash_op){
            //power_manage();
          }
        }
        
        
        //check if stored logs need to be uploaded
        if(logs_in_flash && m_conn_handle != BLE_CONN_HANDLE_INVALID){
          //printf("Loading Flash\n");
          // Request to read 4 bytes from block at an offset of 12 bytes.
          flash_op = true;
          err_code = pstorage_load(old_logs, &base_handle, 16, 0);
          APP_ERROR_CHECK(err_code);
          while(flash_op){
            power_manage();
          }
          //printf("Loading...\n");
          for(int i = 0; i<RAM_LOG_STORAGE; i++){
            printf("%u ",old_logs[i]);
          }
          printf("\n");
          logs_in_flash = false;

          //clear flash page
          flash_op = true;
          err_code = pstorage_clear(&base_handle, 16);
          APP_ERROR_CHECK(err_code);
          while(flash_op){
            power_manage();
          }
        }
      }
        power_manage();
    }
}

/**
 * @}
 */


