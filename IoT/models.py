# =========================
# CONE MODEL
# =========================

class Cone:

    def __init__(self, cone_id):
        self.cone_id = cone_id
        self.green_active = False
        self.red_active = False

    def activate_green(self):
        self.green_active = True

    def deactivate_green(self):
        self.green_active = False

    def activate_red(self):
        self.red_active = True

    def deactivate_red(self):
        self.red_active = False


# =========================
# ROUTE MODEL
# =========================

class Route:

    def __init__(self, cones, difficulty):
        self.cones = cones
        self.difficulty = difficulty

    def display_route(self):
        return " → ".join(str(cone) for cone in self.cones)


# =========================
# TRAINING SESSION MODEL
# =========================

class TrainingSession:

    def __init__(self, player, sport, difficulty):
        self.player = player
        self.sport = sport
        self.difficulty = difficulty

        self.score = 0
        self.mistakes = 0
        self.accuracy = 0
        self.route = []