package io.github.avery11111102.afly;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 載入並提供語言檔（lang/<language>.yml）的訊息。
 * 顏色代碼使用 '&'，透過 Adventure 的 LegacyComponentSerializer 轉成 Component。
 */
public class Lang {

    private final AFly plugin;
    private final LegacyComponentSerializer legacy = LegacyComponentSerializer.legacyAmpersand();
    private FileConfiguration messages;

    public Lang(AFly plugin) {
        this.plugin = plugin;
    }

    /** 載入指定語言；找不到時退回 en，並以 jar 內 en.yml 作為缺漏鍵的後備。 */
    public void load(String language) {
        saveDefault("lang/en.yml");
        saveDefault("lang/zh_TW.yml");

        File file = new File(plugin.getDataFolder(), "lang/" + language + ".yml");
        if (!file.exists()) {
            plugin.getLogger().warning("找不到語言檔 lang/" + language + ".yml，改用 en。");
            file = new File(plugin.getDataFolder(), "lang/en.yml");
        }
        messages = YamlConfiguration.loadConfiguration(file);

        InputStream def = plugin.getResource("lang/en.yml");
        if (def != null) {
            YamlConfiguration defCfg = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(def, StandardCharsets.UTF_8));
            messages.setDefaults(defCfg);
        }
    }

    private void saveDefault(String path) {
        if (!new File(plugin.getDataFolder(), path).exists()) {
            plugin.saveResource(path, false);
        }
    }

    private String apply(String s, Map<String, String> placeholders) {
        if (s == null) return "";
        if (placeholders != null) {
            for (Map.Entry<String, String> e : placeholders.entrySet()) {
                s = s.replace("{" + e.getKey() + "}", e.getValue());
            }
        }
        return s;
    }

    public Component component(String key, Map<String, String> placeholders) {
        String s = messages.getString(key, key);
        return legacy.deserialize(apply(s, placeholders));
    }

    public Component component(String key) {
        return component(key, null);
    }

    /** 取得原始字串（給需要拼接的場合，例如時間單位）。 */
    public String raw(String key) {
        return messages.getString(key, key);
    }

    public List<Component> componentList(String key, Map<String, String> placeholders) {
        List<Component> out = new ArrayList<>();
        for (String line : messages.getStringList(key)) {
            out.add(legacy.deserialize(apply(line, placeholders)));
        }
        return out;
    }
}
