-- Dedicated service account used by IoT/backend_bridge.py to authenticate
-- against the JWT-protected API and POST results to /api/Results/save.
--
-- Idempotent: safe to run against an existing database. IDENTITY assigns the
-- UserId (the bridge logs in by email, so the id value is irrelevant).
--
-- NOTE: PasswordHash is stored in plaintext to match the current auth scheme.
-- Keep these credentials in sync with BRIDGE_EMAIL / BRIDGE_PASSWORD in
-- IoT/backend_bridge.py.

USE [MultiSportTrainerDB];
GO

IF NOT EXISTS (SELECT 1 FROM [dbo].[Users] WHERE [Email] = N'bridge@multisport.local')
BEGIN
    INSERT INTO [dbo].[Users]
        ([FullName], [Email], [PasswordHash], [DateOfBirth], [Role], [SportFocus], [CreatedAt])
    VALUES
        (N'IoT Bridge Service', N'bridge@multisport.local', N'Bridge#Service2026',
         NULL, N'Service', NULL, GETDATE());

    PRINT 'Bridge service user created.';
END
ELSE
BEGIN
    PRINT 'Bridge service user already exists; no action taken.';
END
GO
