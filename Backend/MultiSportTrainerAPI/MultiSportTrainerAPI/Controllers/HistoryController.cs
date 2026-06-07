using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using MultiSportTrainerAPI.Data;

namespace MultiSportTrainerAPI.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class HistoryController : ControllerBase
    {
        private readonly AppDbContext _context;

        public HistoryController(AppDbContext context)
        {
            _context = context;
        }

        [HttpGet("{userId}")]
        public async Task<IActionResult> GetHistory(int userId)
        {
            var history = await _context.TrainingResults
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
                    ConesCount = r.Session != null ? r.Session.ConesCount : 0,
                    Rounds = r.Session != null ? r.Session.Rounds : 0,
                    r.Score,
                    r.Accuracy,
                    r.Mistakes,
                    r.DurationSeconds,
                    r.CreatedAt
                })
                .ToListAsync();

            return Ok(history);
        }
    }
}