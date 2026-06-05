import random
from config import CONES, DIFFICULTY_SETTINGS


def generate_route(difficulty):
    settings = DIFFICULTY_SETTINGS[difficulty]
    route_length = settings["route_length"]

    return random.sample(CONES, route_length)


def generate_distraction_cone(target_cone, difficulty):
    settings = DIFFICULTY_SETTINGS[difficulty]

    distraction_chance = settings["distraction_chance"]

    if random.random() > distraction_chance:
        return None

    possible_cones = [
        cone for cone in CONES
        if cone != target_cone
    ]

    return random.choice(possible_cones)