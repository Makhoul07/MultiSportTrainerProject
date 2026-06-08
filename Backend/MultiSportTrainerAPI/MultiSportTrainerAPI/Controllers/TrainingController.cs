using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using MultiSportTrainerAPI.Data;
using MultiSportTrainerAPI.DTOs;
using MultiSportTrainerAPI.Models;

namespace MultiSportTrainerAPI.Controllers
{
    [Authorize]
    [Route("api/[controller]")]
    [ApiController]
    public class TrainingController : ControllerBase
    {
        private readonly AppDbContext _context;

        public TrainingController(AppDbContext context)
        {
            _context = context;
        }

        [HttpPost("start")]
        public async Task<IActionResult> StartTraining(StartTrainingRequest request)
        {
            var userExists = await _context.Users.AnyAsync(u => u.UserId == request.UserId);

            if (!userExists)
            {
                return NotFound(new { message = "User not found" });
            }

            if (string.IsNullOrWhiteSpace(request.RouteType))
            {
                return BadRequest(new { message = "Route type is required" });
            }

            if (string.IsNullOrWhiteSpace(request.Difficulty))
            {
                return BadRequest(new { message = "Difficulty is required" });
            }

            if (string.IsNullOrWhiteSpace(request.TrainingType))
            {
                return BadRequest(new { message = "Training type is required" });
            }

            TrainingSession session = new TrainingSession
            {
                UserId = request.UserId,
                RouteType = request.RouteType,
                Difficulty = request.Difficulty,
                TrainingType = request.TrainingType,
                ConesCount = request.ConesCount,
                Rounds = request.Rounds,
                DistractionsEnabled = request.DistractionsEnabled,
                StartedAt = DateTime.Now,
                Status = "Started"
            };

            _context.TrainingSessions.Add(session);
            await _context.SaveChangesAsync();

            return Ok(new
            {
                session.SessionId,
                session.UserId,
                session.RouteType,
                session.Difficulty,
                session.TrainingType,
                session.ConesCount,
                session.Rounds,
                session.DistractionsEnabled,
                session.StartedAt,
                session.Status,
                message = "Training session started"
            });
        }

        [HttpPut("complete/{sessionId}")]
        public async Task<IActionResult> CompleteTraining(int sessionId)
        {
            var session = await _context.TrainingSessions.FindAsync(sessionId);

            if (session == null)
            {
                return NotFound(new { message = "Session not found" });
            }

            session.EndedAt = DateTime.Now;
            session.Status = "Completed";

            await _context.SaveChangesAsync();

            return Ok(new
            {
                session.SessionId,
                session.Status,
                session.EndedAt,
                message = "Training session completed"
            });
        }
    }
}