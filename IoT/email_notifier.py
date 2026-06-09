import smtplib
from email.mime.multipart import MIMEMultipart
from email.mime.text import MIMEText

# =========================
# EMAIL CONFIGURATION
# =========================
# NOTE: Use a Gmail *App Password* (requires 2FA on the account), not your normal
# password. Do not commit real credentials to source control.
SENDER_EMAIL = "your_email@gmail.com"
SENDER_APP_PASSWORD = "your_app_password_here"

SMTP_HOST = "smtp.gmail.com"
SMTP_PORT = 587


def _format_duration(duration_seconds):
    total = int(round(float(duration_seconds)))
    minutes = total // 60
    seconds = total % 60
    return f"{minutes:02d}:{seconds:02d}"


def _pretty_route_type(route_type):
    mapping = {
        "custom_route": "Custom Route",
        "generated_route": "AI Generated Route",
        "Custom": "Custom Route",
        "Generated": "AI Generated Route",
    }
    return mapping.get(route_type, str(route_type or "Unknown"))


def send_training_result_email(to_email, player_name, score, accuracy,
                               mistakes, duration_seconds, difficulty, route_type):
    """Send an HTML training-summary email. Returns True on success, False otherwise.

    Never raises -- failures are logged so they don't break the training flow.
    """
    if not to_email:
        print("No recipient email provided; skipping email notification.")
        return False

    if (not SENDER_EMAIL or not SENDER_APP_PASSWORD
            or "your_" in SENDER_EMAIL or "your_" in SENDER_APP_PASSWORD):
        print("Sender email/app password not configured; skipping email notification.")
        return False

    duration_text = _format_duration(duration_seconds)
    difficulty_text = str(difficulty or "Unknown").capitalize()
    route_text = _pretty_route_type(route_type)

    subject = "MultiSport Trainer - Training Complete!"

    html = f"""\
<html>
  <body style="font-family: Arial, sans-serif; background:#f4f6f8; padding:24px;">
    <div style="max-width:520px; margin:auto; background:#ffffff; border-radius:12px;
                overflow:hidden; box-shadow:0 2px 8px rgba(0,0,0,0.08);">
      <div style="background:#1565C0; color:#ffffff; padding:20px 24px;">
        <h2 style="margin:0;">Training Complete!</h2>
        <p style="margin:4px 0 0;">Great work, {player_name}.</p>
      </div>
      <div style="padding:24px;">
        <table style="width:100%; border-collapse:collapse; font-size:15px;">
          <tr><td style="padding:8px 0; color:#666;">Score</td>
              <td style="padding:8px 0; text-align:right; font-weight:bold;">{score}</td></tr>
          <tr><td style="padding:8px 0; color:#666;">Accuracy</td>
              <td style="padding:8px 0; text-align:right; font-weight:bold;">{accuracy:.1f}%</td></tr>
          <tr><td style="padding:8px 0; color:#666;">Mistakes</td>
              <td style="padding:8px 0; text-align:right; font-weight:bold;">{mistakes}</td></tr>
          <tr><td style="padding:8px 0; color:#666;">Duration</td>
              <td style="padding:8px 0; text-align:right; font-weight:bold;">{duration_text}</td></tr>
          <tr><td style="padding:8px 0; color:#666;">Difficulty</td>
              <td style="padding:8px 0; text-align:right; font-weight:bold;">{difficulty_text}</td></tr>
          <tr><td style="padding:8px 0; color:#666;">Route Type</td>
              <td style="padding:8px 0; text-align:right; font-weight:bold;">{route_text}</td></tr>
        </table>
      </div>
      <div style="padding:16px 24px; background:#f0f2f5; color:#888; font-size:12px; text-align:center;">
        MultiSport Trainer
      </div>
    </div>
  </body>
</html>"""

    message = MIMEMultipart("alternative")
    message["Subject"] = subject
    message["From"] = SENDER_EMAIL
    message["To"] = to_email
    message.attach(MIMEText(html, "html"))

    try:
        with smtplib.SMTP(SMTP_HOST, SMTP_PORT) as server:
            server.starttls()
            server.login(SENDER_EMAIL, SENDER_APP_PASSWORD)
            server.sendmail(SENDER_EMAIL, to_email, message.as_string())
        print(f"Training result email sent to {to_email}")
        return True
    except Exception as e:
        print(f"Failed to send training result email: {e}")
        return False
