# AFly — Residence 領地飛行收費

[English](../README.md) · **繁體中文**

> 一個輕量的 **Paper 插件**：玩家飛行時依所在位置每秒扣款，[Residence](https://github.com/Zrips/Residence) 領地內外費率不同。名稱取自指令 `/afly`。

玩家用 `/afly on` 開啟「付費飛行」後，雙擊空白鍵起飛即**每秒**依所在位置自動扣款。站在 **Residence 領地內**收便宜的費率（預設 `1`/秒），飛到**領地外的荒野**則收較高費率（預設 `5`/秒）；餘額不足時自動落地。

> 本版是原 AFly Skript 的**原生 Java 重寫版**——不再需要 Skript、skript-reflect 或 PlaceholderAPI。

---

## ✨ 功能特色

- 🎮 **個人開關**：任何玩家可 `/afly on` / `off` 開關自己的付費飛行。
- ⏱️ 每秒計費，**沒在飛就不扣錢**；只對生存 / 冒險模式收費（可設定）。
- 🏠 領地內 / 領地外**差異化收費**（直接讀取 Residence）。
- 🛫 **起飛提示**：顯示身處領地內 / 荒野、領地名稱與目前費率。
- 👤 在別人領地飛行會**通知該領地擁有者**（可設定）。
- 🔔 **跨界提醒**：飛行中進出領地、費率變動時即時提示。
- 🧾 **飛行明細**：落地時顯示這趟飛了多久、共花多少（可個人開關）。
- 📊 動作列（物品欄上方）常駐顯示目前區域、費率與餘額。
- ⌨️ `/afly` 子指令支援 **Tab 補全**。
- 🔧 **個人偏好**（明細開關、地主通知開關）儲存在 `playerdata.yml`。
- 🌍 **遊戲內語言切換**：`/afly lang <語言>`。
- 🌐 **多語言**：`lang/*.yml`（內建英文與繁體中文）。
- 🛡️ 支援**免費權限節點**（`fly.charge.bypass`）。
- 💸 餘額不足時**強制落地**並自動關閉付費飛行。

---

## 📦 安裝需求

| 類型 | 名稱 | 備註 |
| --- | --- | --- |
| 伺服器核心 | **Paper 1.21+** | 或其分支（Purpur 等） |
| 插件 | **Vault** | 經濟系統橋接 |
| 插件 | 任一經濟插件（EssentialsX / CMI…） | 提供金錢來源 |
| 插件 | **Residence** | 領地外掛 |

---

## 🚀 完整安裝教學

### 方式 A — 使用發佈的 JAR（建議）

1. **準備伺服器。** 執行 **Paper 1.21+**，將必要插件放入 `plugins/`：
   - [Vault](https://github.com/MilkBowl/Vault/releases)
   - 一個經濟插件，例如 [EssentialsX](https://essentialsx.net/downloads.html)
   - [Residence](https://github.com/Zrips/Residence/releases)
2. **確認經濟正常。** 先啟動一次伺服器、進遊戲打 `/balance`，應能看到餘額（EssentialsX 會自動掛上 Vault）。
3. **安裝 AFly。** 從 [Releases](../../releases) 下載 `AFly-x.x.x.jar`，放進 `plugins/`。
4. **完整重啟伺服器**（先 Stop 再 Start），不要用 `/reload`。
5. **設定檔自動生成**於 `plugins/AFly/config.yml` 與 `plugins/AFly/lang/`。要改的話改完執行 `/afly reload`。
6. **測試：** `/afly on`，雙擊空白鍵起飛，觀察動作列餘額每秒遞減。

### 方式 B — 從原始碼自行編譯

需要 **JDK 21+** 與 **Maven**。

```bash
git clone https://github.com/avery11111102/AFly.git
cd AFly
mvn clean package
# 成品：target/AFly-<版本>.jar
```

接著依方式 A 的 1–6 步部署你編出來的 JAR。

> ⚠️ 從 Skript 版遷移過來？請**先移除舊的 `fly_charge.sk`**——兩套同時運作會造成雙重扣款與 `/afly` 指令衝突。

---

## ⚙️ 設定說明

`plugins/AFly/config.yml`：

```yaml
language: en             # 使用哪個 lang/<language>.yml（en、zh_TW…）
interval-ticks: 20       # 每幾 tick 扣一次（20 tick = 1 秒）
cost:
  wilderness: 5.0        # 領地外（荒野）每次扣款
  residence: 1.0         # 領地內每次扣款
charged-gamemodes:       # 哪些遊戲模式會被收費
  - SURVIVAL
  - ADVENTURE
notify-owner: true       # 在他人領地飛行時是否通知地主
actionbar-show-balance: true
```

> 改完執行 `/afly reload` 即可生效，免重啟。

`config.yml` 預設是**英文**。把 `language` 改成 `zh_TW` 再執行 `/afly reload`，`config.yml` 的註解也會跟著換成該語言（你的設定值會保留）。只要該語言有內建的 `config_<language>.yml` 模板即可運作。

### 語言

訊息字串放在 `plugins/AFly/lang/<language>.yml`（顏色代碼用 `&`）。要新增語言，把 `en.yml` 複製成例如 `fr.yml`、翻譯內容，再到 `config.yml` 設 `language: fr`。可用變數：`{residence}`、`{cost}`、`{balance}`、`{player}`、`{cost_res}`、`{cost_wild}`、`{arg}`。

---

## 🎛️ 指令一覽

所有功能都收在 `/afly` 底下（子指令支援 Tab 補全）：

| 指令 | 權限 | 說明 |
| --- | --- | --- |
| `/afly on` | 所有玩家 | 開啟自己的付費飛行（之後雙擊空白鍵起飛） |
| `/afly off` | 所有玩家 | 關閉自己的付費飛行 |
| `/afly info` | 所有人 | 查看費率與自己的開關狀態 |
| `/afly summary [on\|off]` | 所有玩家 | 開關自己的飛行明細（每位玩家獨立） |
| `/afly notify [on\|off]` | 所有玩家 | 開關「他人在你領地飛行」的通知（每位地主獨立） |
| `/afly help` | 所有人 | 顯示指令說明 |
| `/afly lang <語言>` | `afly.admin` | 遊戲內切換伺服器語言（設定檔 + 訊息） |
| `/afly reload` | `afly.admin` | 重新載入設定與語言檔 |

> 不帶參數直接輸入 `/afly` 等同 `/afly help`。

---

## 🔐 權限節點

| 節點 | 預設 | 意義 |
| --- | --- | --- |
| `fly.charge.bypass` | op | 飛行不被扣錢 |
| `afly.admin` | op | 可用 `/afly reload` |

---

## 📝 更新日誌

### v1.0.0
- 首個原生版本：個人 `/afly` 付費飛行開關、每秒計費（領地內外不同費率）、起飛與跨界提示、通知地主、Tab 補全、動作列、餘額不足強制落地。
- 獨立 `config.yml` 與多語言 `lang/*.yml`（英文 + 繁體中文）。

---

## 📄 授權

尚未選擇授權條款。若要公開，建議加上一個（例如 [MIT](https://choosealicense.com/licenses/mit/)）。

---

*作者：avery11111102*
