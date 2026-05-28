# AFly — Release Notes (after v1.3.1)

涵蓋 v1.3.2、v1.4.0、v1.4.1。每個版本附「英文 / 繁中」更新內容，以及可直接貼到 GitHub Releases 的發佈文案。

---

## 📋 Changelog（雙語）

### v1.3.2
**English**
- Configurable flight locations (`flight-allowed` in `config.yml`): restrict paid flight by world list, and toggle it inside / outside residences. Flying into a disallowed spot drops the player safely with a notice.
- Listed two authors.

**繁體中文**
- 可設定的飛行地點（`config.yml` 的 `flight-allowed`）：可用世界清單限制付費飛行，並分別開關「領地內 / 領地外」。飛到不允許的地方會把玩家安全放下並提示。
- 列出兩位作者。

### v1.4.0
**English**
- Seamless cross-boundary flight (`keep-flying-across-regions`, default on): crossing residence ↔ wilderness no longer drops you — you keep flying and just see the new rate. When off, you drop at boundaries with a hint to `/afly on` again.
- Blocked in a no-fly zone? Flight auto-resumes when you return to an allowed area.

**繁體中文**
- 無縫跨界飛行（`keep-flying-across-regions`，預設開）：跨越領地 ↔ 荒野邊界不再被放下，會繼續飛行、只跳出新費率提示。關閉時則會在邊界掉下並提示用 `/afly on` 重飛。
- 被禁飛區擋下後，回到可飛區會自動恢復飛行。

### v1.4.1
**English**
- Smarter fall damage: system-caused drops (no-fly zone, out of money, region boundary) no longer hurt the player, but a player who has flight on and *chooses* to fall without flying still takes normal fall damage.

**繁體中文**
- 更聰明的摔落傷害：系統造成的墜落（禁飛區、沒錢、領地邊界）不再扣血；但若玩家開著飛行卻「自願往下跳、沒起飛」，仍照常受到摔落傷害。

---

## 🚀 GitHub Releases（可直接複製）

> 每個版本一個 Release。Tag 用 `vX.Y.Z`，並上傳對應的 `AFly-X.Y.Z.jar`。

### Release: v1.3.2
**Tag:** `v1.3.2`　**Title:** `v1.3.2 — Configurable flight locations / 可設定飛行地點`

```markdown
### 🇬🇧 English
- **Configurable flight locations** (`flight-allowed` in `config.yml`): restrict paid flight by world list, and toggle it inside / outside residences. Flying into a disallowed spot drops the player safely with a notice.
- Listed two authors.

### 🇹🇼 繁體中文
- **可設定的飛行地點**（`config.yml` 的 `flight-allowed`）：可用世界清單限制付費飛行，並分別開關「領地內 / 領地外」。飛到不允許的地方會把玩家安全放下並提示。
- 列出兩位作者。

**Download:** AFly-1.3.2.jar
```

### Release: v1.4.0
**Tag:** `v1.4.0`　**Title:** `v1.4.0 — Seamless cross-boundary flight / 無縫跨界飛行`

```markdown
### 🇬🇧 English
- **Seamless cross-boundary flight** (`keep-flying-across-regions`, default on): crossing residence ↔ wilderness no longer drops you — you keep flying and just see the new rate. When off, you drop at boundaries with a hint to `/afly on` again.
- Blocked in a no-fly zone? Flight **auto-resumes** when you return to an allowed area.

### 🇹🇼 繁體中文
- **無縫跨界飛行**（`keep-flying-across-regions`，預設開）：跨越領地 ↔ 荒野邊界不再被放下，會繼續飛行、只跳出新費率提示。關閉時則會在邊界掉下並提示用 `/afly on` 重飛。
- 被禁飛區擋下後，回到可飛區會**自動恢復飛行**。

**Download:** AFly-1.4.0.jar
```

### Release: v1.4.1
**Tag:** `v1.4.1`　**Title:** `v1.4.1 — Smarter fall damage / 更聰明的摔落傷害`

```markdown
### 🇬🇧 English
- **Smarter fall damage**: system-caused drops (no-fly zone, out of money, region boundary) no longer hurt the player, but a player who has flight on and *chooses* to fall without flying still takes normal fall damage.

### 🇹🇼 繁體中文
- **更聰明的摔落傷害**：系統造成的墜落（禁飛區、沒錢、領地邊界）不再扣血；但若玩家開著飛行卻「自願往下跳、沒起飛」，仍照常受到摔落傷害。

**Download:** AFly-1.4.1.jar
```
