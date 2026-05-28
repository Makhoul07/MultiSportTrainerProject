namespace MultiSportTrainerAPI.DTOs
{
    public class StartTrainingRequest
    {
        public int UserId { get; set; }

        public string RouteType { get; set; } = string.Empty;

        public string Difficulty { get; set; } = string.Empty;

        public string TrainingType { get; set; } = string.Empty;

        public int ConesCount { get; set; }

        public int Rounds { get; set; }

        public bool DistractionsEnabled { get; set; }
    }
}