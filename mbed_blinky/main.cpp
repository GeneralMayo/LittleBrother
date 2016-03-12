#include "mbed.h"

DigitalOut myled(LED1); // LED1 (refer to pin-out on back of board)

int main() {
    while(1) {
        myled = 1;
        wait(0.2);
        myled = 0;
        wait(0.2);
    }
}
