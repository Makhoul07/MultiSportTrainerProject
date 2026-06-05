"""
backend_bridge.py

MQTT -> Backend bridge.

Listens for finished training results on the MQTT broker and forwards them to the
.NET backend's POST /api/Results/save endpoint, so results produced by the IoT
cones land in MultiSportTrainerDB just like results from the app.

Run alongside training_simulator.py:
    python backend_bridge.py

Requires the `requests` library in this environment:
    pip install requests
"""

import json

import requests
import paho.mqtt.client as mqtt

import config

# =========================
# BACKEND CONFIGURATION
# =========================

# Set this to the machine running the .NET API (e.g. "192.168.1.20").
# Use the LAN IP, not "localhost"/"10.0.2.2" — this script runs on the Pi, not the emulator.
BACKEND_IP = "10.206.240.14"
BACKEND_PORT = 5062

RESULTS_ENDPOINT = f"http://{BACKEND_IP}:{BACKEND_PORT}/api/Results/save"

REQUEST_TIMEOUT = 10  # seconds


# =========================
# SAVE RESULT TO BACKEND
# =========================

def save_result_to_backend(payload):
    session_id = payload.get("session_id", -1)
    user_id = payload.get("user_id", -1)

    # Without a real backend session + user there is nothing valid to attach the
    # result to (both are NOT NULL, FK-checked columns). Skip these.
    if session_id == -1 or user_id == -1:
        print(
            "Skipping save: no valid backend session "
            f"(session_id={session_id}, user_id={user_id})"
        )
        return

    body = {
        "SessionId": session_id,
        "UserId": user_id,
        "Score": payload.get("score", 0),
        "Accuracy": payload.get("accuracy", 0),
        "Mistakes": payload.get("mistakes", 0),
        # Backend DurationSeconds is an int; duration_seconds is a float -> cast.
        "DurationSeconds": int(payload.get("duration_seconds", 0)),
        "AverageReactionSeconds": payload.get("average_reaction_seconds", 0),
        "BestReactionSeconds": payload.get("best_reaction_seconds", 0),
    }

    try:
        response = requests.post(RESULTS_ENDPOINT, json=body, timeout=REQUEST_TIMEOUT)

        if response.status_code in (200, 201):
            print(f"Result saved to backend (session {session_id}): {response.status_code}")
            print(response.text)
        else:
            print(f"Backend returned error {response.status_code}: {response.text}")

    except requests.exceptions.RequestException as e:
        print(f"Failed to reach backend at {RESULTS_ENDPOINT}: {e}")


# =========================
# MQTT CALLBACKS
# =========================

def on_connect(client, userdata, flags, rc):
    if rc == 0:
        print("Connected to MQTT Broker")
        client.subscribe(config.TOPIC_RESULT)
        print(f"Subscribed to {config.TOPIC_RESULT}")
    else:
        print(f"Failed to connect, return code {rc}")


def on_message(client, userdata, msg):
    try:
        payload = json.loads(msg.payload.decode())
    except json.JSONDecodeError as e:
        print(f"Ignoring malformed message on {msg.topic}: {e}")
        return

    print("\n--- RESULT RECEIVED ---")
    print(json.dumps(payload, indent=4))

    if payload.get("message") == "Training finished":
        save_result_to_backend(payload)


# =========================
# MAIN
# =========================

def main():
    if BACKEND_IP == "YOUR_BACKEND_IP":
        print("WARNING: BACKEND_IP is not configured. "
              "Edit backend_bridge.py and set BACKEND_IP to your API host.")

    # paho-mqtt 2.x requires an explicit callback API version. VERSION1 keeps the
    # familiar on_connect(client, userdata, flags, rc) signatures used above.
    client = mqtt.Client(mqtt.CallbackAPIVersion.VERSION1)
    client.on_connect = on_connect
    client.on_message = on_message

    print(f"Connecting to {config.BROKER}:{config.PORT} ...")
    client.connect(config.BROKER, config.PORT, 60)

    client.loop_forever()


if __name__ == "__main__":
    main()
