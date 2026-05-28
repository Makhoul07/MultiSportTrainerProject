using System;

namespace MultiSportTrainerAPI.Models
{
    public class TrainingResult
    {
        public int ResultId { get; set; }

        public int SessionId { get; set; }

        public int UserId { get; set; }

        public int Score { get; set; }

        public decimal Accuracy { get; set; }

        public int Mistakes { get; set; }

        public int DurationSeconds { get; set; }

        public decimal? AverageReactionSeconds { get; set; }

        public decimal? BestReactionSeconds { get; set; }

        public DateTime CreatedAt { get; set; }

        public TrainingSession? Session { get; set; }

        public User? User { get; set; }
    }
}