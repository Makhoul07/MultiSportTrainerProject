import json
import paho.mqtt.client as mqtt

from config import *

# =========================
# MQTT CLIENT
# =========================

client = mqtt.Client(mqtt.CallbackAPIVersion.VERSION1)


# =========================
# CONNECT TO MQTT BROKER
# =========================

def connect_mqtt():

    def on_connect(client, userdata, flags, rc):

        if rc == 0:
            print("Connected to MQTT Broker")

        else:
            print("Failed to connect")

    client.on_connect = on_connect

    client.connect(BROKER, PORT, 60)

    return client


# =========================
# PUBLISH MESSAGE
# =========================

def publish_message(topic, data):

    message = json.dumps(data)

    client.publish(topic, message)

    print(f"Published to {topic}: {message}")


# =========================
# SUBSCRIBE TO TOPIC
# =========================

def subscribe_topic(topic, callback):

    client.subscribe(topic)

    client.on_message = callback

    print(f"Subscribed to {topic}")