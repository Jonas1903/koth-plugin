# KOTH Plugin

A complete King of the Hill (KOTH) plugin for Paper Minecraft 1.21.3+ using Java 22.

## Features

### 🔒 Permission System
- All commands require operator (OP) status
- Only OPs can configure and control KOTH events

### 🗺️ Region Selection System
- Define KOTH zones with two corner points
- Commands: `/koth setpos1` and `/koth setpos2`
- Persistent region storage in config.yml

### ⏱️ Capture Mechanics
- **Automatic Scheduling:** KOTH events start every 1 hour after the previous event ends
- **Capture Time:** 60 seconds (configurable) to capture the KOTH
- **Smart Reset Logic:**
  - Timer resets if the capturing player leaves the area
  - Timer resets if player dies or is knocked out by another player
  - Only one player can capture at a time

### 🎁 Reward System
- Configurable rewards via command or config file
- Execute console commands when a player wins
- Support for multiple rewards (separated by semicolons)
- Command: `/koth setreward <reward>`

### 📊 Boss Bar Display
- White boss bar visible to all online players
- Shows current capturing player's name
- Displays time remaining to capture
- Progress bar fills as capture progresses
- Automatically hides when KOTH is inactive

### 📢 Announcement System
Automatic announcements before KOTH starts:
- 45 minutes before
- 30 minutes before
- 20 minutes before
- 10 minutes before
- 5 minutes before
- 1 minute before

### 🎮 Manual Control Commands
- `/koth setpos1` - Set the first corner of the KOTH region
- `/koth setpos2` - Set the second corner of the KOTH region
- `/koth setreward <reward>` - Set the reward command(s)
- `/koth start` - Manually start a KOTH event immediately
- `/koth stop` - Stop/cancel the current KOTH event
- `/koth reload` - Reload configuration
- `/koth info` - Show current KOTH status and settings

## Installation

1. Ensure you have **Java 22** installed
2. Download the latest release JAR file
3. Place the JAR in your server's `plugins` folder
4. Start/restart your Paper Minecraft server
5. Configure the plugin using `/koth` commands

## Building from Source

```bash
git clone https://github.com/Jonas1903/koth-plugin.git
cd koth-plugin
mvn clean package
```

The compiled JAR will be in the `target` directory.

## Configuration

The `config.yml` file contains all customizable settings:

```yaml
# KOTH interval in minutes (default: 60)
koth-interval: 60

# Capture time in seconds (default: 60)
capture-time: 60

# Announcement times (in minutes before KOTH starts)
announcement-times:
  - 45
  - 30
  - 20
  - 10
  - 5
  - 1

# Reward command (use %player% for player name)
reward: "give %player% diamond 10"

# Region coordinates (automatically set via commands)
region:
  world: "world"
  pos1: {x: 0, y: 0, z: 0}
  pos2: {x: 0, y: 0, z: 0}

# All messages are customizable (use & for color codes)
messages:
  prefix: "&7[&6KOTH&7]&r "
  # ... see config.yml for full list
```

## Quick Setup Guide

1. **Set the KOTH Region:**
   - Stand at the first corner: `/koth setpos1`
   - Stand at the second corner: `/koth setpos2`

2. **Configure Rewards:**
   - `/koth setreward give %player% diamond 64`
   - For multiple rewards: `/koth setreward give %player% diamond 64; give %player% emerald 32`

3. **Start Your First KOTH:**
   - `/koth start` (or wait for automatic scheduling)

4. **Check Status:**
   - `/koth info`

## Technical Details

- **Java Version:** 22
- **Paper API:** 1.21.3-R0.1-SNAPSHOT (compatible with 1.21+)
- **Build Tool:** Maven
- **Package:** `com.koth`

### Project Structure

```
koth-plugin/
├── pom.xml
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── koth/
│       │           ├── KOTHPlugin.java           # Main plugin class
│       │           ├── commands/
│       │           │   └── KOTHCommand.java      # Command handler
│       │           ├── managers/
│       │           │   ├── KOTHManager.java      # Core KOTH logic
│       │           │   ├── RegionManager.java    # Region handling
│       │           │   └── RewardManager.java    # Reward distribution
│       │           ├── listeners/
│       │           │   └── PlayerListener.java   # Event listeners
│       │           └── utils/
│       │               └── MessageUtils.java     # Message formatting
│       └── resources/
│           ├── plugin.yml
│           └── config.yml
```

## Features Explained

### Capture Logic
- When a player enters the KOTH region, they become the "capturing player"
- A timer starts counting from 0 to the configured capture time (default 60 seconds)
- If the player leaves the region for ANY reason (teleport, death, manual movement), the timer resets completely
- If another player enters while someone is capturing, the original capturer's progress is reset
- The first player to reach the full capture time wins

### Boss Bar Behavior
- Only visible when KOTH is active
- Shows capturing player's name
- Displays time remaining (not elapsed)
- Progress bar fills from 0% to 100% as capture progresses
- Automatically added to new players who join during active KOTH

### Scheduling System
- After a KOTH ends (win or manual stop), the next KOTH is scheduled
- Announcement tasks are scheduled at configured intervals before the event
- All scheduling tasks are properly cleaned up on plugin disable
- Manual `/koth start` does not affect the automatic scheduling

## Permissions

This plugin uses OP-based permissions:
- All commands require OP status
- No additional permission nodes needed

## Dependencies

- Paper API 1.21.3+ (provided by server)
- Java 22 runtime

## Support

For issues, questions, or contributions, please visit the GitHub repository.

## License

[Your License Here]

## Author

Jonas1903
