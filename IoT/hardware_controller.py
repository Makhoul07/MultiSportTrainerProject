import json
import time
import paho.mqtt.client as mqtt
import RPi.GPIO as GPIO

BROKER = "broker.hivemq.com"

TOPIC_EVENT = "multisport/trainer/event"
TOPIC_STATUS = "multisport/trainer/status"
TOPIC_RESULT = "multisport/trainer/result"

DETECTION_DISTANCE = 15

BUZZER = 19   # GPIO19 = physical Pin 35

CONES = {
    1: {"green": 18, "red": 23, "trig": 24, "echo": 25, "state": None},
    2: {"green": 5,  "red": 6,  "trig": 12, "echo": 13, "state": None},
    3: {"green": 16, "red": 20, "trig": 21, "echo": 26, "state": None}
}

GPIO.setmode(GPIO.BCM)

GPIO.setup(BUZZER, GPIO.OUT)
GPIO.output(BUZZER, False)

for cone_id, cone in CONES.items():
    GPIO.setup(cone["green"], GPIO.OUT)
    GPIO.setup(cone["red"], GPIO.OUT)
    GPIO.setup(cone["trig"], GPIO.OUT)
    GPIO.setup(cone["echo"], GPIO.IN)

    GPIO.output(cone["green"], False)
    GPIO.output(cone["red"], False)
    GPIO.output(cone["trig"], False)


def buzz(times=1, duration=0.2):
    for _ in range(times):
        GPIO.output(BUZZER, True)
        time.sleep(duration)
        GPIO.output(BUZZER, False)
        time.sleep(0.15)


def turn_off_cone(cone_id):
    GPIO.output(CONES[cone_id]["green"], False)
    GPIO.output(CONES[cone_id]["red"], False)
    CONES[cone_id]["state"] = None


def get_distance(cone_id):
    trig = CONES[cone_id]["trig"]
    echo = CONES[cone_id]["echo"]

    GPIO.output(trig, True)
    time.sleep(0.00001)
    GPIO.output(trig, False)

    timeout = time.time() + 0.04
    while GPIO.input(echo) == 0:
        pulse_start = time.time()
        if time.time() > timeout:
            return None

    timeout = time.time() + 0.04
    while GPIO.input(echo) == 1:
        pulse_end = time.time()
        if time.time() > timeout:
            return None

    return round((pulse_end - pulse_start) * 17150, 2)


def publish_result(client, cone_id, result):
    payload = {
        "type": "cone_result",
        "cone": cone_id,
        "result": result
    }

    client.publish(TOPIC_EVENT, json.dumps(payload))
    print("Published:", payload)


def on_connect(client, userdata, flags, rc):
    print("Connected to MQTT Broker")

    client.subscribe(TOPIC_EVENT)
    client.subscribe(TOPIC_STATUS)
    client.subscribe(TOPIC_RESULT)

    print(f"Subscribed to {TOPIC_EVENT}")
    print(f"Subscribed to {TOPIC_STATUS}")
    print(f"Subscribed to {TOPIC_RESULT}")


def on_message(client, userdata, msg):
    payload = json.loads(msg.payload.decode())
    print("Received:", payload)

    if payload.get("status") == "training_started":
        print("Training started → buzzer")
        buzz(times=2, duration=0.15)

    if payload.get("message") == "Training finished":
        print("Training finished → buzzer")
        buzz(times=2, duration=0.4)

    if payload.get("type") == "cone_result" and payload.get("result") == "wrong":
        print("Wrong cone → buzzer")
        buzz(times=3, duration=0.15)

    if payload.get("type") == "cone_activation":
        green = payload.get("green_light")
        red = payload.get("red_light")

        for cone_id in CONES:
            turn_off_cone(cone_id)

        if green in CONES:
            GPIO.output(CONES[green]["green"], True)
            CONES[green]["state"] = "green"
            print(f"Cone {green} GREEN ON")

        if red in CONES:
            GPIO.output(CONES[red]["red"], True)
            CONES[red]["state"] = "red"
            print(f"Cone {red} RED ON")


client = mqtt.Client(mqtt.CallbackAPIVersion.VERSION1)

client.on_connect = on_connect
client.on_message = on_message

client.connect(BROKER, 1883, 60)
client.loop_start()

try:
    print("Hardware controller running...")

    while True:
        for cone_id, cone in CONES.items():
            if cone["state"] is not None:
                distance = get_distance(cone_id)

                if distance is not None:
                    print(f"Cone {cone_id} Distance: {distance} cm")

                    if distance < DETECTION_DISTANCE:
                        if cone["state"] == "green":
                            print(f"Cone {cone_id} CORRECT")
                            publish_result(client, cone_id, "correct")

                        elif cone["state"] == "red":
                            print(f"Cone {cone_id} WRONG")
                            publish_result(client, cone_id, "wrong")

                        turn_off_cone(cone_id)
                        time.sleep(2)

        time.sleep(0.2)

except KeyboardInterrupt:
    print("Stopping hardware controller...")

finally:
    for cone_id in CONES:
        turn_off_cone(cone_id)

    GPIO.output(BUZZER, False)
    GPIO.cleanup()
    client.loop_stop()
