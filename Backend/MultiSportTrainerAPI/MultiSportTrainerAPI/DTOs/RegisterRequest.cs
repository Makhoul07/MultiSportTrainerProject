namespace MultiSportTrainerAPI.DTOs
{
    public class RegisterRequest
    {
        public string FullName { get; set; } = string.Empty;

        public string Email { get; set; } = string.Empty;

        public string Password { get; set; } = string.Empty;

        public DateTime? DateOfBirth { get; set; }

        public string Role { get; set; } = "Player";

        public string? SportFocus { get; set; }
    }
}