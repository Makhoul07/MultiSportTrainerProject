using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using MultiSportTrainerAPI.Data;
using MultiSportTrainerAPI.DTOs;
using AppRoute = MultiSportTrainerAPI.Models.Route;
using MultiSportTrainerAPI.Models;
using MultiSportTrainerAPI.Services;

namespace MultiSportTrainerAPI.Controllers
{
    [Authorize]
    [Route("api/[controller]")]
    [ApiController]
    public class RoutesController : ControllerBase
    {
        private readonly AppDbContext _context;
        private readonly ClaudeRouteService _routeService;

        public RoutesController(AppDbContext context, ClaudeRouteService routeService)
        {
            _context = context;
            _routeService = routeService;
        }

        /// <summary>
        /// Generates a personalised cone sequence with Claude, tailored to the
        /// player's recorded stats. Returns 502 on any generation failure so the
        /// Android client can fall back to a default route.
        /// </summary>
        [HttpPost("generate")]
        public async Task<IActionResult> GenerateRoute(GenerateRouteRequest request)
        {
            var userExists = await _context.Users.AnyAsync(u => u.UserId == request.UserId);

            if (!userExists)
            {
                return NotFound(new { message = "User not found" });
            }

            string difficulty = string.IsNullOrWhiteSpace(request.Difficulty)
                ? "Medium"
                : request.Difficulty;

            int targetSteps = RoundsForDifficulty(difficulty);

            // Pull the player's stats here so they never need to touch the client.
            var results = await _context.TrainingResults
                .Where(r => r.UserId == request.UserId)
                .ToListAsync();

            int totalTrainings = results.Count;
            int bestScore = results.Any() ? results.Max(r => r.Score) : 0;
            double avgAccuracy = results.Any() ? (double)Math.Round(results.Average(r => r.Accuracy), 2) : 0;

            try
            {
                var coneSequence = await _routeService.GenerateConeSequenceAsync(
                    difficulty, targetSteps, totalTrainings, avgAccuracy, bestScore);

                return Ok(new
                {
                    coneSequence,
                    rounds = coneSequence.Count
                });
            }
            catch (Exception ex)
            {
                return StatusCode(502, new { message = "Route generation failed", detail = ex.Message });
            }
        }

        // Mirrors training_simulator.get_rounds_from_difficulty / the Android UI.
        private static int RoundsForDifficulty(string difficulty)
        {
            if (difficulty.Equals("Easy", StringComparison.OrdinalIgnoreCase))
            {
                return 4;
            }

            if (difficulty.Equals("Hard", StringComparison.OrdinalIgnoreCase))
            {
                return 8;
            }

            return 6;
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