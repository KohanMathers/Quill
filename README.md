# Quill

A sandboxed scripting language for Minecraft event automation. Create community-run events without bugging your admins.

## What is Quill?

Quill is a simple, event-driven scripting language designed for Minecraft servers. It allows players to create custom game modes, mini-games, and automated events within sandboxed scopes — all without requiring admin intervention for every command block.

### Key Features

- 🎯 **Event-Driven** - React to player actions, block changes, and world events
- 🔒 **Sandboxed** - Scripts run in isolated scopes with physical boundaries
- 🎮 **Player-Friendly** - Simple syntax designed for non-programmers
- 🛡️ **Permission-Controlled** - Admins can revoke specific functions per scope
- 💾 **Persistent** - State and variables survive server restarts
- ✍️ **In-Game Editor** - Write and test code directly in Minecraft

## Quick Example

```
// Create an arena
let arena = new Scope(-50, 60, -50, 50, 100, 50);
arena.game_active = false;

// Start game on chat command
OnEvent(PlayerChat) {
    if chat.message == "!start" {
        arena.game_active = true;
        broadcast("Game starting!");
        
        for player in arena.players {
            set_gamemode(player, "survival");
            give(player, "minecraft:stone_sword");
            teleport(player, 0, 65, 0);
        }
    }
}

// Handle player deaths
OnEvent(PlayerDeath) {
    if arena.game_active && in_region(player, arena) {
        broadcast("{player.name} was eliminated!");
    }
}
```

## Installation

### Requirements
- Minecraft Server (Bukkit/Spigot/Paper)
- Java 21 or higher

### Setup
1. Download the latest `Quill.jar` from [Releases](https://github.com/kohanmathers/quill/releases)
2. Place in your server's `plugins/` folder
3. Restart your server
4. Configure in `plugins/Quill/config.yml`

## Usage

### For Admins

Create a scope for a player:
```
/quill scope create <scope_name> <owner_username> <x1> <y1> <z1> <x2> <y2> <z2>
```

Join a player to a scope (so they're affected by its scripts):
```
/quill scope join <scope_name> <username>
```

Manage permissions:
```
/quill permission revoke <scope_name> <function_name>
/quill permission grant <scope_name> <function_name>
/quill permission list <scope_name>
```

### For Players

Open the in-game editor:
```
/quill edit
```

Load a script file:
```
/quill load <filename>
```

Run/compile your script:
```
/quill run
```

View scope info:
```
/quill info
```

## Documentation

- [Language Specification](SPEC.md) - Complete language reference
- [Tutorial](docs/TUTORIAL.md) - Step-by-step guide *(coming soon)*
- [API Reference](docs/API.md) - Built-in functions *(coming soon)*
- [Examples](examples/) - Sample scripts *(coming soon)*

## Development Status

⚠️ **Beta** - Quill is in early development. Expect breaking changes.

**Current Status:**
- [x] Language specification
- [x] Lexer/Tokenizer
- [x] Parser
- [x] Interpreter
- [x] Bukkit plugin integration
- [x] In-game editor
- [x] Persistence layer
- [x] Permission system

## Contributing

Contributions are welcome! Please read [CONTRIBUTING.md](CONTRIBUTING.md) before submitting PRs.

## License

Quill is licensed under the [MIT License](LICENSE).

## Roadmap

**v0.1.0 (Alpha)**
- Basic interpreter
- Core event handlers
- Essential built-in functions
- File-based script loading

**v0.2.0 (Beta)**
- In-game editor
- Permission system
- Persistence layer
- Error handling improvements

**v1.0.0 (Stable)**
- Complete API
- Documentation
- Performance optimizations
- Production-ready

**Future**
- Visual scripting editor
- Script marketplace
- Advanced debugging tools
- Multi-server sync

## Community

- **Issues** - [GitHub Issues](https://github.com/kohanmathers/quill/issues)
- **Discussions** - [GitHub Discussions](https://github.com/kohanmathers/quill/discussions)
- **Discord** - [KM's Support & Updates](https://discord.gg/FZuVXszuuM)

## Inspiration

Quill is inspired by [Skript](https://github.com/SkriptLang/Skript) and similar Minecraft scripting languages, but designed with modern language features and a focus on scoped, sandboxed execution.

## Authors

Created with ❤️ by Kohan Mathers

---

**Star ⭐ this repo if you're interested in the project!**
