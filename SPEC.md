# Quill Language Specification v1.0

## Overview
Quill is a sandboxed scripting language for Minecraft server event automation. It provides event-driven programming with hierarchical scope management and granular permission control.

## Syntax Fundamentals

### Comments
```
// Single-line comment
/* Multi-line
   comment */
```

### Variable Declaration
```
let variable_name = value
```

Variables are dynamically typed. All root-level variables are global to the scope. Subscope-local variables are created by assigning them a value.

### Data Types

#### Primitives
- **Number**: `42`, `3.14`, `-100`
- **String**: `"hello"`, `'world'`
- **Boolean**: `true`, `false`
- **Null**: `null`

#### Complex Types
- **Player**: Reference to a Minecraft player
- **Location**: `location(x, y, z)` - represents a point in 3D space
- **Item**: `item("minecraft:diamond_sword", amount, metadata)` - represents an item stack
- **Scope**: Reference to a subscope
- **List**: `[1, 2, 3, "hello", true]` - dynamic array of any type
- **Entity**: Reference to a Minecraft entity

### Operators

#### Arithmetic
```
+  // Addition
-  // Subtraction
*  // Multiplication
/  // Division
%  // Modulo
```

#### Comparison
```
==  // Equal
!=  // Not equal
>   // Greater than
<   // Less than
>=  // Greater than or equal
<=  // Less than or equal
```

#### Logical
```
&&  // AND
||  // OR
!   // NOT
```

#### String Operations
```
+   // Concatenation: "Hello " + "world"
{}  // Interpolation: "Hello {player.name}"
```

### Control Flow

#### If Statements
```
if condition {
    // code
}

if condition {
    // code
} else {
    // code
}

if condition1 {
    // code
} else if condition2 {
    // code
} else {
    // code
}
```

#### For Loops
```
// Iterate over list
for item in list {
    // code
}

// Iterate over range
for i in range(0, 10) {
    // code
}

// Iterate over scope players (enters that scope's context)
for player in subscope.players {
    // code - can access subscope's local variables
}
```

#### While Loops
```
while condition {
    // code
}
```

#### Loop Control
```
break     // Exit loop
continue  // Skip to next iteration
```

### Functions

#### Function Definition
```
func function_name(param1, param2, ...) {
    // code
    return value
}

// No return value
func do_something(param) {
    // code
}
```

#### Function Calls
```
result = function_name(arg1, arg2);
do_something(arg);
```

### Scope Management

#### Creating Subscopes
```
let subscope = new Scope(x1, y1, z1, x2, y2, z2);
```

#### Defining Scope-Local Variables
```
subscope.variable_name = initial_value;
```

#### Scope Properties
```
subscope.players  // List of players in the subscope
subscope.region   // The physical region bounds
```

### Event Handlers

#### Syntax
```
OnEvent(EventName) {
    // code
}
```

#### Available Events

**Player Events:**
- `PlayerJoin` - Context: `player`
- `PlayerQuit` - Context: `player`
- `PlayerChat` - Context: `player`, `chat.message`
- `PlayerMove` - Context: `player`, `move.from`, `move.to`
- `PlayerDamage` - Context: `damage.target`, `damage.source`, `damage.amount`, `damage.cause`, `event`
- `PlayerDeath` - Context: `player`, `death.killer`, `death.cause`
- `PlayerRespawn` - Context: `player`
- `PlayerInteract` - Context: `player`, `interact.block`, `interact.item`, `event`
- `PlayerDropItem` - Context: `player`, `item`, `event`
- `PlayerPickupItem` - Context: `player`, `item`, `event`
- `PlayerTeleport` - Context: `player`, `teleport.from`, `teleport.to`, `event`
- `PlayerGameModeChange` - Context: `player`, `gamemode.old`, `gamemode.new`, `event`

**Block Events:**
- `BlockBreak` - Context: `player`, `block.type`, `block.location`, `event`
- `BlockPlace` - Context: `player`, `block.type`, `block.location`, `event`
- `BlockInteract` - Context: `player`, `block.type`, `block.location`, `event`

**Entity Events:**
- `EntitySpawn` - Context: `entity`, `entity.type`, `entity.location`
- `EntityDeath` - Context: `entity`, `entity.killer`
- `EntityDamage` - Context: `entity`, `damage.source`, `damage.amount`, `event`

**World Events:**
- `TimeChange` - Context: `world`, `time.old`, `time.new`
- `WeatherChange` - Context: `world`, `weather.new`

**Custom Events:**
- `CustomEvent` - Context: `event.name`, `event.data` (for triggering custom logic)

### Built-in Functions

#### Player Functions

```
teleport(player, x, y, z) -> Boolean
teleport(player, location) -> Boolean
// Teleports player to coordinates or location. Returns success.

give(player, item_id) -> Boolean
give(player, item_id, amount) -> Boolean
give(player, item) -> Boolean
// Gives item to player. Returns success.

remove_item(player, item_id) -> Number
remove_item(player, item_id, amount) -> Number
// Removes items from player inventory. Returns amount removed.

set_gamemode(player, mode) -> Boolean
// Mode: "survival", "creative", "adventure", "spectator"

set_health(player, amount) -> Boolean
// Sets player health (0-20)

set_hunger(player, amount) -> Boolean
// Sets player hunger (0-20)

heal(player) -> Boolean
// Fully heals player

kill(player) -> Boolean
// Kills player

sendmessage(player, message) -> Boolean
// Sends message to player

sendtitle(player, title, subtitle, fade_in, stay, fade_out) -> Boolean
// Sends title to player (times in ticks)

playsound(player, sound, volume, pitch) -> Boolean
// Plays sound to player

give_effect(player, effect, duration, amplifier) -> Boolean
// Gives potion effect (duration in ticks)

remove_effect(player, effect) -> Boolean
// Removes potion effect

clear_effects(player) -> Boolean
// Removes all potion effects

set_flying(player, flying) -> Boolean
// Sets player flying state

kick(player, reason) -> Boolean
// Kicks player from server

get_health(player) -> Number
// Returns player health

get_hunger(player) -> Number
// Returns player hunger

get_location(player) -> Location
// Returns player location

get_gamemode(player) -> String
// Returns player gamemode

has_item(player, item_id) -> Boolean
has_item(player, item_id, amount) -> Boolean
// Checks if player has item

get_name(player) -> String
// Returns player name

is_online(player) -> Boolean
// Checks if player is online

is_op(player) -> Boolean
// Checks if player is operator
```

#### Scope Functions

```
addtoscope(player, scope) -> Boolean
// Adds player to scope's player list

removefromscope(player, scope) -> Boolean
// Removes player from scope's player list

getplayers(scope) -> List
// Returns list of players in scope

in_region(player, scope) -> Boolean
in_region(location, scope) -> Boolean
// Checks if player/location is within scope's physical region

get_region(scope) -> Region
// Returns scope's region bounds (object with x1,y1,z1,x2,y2,z2)

set_region(scope, x1, y1, z1, x2, y2, z2) -> Boolean
// Updates scope's physical region
```

#### World Functions

```
set_block(x, y, z, block_id) -> Boolean
set_block(location, block_id) -> Boolean
// Sets block at location

get_block(x, y, z) -> String
get_block(location) -> String
// Returns block type at location

break_block(x, y, z) -> Boolean
break_block(location) -> Boolean
// Breaks block naturally (drops items)

spawn_entity(entity_type, x, y, z) -> Entity
spawn_entity(entity_type, location) -> Entity
// Spawns entity at location

remove_entity(entity) -> Boolean
// Removes entity from world

create_explosion(x, y, z, power) -> Boolean
create_explosion(location, power) -> Boolean
// Creates explosion (power 0-10, fire optional)

create_explosion(x, y, z, power, fire) -> Boolean
create_explosion(location, power, fire) -> Boolean

strike_lightning(x, y, z) -> Boolean
strike_lightning(location) -> Boolean
// Strikes lightning

set_time(world, time) -> Boolean
// Sets world time (0-24000)

get_time(world) -> Number
// Returns world time

set_weather(world, weather, duration) -> Boolean
// Weather: "clear", "rain", "thunder". Duration in ticks.

get_weather(world) -> String
// Returns current weather

get_world(name) -> World
// Returns world by name

broadcast(message) -> Boolean
// Broadcasts message to all players in scope
```

#### Utility Functions

```
cancel(event) -> Boolean
// Cancels the current event (prevents default behavior)

wait(ticks) -> Boolean
// Delays execution (async, doesn't block other events)

random(max) -> Number
random(min, max) -> Number
// Returns random number

random_choice(list) -> Any
// Returns random item from list

round(number) -> Number
floor(number) -> Number
ceil(number) -> Number
abs(number) -> Number
// Math functions

sqrt(number) -> Number
pow(base, exponent) -> Number
// More math

distance(loc1, loc2) -> Number
distance(x1, y1, z1, x2, y2, z2) -> Number
// Calculates distance between points

log(message) -> Boolean
// Logs message to server console (for debugging)

trigger_custom(event_name, data) -> Boolean
// Triggers a custom event with data

get_player(name) -> Player
// Gets player by name (returns null if not found)

get_online_players() -> List
// Returns list of all online players

len(list) -> Number
len(string) -> Number
// Returns length of list or string

append(list, item) -> Boolean
// Adds item to end of list

remove(list, index) -> Any
// Removes and returns item at index

contains(list, item) -> Boolean
contains(string, substring) -> Boolean
// Checks if list contains item or string contains substring

split(string, delimiter) -> List
// Splits string into list

join(list, delimiter) -> String
// Joins list into string

to_string(value) -> String
to_number(value) -> Number
to_boolean(value) -> Boolean
// Type conversion

type_of(value) -> String
// Returns type name: "number", "string", "boolean", "player", "list", etc.
```

#### Location & Item Constructors

```
location(x, y, z) -> Location
location(x, y, z, world) -> Location
// Creates location object

item(item_id) -> Item
item(item_id, amount) -> Item
item(item_id, amount, metadata) -> Item
// Creates item object
```

### Object Properties

#### Player Properties
```
player.name          // String
player.health        // Number
player.hunger        // Number
player.location      // Location
player.gamemode      // String
player.flying        // Boolean
player.online        // Boolean
```

#### Location Properties
```
location.x           // Number
location.y           // Number
location.z           // Number
location.world       // World
```

#### Item Properties
```
item.type            // String (e.g., "minecraft:diamond_sword")
item.amount          // Number
item.metadata        // Object (NBT data)
```

#### Entity Properties
```
entity.type          // String
entity.location      // Location
entity.health        // Number
entity.alive         // Boolean
```

## Scope Context Rules

1. **Root-level variables** are global to the entire scope and accessible everywhere
2. **Subscope-local variables** are only accessible when operating within that subscope's context
3. **Context is entered** when:
   - Iterating over `subscope.players`
   - Calling functions that operate on subscope players
4. **Within a subscope context**, both global and subscope-local variables are accessible
5. **Outside a subscope context**, only global variables are accessible

### Example:
```
let global_count = 0;  // Global variable

let arena1 = new Scope(-100, 0, -100, 100, 256, 100);
arena1.kills = 0;  // Local to arena1

let arena2 = new Scope(200, 0, 200, 400, 256, 400);
arena2.kills = 0;  // Local to arena2 (different variable)

OnEvent(PlayerDeath) {
    global_count = global_count + 1;  // Always works
    
    for player in arena1.players {
        kills = kills + 1;  // Accesses arena1's kills variable
        sendmessage(player, "Arena 1 kills: {kills}");
    }
    
    for player in arena2.players {
        kills = kills + 1;  // Accesses arena2's kills variable
        sendmessage(player, "Arena 2 kills: {kills}");
    }
    
    // kills = kills + 1;  // ERROR: No scope context here
}
```

## Permission System

Admins can revoke specific built-in functions from specific scopes using the permission system:

```
/quill permission revoke <scope_name> <function_name>
/quill permission grant <scope_name> <function_name>
/quill permission list <scope_name>
```

When a revoked function is called, it will fail silently or throw an error (configurable).

## Error Handling

The language includes basic error handling:

```
try {
    // code that might fail
} catch error {
    log("Error occurred: {error}");
}
```

Errors can occur from:
- Type mismatches
- Invalid function arguments
- Permission denied
- Null reference access
- Division by zero
- Out of bounds access

## Complete Example

```
// Global variables
let game_active = false;
let winner = null;

// Create arena subscope
let arena = new Scope(-50, 60, -50, 50, 100, 50);
arena.participants = [];
arena.eliminations = 0;

// Helper function
func reset_game() {
    game_active = false;
    winner = null;
    arena.eliminations = 0;
    arena.participants = [];
}

// Player join event
OnEvent(PlayerJoin) {
    if in_region(player, arena) && !game_active {
        addtoscope(player, arena);
        append(arena.participants, player);
        broadcast("{player.name} joined the arena!");
    }
}

// Start game with chat command
OnEvent(PlayerChat) {
    if chat.message == "!start" && !game_active {
        if len(arena.participants) >= 2 {
            game_active = true;
            broadcast("Game starting!");
            
            for player in arena.players {
                set_gamemode(player, "survival");
                give(player, "minecraft:stone_sword");
                give(player, "minecraft:bread", 5);
                heal(player);
            }
        } else {
            sendmessage(player, "Need at least 2 players!");
        }
    }
}

// Handle player deaths
OnEvent(PlayerDeath) {
    if game_active && in_region(player, arena) {
        for p in arena.players {
            arena.eliminations = arena.eliminations + 1;
            
            let remaining = len(arena.participants) - arena.eliminations;
            broadcast("{player.name} was eliminated! {remaining} remaining.");
            
            if remaining == 1 {
                // Find winner
                for survivor in arena.players {
                    if survivor.health > 0 {
                        winner = survivor;
                        broadcast("{winner.name} wins the game!");
                        give(winner, "minecraft:diamond", 10);
                    }
                }
                
                wait(100);  // Wait 5 seconds
                reset_game();
            }
        }
    }
}

// Prevent leaving arena during game
OnEvent(PlayerMove) {
    if game_active && contains(arena.participants, player) {
        if !in_region(move.to, arena) {
            teleport(player, move.from);
            sendmessage(player, "You cannot leave during the game!");
        }
    }
}
```

## Language Philosophy

Quill is designed with the following principles:

1. **Simplicity**: Easy syntax for non-programmers
2. **Safety**: Sandboxed execution with permission controls
3. **Event-driven**: Natural fit for Minecraft's event model
4. **Scoped**: Clear boundaries for code execution and effects
5. **Persistent**: State survives server restarts
6. **Debuggable**: Built-in logging and error messages

## Implementation Notes

The language should be implemented as a Minecraft plugin (Bukkit/Spigot/Paper) with:

- **Lexer**: Tokenize source code
- **Parser**: Build abstract syntax tree (AST)
- **Interpreter**: Execute AST with scope context tracking
- **Permission manager**: Control function access per scope
- **Persistence layer**: Save/load scope state and code
- **In-game editor**: Commands for writing/editing code
- **Event bridge**: Connect Minecraft events to OnEvent handlers

---

**Version**: 1.0  
**Date**: October 2025  
**Status**: Specification Draft
