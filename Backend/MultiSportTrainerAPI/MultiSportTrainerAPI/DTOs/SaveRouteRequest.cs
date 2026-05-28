namespace MultiSportTrainerAPI.DTOs
{
    public class SaveRouteRequest
    {
        public int UserId { get; set; }

        public int? SessionId { get; set; }

        public string RouteType { get; set; } = string.Empty;

        public List<int> ConeSequence { get; set; } = new List<int>();
    }
}