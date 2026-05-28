using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using MultiSportTrainerAPI.Data;

namespace MultiSportTrainerAPI.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class StatisticsController : ControllerBase
    {
        private readonly AppDbContext _context;

        public StatisticsController(AppDbContext context)
        {
            _context = context;
        }

        [HttpGet("{userId}")]
        public async Task<IActionResult> GetStatistics(int userId)
        {
            var results = await _context.TrainingResults
                .Where(r => r.UserId == userId)
                .ToListAsync();

            if (!results.Any())
            {
                return Ok(new
                {
                    totalTrainings = 0,
                    bestScore = 0,
                    avgScore = 0,
                    avgAccuracy = 0,
                    avgReaction = 0,
                    totalTimeSeconds = 0
                });
            }

            var statistics = new
            {
                totalTrainings = results.Count,
                bestScore = results.Max(r => r.Score),
                avgScore = Math.Round(results.Average(r => r.Score), 2),
                avgAccuracy = Math.Round(results.Average(r => r.Accuracy), 2),
                avgReaction = Math.Round(results.Average(r => r.AverageReactionSeconds ?? 0), 2),
                bestReaction = results.Min(r => r.BestReactionSeconds ?? 0),
                totalTimeSeconds = results.Sum(r => r.DurationSeconds)
            };

            return Ok(statistics);
        }

        [HttpGet("latest/{userId}")]
        public async Task<IActionResult> GetLatestResult(int userId)
        {
            var latestResult = await _context.TrainingResults
                .Where(r => r.UserId == userId)
                .Include(r => r.Session)
                .OrderByDescending(r => r.CreatedAt)
                .Select(r => new
                {
                    r.ResultId,
                    r.SessionId,
                    TrainingType = r.Session != null ? r.Session.TrainingType : "",
                    RouteType = r.Session != null ? r.Session.RouteType : "",
                    Difficulty = r.Session != null ? r.Session.Difficulty : "",
                    r.Score,
                    r.Accuracy,
                    r.Mistakes,
                    r.DurationSeconds,
                    r.AverageReactionSeconds,
                    r.BestReactionSeconds,
                    r.CreatedAt
                })
                .FirstOrDefaultAsync();

            if (latestResult == null)
            {
                return NotFound(new { message = "No result found for this user" });
            }

            return Ok(latestResult);
        }
    }
}