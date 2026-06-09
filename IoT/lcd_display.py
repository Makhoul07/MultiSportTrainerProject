"""Grove LCD RGB Backlight (16x2) driver over I2C.

The panel exposes two I2C devices:
  * 0x3E - the JHD1313/AIP31068 character LCD controller (HD44780-compatible)
  * 0x62 - the PCA9633 RGB backlight controller (Grove LCD RGB v2.0)

This module is written to degrade gracefully: if smbus2 is not installed or the
I2C bus cannot be opened (e.g. running training_simulator.py on a dev machine
instead of the Raspberry Pi), every function becomes a logged no-op so the
training session keeps running without a display.
"""

import time

try:
    from smbus2 import SMBus
except ImportError:  # not on a Pi / smbus2 not installed
    SMBus = None


# I2C addresses
LCD_ADDR = 0x3E   # character LCD controller
RGB_ADDR = 0x62   # RGB backlight controller (PCA9633)

I2C_BUS = 1       # Raspberry Pi 3B uses I2C bus 1

# How long the brief feedback screens (CORRECT! / WRONG!) stay up before the
# next round overwrites them.
FEEDBACK_HOLD_SECONDS = 0.8

_bus = None
_available = False


def _lcd_cmd(command):
    """Send a command byte to the LCD controller."""
    if not _available:
        return
    try:
        _bus.write_byte_data(LCD_ADDR, 0x80, command)
    except OSError as exc:
        print(f"[lcd] command failed: {exc}")


def _lcd_char(char):
    """Send a single data byte (character) to the LCD controller."""
    if not _available:
        return
    try:
        _bus.write_byte_data(LCD_ADDR, 0x40, ord(char))
    except OSError as exc:
        print(f"[lcd] write failed: {exc}")


def init_lcd():
    """Open the I2C bus and initialise the display.

    Returns True if the display is ready, False if it is unavailable (the
    caller can ignore the result; all other functions no-op when unavailable).
    """
    global _bus, _available

    if SMBus is None:
        print("[lcd] smbus2 not available - display disabled")
        _available = False
        return False

    try:
        _bus = SMBus(I2C_BUS)
    except (FileNotFoundError, OSError) as exc:
        print(f"[lcd] could not open I2C bus {I2C_BUS}: {exc} - display disabled")
        _available = False
        return False

    _available = True

    # JHD1313 init sequence.
    time.sleep(0.05)
    _lcd_cmd(0x28)  # function set: 2 lines, 5x8 dots
    _lcd_cmd(0x28)
    _lcd_cmd(0x0C)  # display on, cursor off, blink off
    _lcd_cmd(0x01)  # clear display
    time.sleep(0.05)
    _lcd_cmd(0x06)  # entry mode: increment, no shift

    set_backlight(255, 255, 255)
    print("[lcd] display initialised")
    return True


def set_backlight(r, g, b):
    """Set the RGB backlight colour via the PCA9633 controller."""
    if not _available:
        return
    try:
        _bus.write_byte_data(RGB_ADDR, 0x00, 0x00)  # MODE1
        _bus.write_byte_data(RGB_ADDR, 0x01, 0x00)  # MODE2
        _bus.write_byte_data(RGB_ADDR, 0x08, 0xAA)  # LEDOUT: all channels PWM
        _bus.write_byte_data(RGB_ADDR, 0x04, r & 0xFF)
        _bus.write_byte_data(RGB_ADDR, 0x03, g & 0xFF)
        _bus.write_byte_data(RGB_ADDR, 0x02, b & 0xFF)
    except OSError as exc:
        print(f"[lcd] backlight failed: {exc}")


def clear_display():
    """Clear the screen."""
    _lcd_cmd(0x01)
    time.sleep(0.002)


def write_text(line1, line2=""):
    """Write up to 16 characters on each of the two lines."""
    if not _available:
        return

    clear_display()

    _lcd_cmd(0x80)  # move cursor to start of line 1
    for char in str(line1)[:16]:
        _lcd_char(char)

    _lcd_cmd(0xC0)  # move cursor to start of line 2
    for char in str(line2)[:16]:
        _lcd_char(char)


def show_ready():
    """Idle / start screen: green backlight."""
    set_backlight(0, 255, 0)
    write_text("MultiSport", "Ready!")


def show_round(round_num, total_rounds):
    """Per-round prompt: blue backlight."""
    set_backlight(0, 0, 255)
    write_text(f"Round {round_num}/{total_rounds}", "Get Ready!")


def show_correct():
    """Brief positive feedback: green backlight."""
    set_backlight(0, 255, 0)
    write_text("CORRECT!", "")
    time.sleep(FEEDBACK_HOLD_SECONDS)


def show_wrong():
    """Brief negative feedback: red backlight."""
    set_backlight(255, 0, 0)
    write_text("WRONG!", "")
    time.sleep(FEEDBACK_HOLD_SECONDS)


def show_timer(elapsed_seconds):
    """Live timer: 'Time: MM:SS' on line 1, 'Training...' on line 2."""
    total = int(elapsed_seconds)
    minutes = total // 60
    seconds = total % 60
    write_text(f"Time: {minutes:02d}:{seconds:02d}", "Training...")


def show_finished(score, accuracy):
    """End-of-session summary: blue backlight."""
    set_backlight(0, 0, 255)
    write_text(f"Score: {score}", f"Acc: {int(round(accuracy))}%")
