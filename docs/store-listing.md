# AFly — Store / Website Listing Copy

可直接複製到 SpigotMC / Modrinth / Hangar / BuiltByBit 等插件網站。以下提供「英文 / 繁體中文」各一份短版與長版。Markdown 格式。

---

## 🇬🇧 English — Short

> **AFly — make flying cost something.**
> A lightweight Paper plugin that charges players *per second* while they fly: cheap inside their own Residence claim, pricier out in the wilderness. A clean economy sink that rewards players for staying on their own land — with per-player toggle, owner notifications, and full multi-language support.

---

## 🇬🇧 English — Long

# ✈️ AFly — Residence-based Pay-to-Fly

**Flight shouldn't be free.** AFly turns flying into a meaningful gold sink: while a player flies, they pay **per second**, and the rate depends on *where* they are. Standing on their own [Residence](https://www.spigotmc.org/resources/residence.11480/) claim? Cheap. Cruising over the open wilderness? That'll cost more. It's a simple, fair way to drain excess currency from your economy and gently encourage players to build and stay on their own land.

No PlaceholderAPI, no scripts, no fuss — just drop it in and configure.

## Why AFly?

- 💰 **A real economy sink.** Continuous, opt-in spending that fights inflation without punishing players who don't fly.
- 🏠 **Location-aware pricing.** Reward players for flying at home — set cheap rates inside Residence claims and higher rates in the wild.
- 🎮 **Opt-in and fair.** Players choose when to fly with `/afly on`; if they're not flying, they're not charged.
- 🌐 **Speaks your language.** Every message lives in a language file. Ships with English and Traditional Chinese; add your own in minutes.

## Features

- 🎮 **Per-player toggle** — anyone can `/afly on` / `off` to control their own paid flight.
- ⏱️ **Per-second billing**, charged *only while actually flying*. Survival/Adventure only (configurable).
- 🏠 **Different rates inside vs. outside** a Residence region.
- 🛫 **Takeoff notice** — shows whether you're in a region or the wild, the region's name, and the current rate.
- 👤 **Owner notifications** — flying inside someone's claim notifies the owner.
- 🔔 **Boundary alerts** — cross a region border and you're told the rate changed, instantly.
- 📊 **Live action bar** showing your zone, rate, and remaining balance.
- ⌨️ **Tab completion** on all sub-commands.
- 🛡️ **Bypass permission** for staff who shouldn't be charged.
- 💸 **Out of money? Safe forced landing** and paid flight auto-disables.

## How it works

1. A player runs **`/afly on`** — this grants flight.
2. They **double-tap space** to take off. From that moment, money ticks down each second.
3. The action bar shows their current zone, rate, and balance; crossing in/out of Residence claims updates the rate (with a chat alert).
4. Run out of money and they're gently set down, with paid flight switched off until they top up.

## Commands & Permissions

| Command | Permission | Description |
| --- | --- | --- |
| `/afly on` | everyone | Enable your paid flight |
| `/afly off` | everyone | Disable your paid flight |
| `/afly info` | everyone | Show rates and your status |
| `/afly help` | everyone | Show help |
| `/afly reload` | `afly.admin` | Reload config & language |

`fly.charge.bypass` lets staff fly for free.

## Configuration

A clean `config.yml` (rates, billing interval, charged game modes, owner-notify toggle, language selection) plus per-language `lang/*.yml` message files. Edit and `/afly reload` — no restart needed.

## Requirements

- **Paper 1.21+**
- **Vault** + any economy plugin (EssentialsX, CMI, …)
- **Residence**

---

## 🇹🇼 繁體中文 — 短版

> **AFly — 讓「飛行」開始有成本。**
> 一個輕量的 Paper 插件：玩家飛行時**每秒扣款**，在自己的 Residence 領地內便宜、飛到荒野則較貴。是個乾淨的經濟回收機制，鼓勵玩家待在自家領地——支援個人開關、通知地主、完整多語言。

---

## 🇹🇼 繁體中文 — 長版

# ✈️ AFly — Residence 領地付費飛行

**飛行不該是免費的。** AFly 把飛行變成一個有意義的金錢回收管道：玩家飛行時**每秒**付費，且費率取決於他**人在哪裡**。站在自己的 [Residence](https://www.spigotmc.org/resources/residence.11480/) 領地上？便宜。在開闊荒野巡航？那就貴一點。這是一個簡單又公平的方式，從你的經濟系統抽走多餘的貨幣，並溫和地鼓勵玩家在自己的領地上建設與活動。

不需要 PlaceholderAPI、不需要腳本、不囉嗦——丟進去、設定好即可。

## 為什麼用 AFly？

- 💰 **真正的經濟回收。** 持續性、自願性的消費，能對抗通膨，又不會懲罰不飛行的玩家。
- 🏠 **依位置計價。** 獎勵在家飛行——領地內設便宜費率、荒野設較高費率。
- 🎮 **自願且公平。** 玩家用 `/afly on` 自行決定何時飛；沒在飛就不扣錢。
- 🌐 **說你的語言。** 所有訊息都在語言檔裡。內建英文與繁體中文，幾分鐘就能新增自己的語言。

## 功能特色

- 🎮 **個人開關**：任何玩家可 `/afly on` / `off` 控制自己的付費飛行。
- ⏱️ **每秒計費**，且**只在真的飛行時**扣款。僅生存 / 冒險模式（可設定）。
- 🏠 **領地內外差異化費率**。
- 🛫 **起飛提示**：顯示你在領地內或荒野、領地名稱與目前費率。
- 👤 **通知地主**：在他人領地飛行會通知該領地擁有者。
- 🔔 **跨界提醒**：飛越領地邊界時，立即告知費率變動。
- 📊 **即時動作列**：顯示所在區域、費率與剩餘餘額。
- ⌨️ 所有子指令支援 **Tab 補全**。
- 🛡️ **免費權限**：給不該被收費的管理員。
- 💸 **餘額不足？安全落地**，並自動關閉付費飛行直到補錢。

## 運作流程

1. 玩家輸入 **`/afly on`** — 取得飛行能力。
2. **雙擊空白鍵**起飛，從此每秒扣款。
3. 動作列顯示目前區域、費率與餘額；進出領地時即時更新費率（並跳聊天提醒）。
4. 餘額用盡會被安全放下，付費飛行自動關閉，補錢後再開即可。

## 指令與權限

| 指令 | 權限 | 說明 |
| --- | --- | --- |
| `/afly on` | 所有玩家 | 開啟付費飛行 |
| `/afly off` | 所有玩家 | 關閉付費飛行 |
| `/afly info` | 所有人 | 查看費率與狀態 |
| `/afly help` | 所有人 | 顯示說明 |
| `/afly reload` | `afly.admin` | 重新載入設定與語言 |

`fly.charge.bypass` 可讓管理員免費飛行。

## 設定

乾淨的 `config.yml`（費率、計費間隔、收費的遊戲模式、通知地主開關、語言選擇）外加各語言的 `lang/*.yml` 訊息檔。改完 `/afly reload`，免重啟。

## 需求

- **Paper 1.21+**
- **Vault** + 任一經濟插件（EssentialsX、CMI…）
- **Residence**
