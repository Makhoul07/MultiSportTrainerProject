import json
import time
import paho.mqtt.client as mqtt
import RPi.GPIO as GPIO

GREEN_LED = 18
RED_LED = 23

TRIG = 24
ECHO = 25

BROKER = "broker.hivemq.com"
EVENT_TOPIC = "multisport/trainer/event"

DETECTION_DISTANCE = 15  # cm

active_state = None  # "green", "red", or None

GPIO.setmode(GPIO.BCM)

GPIO.setup(GREEN_LED, GPIO.OUT)
GPIO.setup(RED_LED, GPIO.OUT)
GPIO.setup(TRIG, GPIO.OUT)
GPIO.setup(ECHO, GPIO.IN)

GPIO.output(GREEN_LED, False)
GPIO.output(RED_LED, False)
GPIO.output(TRIG, False)


def turn_off_leds():
    GPIO.output(GREEN_LED, False)
    GPIO.output(RED_LED, False)


def get_distance():
    GPIO.output(TRIG, True)
    time.sleep(0.00001)
    GPIO.output(TRIG, False)

    timeout = time.time() + 0.04
    while GPIO.input(ECHO) == 0:
        pulse_start = time.time()
        if time.time() > timeout:
            return None

    timeout = time.time() + 0.04
    while GPIO.input(ECHO) == 1:
        pulse_end = time.time()
        if time.time() > timeout:
            return None

    distance = (pulse_end - pulse_start) * 17150
    return round(distance, 2)


def publish_result(client, result):
    payload = {
        "type": "cone_result",
        "cone": 1,
        "result": result
    }

    client.publish(EVENT_TOPIC, json.dumps(payload))
    print("Published:", payload)


def on_connect(client, userdata, flags, rc):
    print("Connected to MQTT Broker")
    client.subscribe(EVENT_TOPIC)
    print(f"Subscribed to {EVENT_TOPIC}")


def on_message(client, userdata, msg):
    global active_state

    payload = json.loads(msg.payload.decode())
    print("Received:", payload)

    if payload.get("type") == "cone_activation":
        green = payload.get("green_light")
        red = payload.get("red_light")

        turn_off_leds()
        active_state = None

        if green == 1:
            GPIO.output(GREEN_LED, True)
            active_state = "green"
            print("Cone 1 GREEN ON")

        elif red == 1:
            GPIO.output(RED_LED, True)
            active_state = "red"
            print("Cone 1 RED ON")


client = mqtt.Client(mqtt.CallbackAPIVersion.VERSION1)
client.on_connect = on_connect
client.on_message = on_message

try:
    client.connect(BROKER, 1883, 60)
    client.loop_start()

    print("Cone 1 hardware controller running...")

    while True:
        if active_state is not None:
            distance = get_distance()

            if distance is not None:
                print(f"Distance: {distance} cm")

                if distance < DETECTION_DISTANCE:
                    if active_state == "green":
                        print("Correct: Cone 1 reached")
                        publish_result(client, "correct")

                    elif active_state == "red":
                        print("Wrong: Red Cone 1 reached")
                        publish_result(client, "wrong")

                    turn_off_leds()
                    active_state = None
                    time.sleep(2)

        time.sleep(0.2)

except KeyboardInterrupt:
    print("Stopping Cone 1 controller...")

finally:
    turn_off_leds()
    GPIO.cleanup()
    client.loop_stop()
