package io.github.avery11111102.afly;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.GameMode;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

/**
 * AFly — 依 Residence 領地內外差異化收費的「付費飛行」原生插件。
 * 作者：avery11111102
 */
public class AFly extends JavaPlugin {

    public static final String BYPASS_PERM = "fly.charge.bypass";
    public static final String ADMIN_PERM = "afly.admin";

    private Economy economy;
    private ResidenceHook residence;
    private Lang lang;

    // 設定值
    private long intervalTicks;
    private double costWild;
    private double costRes;
    private boolean notifyOwner;
    private boolean showBalance;
    private Set<GameMode> chargedModes = EnumSet.noneOf(GameMode.class);

    // 執行期狀態
    private final Set<UUID> flyEnabled = new HashSet<>();
    private final Set<UUID> wasFlying = new HashSet<>();
    private final Map<UUID, String> lastZone = new HashMap<>();

    // 飛行明細：本次飛行起始時間（毫秒）與累計花費
    private final Map<UUID, Long> flightStart = new HashMap<>();
    private final Map<UUID, Double> flightSpent = new HashMap<>();

    private PlayerData playerData;
    private ChargeTask task;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        lang = new Lang(this);

        if (!setupEconomy()) {
            getLogger().severe("找不到 Vault 經濟服務，AFly 停用。");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        Plugin resPlugin = getServer().getPluginManager().getPlugin("Residence");
        if (resPlugin == null) {
            getLogger().severe("找不到 Residence，AFly 停用。");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        residence = new ResidenceHook(resPlugin);
        if (residence.isBroken()) {
            getLogger().warning("Residence API 反射初始化失敗，領地判斷可能無法運作。");
        }

        playerData = new PlayerData(this);
        playerData.load();

        reloadAll();

        AFlyCommand cmd = new AFlyCommand(this);
        getCommand("afly").setExecutor(cmd);
        getCommand("afly").setTabCompleter(cmd);

        getLogger().info("AFly 已啟用。");
    }

    @Override
    public void onDisable() {
        if (task != null) task.cancel();
        if (playerData != null) playerData.save();
        flyEnabled.clear();
        wasFlying.clear();
        lastZone.clear();
        flightStart.clear();
        flightSpent.clear();
    }

    /** 重新讀取設定與語言檔，並重啟扣款排程。 */
    public void reloadAll() {
        reloadConfig();
        FileConfiguration c = getConfig();

        intervalTicks = Math.max(1L, c.getLong("interval-ticks", 20L));
        costWild = c.getDouble("cost.wilderness", 5.0);
        costRes = c.getDouble("cost.residence", 1.0);
        notifyOwner = c.getBoolean("notify-owner", true);
        showBalance = c.getBoolean("actionbar-show-balance", true);

        chargedModes = EnumSet.noneOf(GameMode.class);
        for (String s : c.getStringList("charged-gamemodes")) {
            try {
                chargedModes.add(GameMode.valueOf(s.toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException ignored) {
                getLogger().warning("config.yml 中未知的遊戲模式：" + s);
            }
        }
        if (chargedModes.isEmpty()) {
            chargedModes.add(GameMode.SURVIVAL);
            chargedModes.add(GameMode.ADVENTURE);
        }

        String language = c.getString("language", "en");
        applyLocalizedConfig(language);
        lang.load(language);

        if (task != null) task.cancel();
        task = new ChargeTask(this);
        task.runTaskTimer(this, intervalTicks, intervalTicks);
    }

    /**
     * 讓 config.yml 的「註解」跟隨所選語言：
     * 預設為英文；當 language 改成有對應模板（config_&lt;lang&gt;.yml）的語言並 /afly reload 後，
     * 會以該語言模板重生 config.yml，但「保留使用者目前的所有設定值」。
     * 透過資料夾內的 .config-lang 標記檔避免每次 reload 都重寫。
     */
    private void applyLocalizedConfig(String language) {
        try {
            File marker = new File(getDataFolder(), ".config-lang");
            String current = marker.exists()
                    ? new String(Files.readAllBytes(marker.toPath()), StandardCharsets.UTF_8).trim()
                    : null;
            if (language.equals(current)) return; // 已是此語言，無需重寫

            String resource = language.equalsIgnoreCase("en") ? "config.yml" : "config_" + language + ".yml";
            InputStream in = getResource(resource);
            if (in == null) {
                // 無對應語言模板：保留現有 config，僅記錄避免重複嘗試
                Files.write(marker.toPath(), language.getBytes(StandardCharsets.UTF_8));
                return;
            }

            YamlConfiguration template = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(in, StandardCharsets.UTF_8));
            // 把使用者目前的設定值套到語言模板上（保留註解、換成該語言）
            for (String key : getConfig().getKeys(true)) {
                if (!getConfig().isConfigurationSection(key)) {
                    template.set(key, getConfig().get(key));
                }
            }
            template.save(new File(getDataFolder(), "config.yml"));
            Files.write(marker.toPath(), language.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            getLogger().warning("無法切換 config.yml 語言：" + e.getMessage());
        }
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) return false;
        RegisteredServiceProvider<Economy> rsp =
                getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        economy = rsp.getProvider();
        return economy != null;
    }

    /** 金額格式化：整數不顯示小數，否則保留兩位。 */
    public String fmt(double d) {
        if (d == Math.floor(d) && !Double.isInfinite(d)) {
            return String.valueOf((long) d);
        }
        return String.format(Locale.US, "%.2f", d);
    }

    // ---- 飛行明細 / 工作階段 ----

    /** 記錄一次飛行的開始（起飛時呼叫）。 */
    public void startSession(UUID id) {
        flightStart.put(id, System.currentTimeMillis());
        flightSpent.put(id, 0.0);
    }

    /** 累加本次飛行花費。 */
    public void addSpent(UUID id, double amount) {
        flightSpent.merge(id, amount, Double::sum);
    }

    /** 清除一次飛行的暫存狀態（不顯示明細）。 */
    public void clearSession(UUID id) {
        flightStart.remove(id);
        flightSpent.remove(id);
        wasFlying.remove(id);
        lastZone.remove(id);
    }

    /** 結束飛行：若玩家有開啟明細，顯示「飛了多久 + 共花多少」，然後清除狀態。 */
    public void endFlight(Player p) {
        UUID id = p.getUniqueId();
        Long start = flightStart.get(id);
        Double spent = flightSpent.get(id);
        clearSession(id);
        if (start == null) return; // 沒有實際飛行工作階段
        if (!playerData.showSummary(id)) return;

        long seconds = Math.max(0L, (System.currentTimeMillis() - start) / 1000L);
        Map<String, String> ph = new HashMap<>();
        ph.put("duration", formatDuration(seconds));
        ph.put("spent", fmt(spent == null ? 0.0 : spent));
        p.sendMessage(lang.component("summary-line", ph));
        p.sendMessage(lang.component("summary-hint"));
    }

    private String formatDuration(long seconds) {
        long m = seconds / 60L;
        long s = seconds % 60L;
        String minUnit = lang.raw("dur-min");
        String secUnit = lang.raw("dur-sec");
        return (m > 0 ? m + minUnit : "") + s + secUnit;
    }

    /** 遊戲內語言切換：寫入 config 的 language，重生本地化 config.yml 並重載。 */
    public void setLanguage(String language) {
        getConfig().set("language", language);
        saveConfig();
        reloadAll();
    }

    /** 可用語言清單：內建 en/zh_TW + 玩家自行新增於 lang/ 的 *.yml。 */
    public List<String> availableLanguages() {
        Set<String> set = new TreeSet<>();
        set.add("en");
        set.add("zh_TW");
        File langDir = new File(getDataFolder(), "lang");
        File[] files = langDir.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files != null) {
            for (File f : files) {
                String n = f.getName();
                set.add(n.substring(0, n.length() - 4));
            }
        }
        return new ArrayList<>(set);
    }

    // ---- getters ----
    public Economy economy() { return economy; }
    public ResidenceHook residence() { return residence; }
    public Lang lang() { return lang; }
    public PlayerData playerData() { return playerData; }
    public double costWild() { return costWild; }
    public double costRes() { return costRes; }
    public boolean notifyOwner() { return notifyOwner; }
    public boolean showBalance() { return showBalance; }
    public Set<GameMode> chargedModes() { return chargedModes; }
    public Set<UUID> flyEnabled() { return flyEnabled; }
    public Set<UUID> wasFlying() { return wasFlying; }
    public Map<UUID, String> lastZone() { return lastZone; }
}
