from config import CORRECT_SCORE


def calculate_accuracy(total_steps, mistakes):
    if total_steps == 0:
        return 0

    correct_steps = total_steps - mistakes
    return round((correct_steps / total_steps) * 100, 2)


def update_score_correct(current_score):
    return current_score + CORRECT_SCORE


def update_score_wrong(current_score, penalty):
    return current_score - penalty