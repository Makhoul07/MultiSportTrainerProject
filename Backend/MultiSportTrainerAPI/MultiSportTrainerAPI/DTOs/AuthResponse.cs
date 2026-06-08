namespace MultiSportTrainerAPI.DTOs
{
    public class AuthResponse
    {
        public int UserId { get; set; }

        public string FullName { get; set; } = string.Empty;

        public string Email { get; set; } = string.Empty;

        public string Role { get; set; } = string.Empty;

        public string? SportFocus { get; set; }

        public string Token { get; set; } = string.Empty;

        public string Message { get; set; } = string.Empty;
    }
}