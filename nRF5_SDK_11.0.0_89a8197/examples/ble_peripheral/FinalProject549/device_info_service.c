
#include <stdint.h>
#include <string.h>
#include "nrf_gpio.h"
#include "device_info_service.h"
#include "ble_srv_common.h"
#include "app_error.h"
#include "SEGGER_RTT.h"


void ble_device_info_on_ble_evt(ble_dis_t * p_device_info, ble_evt_t * p_ble_evt)
{  
  if(p_ble_evt->header.evt_id == BLE_GATTS_EVT_WRITE){
    ble_gatts_evt_write_t * p_evt_write = &p_ble_evt->evt.gatts_evt.params.write;
    if(p_evt_write->handle == p_device_info->device_name_handle.value_handle){
      printf("Device Name: %s\n",DEVICE_NAME);
    } else if(p_evt_write->handle == p_device_info->device_id_handle.value_handle)      
      printf("Device ID: %u",DEVICE_ID);
    else{   
      for(int i =0; i<SENSOR_NUM; i++){
        if(p_evt_write->handle == p_device_info->device_sensor_handles[i].value_handle){
          //write sensor name
          printf("Sensor%u Name: %s\n", i,SENSOR_NAMES[i]);
        }
      }
    }
  }
}


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
    id_uuid.uuid      = BLE_UUID_DEVICE_ID_CHARACTERISTC_UUID;
    sd_ble_uuid_vs_add(&base_uuid, &id_uuid.type);
    APP_ERROR_CHECK(err_code);
    
    //add sensor char uuid's
    ble_uuid_t          sensor_names_uuid[SENSOR_NUM];
    for(int i = 0; i< SENSOR_NUM; i++){
      sensor_names_uuid[i].uuid      = BLE_UUID_SENSOR_START_CHARACTERISTC_UUID;
      err_code = sd_ble_uuid_vs_add(&base_uuid, &sensor_names_uuid[i].type);
      APP_ERROR_CHECK(err_code);
    }

    //add sensor on off uuid
    ble_uuid_t          sensor_switch_uuid;
    sensor_switch_uuid.uuid      = BLE_UUID_SENSOR_SWITCH_CHARACTERISTC_UUID;
    sd_ble_uuid_vs_add(&base_uuid, &sensor_switch_uuid.type);
    APP_ERROR_CHECK(err_code);

    // Add read properties to characteristics
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
    attr_md.vloc        = BLE_GATTS_VLOC_USER;   
    
    
    // Set read/write security levels to our characteristic
    BLE_GAP_CONN_SEC_MODE_SET_OPEN(&attr_md.read_perm);
    BLE_GAP_CONN_SEC_MODE_SET_OPEN(&attr_md.write_perm);

    
    // Configure the name characteristic value attribute
    ble_gatts_attr_t    attr_name_value;
    memset(&attr_name_value, 0, sizeof(attr_name_value));        
    attr_name_value.p_uuid      = &name_uuid;
    attr_name_value.p_attr_md   = &attr_md;
    attr_name_value.max_len     = NAME_SIZE;
    attr_name_value.init_len    = NAME_SIZE;
    for(int i = 0; i<NAME_SIZE; i++){
      DEVICE_NAME[i]                 = ' ';
    }
    attr_name_value.p_value     = DEVICE_NAME;
    
    //DEVICE_NAME[0] = 'D';
    //DEVICE_NAME[1] = 'N';
		
		
    // Configure the id characteristic value attribute
    ble_gatts_attr_t    attr_id_value;
    memset(&attr_id_value, 0, sizeof(attr_id_value));        
    attr_id_value.p_uuid      = &id_uuid;
    attr_id_value.p_attr_md   = &attr_md;
    attr_id_value.max_len     = 4;
    attr_id_value.init_len    = 4;
    DEVICE_ID                 = -1;
    attr_id_value.p_value     = &DEVICE_ID;

    //initialize sensor_name values
    for(int i = 0; i<SENSOR_NUM; i++){
      for(int j = 0; j<NAME_SIZE; j++){
        SENSOR_NAMES[i][j] = ' ';
      }
    }
    
//    SENSOR_NAMES[0][0] = '1';
//    SENSOR_NAMES[0][1] = '_';
//    SENSOR_NAMES[0][2] = 's';
//    SENSOR_NAMES[1][0] = '2';
//    SENSOR_NAMES[1][1] = '_';
//    SENSOR_NAMES[1][2] = 's';
//    SENSOR_NAMES[2][0] = '3';
//    SENSOR_NAMES[2][1] = '_';
//    SENSOR_NAMES[2][2] = 's';

    // Configure the sensor name characteristic value attributes
    ble_gatts_attr_t    attr_sensor_names_value[SENSOR_NUM];
    for(int i = 0; i<SENSOR_NUM;i++){
      memset(&(attr_sensor_names_value[i]), 0, sizeof(attr_sensor_names_value[i]));        
      attr_sensor_names_value[i].p_uuid      = &(sensor_names_uuid[i]);
      attr_sensor_names_value[i].p_attr_md   = &attr_md;
      attr_sensor_names_value[i].max_len     = NAME_SIZE;
      attr_sensor_names_value[i].init_len    = NAME_SIZE;
      attr_sensor_names_value[i].p_value     = SENSOR_NAMES[i];
    }

    ble_gatts_attr_t    attr_sensor_switch_value;
    memset(&attr_sensor_switch_value, 0, sizeof(attr_sensor_switch_value));        
    attr_sensor_switch_value.p_uuid      = &sensor_switch_uuid;
    attr_sensor_switch_value.p_attr_md   = &attr_md;
    attr_sensor_switch_value.max_len     = 1;
    attr_sensor_switch_value.init_len    = 1;
    SENSORS_ON                 = true;
    attr_sensor_switch_value.p_value     = &SENSORS_ON;
    
    // Add our new characteristics to the service
    err_code = sd_ble_gatts_characteristic_add(p_our_service->service_handle,
                                       &char_md,
                                       &attr_name_value,
                                       &p_our_service->device_name_handle);
    APP_ERROR_CHECK(err_code);                                                                                                                                                                                    
    err_code = sd_ble_gatts_characteristic_add(p_our_service->service_handle,
                           &char_md,
                           &attr_id_value,
                           &p_our_service->device_id_handle);
    APP_ERROR_CHECK(err_code);
    
    for(int i = 0; i<SENSOR_NUM; i++){
      err_code = sd_ble_gatts_characteristic_add(p_our_service->service_handle,
                           &char_md,
                           &attr_sensor_names_value[i],
                           &p_our_service->device_sensor_handles[i]);
      APP_ERROR_CHECK(err_code);
    }

    err_code = sd_ble_gatts_characteristic_add(p_our_service->service_handle,
                           &char_md,
                           &attr_sensor_switch_value,
                           &p_our_service->device_sensor_switch_handle);
    APP_ERROR_CHECK(err_code);

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
