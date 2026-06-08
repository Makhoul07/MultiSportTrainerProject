import json
import random
import time

from session_logger import save_session
from email_notifier import send_training_result_email
from config import *

from mqtt_handler import (
    connect_mqtt,
    publish_message,
    subscribe_topic
)

from route_generator import generate_distraction_cone

from scoring_system import (
    calculate_accuracy,
    update_score_correct,
    update_score_wrong
)

from event_formatter import (
    training_started_event,
    cone_activation_event
)

current_score = 0
mistakes = 0
current_session = None
waiting_for_real_cone = False
session_start_time = None

REAL_CONES = [1, 2, 3]


def get_rounds_from_difficulty(difficulty):
    if difficulty == "easy":
        return 4
    elif difficulty == "medium":
        return 6
    elif difficulty == "hard":
        return 8
    else:
        return 5


def normalize_difficulty(value):
    """Map any-casing/whitespace difficulty to a valid DIFFICULTY_SETTINGS key.

    The app sends lowercase, but main_controller.py forwards raw keyboard input
    ("Medium", "HARD", " hard ") straight through. An unmatched key would raise
    KeyError inside generate_distraction_cone -- and since that happens before
    the cone_activation is published, it silently kills the whole round (no
    green and no red light). Default to "medium" so distractions still fire on
    an unrecognized value.
    """
    normalized = str(value).strip().lower()

    if normalized in DIFFICULTY_SETTINGS:
        return normalized

    return "medium"


def generate_real_cone_route(rounds):
    return [
        random.choice(REAL_CONES)
        for _ in range(rounds)
    ]


def finish_training():
    global current_session

    accuracy = calculate_accuracy(
        len(current_session["route"]),
        mistakes
    )

    duration_seconds = round(
        time.time() - session_start_time,
        2
    )

    final_result = {
        "session_id": current_session.get("session_id", -1),
        "user_id": current_session.get("user_id", -1),
        "player": current_session["player"],
        "mode": current_session["mode"],
        "difficulty": current_session.get("difficulty"),
        "rounds": len(current_session["route"]),
        "route": current_session["route"],
        "score": current_score,
        "mistakes": mistakes,
        "accuracy": accuracy,
        "duration_seconds": duration_seconds,
        "message": "Training finished"
    }

    publish_message(
        TOPIC_RESULT,
        final_result
    )

    save_session(final_result)

    send_training_result_email(
        to_email=current_session.get("email", ""),
        player_name=current_session["player"],
        score=current_score,
        accuracy=accuracy,
        mistakes=mistakes,
        duration_seconds=duration_seconds,
        difficulty=current_session.get("difficulty"),
        route_type=current_session.get("mode"),
    )

    print("\n=== TRAINING FINISHED ===")


def process_next_cone():
    global waiting_for_real_cone

    if current_session["index"] >= len(current_session["route"]):
        finish_training()
        return

    target_cone = current_session["route"][current_session["index"]]

    difficulty = current_session.get("difficulty", "medium")

    distraction_cone = generate_distraction_cone(
        target_cone,
        difficulty
    )

    if distraction_cone not in REAL_CONES:
        distraction_cone = None

    publish_message(
        TOPIC_EVENT,
        cone_activation_event(
            target_cone,
            distraction_cone
        )
    )

    print(f"\nROUND {current_session['index'] + 1}/{len(current_session['route'])}")
    print(f"GREEN → Cone {target_cone}")

    if distraction_cone:
        print(f"RED → Cone {distraction_cone}")

    waiting_for_real_cone = True
    print("Waiting for real cone sensor...")


def handle_real_cone_result(data):
    global current_score
    global mistakes
    global waiting_for_real_cone

    if not waiting_for_real_cone:
        return

    difficulty = current_session.get("difficulty", "medium")
    settings = DIFFICULTY_SETTINGS[difficulty]

    result = data.get("result")
    cone = data.get("cone")

    expected_cone = current_session["route"][current_session["index"]]

    if result == "correct" and cone == expected_cone:
        current_score = update_score_correct(
            current_score
        )

        print(f"Real Cone {cone} correct")

    else:
        mistakes += 1

        current_score = update_score_wrong(
            current_score,
            settings["wrong_penalty"]
        )

        print(f"Real Cone {cone} wrong")

    waiting_for_real_cone = False
    current_session["index"] += 1

    process_next_cone()


def start_training(data):
    global current_score
    global mistakes
    global current_session
    global waiting_for_real_cone
    global session_start_time

    current_score = 0
    mistakes = 0
    waiting_for_real_cone = False
    session_start_time = time.time()

    player = data["player"]
    mode = data["mode"]

    session_id = data.get("session_id", -1)
    user_id = data.get("user_id", -1)
    email = data.get("email", "")

    if mode == "custom_route":
        # Use the exact sequence the player built in the app. Keep only real
        # cones (1-3); the app already enforces a minimum of 3 taps.
        route = [c for c in data.get("route", []) if c in REAL_CONES]
        difficulty = normalize_difficulty(data.get("difficulty", "medium"))

        if len(route) < 3:
            print("Custom route needs at least 3 real cones (1-3). Aborting.")
            return

        rounds = len(route)

    elif mode == "generated_route":
        difficulty = normalize_difficulty(data.get("difficulty", "medium"))
        rounds = get_rounds_from_difficulty(difficulty)
        route = generate_real_cone_route(rounds)

    else:
        print("Invalid mode received")
        return

    current_session = {
        "session_id": session_id,
        "user_id": user_id,
        "email": email,
        "player": player,
        "mode": mode,
        "difficulty": difficulty,
        "route": route,
        "index": 0
    }

    print("\n=== 3-CONE PRACTICE STARTED ===")
    print("Player:", player)
    print("Mode:", mode)
    print("Difficulty:", difficulty)
    print("Rounds:", rounds)
    print("Generated Route:", route)

    publish_message(
        TOPIC_STATUS,
        {
            "status": "training_started",
            "session_id": session_id,
            "user_id": user_id,
            "player": player,
            "mode": mode,
            "difficulty": difficulty,
            "rounds": rounds,
            "route": route
        }
    )

    process_next_cone()


def on_message(client, userdata, msg):
    data = json.loads(
        msg.payload.decode()
    )

    if msg.topic == TOPIC_COMMAND:
        print("\nCommand received:")
        print(data)

        if data.get("command") == "start_training":
            start_training(data)

    elif msg.topic == TOPIC_EVENT:
        if data.get("type") == "cone_result" and data.get("cone") in REAL_CONES:
            handle_real_cone_result(data)


mqtt_client = connect_mqtt()

subscribe_topic(
    TOPIC_COMMAND,
    on_message
)

subscribe_topic(
    TOPIC_EVENT,
    on_message
)

mqtt_client.loop_forever()
