namespace MultiSportTrainerAPI.DTOs
{
    public class UpdateUserRequest
    {
        public string FullName { get; set; } = string.Empty;

        public string Email { get; set; } = string.Empty;

        public DateTime? DateOfBirth { get; set; }

        public string Role { get; set; } = "Player";

        public string? SportFocus { get; set; }
    }
}