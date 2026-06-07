namespace MultiSportTrainerAPI.DTOs
{
    public class SaveResultRequest
    {
        public int SessionId { get; set; }

        public int UserId { get; set; }

        public int Score { get; set; }

        public decimal Accuracy { get; set; }

        public int Mistakes { get; set; }

        public int DurationSeconds { get; set; }
    }
}