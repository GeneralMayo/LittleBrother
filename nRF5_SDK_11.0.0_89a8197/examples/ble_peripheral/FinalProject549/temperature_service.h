
#ifndef TEMPERATURE_SERVICE_H__
#define TEMPERATURE_SERVICE_H__

#include <stdint.h>
#include <string.h>

#include "ble.h"
#include "ble_srv_common.h"


// Defining 16-bit service and 128-bit base UUIDs
#define BLE_UUID_TEMPERATURE_BASE_UUID              {{0x7b,0x50,0x91,0x75,0x00,0xbc,0x46,0x37,0x97,0xfe,0xe1,0xa2,0xb3,0xff,0x59,0xbd}}
#define BLE_UUID_TEMPERATURE_SERVICE_UUID           0x2222 

// Defining 16-bit characteristic UUIDs
#define BLE_UUID_SENSOR_ID_CHARACTERISTC_UUID          	0x0001 
#define BLE_UUID_SENSOR_NAME_CHARACTERISTC_UUID         0x0002
#define BLE_UUID_SENSOR_VALUE_CHARACTERISTC_UUID				0x0003


// status information for temperature_service
typedef struct
{	
        //handles for ble connection  
        uint16_t                    conn_handle; 
        // service handle for ble stack
        uint16_t                    service_handle;
        // characteristic handels for ble stack
        ble_gatts_char_handles_t    id_handles;
        ble_gatts_char_handles_t    name_handles;
        ble_gatts_char_handles_t    data_handles;
}ble_tss_t;

struct  __attribute__((__packed__)) temp_log{
	uint32_t time;
	uint32_t value;
	uint16_t id;
};


/**@brief Function for initializing our new service.
 *
 * @param[in]   p_our_service       Pointer to Our Service structure.
 */
void temperature_service_init(ble_tss_t * p_our_service);

/**@brief Function for handling BLE Stack events related to temperature_service and characteristic.
 * 
 * @param[in]   p_our_service       Temperature Service structure.
 * @param[in]   p_ble_evt  Event received from the BLE stack.
 */
void ble_temperature_service_on_ble_evt(ble_tss_t * p_temperature_service, ble_evt_t * p_ble_evt);

/**@brief Function for updating and sending new characteristic values
 *
 * @details The application calls this function whenever the timer_timeout_handler triggers
 *
 * @param[in]   p_tss_service            Temperature Service structure.
 * @param[in]   temperature_value     	 New temperature value.
 */
void termperature_characteristic_update(ble_tss_t *p_tss_service, int32_t *temperature_value, uint32_t *time);

#endif  /* _ TEMPERATURE_SERVICE_H__ */
