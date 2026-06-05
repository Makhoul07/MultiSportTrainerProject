import RPi.GPIO as GPIO
import time

TRIG = 12
ECHO = 13

GPIO.setmode(GPIO.BCM)
GPIO.setup(TRIG, GPIO.OUT)
GPIO.setup(ECHO, GPIO.IN)

GPIO.output(TRIG, False)
time.sleep(1)

print("Ultrasonic Sensor Test")

try:
    while True:
        GPIO.output(TRIG, True)
        time.sleep(0.00001)
        GPIO.output(TRIG, False)

        timeout = time.time() + 0.04
        while GPIO.input(ECHO) == 0:
            pulse_start = time.time()
            if time.time() > timeout:
                print("No echo start - check TRIG/ECHO/VCC/GND")
                break

        timeout = time.time() + 0.04
        while GPIO.input(ECHO) == 1:
            pulse_end = time.time()
            if time.time() > timeout:
                print("No echo end - check ECHO divider")
                break

        if 'pulse_start' in locals() and 'pulse_end' in locals():
            distance = round((pulse_end - pulse_start) * 17150, 2)
            print(f"Distance: {distance} cm")

        time.sleep(1)

except KeyboardInterrupt:
    GPIO.cleanup()
