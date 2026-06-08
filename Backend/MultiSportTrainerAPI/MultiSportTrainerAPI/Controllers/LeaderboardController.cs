using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using MultiSportTrainerAPI.Data;

namespace MultiSportTrainerAPI.Controllers
{
    [Authorize]
    [Route("api/[controller]")]
    [ApiController]
    public class LeaderboardController : ControllerBase
    {
        private readonly AppDbContext _context;

        public LeaderboardController(AppDbContext context)
        {
            _context = context;
        }

        [HttpGet]
        public async Task<IActionResult> GetLeaderboard()
        {
            var leaderboard = await _context.TrainingResults
                .Include(r => r.User)
                .GroupBy(r => new
                {
                    r.UserId,
                    UserName = r.User != null ? r.User.FullName : "Unknown"
                })
                .Select(g => new
                {
                    userId = g.Key.UserId,
                    fullName = g.Key.UserName,
                    bestScore = g.Max(r => r.Score),
                    avgAccuracy = Math.Round(g.Average(r => r.Accuracy), 2),
                    totalTrainings = g.Count()
                })
                .OrderByDescending(x => x.bestScore)
                .ToListAsync();

            return Ok(leaderboard);
        }
    }
}