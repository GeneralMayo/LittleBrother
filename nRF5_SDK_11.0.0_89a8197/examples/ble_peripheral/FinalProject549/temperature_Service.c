
#include <stdint.h>
#include <string.h>
#include "nrf_gpio.h"
#include "temperature_service.h"
#include "ble_srv_common.h"
#include "app_error.h"
#include "SEGGER_RTT.h"

// Update the connection handle temperature_service is using.
// Note: this will be used for sending data.
void ble_temperature_service_on_ble_evt(ble_tss_t * p_temperature_service, ble_evt_t * p_ble_evt)
{  
	switch (p_ble_evt->header.evt_id)
    {
        case BLE_GAP_EVT_CONNECTED:
            p_temperature_service->conn_handle = p_ble_evt->evt.gap_evt.conn_handle;
            break;
        case BLE_GAP_EVT_DISCONNECTED:
            p_temperature_service->conn_handle = BLE_CONN_HANDLE_INVALID;
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
	
		// Add sensor id UUID
    //ble_uuid_t          id_uuid;
    //id_uuid.uuid      = BLE_UUID_SENSOR_ID_CHARACTERISTC_UUID;
    //sd_ble_uuid_vs_add(&base_uuid, &id_uuid.type);
    
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
    attr_md.vloc        = BLE_GATTS_VLOC_STACK;   
    // Set read/write security levels to our characteristic
    BLE_GAP_CONN_SEC_MODE_SET_OPEN(&attr_md.read_perm);
    BLE_GAP_CONN_SEC_MODE_SET_OPEN(&attr_md.write_perm);
    
    
    // Configure the sensor data characteristic value attribute
    ble_gatts_attr_t    attr_char_value;
    memset(&attr_char_value, 0, sizeof(attr_char_value));        
    attr_char_value.p_uuid      = &char_uuid;
    attr_char_value.p_attr_md   = &attr_md;
    attr_char_value.max_len     = 10;
    attr_char_value.init_len    = 10;
		struct temp_log log;
		log.id = BLE_UUID_SENSOR_ID_CHARACTERISTC_UUID;
		log.value = 0;
		log.time = 0;
    //uint8_t value[4]            = {0x12,0x34,0x56,0x78};
    attr_char_value.p_value     = (uint8_t *)&log;
		
		// Configure the id characteristic value attribute
    //ble_gatts_attr_t    attr_id_value;
    //memset(&attr_id_value, 0, sizeof(attr_id_value));        
    //attr_id_value.p_uuid      = &id_uuid;
    //attr_id_value.p_attr_md   = &attr_md;
    //attr_id_value.max_len     = 4;
    //attr_id_value.init_len    = 4;
    //uint8_t id_value          = 1;
    //attr_id_value.p_value     = &id_value;

    // Add sensor data characteristic to the service
    err_code = sd_ble_gatts_characteristic_add(p_our_service->service_handle,
                                       &char_md,
                                       &attr_char_value,
                                       &p_our_service->data_handles);
    APP_ERROR_CHECK(err_code);
		
		
		
		//err_code = sd_ble_gatts_characteristic_add(p_our_service->service_handle,
    //                                   &char_md,
    //                                   &attr_id_value,
    //                                  &p_our_service->id_handles);
		//APP_ERROR_CHECK(err_code);
    return NRF_SUCCESS;
}

static uint32_t name_char_add(ble_tss_t * p_our_service){
		uint32_t   err_code = 0;
	
		// Add sensor name UUID
    ble_uuid_t          name_uuid;
    name_uuid.uuid      = BLE_UUID_SENSOR_NAME_CHARACTERISTC_UUID;
    ble_uuid128_t       base_uuid = BLE_UUID_TEMPERATURE_BASE_UUID;
		sd_ble_uuid_vs_add(&base_uuid, &name_uuid.type);
	
		// Add read/write properties
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
    attr_md.vloc        = BLE_GATTS_VLOC_STACK;   
    // Set read/write security levels to our characteristic
    BLE_GAP_CONN_SEC_MODE_SET_OPEN(&attr_md.read_perm);
    BLE_GAP_CONN_SEC_MODE_SET_OPEN(&attr_md.write_perm);
		
		// Configure the name characteristic value attribute
    ble_gatts_attr_t    attr_name_value;
    memset(&attr_name_value, 0, sizeof(attr_name_value));        
    attr_name_value.p_uuid      = &name_uuid;
    attr_name_value.p_attr_md   = &attr_md;
    attr_name_value.max_len     = 12;
    attr_name_value.init_len    = 12;
    char *name_value          = "Temp_Sensor";
    attr_name_value.p_value     = name_value;
	
		//Add new characteristics to the service
    err_code = sd_ble_gatts_characteristic_add(p_our_service->service_handle,
                                       &char_md,
                                       &attr_name_value,
																			 &p_our_service->name_handles);
		//APP_ERROR_CHECK(err_code);
		return NRF_SUCCESS;
}

static uint32_t id_char_add(ble_tss_t * p_our_service){
		uint32_t   err_code = 0;
	
		// Add sensor name UUID
    ble_uuid_t          id_uuid;
    id_uuid.uuid      = BLE_UUID_SENSOR_ID_CHARACTERISTC_UUID;
    ble_uuid128_t       base_uuid = BLE_UUID_TEMPERATURE_BASE_UUID;
		sd_ble_uuid_vs_add(&base_uuid, &id_uuid.type);
	
		// Add read/write properties 
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
    attr_md.vloc        = BLE_GATTS_VLOC_STACK;   
    // Set read/write security levels to our characteristic
    BLE_GAP_CONN_SEC_MODE_SET_OPEN(&attr_md.read_perm);
    BLE_GAP_CONN_SEC_MODE_SET_OPEN(&attr_md.write_perm);
		
		// Configure the id characteristic value attribute
    ble_gatts_attr_t    attr_id_value;
    memset(&attr_id_value, 0, sizeof(attr_id_value));        
    attr_id_value.p_uuid      = &id_uuid;
    attr_id_value.p_attr_md   = &attr_md;
    attr_id_value.max_len     = 4;
    attr_id_value.init_len    = 2;
    uint8_t id_value          = 1;
    attr_id_value.p_value     = &id_value;
	
		//Add new characteristics to the service
    err_code = sd_ble_gatts_characteristic_add(p_our_service->service_handle,
                                       &char_md,
                                       &attr_id_value,
																			 &p_our_service->name_handles);
		//APP_ERROR_CHECK(err_code);
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
		//name_char_add(p_our_service);
		//id_char_add(p_our_service);
}

// Function to be called when updating characteristic value
void termperature_characteristic_update(ble_tss_t *p_tss_service, int32_t *temperature_value, uint32_t *time)
{
    uint32_t err_code;
		//SEGGER_RTT_printf(0,"Updating Temp Service\n");
		// Update characteristic value if device still connected to Little Brother
    if (p_tss_service->conn_handle != BLE_CONN_HANDLE_INVALID)
    {
        uint16_t               len = 10;
        ble_gatts_hvx_params_t hvx_params;
        memset(&hvx_params, 0, sizeof(hvx_params));

        hvx_params.handle = p_tss_service->data_handles.value_handle;
        hvx_params.type   = BLE_GATT_HVX_NOTIFICATION;
        hvx_params.offset = 0;
        hvx_params.p_len  = &len;
				struct temp_log log;
				log.id = BLE_UUID_SENSOR_ID_CHARACTERISTC_UUID;
				log.time = *time;
				log.value = *temperature_value;
				
        hvx_params.p_data = (uint8_t*)&log;  
				
        err_code= sd_ble_gatts_hvx(p_tss_service->conn_handle, &hvx_params);
				//SEGGER_RTT_printf(0,"Connection Handle: %u\n",p_tss_service->conn_handle);
				//SEGGER_RTT_printf(0,"Invalid Connection Handle: %u\n",BLE_CONN_HANDLE_INVALID);
				//APP_ERROR_CHECK(err_code);
    }   
}
