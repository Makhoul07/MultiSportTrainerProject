# =========================
# STATUS EVENTS
# =========================

def training_started_event(player, sport, difficulty, route):
    return {
        "status": "training_started",
        "player": player,
        "sport": sport,
        "difficulty": difficulty,
        "route": route
    }


# =========================
# CONE ACTIVATION EVENT
# =========================

def cone_activation_event(target_cone, distraction_cone):
    return {
        "type": "cone_activation",
        "target_cone": target_cone,
        "green_light": target_cone,
        "red_light": distraction_cone
    }


# =========================
# CONE RESULT EVENTS
# =========================

def correct_cone_event(cone, score):
    return {
        "type": "cone_result",
        "cone": cone,
        "result": "correct",
        "score": score
    }


def wrong_cone_event(wrong_cone, expected_cone, score):
    return {
        "type": "cone_result",
        "cone": wrong_cone,
        "expected_cone": expected_cone,
        "result": "wrong",
        "score": score
    }


# =========================
# FINAL RESULT EVENT
# =========================

def final_result_event(player, sport, difficulty, score, mistakes, accuracy):
    return {
        "player": player,
        "sport": sport,
        "difficulty": difficulty,
        "score": score,
        "mistakes": mistakes,
        "accuracy": accuracy,
        "message": "Training finished"
    }