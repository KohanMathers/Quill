# 🪶Quill - Player-Powered Events Without Admin Babysitting

**Tired of players bugging admins for every custom event?** Quill lets your community run their own mini-games, arenas, and automated events - safely sandboxed, fully controlled, zero admin intervention needed.

## 🚀 The Problem Quill Solves

Your players want to run a PvP tournament. A parkour race. A custom boss fight. Maybe a city-wide scavenger hunt.

**Traditional approach:**
- Player asks admin for command blocks ❌
- Admin sets everything up manually ❌
- Admin has to supervise so nothing breaks ❌
- Event ends, cleanup is a nightmare ❌

**With Quill:**
- Admin creates a scope, hands it to the player ✅
- Player writes their event script in minutes ✅
- Script runs safely in its boundaries ✅
- Auto-cleanup, state persists, nothing breaks ✅

---

## ✨ Key Features

### 🔒 **Sandboxed Execution**
Scripts run in physical boundaries. A player's arena script can't accidentally fill your spawn with TNT or teleport everyone to the void. What happens in the scope, stays in the scope.

### 🎯 **Event-Driven Programming**
React to player actions, block interactions, mob deaths, chat messages, and more. No command blocks, no redstone contraptions - just clean, readable code.

### 🛡️ **Granular Permissions**
Whitelist or blacklist functions per scope. Arena scripts get `teleport` and `give` but not `set_block`. Build zones get `set_block` but not `kill`. You control exactly what each scope can do.

### ✍️ **Web-Based Editor**
No port forwarding. No server file access. Players get a session ID, open the web editor, write their script, and it's instantly on the server. Works behind proxies, NAT, everything.

### 💾 **Persistent State**
Script variables survive server restarts. Player scores, game states, team assignments - all automatically saved and restored.

### 🎮 **Player-Friendly Syntax**
Designed for non-programmers. If your players can write command block logic, they can write Quill scripts.

---

## 📝 Quick Example

```javascript
// A simple PvP arena that starts on command
let arena = new Scope(-50, 60, -50, 50, 100, 50);
arena.game_active = false;

OnEvent(PlayerChat) {
    if chat.message == "!start" && in_region(player, arena) {
        arena.game_active = true;
        broadcast("Arena starting in 3... 2... 1...");
        
        for p in arena.players {
            set_gamemode(p, "survival");
            give(p, "diamond_sword", 1);
            heal(p);
        }
    }
}

OnEvent(PlayerDeath) {
    if arena.game_active && in_region(player, arena) {
        broadcast("{player.name} was eliminated!");
        set_gamemode(player, "spectator");
    }
}
```

That's it. No command blocks, no plugins for every feature, no admin intervention.

---

## 🎯 Perfect For

- **Community Servers** - Let trusted players run events
- **Creative/Build Servers** - Automated build competitions
- **Minigame Networks** - Rapid prototyping without touching code
- **RPG Servers** - Quest systems, boss fights, custom mechanics
- **Prison/Skyblock** - Player-run shops, automated systems

---

## 🔧 How It Works

### For Admins:
1. Install Quill (drag-drop JAR, restart server)
2. Create scopes for players: `/quill scope create arena PlayerName -50 60 -50 50 100 50 whitelist`
3. Set permissions: `/quill permission grant arena teleport`
4. Done. Player handles the rest.

### For Players:
1. Run `/quill edit` to get editor link
2. Write your script in the web editor
3. Save, and it's live on the server
4. Test, iterate, publish your event

---

## 🛡️ Security Model

**Quill is built on zero-trust principles:**

- ✅ Scripts can ONLY affect their defined physical boundaries
- ✅ Function permissions are opt-in (whitelist) or opt-out (blacklist)
- ✅ No filesystem access, no system commands, no plugin conflicts
- ✅ Infinite loop detection prevents server crashes
- ✅ Scripts run in isolated contexts - no variable collisions

**Your players get creative freedom. You keep total control.**

---

## 📋 Requirements

- **Minecraft Server:** Bukkit, Spigot, or Paper
- **Java Version:** 21 or higher
- **Recommended:** Paper 1.20+ for best performance

---

## 🚀 Installation

1. Download `Quill-x.x.x.jar`
2. Drop it in your `plugins/` folder
3. Restart your server
4. Configure editor URL in `plugins/Quill/config.yml` (optional)
5. Create your first scope!

**First-time setup takes under 5 minutes.**

---

## 📚 Resources

- **Documentation:** [Full API Reference](https://quill.kmathers.co.uk/docs)
- **Support:** [Discord Community](https://discord.gg/FZuVXszuuM)

---

## 🎨 Why Quill Instead of Skript?

### (Yes, I hear this a lot)

**Skript is great** - if you're an admin writing global server logic. **Quill is different** - it's designed for players writing localized, sandboxed events.

| Feature | Skript | Quill |
|---------|--------|-------|
| **Sandboxed execution** | ❌ Global scope | ✅ Physical boundaries |
| **Player-run events** | ❌ Admin only | ✅ Delegated safely |
| **Permission control** | ❌ All or nothing | ✅ Per-scope granular |
| **Web editor** | ❌ File-based | ✅ Zero setup |
| **Persistence** | ⚠️ Manual | ✅ Automatic |

**Different tools for different server models.** If you need player empowerment, Quill is built for that.

---

## 📜 License

MIT License - Free and open source forever.

---

## ❤️ Credits

Created by **Kohan Mathers** with love for the Minecraft community.

Inspired by Skript, but designed for a fundamentally different trust model.

---

**🌟 If Quill helps your server, leave a review and star the GitHub repo!**

---

**Questions? Join the [Discord](https://discord.gg/FZuVXszuuM) or open a [GitHub Issue](https://github.com/kohanmathers/quill/issues).**