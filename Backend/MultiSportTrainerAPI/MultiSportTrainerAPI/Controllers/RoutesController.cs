using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using MultiSportTrainerAPI.Data;
using MultiSportTrainerAPI.DTOs;
using AppRoute = MultiSportTrainerAPI.Models.Route;
using MultiSportTrainerAPI.Models;

namespace MultiSportTrainerAPI.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class RoutesController : ControllerBase
    {
        private readonly AppDbContext _context;

        public RoutesController(AppDbContext context)
        {
            _context = context;
        }

        [HttpPost("save")]
        public async Task<IActionResult> SaveRoute(SaveRouteRequest request)
        {
            var userExists = await _context.Users.AnyAsync(u => u.UserId == request.UserId);

            if (!userExists)
            {
                return NotFound(new { message = "User not found" });
            }

            if (request.SessionId != null)
            {
                var sessionExists = await _context.TrainingSessions
                    .AnyAsync(s => s.SessionId == request.SessionId);

                if (!sessionExists)
                {
                    return NotFound(new { message = "Session not found" });
                }
            }

            if (string.IsNullOrWhiteSpace(request.RouteType))
            {
                return BadRequest(new { message = "Route type is required" });
            }

            if (request.ConeSequence == null || request.ConeSequence.Count == 0)
            {
                return BadRequest(new { message = "Cone sequence is required" });
            }

            AppRoute route = new AppRoute
            {
                UserId = request.UserId,
                SessionId = request.SessionId,
                RouteType = request.RouteType,
                CreatedAt = DateTime.Now
            };

            _context.Routes.Add(route);
            await _context.SaveChangesAsync();

            for (int i = 0; i < request.ConeSequence.Count; i++)
            {
                RouteStep step = new RouteStep
                {
                    RouteId = route.RouteId,
                    ConeNumber = request.ConeSequence[i],
                    StepOrder = i + 1
                };

                _context.RouteSteps.Add(step);
            }

            await _context.SaveChangesAsync();

            return Ok(new
            {
                route.RouteId,
                route.UserId,
                route.SessionId,
                route.RouteType,
                coneSequence = request.ConeSequence,
                message = "Route saved successfully"
            });
        }

        [HttpGet("user/{userId}")]
        public async Task<IActionResult> GetUserRoutes(int userId)
        {
            var routes = await _context.Routes
                .Where(r => r.UserId == userId)
                .Include(r => r.RouteSteps)
                .OrderByDescending(r => r.CreatedAt)
                .Select(r => new
                {
                    r.RouteId,
                    r.RouteType,
                    r.SessionId,
                    r.CreatedAt,
                    coneSequence = r.RouteSteps
                        .OrderBy(s => s.StepOrder)
                        .Select(s => s.ConeNumber)
                        .ToList()
                })
                .ToListAsync();

            return Ok(routes);
        }

        [HttpGet("session/{sessionId}")]
        public async Task<IActionResult> GetSessionRoute(int sessionId)
        {
            var route = await _context.Routes
                .Where(r => r.SessionId == sessionId)
                .Include(r => r.RouteSteps)
                .OrderByDescending(r => r.CreatedAt)
                .Select(r => new
                {
                    r.RouteId,
                    r.RouteType,
                    r.SessionId,
                    r.CreatedAt,
                    coneSequence = r.RouteSteps
                        .OrderBy(s => s.StepOrder)
                        .Select(s => s.ConeNumber)
                        .ToList()
                })
                .FirstOrDefaultAsync();

            if (route == null)
            {
                return NotFound(new { message = "Route not found for this session" });
            }

            return Ok(route);
        }
    }
}