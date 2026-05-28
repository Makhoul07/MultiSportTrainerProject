namespace MultiSportTrainerAPI.Models
{
    public class RouteStep
    {
        public int StepId { get; set; }

        public int RouteId { get; set; }

        public int ConeNumber { get; set; }

        public int StepOrder { get; set; }

        public Route? Route { get; set; }
    }
}