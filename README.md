# MultiSport Trainer Project

This repository contains the full MultiSport Trainer system:

- Android mobile app
- ASP.NET Core Web API backend
- SQL Server database script

## Requirements

- Android Studio
- Visual Studio 2022
- SQL Server / SSMS
- .NET 8 SDK
- Git

## Database Setup

1. Open SQL Server Management Studio.
2. Open `Database/MultiSportTrainerDB.sql`.
3. Execute the script.
4. Make sure the database `MultiSportTrainerDB` is created.

## Backend Setup

1. Open `Backend/MultiSportTrainerAPI/MultiSportTrainerAPI.sln` in Visual Studio.
2. Open `appsettings.json`.
3. Check the connection string.

If your SQL Server name is local default:

```json
"DefaultConnection": "Server=.;Database=MultiSportTrainerDB;Trusted_Connection=True;TrustServerCertificate=True;"