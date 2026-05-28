package io.github.avery11111102.afly;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 週期性任務：每隔 interval-ticks 檢查全服玩家，對「已開啟 afly 且正在飛行」者扣款，
 * 並處理起飛提示、跨界提醒、通知地主、動作列、飛行明細與餘額不足落地。
 */
public class ChargeTask extends BukkitRunnable {

    private final AFly plugin;

    public ChargeTask(AFly plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            UUID id = p.getUniqueId();

            // 未開啟付費飛行 → 清除任何殘留狀態（不顯示明細）
            if (!plugin.flyEnabled().contains(id)) {
                plugin.clearSession(id);
                continue;
            }

            // 飛行結束偵測：剛剛還在收費飛行、現在停了 → 顯示明細
            if (plugin.wasFlying().contains(id) && !p.isFlying()) {
                plugin.endFlight(p);
                continue;
            }

            // 沒在飛 → 不處理
            if (!p.isFlying()) continue;

            // 只對指定遊戲模式收費
            if (!plugin.chargedModes().contains(p.getGameMode())) continue;

            // 免費權限
            if (p.hasPermission(AFly.BYPASS_PERM)) continue;

            // 判斷所在領地
            Object res = plugin.residence().getResidence(p.getLocation());
            boolean inRes = res != null;
            String name = inRes ? plugin.residence().nameOf(res) : null;
            if (inRes && (name == null || name.isEmpty())) name = "?";
            double cost = inRes ? plugin.costRes() : plugin.costWild();
            String zoneKey = inRes ? ("res:" + name) : "wild";

            boolean wasFly = plugin.wasFlying().contains(id);
            String prevZone = plugin.lastZone().get(id);

            if (!wasFly) {
                // 剛起飛 → 開始一段飛行工作階段
                plugin.startSession(id);
                sendZone(p, "takeoff-residence", "takeoff-wild", inRes, name, cost);
                if (inRes) notifyOwner(p, res, name);
            } else if (prevZone != null && !prevZone.equals(zoneKey)) {
                // 飛行中跨越領地邊界，費率變動
                sendZone(p, "enter-residence", "enter-wild", inRes, name, cost);
                if (inRes) notifyOwner(p, res, name);
            }

            plugin.wasFlying().add(id);
            plugin.lastZone().put(id, zoneKey);

            // 扣款
            double balance = plugin.economy().getBalance(p);
            if (balance >= cost) {
                plugin.economy().withdrawPlayer(p, cost);
                plugin.addSpent(id, cost);
                sendActionBar(p, inRes, cost);
            } else {
                p.sendMessage(plugin.lang().component("insufficient"));
                plugin.endFlight(p); // 顯示明細並清除狀態
                p.setFlying(false);
                p.setAllowFlight(false);
                plugin.flyEnabled().remove(id);
            }
        }
    }

    private void sendZone(Player p, String resKey, String wildKey, boolean inRes, String name, double cost) {
        Map<String, String> ph = new HashMap<>();
        ph.put("cost", plugin.fmt(cost));
        if (inRes) {
            ph.put("residence", name);
            p.sendMessage(plugin.lang().component(resKey, ph));
        } else {
            p.sendMessage(plugin.lang().component(wildKey, ph));
        }
    }

    private void sendActionBar(Player p, boolean inRes, double cost) {
        Map<String, String> ph = new HashMap<>();
        ph.put("cost", plugin.fmt(cost));
        ph.put("balance", plugin.showBalance() ? plugin.fmt(plugin.economy().getBalance(p)) : "");
        p.sendActionBar(plugin.lang().component(inRes ? "actionbar-residence" : "actionbar-wild", ph));
    }

    /** 通知地主（自帶守門：伺服器主開關 + 地主個人開關 + 不通知自己）。 */
    private void notifyOwner(Player flyer, Object res, String name) {
        if (!plugin.notifyOwner()) return; // 伺服器主開關關閉
        String owner = plugin.residence().ownerOf(res);
        if (owner == null || owner.isEmpty()) return;
        if (owner.equalsIgnoreCase(flyer.getName())) return; // 飛自己領地不通知自己
        Player ownerP = plugin.getServer().getPlayerExact(owner);
        if (ownerP == null) return; // 地主不在線上
        if (!plugin.playerData().ownerNotify(ownerP.getUniqueId())) return; // 地主已關閉通知
        Map<String, String> ph = new HashMap<>();
        ph.put("player", flyer.getName());
        ph.put("residence", name);
        ownerP.sendMessage(plugin.lang().component("owner-notify", ph));
    }
}
