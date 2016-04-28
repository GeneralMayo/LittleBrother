
#include <stdint.h>
#include <string.h>
#include "nrf_gpio.h"
#include "device_info_service.h"
#include "ble_srv_common.h"
#include "app_error.h"
#include "SEGGER_RTT.h"

/**@brief Function for adding characterstics to "Our service" that we initiated in the previous tutorial. 
 *
 * @param[in]   p_our_service        Service the characteristics are being added to
 *
 */
static uint32_t chars_add(ble_dis_t * p_our_service)
{
    uint32_t   err_code = 0;
    
    // Add device name UUID
    ble_uuid_t          name_uuid;
    ble_uuid128_t       base_uuid = BLE_UUID_DEVICE_INFO_BASE_UUID;
    name_uuid.uuid      = BLE_UUID_DEVICE_NAME_CHARACTERISTC_UUID;
    err_code = sd_ble_uuid_vs_add(&base_uuid, &name_uuid.type);
    APP_ERROR_CHECK(err_code);
	
    // Add device id UUID
    ble_uuid_t          id_uuid;
    id_uuid.uuid      = BLE_UUID_DEVICE_NAME_CHARACTERISTC_UUID;
    sd_ble_uuid_vs_add(&base_uuid, &id_uuid.type);
    APP_ERROR_CHECK(err_code);
    
    // Add read properties to our characteristics
    ble_gatts_char_md_t char_md;
    memset(&char_md, 0, sizeof(char_md));
    char_md.char_props.read = 1;
    char_md.char_props.write = 1;

    
    // Configuring Client Characteristic Configuration Descriptor metadata and add to char_md structure
    ble_gatts_attr_md_t cccd_md;
    memset(&cccd_md, 0, sizeof(cccd_md));
    BLE_GAP_CONN_SEC_MODE_SET_OPEN(&cccd_md.read_perm);
    cccd_md.vloc                = BLE_GATTS_VLOC_STACK;    
    //char_md.p_cccd_md           = &cccd_md;
    //char_md.char_props.notify   = 1;
   
    
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
    
    // Set name characteristic length in number of bytes
    attr_name_value.max_len     = 32;
    attr_name_value.init_len    = 12;
    char *name_value          = "Device_Info";
    attr_name_value.p_value     = name_value;
		
		
    // Configure the id characteristic value attribute
    ble_gatts_attr_t    attr_id_value;
    memset(&attr_id_value, 0, sizeof(attr_id_value));        
    attr_id_value.p_uuid      = &id_uuid;
    attr_id_value.p_attr_md   = &attr_md;
    
    // Set name characteristic length in number of bytes
    attr_id_value.max_len     = 4;
    attr_id_value.init_len    = 2;
    uint8_t id_value          = 1;
    attr_id_value.p_value     = &id_value;

    // OUR_JOB: Step 2.E, Add our new characteristic to the service
    err_code = sd_ble_gatts_characteristic_add(p_our_service->service_handle,
                                       &char_md,
                                       &attr_name_value,
                                       &p_our_service->char_handles);
																			 
		err_code = sd_ble_gatts_characteristic_add(p_our_service->service_handle,
                                       &char_md,
                                       &attr_id_value,
                                       &p_our_service->char_handles);
    APP_ERROR_CHECK(err_code);
    
    printf("Service handle: %d\n\r", p_our_service->service_handle);
    printf("Char value handle: %d\r\n", p_our_service->char_handles.value_handle);
    printf("Char cccd handle: %d\r\n", p_our_service->char_handles.cccd_handle);

    return NRF_SUCCESS;
}


/**@brief Function for initiating our new service.
 *
 * @param[in]   p_our_service        Our Service structure.
 *
 */
void divice_info_service_init(ble_dis_t * p_our_service)
{
    uint32_t   err_code;

    // Declare 16-bit service and 128-bit base UUIDs and add them to the BLE stack
    ble_uuid_t        service_uuid;
    ble_uuid128_t     base_uuid = BLE_UUID_DEVICE_INFO_BASE_UUID;
    service_uuid.uuid = BLE_UUID_DEVICE_INFO_SERVICE_UUID;
    err_code = sd_ble_uuid_vs_add(&base_uuid, &service_uuid.type);
    APP_ERROR_CHECK(err_code);    
    
    // Add device_info service
    err_code = sd_ble_gatts_service_add(BLE_GATTS_SRVC_TYPE_PRIMARY,
                                        &service_uuid,
                                        &p_our_service->service_handle);
    
    APP_ERROR_CHECK(err_code);
    
    chars_add(p_our_service);
}
