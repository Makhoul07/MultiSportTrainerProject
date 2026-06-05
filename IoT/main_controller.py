import json

from config import *
from mqtt_handler import (
    connect_mqtt,
    publish_message,
    subscribe_topic
)

def on_message(client, userdata, msg):
    data = json.loads(msg.payload.decode())

    print("\n--- MESSAGE RECEIVED ---")
    print("Topic:", msg.topic)
    print(json.dumps(data, indent=4))


mqtt_client = connect_mqtt()

subscribe_topic(TOPIC_STATUS, on_message)
subscribe_topic(TOPIC_EVENT, on_message)
subscribe_topic(TOPIC_RESULT, on_message)

print("=== MULTI-SPORT TRAINER CONTROLLER ===")

player = input("Enter player name: ")

mode = input("Enter mode (custom_route/generated_route): ")

command = {
    "command": "start_training",
    "player": player,
    "mode": mode
}

if mode == "custom_route":
    rounds = int(input("Enter number of rounds: "))
    command["rounds"] = rounds

elif mode == "generated_route":
    difficulty = input("Enter difficulty (easy/medium/hard): ")
    command["difficulty"] = difficulty

else:
    print("Invalid mode")
    exit()

publish_message(
    TOPIC_COMMAND,
    command
)

print("\nStart command sent. Waiting for simulator...\n")

mqtt_client.loop_forever()
