namespace MultiSportTrainerAPI.DTOs
{
    public class GenerateRouteRequest
    {
        public int UserId { get; set; }

        public string Difficulty { get; set; } = "Medium";
    }
}
