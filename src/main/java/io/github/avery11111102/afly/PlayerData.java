package io.github.avery11111102.afly;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * 每位玩家獨立、可持久化的偏好設定（plugins/AFly/playerdata.yml）。
 *   <uuid>.summary       : 飛行結束是否顯示明細（預設 true）
 *   <uuid>.owner-notify  : 身為地主是否接收「有人在你領地飛行」通知（預設 true）
 *   <uuid>.admin-notify  : 管理員是否接收玩家切換飛行模式通知（預設 false）
 */
public class PlayerData {

    private final AFly plugin;
    private final File file;
    private FileConfiguration data;

    public PlayerData(AFly plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "playerdata.yml");
    }

    public void load() {
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().warning("無法建立 playerdata.yml：" + e.getMessage());
            }
        }
        data = YamlConfiguration.loadConfiguration(file);
    }

    public void save() {
        try {
            data.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("無法儲存 playerdata.yml：" + e.getMessage());
        }
    }

    public boolean showSummary(UUID id) {
        return data.getBoolean(id + ".summary", true);
    }

    public void setShowSummary(UUID id, boolean value) {
        data.set(id + ".summary", value);
        save();
    }

    public boolean ownerNotify(UUID id) {
        return data.getBoolean(id + ".owner-notify", true);
    }

    public void setOwnerNotify(UUID id, boolean value) {
        data.set(id + ".owner-notify", value);
        save();
    }

    public boolean adminNotify(UUID id) {
        return data.getBoolean(id + ".admin-notify", false);
    }

    public void setAdminNotify(UUID id, boolean value) {
        data.set(id + ".admin-notify", value);
        save();
    }
}
