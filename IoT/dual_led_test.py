import RPi.GPIO as GPIO
import time

GREEN = 18
RED = 23

GPIO.setmode(GPIO.BCM)

GPIO.setup(GREEN, GPIO.OUT)
GPIO.setup(RED, GPIO.OUT)

while True:

    print("GREEN ON")
    GPIO.output(GREEN, True)
    GPIO.output(RED, False)

    time.sleep(2)

    print("RED ON")
    GPIO.output(GREEN, False)
    GPIO.output(RED, True)

    time.sleep(2)
