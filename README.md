# AFly — Residence-based Pay-to-Fly

**English** · [繁體中文](docs/README.zh-TW.md)

📥 **Download on [Modrinth](https://modrinth.com/plugin/afly)**

> A lightweight **Paper plugin** that charges players money per second while flying, with different rates inside vs. outside [Residence](https://github.com/Zrips/Residence) regions. Named after its command, `/afly`.

Players run `/afly on` to enable paid flight; once they double-tap space to take off, money is deducted **every second** based on where they are. Flying inside **your own Residence** is cheap (default `1`/sec); flying over **someone else's land or the open wilderness** costs more (default `5`/sec). When a player runs out of money they are dropped out of flight automatically.

> This is the native Java rewrite of the original AFly Skript — no Skript, skript-reflect, or PlaceholderAPI required.

---

## ✨ Features

- 🎮 **Per-player toggle** — anyone can `/afly on` / `off` to control their own paid flight.
- ⏱️ Charges **per second, only while actually flying** (no flight = no charge). Survival / Adventure only (configurable).
- 🏠 **Cheap only on your own land** — the low rate applies inside residences *you own*; someone else's residence is charged at the wilderness rate.
- 🛫 **Takeoff notice** — shows whether you're inside a region or in the wild, the region name, and the current rate.
- 👤 **Owner notification** — flying inside someone's region notifies that region's owner (configurable).
- 🔔 **Boundary alerts** — crossing into/out of a region (rate change) is announced instantly.
- 🧾 **Flight summary** — on landing, players see how long they flew and how much they spent (per-player toggle).
- 📊 A persistent **action bar** shows your current zone, rate, and balance.
- ⌨️ `/afly` sub-commands support **tab completion**.
- 🔧 **Per-player preferences** (summary on/off, owner-alerts on/off) persist in `playerdata.yml`.
- 🌍 **In-game language switching** via `/afly lang <language>`.
- 🌐 **Multi-language** via `lang/*.yml` (ships with English & Traditional Chinese).
- 🛡️ **Bypass permission** (`fly.charge.bypass`) for staff who shouldn't be charged.
- 💸 Out of money → **forced landing** and paid flight auto-disabled.

---

## 📦 Requirements

| Type | Name | Notes |
| --- | --- | --- |
| Server | **Paper 1.21+** | or a fork (Purpur, etc.) |
| Plugin | **Vault** | economy bridge |
| Plugin | An economy plugin (EssentialsX, CMI, …) | provides the actual money |
| Plugin | **Residence** | land-claim plugin |

---

## 🚀 Installation (full tutorial)

### Option A — Use the released JAR (recommended)

1. **Prepare the server.** Run **Paper 1.21+**. Install the required plugins into `plugins/`:
   - [Vault](https://github.com/MilkBowl/Vault/releases)
   - An economy plugin, e.g. [EssentialsX](https://essentialsx.net/downloads.html) (`EssentialsX.jar` + `EssentialsXSpawn` optional)
   - [Residence](https://github.com/Zrips/Residence/releases)
2. **Confirm economy works.** Start the server once, join, run `/balance` — you should see a balance. (EssentialsX hooks into Vault automatically.)
3. **Install AFly.** Download `AFly-x.x.x.jar` from [Modrinth](https://modrinth.com/plugin/afly) (or the [Releases](../../releases) page) and drop it into `plugins/`.
4. **Restart the server** — do a **full restart** (stop, then start), not `/reload`.
5. **Config is generated** at `plugins/AFly/config.yml` and `plugins/AFly/lang/`. Edit if desired, then run `/afly reload`.
6. **Test:** `/afly on`, double-tap space to fly, and watch the action bar tick your balance down.

### Option B — Build from source

Requires **JDK 21+** and **Maven**.

```bash
git clone https://github.com/Avery11111101/AFly.git
cd AFly
mvn clean package
# Result: target/AFly-<version>.jar
```

Then follow steps 1–6 above with the JAR you built.

> ⚠️ Migrating from the Skript version? **Remove the old `fly_charge.sk`** first — running both at once causes double charging and `/afly` command conflicts.

---

## ⚙️ Configuration

`plugins/AFly/config.yml`:

```yaml
language: en             # which lang/<language>.yml to use (en, zh_TW, …)
interval-ticks: 20       # ticks between each charge (20 ticks = 1 second)
cost:
  wilderness: 5.0        # cost per charge outside any region
  residence: 1.0         # cost per charge inside a region
charged-gamemodes:       # which game modes are charged
  - SURVIVAL
  - ADVENTURE
notify-owner: true       # notify a region's owner when someone flies inside it
actionbar-show-balance: true
```

> After editing, run `/afly reload` — no server restart needed.

`config.yml` ships in **English**. Set `language: zh_TW` and run `/afly reload` — the comments in `config.yml` also switch to that language (your values are preserved). This works for any language that has a bundled `config_<language>.yml` template.

### Languages

Message strings live in `plugins/AFly/lang/<language>.yml` (color codes use `&`). To add a language, copy `en.yml` to e.g. `fr.yml`, translate the values, and set `language: fr` in `config.yml`. Available placeholders: `{residence}`, `{cost}`, `{balance}`, `{player}`, `{cost_res}`, `{cost_wild}`, `{arg}`.

---

## 🎛️ Commands

Everything lives under `/afly` (sub-commands tab-complete):

| Command | Permission | Description |
| --- | --- | --- |
| `/afly on` | everyone | Enable your paid flight (then double-tap space to fly) |
| `/afly off` | everyone | Disable your paid flight |
| `/afly info` | everyone | Show rates and your current status |
| `/afly summary [on\|off]` | everyone | Toggle your end-of-flight summary (per player) |
| `/afly notify [on\|off]` | everyone | Toggle alerts for flights in *your* residences (per land owner) |
| `/afly help` | everyone | Show the help menu |
| `/afly lang <language>` | `afly.admin` | Switch the server language (config + messages) in-game |
| `/afly reload` | `afly.admin` | Reload config & language files |

> Running `/afly` with no argument is the same as `/afly help`.

---

## 🔐 Permissions

| Node | Default | Meaning |
| --- | --- | --- |
| `fly.charge.bypass` | op | Fly without being charged |
| `afly.admin` | op | Use `/afly reload` |

---

## 📝 Changelog

### v1.3.1
- Added the project GitHub link to `config.yml`.

### v1.3.0
- Someone else's residence is now charged at the **wilderness rate** — the cheap rate applies only inside residences you own. Adds dedicated "other residence" notices.

### v1.2.0
- **Flight summary** on landing (time aloft + total spent) with a per-player `/afly summary` toggle.
- **`/afly notify`** lets land owners toggle "someone is flying in your residence" alerts (per player).
- **`/afly lang <language>`** switches the server language in-game.
- Per-player preferences persist in `playerdata.yml`.

### v1.1.0
- `config.yml` now defaults to **English**; switching `language` and running `/afly reload` localizes the config comments too (values preserved).

### v1.0.0
- First native release: per-player `/afly` paid-flight toggle, per-second billing with inside/outside Residence rates, takeoff & boundary notices, owner notifications, tab completion, action bar, forced landing when out of money.
- Independent `config.yml` and multi-language `lang/*.yml` (English + Traditional Chinese).

---

## 📄 License

Released under the [MIT License](LICENSE). © 2026 Avery11111101.

---

*Author: avery11111102*
