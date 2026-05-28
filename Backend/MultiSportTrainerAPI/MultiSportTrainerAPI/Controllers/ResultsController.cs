using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using MultiSportTrainerAPI.Data;
using MultiSportTrainerAPI.DTOs;
using MultiSportTrainerAPI.Models;

namespace MultiSportTrainerAPI.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class ResultsController : ControllerBase
    {
        private readonly AppDbContext _context;

        public ResultsController(AppDbContext context)
        {
            _context = context;
        }

        [HttpPost("save")]
        public async Task<IActionResult> SaveResult(SaveResultRequest request)
        {
            var userExists = await _context.Users.AnyAsync(u => u.UserId == request.UserId);

            if (!userExists)
            {
                return NotFound(new { message = "User not found" });
            }

            var session = await _context.TrainingSessions.FindAsync(request.SessionId);

            if (session == null)
            {
                return NotFound(new { message = "Session not found" });
            }

            TrainingResult result = new TrainingResult
            {
                SessionId = request.SessionId,
                UserId = request.UserId,
                Score = request.Score,
                Accuracy = request.Accuracy,
                Mistakes = request.Mistakes,
                DurationSeconds = request.DurationSeconds,
                AverageReactionSeconds = request.AverageReactionSeconds,
                BestReactionSeconds = request.BestReactionSeconds,
                CreatedAt = DateTime.Now
            };

            _context.TrainingResults.Add(result);

            session.Status = "Completed";
            session.EndedAt = DateTime.Now;

            await _context.SaveChangesAsync();

            return Ok(new
            {
                result.ResultId,
                result.SessionId,
                result.UserId,
                result.Score,
                result.Accuracy,
                result.Mistakes,
                result.DurationSeconds,
                result.AverageReactionSeconds,
                result.BestReactionSeconds,
                result.CreatedAt,
                message = "Result saved successfully"
            });
        }

        [HttpGet("user/{userId}")]
        public async Task<IActionResult> GetUserResults(int userId)
        {
            var results = await _context.TrainingResults
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
                .ToListAsync();

            return Ok(results);
        }
    }
}