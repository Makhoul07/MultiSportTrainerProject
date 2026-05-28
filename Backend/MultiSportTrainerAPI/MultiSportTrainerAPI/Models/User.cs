using System;
using System.Collections.Generic;

namespace MultiSportTrainerAPI.Models
{
    public class User
    {
        public int UserId { get; set; }

        public string FullName { get; set; } = string.Empty;

        public string Email { get; set; } = string.Empty;

        public string PasswordHash { get; set; } = string.Empty;

        public DateTime? DateOfBirth { get; set; }

        public string Role { get; set; } = "Player";

        public string? SportFocus { get; set; }

        public DateTime CreatedAt { get; set; }

        public ICollection<TrainingSession> TrainingSessions { get; set; } = new List<TrainingSession>();

        public ICollection<Route> Routes { get; set; } = new List<Route>();

        public ICollection<TrainingResult> TrainingResults { get; set; } = new List<TrainingResult>();
    }
}