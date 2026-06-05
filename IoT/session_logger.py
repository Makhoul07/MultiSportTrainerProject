import json
from datetime import datetime


# =========================
# SAVE SESSION RESULT
# =========================

def save_session(result_data):

    log_entry = {
        "timestamp": str(datetime.now()),
        "result": result_data
    }

    try:
        with open("training_history.json", "a") as file:
            file.write(json.dumps(log_entry))
            file.write("\n")

        print("Session saved to training_history.json")

    except Exception as e:
        print("Error saving session:", e)