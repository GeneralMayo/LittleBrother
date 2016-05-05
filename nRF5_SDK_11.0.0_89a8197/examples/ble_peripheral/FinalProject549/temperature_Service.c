#include <stdint.h>
#include <string.h>
#include "nrf_gpio.h"
#include "temperature_service.h"
#include "ble_srv_common.h"
#include "app_error.h"
#include "SEGGER_RTT.h"

uint32_t device_read_idx = 0;

void on_write(temp_log *temp_ram_storage,uint32_t len, ble_tss_t * p_temperature_service){
  //send data
  log_buff = temp_ram_storage[device_read_idx];
  num_log_buff-=1;
   
  //update read index
  device_read_idx = (device_read_idx +1)%len;
}


// Update the connection handle temperature_service is using.
// Note: this will be used for sending data.
void ble_temperature_service_on_ble_evt(ble_tss_t * p_temperature_service, ble_evt_t * p_ble_evt,
temp_log *temp_ram_storage, uint32_t len)
{  
    switch (p_ble_evt->header.evt_id)
    {
        case BLE_GAP_EVT_CONNECTED:
            p_temperature_service->conn_handle = p_ble_evt->evt.gap_evt.conn_handle;
            break;
        case BLE_GAP_EVT_DISCONNECTED:
            p_temperature_service->conn_handle = BLE_CONN_HANDLE_INVALID;
            break;
        case BLE_GATTS_EVT_WRITE:
            //on_write(temp_ram_storage,len,p_temperature_service);
            break;
        default:
            // No implementation needed.
            break;
    }
}

/**@brief Function for adding our new characterstic to "Our service" that we initiated in the previous tutorial. 
 *
 * @param[in]   p_our_service        Our Service structure.
 *
 */
static uint32_t data_value_char_add(ble_tss_t * p_our_service)
{
    uint32_t   err_code = 0; 
    
    // Add data value characteristic UUID
    ble_uuid_t          char_uuid;
    ble_uuid128_t       base_uuid = BLE_UUID_TEMPERATURE_BASE_UUID;
    char_uuid.uuid      = BLE_UUID_SENSOR_VALUE_CHARACTERISTC_UUID;
    sd_ble_uuid_vs_add(&base_uuid, &char_uuid.type);
	
    // Add dummy_write UUID
    ble_uuid_t            dummy_write_uuid;
    dummy_write_uuid.uuid      = BLE_UUID_DUMMY_WRITE_CHARACTERISTC_UUID;
    sd_ble_uuid_vs_add(&base_uuid, &dummy_write_uuid.type);

    // Add log_num UUID
    ble_uuid_t            log_num_uuid;
    log_num_uuid.uuid      = BLE_UUID_LOG_NUM_CHARACTERISTC_UUID;
    sd_ble_uuid_vs_add(&base_uuid, &log_num_uuid.type);
    
    // Add read/write properties to characteristics
    ble_gatts_char_md_t char_md;
    memset(&char_md, 0, sizeof(char_md));
    char_md.char_props.read = 1;
    char_md.char_props.write = 1;

    // Configure Client Characteristic Configuration Descriptor metadata and add to char_md structure
    ble_gatts_attr_md_t cccd_md;
    memset(&cccd_md, 0, sizeof(cccd_md));
    BLE_GAP_CONN_SEC_MODE_SET_OPEN(&cccd_md.read_perm);
    BLE_GAP_CONN_SEC_MODE_SET_OPEN(&cccd_md.write_perm);
    cccd_md.vloc                = BLE_GATTS_VLOC_STACK;    
    char_md.p_cccd_md           = &cccd_md;
    char_md.char_props.notify   = 1;
   
    // Configure the attribute metadata
    ble_gatts_attr_md_t attr_md;
    memset(&attr_md, 0, sizeof(attr_md)); 
    attr_md.vloc        = BLE_GATTS_VLOC_USER;  
    // Set read/write security levels to our characteristic
    BLE_GAP_CONN_SEC_MODE_SET_OPEN(&attr_md.read_perm);
    BLE_GAP_CONN_SEC_MODE_SET_OPEN(&attr_md.write_perm);
    
    
    // Configure the sensor data characteristic value attribute
    memset(&attr_char_value, 0, sizeof(attr_char_value));        
    attr_char_value.p_uuid      = &char_uuid;
    attr_char_value.p_attr_md   = &attr_md;
    attr_char_value.max_len     = 14;
    attr_char_value.init_len    = 14;
    temp_log log;
    log.id = 1;
    log.log_id = 0x44444444;
    log.value = 0x92222222;
    log.time = 0x12345678;
    log_buff = log;
    attr_char_value.p_value     = (uint8_t *)&log_buff;
    
    // Configure the dummy_write characteristic value attribute
    ble_gatts_attr_t    attr_dummy_write_value;
    memset(&attr_dummy_write_value, 0, sizeof(attr_dummy_write_value));        
    attr_dummy_write_value.p_uuid      = &dummy_write_uuid;
    attr_dummy_write_value.p_attr_md   = &attr_md;
    attr_dummy_write_value.max_len     = 1;
    attr_dummy_write_value.init_len    = 1;
    uint8_t dw_value          = 1;
    attr_dummy_write_value.p_value     = &dw_value;

    memset(&attr_log_num_value, 0, sizeof(attr_log_num_value));        
    attr_log_num_value.p_uuid      = &log_num_uuid;
    attr_log_num_value.p_attr_md   = &attr_md;
    attr_log_num_value.max_len     = 2;
    attr_log_num_value.init_len    = 2;
    uint16_t ln_value          = 0;
    num_log_buff = ln_value;
    attr_log_num_value.p_value     = &num_log_buff;

    // Add sensor data characteristic to the service
    err_code = sd_ble_gatts_characteristic_add(p_our_service->service_handle,
                                       &char_md,
                                       &attr_char_value,
                                       &p_our_service->data_handle);
    APP_ERROR_CHECK(err_code);
		
    //Add dummy_write characteristic                     
//    err_code = sd_ble_gatts_characteristic_add(p_our_service->service_handle,
//                          &char_md,
//                          &attr_dummy_write_value,
//                          &p_our_service->dummy_write_handle);
//    APP_ERROR_CHECK(err_code);
    
    //add log num characteristic
//    err_code = sd_ble_gatts_characteristic_add(p_our_service->service_handle,
//                          &char_md,
//                          &attr_log_num_value,
//                          &p_our_service->log_num_handle);
//    APP_ERROR_CHECK(err_code);
    return NRF_SUCCESS;
}

/**@brief Function for initiating our new service.
 *
 * @param[in]   p_our_service        Our Service structure.
 *
 */
void temperature_service_init(ble_tss_t * p_our_service)
{
    uint32_t   err_code;

    // Declare 16-bit service and 128-bit base UUIDs and add them to the BLE stack
    ble_uuid_t        service_uuid;
    ble_uuid128_t     base_uuid = BLE_UUID_TEMPERATURE_BASE_UUID;
    service_uuid.uuid = BLE_UUID_TEMPERATURE_SERVICE_UUID;
    err_code = sd_ble_uuid_vs_add(&base_uuid, &service_uuid.type);
	
    // add service to gatt server
    err_code = sd_ble_gatts_service_add(BLE_GATTS_SRVC_TYPE_PRIMARY,
                                        &service_uuid,
                                        &p_our_service->service_handle);
		
    // there is initially no connection with any device
    p_our_service->conn_handle = BLE_CONN_HANDLE_INVALID;
    
    data_value_char_add(p_our_service);
}

// Function to be called when updating characteristic value
uint32_t termperature_characteristic_update(ble_tss_t *p_tss_service,
                                        temp_log most_recent_log)
{
    uint32_t err_code;
    // Update characteristic value if device still connected to Little Brother
    uint16_t               len = 14;
    ble_gatts_hvx_params_t hvx_params;
    memset(&hvx_params, 0, sizeof(hvx_params));

    hvx_params.handle = p_tss_service->data_handle.value_handle;
    hvx_params.type   = BLE_GATT_HVX_NOTIFICATION;
    hvx_params.offset = 0;
    hvx_params.p_len  = &len;
    hvx_params.p_data = (uint8_t*)&most_recent_log;

    
    //if(send_flag){     
      //send_flag = false;
      err_code = sd_ble_gatts_hvx(p_tss_service->conn_handle, &hvx_params);
    //}
    return err_code;
}
