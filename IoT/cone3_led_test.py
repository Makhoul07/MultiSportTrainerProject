import RPi.GPIO as GPIO
import time

GREEN = 16
RED = 20

GPIO.setmode(GPIO.BCM)
GPIO.setup(GREEN, GPIO.OUT)
GPIO.setup(RED, GPIO.OUT)

print("Green ON")
GPIO.output(GREEN, True)
time.sleep(2)

print("Red ON")
GPIO.output(GREEN, False)
GPIO.output(RED, True)
time.sleep(2)

GPIO.output(GREEN, False)
GPIO.output(RED, False)
GPIO.cleanup()
