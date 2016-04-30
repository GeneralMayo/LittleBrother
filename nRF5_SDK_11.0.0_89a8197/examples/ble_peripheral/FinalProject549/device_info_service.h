
#ifndef DEVICE_INFO_SERVICE_H__
#define DEVICE_INFO_SERVICE_H__

#include <stdint.h>
#include <string.h>

#include "ble.h"
#include "ble_srv_common.h"
// Defining 16-bit service and 128-bit base UUIDs
#define BLE_UUID_DEVICE_INFO_BASE_UUID              {{0x98,0x46,0x29,0x90,0x54,0x79,0x44,0x54,0x89,0xac,0x0b,0x29,0xca,0x5d,0x70,0x4f}} // 128-bit base UUID
#define BLE_UUID_DEVICE_INFO_SERVICE_UUID           0x1111 // Just a random, but recognizable value

// Defining 16-bit characteristic UUID
#define BLE_UUID_DEVICE_NAME_CHARACTERISTC_UUID          0x0001 
#define BLE_UUID_DEVICE_ID_CHARACTERISTC_UUID            0x0002
#define BLE_UUID_SENSOR_START_CHARACTERISTC_UUID         0x0003


// This structure contains various status information for our service. 
// The name is based on the naming convention used in Nordics SDKs. 
 
typedef struct
{
    uint16_t                    service_handle; /**< Handle of Our Service (as provided by the BLE stack). */
    // OUR_JOB: Step 2.D, Add handles for the characteristic attributes to our struct
    ble_gatts_char_handles_t    char_handles;
}ble_dis_t;


/**@brief Function for initializing our new service.
 *
 * @param[in]   p_our_service       Pointer to Our Service structure.
 */
void divice_info_service_init(ble_dis_t * p_our_service);

#endif  /* _ DEVICE_INFO_SERVICE_H__ */
