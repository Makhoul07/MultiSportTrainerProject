using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using MultiSportTrainerAPI.Data;
using MultiSportTrainerAPI.DTOs;

namespace MultiSportTrainerAPI.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class UsersController : ControllerBase
    {
        private readonly AppDbContext _context;

        public UsersController(AppDbContext context)
        {
            _context = context;
        }

        [HttpGet("{userId}")]
        public async Task<IActionResult> GetUser(int userId)
        {
            var user = await _context.Users
                .Where(u => u.UserId == userId)
                .Select(u => new
                {
                    u.UserId,
                    u.FullName,
                    u.Email,
                    u.DateOfBirth,
                    u.Role,
                    u.SportFocus,
                    u.CreatedAt,

                    TotalTrainings = _context.TrainingResults.Count(r => r.UserId == userId),

                    BestScore = _context.TrainingResults
                        .Where(r => r.UserId == userId)
                        .Select(r => (int?)r.Score)
                        .Max() ?? 0,

                    AvgAccuracy = _context.TrainingResults
                        .Where(r => r.UserId == userId)
                        .Select(r => (decimal?)r.Accuracy)
                        .Average() ?? 0,

                    AvgReaction = _context.TrainingResults
                        .Where(r => r.UserId == userId)
                        .Select(r => r.AverageReactionSeconds)
                        .Average() ?? 0
                })
                .FirstOrDefaultAsync();

            if (user == null)
            {
                return NotFound(new { message = "User not found" });
            }

            return Ok(user);
        }

        [HttpPut("{userId}")]
        public async Task<IActionResult> UpdateUser(int userId, UpdateUserRequest request)
        {
            var user = await _context.Users.FindAsync(userId);

            if (user == null)
            {
                return NotFound(new { message = "User not found" });
            }

            if (string.IsNullOrWhiteSpace(request.FullName))
            {
                return BadRequest(new { message = "Full name is required" });
            }

            if (string.IsNullOrWhiteSpace(request.Email))
            {
                return BadRequest(new { message = "Email is required" });
            }

            bool emailUsedByAnotherUser = await _context.Users
                .AnyAsync(u => u.Email == request.Email && u.UserId != userId);

            if (emailUsedByAnotherUser)
            {
                return BadRequest(new { message = "Email is already used by another user" });
            }

            user.FullName = request.FullName;
            user.Email = request.Email;
            user.DateOfBirth = request.DateOfBirth;
            user.Role = string.IsNullOrWhiteSpace(request.Role) ? "Player" : request.Role;
            user.SportFocus = request.SportFocus;

            await _context.SaveChangesAsync();

            return Ok(new
            {
                user.UserId,
                user.FullName,
                user.Email,
                user.DateOfBirth,
                user.Role,
                user.SportFocus,
                message = "Profile updated successfully"
            });
        }
    }
}   