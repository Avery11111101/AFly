package io.github.avery11111102.afly;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 週期性任務：每隔 interval-ticks 檢查全服玩家，對「已開啟 afly 且正在飛行」者扣款，
 * 並處理起飛提示、跨界提醒、通知地主、動作列、飛行明細與餘額不足落地。
 *
 * 計價區域分三種：
 *   OWN   (0) = 自己的領地        → 便宜（cost-res）
 *   OTHER (1) = 別人的領地        → 以荒野費率計費（cost-wild）
 *   WILD  (2) = 荒野（無領地）     → 荒野費率（cost-wild）
 */
public class ChargeTask extends BukkitRunnable {

    private static final int ZONE_OWN = 0;
    private static final int ZONE_OTHER = 1;
    private static final int ZONE_WILD = 2;

    private final AFly plugin;

    public ChargeTask(AFly plugin) {
        this.plugin = plugin;
    }

    // isOnGround() 在 Paper 26.1 標記廢棄（取自客戶端、可被偽造）。
    // 本插件用它判斷「飛行被放下時是否還在半空」以決定是否套用一次性摔落保護；
    // 此情境下客戶端視角正是我們需要的，故抑制廢棄警告。
    @Override
    @SuppressWarnings("deprecation")
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
                boolean midAir = !p.isOnGround();
                plugin.endFlight(p);
                // 半空中被放下、且不是玩家自願停飛 → 視為系統造成的墜落，免摔傷
                if (midAir && !plugin.recentlyStoppedIntentionally(id)) {
                    plugin.protectNextFall(id);
                }
                // 未開啟「跨界維持飛行」且是半空中掉下來 → 提示可重新飛
                if (!plugin.keepFlyingAcrossRegions() && midAir && plugin.flyEnabled().contains(id)) {
                    p.sendMessage(plugin.lang().component("dropped-hint"));
                }
                continue;
            }

            // 只處理收費模式（生存/冒險）；創造/旁觀的飛行能力一律不碰
            if (!plugin.chargedModes().contains(p.getGameMode())) continue;

            // 地點許可：世界 + 領地內/外
            boolean worldOk = plugin.worldAllowed(p.getWorld().getName());
            Object res = worldOk ? plugin.residence().getResidence(p.getLocation()) : null;
            boolean inResForAllow = res != null;
            boolean allowedHere = worldOk && plugin.zoneAllowed(inResForAllow);

            if (!allowedHere) {
                // 此處不允許付費飛行 → 安全放下並收回飛行能力（提示去重；系統墜落免傷）
                if (p.isFlying()) {
                    p.setFlying(false);
                    plugin.protectNextFall(id);
                    plugin.endFlight(p);
                }
                p.setAllowFlight(false);
                if (plugin.blockedFlight().add(id)) {
                    p.sendMessage(plugin.lang().component("no-fly-here"));
                }
                continue;
            }

            // 剛從禁飛區回到可飛區 → 自動恢復飛行能力
            if (plugin.blockedFlight().remove(id)) {
                p.setAllowFlight(true);
                p.sendMessage(plugin.lang().component("flight-resumed"));
            }

            // 允許飛行 → 確保有飛行能力（可雙擊起飛）
            if (!p.getAllowFlight()) p.setAllowFlight(true);

            if (!p.isFlying()) continue;
            if (p.hasPermission(AFly.BYPASS_PERM)) continue;

            // 判斷所在領地與計價區域
            String name = null;
            int zone;
            double cost;
            if (res == null) {
                zone = ZONE_WILD;
                cost = plugin.costWild();
            } else {
                name = plugin.residence().nameOf(res);
                if (name == null || name.isEmpty()) name = "?";
                // 優先以 UUID 比對（玩家改名後仍正確）；新版 Residence 不支援時退回名稱比對
                java.util.UUID ownerUUID = plugin.residence().ownerUUIDOf(res);
                boolean own;
                if (ownerUUID != null) {
                    own = ownerUUID.equals(p.getUniqueId());
                } else {
                    String owner = plugin.residence().ownerOf(res);
                    own = owner != null && owner.equalsIgnoreCase(p.getName());
                }
                if (own) {
                    zone = ZONE_OWN;
                    cost = plugin.costRes();
                } else {
                    // 別人的領地：算荒野價
                    zone = ZONE_OTHER;
                    cost = plugin.costWild();
                }
            }
            String zoneKey = switch (zone) {
                case ZONE_OWN -> "own:" + name;
                case ZONE_OTHER -> "other:" + name;
                default -> "wild";
            };

            boolean wasFly = plugin.wasFlying().contains(id);
            String prevZone = plugin.lastZone().get(id);

            if (!wasFly) {
                plugin.startSession(id);
                sendZone(p, "takeoff", zone, name, cost);
                if (zone == ZONE_OTHER) notifyOwner(p, res, name);
            } else if (prevZone != null && !prevZone.equals(zoneKey)) {
                sendZone(p, "enter", zone, name, cost);
                if (zone == ZONE_OTHER) notifyOwner(p, res, name);
            }

            plugin.wasFlying().add(id);
            plugin.lastZone().put(id, zoneKey);

            // 扣款
            double balance = plugin.economy().getBalance(p);
            if (balance >= cost) {
                plugin.economy().withdrawPlayer(p, cost);
                plugin.addSpent(id, cost);
                sendActionBar(p, zone, name, cost);
            } else {
                p.sendMessage(plugin.lang().component("insufficient"));
                plugin.protectNextFall(id); // 沒錢被迫落地 → 系統造成，免摔傷
                plugin.endFlight(p);
                p.setFlying(false);
                p.setAllowFlight(false);
                plugin.flyEnabled().remove(id);
            }
        }
    }

    /** prefix = "takeoff" 或 "enter"。依區域挑選對應的訊息鍵。 */
    private void sendZone(Player p, String prefix, int zone, String name, double cost) {
        Map<String, String> ph = new HashMap<>();
        ph.put("cost", plugin.fmt(cost));
        switch (zone) {
            case ZONE_OWN -> {
                ph.put("residence", name);
                p.sendMessage(plugin.lang().component(prefix + "-residence", ph));
            }
            case ZONE_OTHER -> {
                ph.put("residence", name);
                p.sendMessage(plugin.lang().component(prefix + "-other", ph));
            }
            default -> p.sendMessage(plugin.lang().component(prefix + "-wild", ph));
        }
    }

    private void sendActionBar(Player p, int zone, String name, double cost) {
        Map<String, String> ph = new HashMap<>();
        ph.put("cost", plugin.fmt(cost));
        ph.put("residence", name == null ? "" : name);
        ph.put("balance", plugin.showBalance() ? plugin.fmt(plugin.economy().getBalance(p)) : "");
        String key = switch (zone) {
            case ZONE_OWN -> "actionbar-residence";
            case ZONE_OTHER -> "actionbar-other";
            default -> "actionbar-wild";
        };
        p.sendActionBar(plugin.lang().component(key, ph));
    }

    /** 通知地主（自帶守門：伺服器主開關 + 地主個人開關 + 不通知自己）。 */
    private void notifyOwner(Player flyer, Object res, String name) {
        if (!plugin.notifyOwner()) return;
        // 優先用 UUID 找線上地主（getPlayerExact 在新版會因玩家改名失效）
        Player ownerP = null;
        java.util.UUID ownerUUID = plugin.residence().ownerUUIDOf(res);
        if (ownerUUID != null) {
            if (ownerUUID.equals(flyer.getUniqueId())) return;
            ownerP = plugin.getServer().getPlayer(ownerUUID);
        } else {
            String owner = plugin.residence().ownerOf(res);
            if (owner == null || owner.isEmpty()) return;
            if (owner.equalsIgnoreCase(flyer.getName())) return;
            ownerP = plugin.getServer().getPlayerExact(owner);
        }
        if (ownerP == null) return;
        if (!plugin.playerData().ownerNotify(ownerP.getUniqueId())) return;
        Map<String, String> ph = new HashMap<>();
        ph.put("player", flyer.getName());
        ph.put("residence", name);
        ownerP.sendMessage(plugin.lang().component("owner-notify", ph));
    }
}
