# KOTH Plugin Implementation Summary

This document provides a detailed overview of the implementation and design decisions for the KOTH (King of the Hill) plugin.

## Implementation Checklist

### ✅ Core Requirements Met

1. **Technical Requirements**
   - ✅ Java 22 configured in pom.xml
   - ✅ Maven build system setup
   - ✅ Paper API 1.21.3 (compatible with 1.21+)
   - ✅ IntelliJ IDEA compatible structure

2. **Permission System**
   - ✅ All commands check for OP status via `sender.isOp()`
   - ✅ Non-OPs receive "no permission" message

3. **Region Selection System**
   - ✅ `/koth setpos1` and `/koth setpos2` commands
   - ✅ Region stored persistently in config.yml
   - ✅ RegionManager handles coordinate checking
   - ✅ 3D bounding box collision detection

4. **Capture Mechanics**
   - ✅ 1 hour cooldown between KOTH events (configurable)
   - ✅ 60 second capture time (configurable)
   - ✅ Timer resets completely if player leaves area
   - ✅ Timer resets if player dies or teleports
   - ✅ Timer resets if another player enters (knockout)
   - ✅ Only one player can capture at a time
   - ✅ Runs every second via BukkitRunnable

5. **Reward System**
   - ✅ `/koth setreward <reward>` command
   - ✅ Configurable in config.yml
   - ✅ Support for multiple commands (semicolon-separated)
   - ✅ `%player%` placeholder replacement
   - ✅ Executed from console for proper permissions

6. **Boss Bar Display**
   - ✅ White color with progress overlay
   - ✅ Shows capturing player name
   - ✅ Shows time remaining (not elapsed)
   - ✅ Progress bar fills from 0% to 100%
   - ✅ Visible to all online players
   - ✅ Hidden when KOTH not active
   - ✅ Automatically added to joining players

7. **Announcement System**
   - ✅ Broadcasts at: 45, 30, 20, 10, 5, 1 minutes before
   - ✅ All times configurable in config.yml
   - ✅ Uses BukkitScheduler for timing
   - ✅ Properly cancelled on reload/shutdown

8. **Manual Control Commands**
   - ✅ `/koth setpos1` - Set first position
   - ✅ `/koth setpos2` - Set second position
   - ✅ `/koth setreward <reward>` - Set reward
   - ✅ `/koth start` - Manually start event
   - ✅ `/koth stop` - Stop current event
   - ✅ `/koth reload` - Reload configuration
   - ✅ `/koth info` - Show status and settings
   - ✅ Tab completion support

9. **Project Structure**
   - ✅ Follows recommended package structure
   - ✅ Separation of concerns (managers, commands, listeners, utils)
   - ✅ plugin.yml and config.yml in resources

10. **Configuration File**
    - ✅ KOTH interval configuration
    - ✅ Capture time configuration
    - ✅ Announcement times list
    - ✅ Customizable messages with color codes
    - ✅ Reward configuration
    - ✅ Region coordinates storage

11. **Additional Features**
    - ✅ Proper error handling
    - ✅ Player feedback messages
    - ✅ Resource cleanup on plugin disable
    - ✅ BukkitRunnable for async tasks
    - ✅ .gitignore for Maven/IDE files
    - ✅ Comprehensive README documentation

## Design Decisions

### Architecture

The plugin follows a clean architecture with separation of concerns:

- **KOTHPlugin.java**: Main plugin class, initialization and coordination
- **Managers**: Business logic for different aspects (KOTH, Region, Reward)
- **Commands**: Command handling and validation
- **Listeners**: Event handling for player actions
- **Utils**: Shared utilities (message formatting)

### Capture Logic Implementation

The capture system uses a tick-based approach:
1. Every second (20 ticks), check who is in the region
2. If the capturing player left or died, reset progress completely
3. If a different player entered, reset and transfer to them
4. Increment progress if same player is still capturing
5. Complete capture when progress >= required time

This ensures:
- Accurate timing
- Instant response to player movement
- Fair reset mechanics
- Single active capturer

### Boss Bar Implementation

Using Paper's Adventure API:
- BossBar component with white color
- Progress calculated as: `captureProgress / captureTimeRequired`
- Name updated with player and time info
- Added to all players on creation
- Removed from all players on KOTH end

### Scheduling System

Three types of scheduled tasks:
1. **Main Schedule**: Triggers KOTH start after interval
2. **Announcements**: Multiple tasks for different time intervals
3. **Capture Task**: Runs every second during active KOTH

All tasks are:
- Stored for cancellation
- Cleaned up on shutdown/reload
- Rescheduled after KOTH ends

### Message System

Centralized message handling:
- All messages in config.yml
- Color code support via `&` symbol
- Placeholder replacement
- Consistent prefix
- Adventure API for modern text components

### Region Management

Simple 3D bounding box:
- Two corner points define region
- Min/Max calculation for each axis
- World matching for cross-world safety
- Persistent storage in config
- Reload support

## Testing Recommendations

When testing the plugin (requires a Paper server):

1. **Region Setup**
   - Set two positions at different heights
   - Verify region boundary detection
   - Test cross-world scenarios

2. **Capture Mechanics**
   - Single player capturing to completion
   - Player leaving mid-capture
   - Player death mid-capture
   - Player teleport mid-capture
   - Two players fighting in zone

3. **Scheduling**
   - Wait for announcements
   - Verify announcement timing
   - Test manual start/stop
   - Check reload behavior

4. **Boss Bar**
   - Visible to all players
   - Updates every second
   - Proper cleanup
   - Join during active KOTH

5. **Rewards**
   - Single command reward
   - Multiple command rewards
   - Invalid commands (error handling)

6. **Permissions**
   - Non-OP command attempts
   - OP command execution

## Known Limitations

1. **Paper 1.21.3**: Using 1.21.3 API as 1.21.8 doesn't exist yet. The plugin will work on 1.21.3+.

2. **Build Dependencies**: Requires internet connection to download Paper API from their repository during first build.

3. **Single Region**: Only one KOTH region can be configured. Multiple regions would require architectural changes.

4. **Console Commands Only**: Plugin commands can only be run by OPs. For more granular permissions, a permission plugin like LuckPerms would be needed with custom permission nodes.

## Future Enhancement Ideas

- Multiple KOTH regions with rotation
- Player statistics tracking
- Leaderboard system
- Particle effects in region
- Sound effects for capture events
- Hologram display at region
- Team-based KOTH mode
- Custom permission nodes
- Database storage for stats
- Web dashboard integration

## Maintenance Notes

### Adding New Commands

1. Add subcommand in `KOTHCommand.onCommand()`
2. Add to tab completion
3. Update usage message
4. Add message keys to config.yml

### Modifying Timings

All timings are in config.yml:
- `koth-interval`: Minutes between events
- `capture-time`: Seconds to capture
- `announcement-times`: List of minutes before

### Customizing Messages

Edit config.yml `messages` section:
- Use `&` for color codes
- Use `%player%`, `%time%`, etc. for placeholders
- Empty strings disable that message

### Adding New Managers

1. Create class in `managers` package
2. Initialize in `KOTHPlugin.onEnable()`
3. Provide getter method
4. Add shutdown logic if needed

## Build Instructions

```bash
# Clone repository
git clone https://github.com/Jonas1903/koth-plugin.git
cd koth-plugin

# Build with Maven
mvn clean package

# Output JAR location
# target/koth-plugin-1.0.0.jar
```

## Dependencies

- Paper API 1.21.3-R0.1-SNAPSHOT (provided)
- Maven Compiler Plugin 3.13.0
- Maven Shade Plugin 3.5.3

No runtime dependencies beyond Paper server.
