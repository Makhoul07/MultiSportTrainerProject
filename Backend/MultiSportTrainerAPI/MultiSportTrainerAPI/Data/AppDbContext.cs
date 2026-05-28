using Microsoft.EntityFrameworkCore;
using MultiSportTrainerAPI.Models;
using AppRoute = MultiSportTrainerAPI.Models.Route;

namespace MultiSportTrainerAPI.Data
{
    public class AppDbContext : DbContext
    {
        public AppDbContext(DbContextOptions<AppDbContext> options)
            : base(options)
        {
        }

        public DbSet<User> Users => Set<User>();
        public DbSet<TrainingSession> TrainingSessions => Set<TrainingSession>();
        public DbSet<AppRoute> Routes => Set<AppRoute>();
        public DbSet<RouteStep> RouteSteps => Set<RouteStep>();
        public DbSet<TrainingResult> TrainingResults => Set<TrainingResult>();

        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {
            base.OnModelCreating(modelBuilder);

            // Table names
            modelBuilder.Entity<User>().ToTable("Users");
            modelBuilder.Entity<TrainingSession>().ToTable("TrainingSessions");
            modelBuilder.Entity<AppRoute>().ToTable("Routes");
            modelBuilder.Entity<RouteStep>().ToTable("RouteSteps");
            modelBuilder.Entity<TrainingResult>().ToTable("TrainingResults");

            // Primary keys
            modelBuilder.Entity<User>()
                .HasKey(u => u.UserId);

            modelBuilder.Entity<TrainingSession>()
                .HasKey(ts => ts.SessionId);

            modelBuilder.Entity<AppRoute>()
                .HasKey(r => r.RouteId);

            modelBuilder.Entity<RouteStep>()
                .HasKey(rs => rs.StepId);

            modelBuilder.Entity<TrainingResult>()
                .HasKey(tr => tr.ResultId);

            // Unique email
            modelBuilder.Entity<User>()
                .HasIndex(u => u.Email)
                .IsUnique();

            // Decimal precision
            modelBuilder.Entity<TrainingResult>()
                .Property(tr => tr.Accuracy)
                .HasPrecision(5, 2);

            modelBuilder.Entity<TrainingResult>()
                .Property(tr => tr.AverageReactionSeconds)
                .HasPrecision(5, 2);

            modelBuilder.Entity<TrainingResult>()
                .Property(tr => tr.BestReactionSeconds)
                .HasPrecision(5, 2);

            // Relationships
            modelBuilder.Entity<TrainingSession>()
                .HasOne(ts => ts.User)
                .WithMany(u => u.TrainingSessions)
                .HasForeignKey(ts => ts.UserId);

            modelBuilder.Entity<AppRoute>()
                .HasOne(r => r.User)
                .WithMany(u => u.Routes)
                .HasForeignKey(r => r.UserId);

            modelBuilder.Entity<AppRoute>()
                .HasOne(r => r.Session)
                .WithMany(ts => ts.Routes)
                .HasForeignKey(r => r.SessionId);

            modelBuilder.Entity<RouteStep>()
                .HasOne(rs => rs.Route)
                .WithMany(r => r.RouteSteps)
                .HasForeignKey(rs => rs.RouteId);

            modelBuilder.Entity<TrainingResult>()
                .HasOne(tr => tr.User)
                .WithMany(u => u.TrainingResults)
                .HasForeignKey(tr => tr.UserId);

            modelBuilder.Entity<TrainingResult>()
                .HasOne(tr => tr.Session)
                .WithMany(ts => ts.TrainingResults)
                .HasForeignKey(tr => tr.SessionId);
        }
    }
}