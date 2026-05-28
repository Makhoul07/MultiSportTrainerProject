using System;
using System.Collections.Generic;

namespace MultiSportTrainerAPI.Models
{
    public class Route
    {
        public int RouteId { get; set; }

        public int UserId { get; set; }

        public int? SessionId { get; set; }

        public string RouteType { get; set; } = string.Empty;

        public DateTime CreatedAt { get; set; }

        public User? User { get; set; }

        public TrainingSession? Session { get; set; }

        public ICollection<RouteStep> RouteSteps { get; set; } = new List<RouteStep>();
    }
}