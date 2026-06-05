# =========================
# MQTT CONFIGURATION
# =========================

BROKER = "broker.hivemq.com"
PORT = 1883

TOPIC_COMMAND = "multisport/trainer/command"
TOPIC_STATUS = "multisport/trainer/status"
TOPIC_EVENT = "multisport/trainer/event"
TOPIC_RESULT = "multisport/trainer/result"
TOPIC_SETUP = "multisport/trainer/setup"


# =========================
# SYSTEM CONFIGURATION
# =========================

CONE_COUNT = 5

CONES = [1, 2, 3, 4, 5]

SPORT_TYPES = [
    "football",
    "basketball",
    "agility",
    "obstacle",
    "custom"
]

TRAINING_MODES = [
    "custom_route",
    "generated_route"
]

DIFFICULTY_LEVELS = [
    "easy",
    "medium",
    "hard"
]


# =========================
# DIFFICULTY SETTINGS
# =========================

DIFFICULTY_SETTINGS = {

    "easy": {
        "route_length": 3,
        "step_delay": 2.5,
        "distraction_chance": 0.0,
        "wrong_penalty": 2
    },

    "medium": {
        "route_length": 4,
        "step_delay": 2.0,
        "distraction_chance": 0.25,
        "wrong_penalty": 4
    },

    "hard": {
        "route_length": 5,
        "step_delay": 1.5,
        "distraction_chance": 0.45,
        "wrong_penalty": 6
    }
}


# =========================
# SCORING SETTINGS
# =========================

CORRECT_SCORE = 10

WRONG_SCORE_PENALTY = 3

DISTRACTION_MISTAKE_PENALTY = 5


# =========================
# SESSION SETTINGS
# =========================

DEFAULT_PLAYER_NAME = "Guest"

DEFAULT_DURATION_SECONDS = 30
