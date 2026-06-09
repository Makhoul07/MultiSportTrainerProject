using System.Text.Json;
using System.Text.Json.Serialization;
using Anthropic;
using Anthropic.Models.Messages;

namespace MultiSportTrainerAPI.Services
{
    /// <summary>
    /// Generates a personalised cone-drill sequence by calling the Claude API
    /// server-side. The Anthropic API key stays on the server (read from
    /// configuration or the ANTHROPIC_API_KEY environment variable) and is never
    /// shipped to the Android client.
    /// </summary>
    public class ClaudeRouteService
    {
        // The rig has exactly three physical cones.
        private const int MinCone = 1;
        private const int MaxCone = 3;

        // Clamp to keep a malformed response from producing an absurd route.
        private const int MaxSteps = 12;
        private const int MinSteps = 3;

        private readonly string? _apiKey;
        private AnthropicClient? _client;

        public ClaudeRouteService(IConfiguration configuration)
        {
            _apiKey = configuration["Anthropic:ApiKey"];

            if (string.IsNullOrWhiteSpace(_apiKey))
            {
                _apiKey = Environment.GetEnvironmentVariable("ANTHROPIC_API_KEY");
            }
        }

        private AnthropicClient Client()
        {
            if (string.IsNullOrWhiteSpace(_apiKey))
            {
                throw new InvalidOperationException(
                    "Anthropic API key is not configured. Set Anthropic:ApiKey or the " +
                    "ANTHROPIC_API_KEY environment variable.");
            }

            return _client ??= new AnthropicClient { ApiKey = _apiKey };
        }

        /// <summary>
        /// Asks Claude for an ordered list of cone numbers (1-3) tailored to the
        /// player's recent performance. Throws if the key is missing or Claude
        /// returns an unusable sequence; the caller maps any failure to a 502 so
        /// the Android client can fall back to a default route.
        /// </summary>
        public async Task<List<int>> GenerateConeSequenceAsync(
            string difficulty,
            int targetSteps,
            int totalTrainings,
            double avgAccuracy,
            int bestScore)
        {
            // Everything Claude needs is in one user message, constrained to a JSON
            // schema so the response is always a clean { "coneSequence": [...] }.
            string prompt =
                "You design cone-dribbling drills for a football training rig with exactly three " +
                "physical cones numbered 1, 2 and 3. A route is an ordered list of cone numbers the " +
                "player touches in sequence. Avoid touching the same cone twice in a row, and make " +
                "harder routes change direction more often.\n\n" +
                $"Create a {difficulty} drill with exactly {targetSteps} steps using only cone numbers " +
                "1, 2 and 3. Tailor it to this player profile: " +
                $"{totalTrainings} sessions completed, average accuracy {avgAccuracy}%, best score {bestScore}.";

            var schema = new Dictionary<string, JsonElement>
            {
                ["type"] = JsonSerializer.SerializeToElement("object"),
                ["properties"] = JsonSerializer.SerializeToElement(new
                {
                    coneSequence = new
                    {
                        type = "array",
                        items = new { type = "integer", @enum = new[] { 1, 2, 3 } }
                    }
                }),
                ["required"] = JsonSerializer.SerializeToElement(new[] { "coneSequence" }),
                ["additionalProperties"] = JsonSerializer.SerializeToElement(false),
            };

            var parameters = new MessageCreateParams
            {
                Model = Model.ClaudeOpus4_8,
                MaxTokens = 300,
                Messages = [new() { Role = Role.User, Content = prompt }],
                OutputConfig = new OutputConfig
                {
                    Format = new JsonOutputFormat { Schema = schema },
                },
            };

            var response = await Client().Messages.Create(parameters);

            string? json = response.Content
                .Select(b => b.Value)
                .OfType<TextBlock>()
                .Select(t => t.Text)
                .FirstOrDefault();

            if (string.IsNullOrWhiteSpace(json))
            {
                throw new InvalidOperationException("Claude returned an empty response.");
            }

            var parsed = JsonSerializer.Deserialize<GeneratedRoute>(json);

            var sequence = (parsed?.ConeSequence ?? new List<int>())
                .Where(n => n >= MinCone && n <= MaxCone)
                .ToList();

            if (sequence.Count < MinSteps)
            {
                throw new InvalidOperationException("Claude returned an unusable cone sequence.");
            }

            if (sequence.Count > MaxSteps)
            {
                sequence = sequence.Take(MaxSteps).ToList();
            }

            return sequence;
        }

        private class GeneratedRoute
        {
            [JsonPropertyName("coneSequence")]
            public List<int> ConeSequence { get; set; } = new();
        }
    }
}
