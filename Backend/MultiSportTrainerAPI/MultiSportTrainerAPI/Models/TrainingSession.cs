using System;
using System.Collections.Generic;

namespace MultiSportTrainerAPI.Models
{
    public class TrainingSession
    {
        public int SessionId { get; set; }

        public int UserId { get; set; }

        public string RouteType { get; set; } = string.Empty;

        public string Difficulty { get; set; } = string.Empty;

        public string TrainingType { get; set; } = string.Empty;

        public int ConesCount { get; set; }

        public int Rounds { get; set; }

        public bool DistractionsEnabled { get; set; }

        public DateTime StartedAt { get; set; }

        public DateTime? EndedAt { get; set; }

        public string Status { get; set; } = "Started";

        public User? User { get; set; }

        public ICollection<Route> Routes { get; set; } = new List<Route>();

        public ICollection<TrainingResult> TrainingResults { get; set; } = new List<TrainingResult>();
    }
}