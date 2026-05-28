package io.github.avery11111102.afly;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;

import java.util.UUID;

/**
 * 維持「跨越領地邊界仍持續飛行」的體驗。
 *
 * 背景：Residence（或其他外掛）常在玩家進出領地時，依其 fly 旗標把玩家的飛行能力關掉，
 *       導致跨界瞬間掉到地上。本監聽器以 HIGHEST 優先權在同一個 move 事件中「搶回」飛行，
 *       讓玩家在可飛行區域之間穿越時不會掉落（僅由 ChargeTask 跳出費率變更提示）。
 *
 *  - keep-flying-across-regions = true：可飛區域間穿越維持飛行。
 *  - 進入「禁飛區」：立即安全放下並提示，回到可飛區時自動恢復。
 *  - 玩家真的墜落時，摔落傷害照常由原版計算（本插件不做保護）。
 */
public class FlightListener implements Listener {

    private final AFly plugin;

    public FlightListener(AFly plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (event.getTo() == null) return;
        // 只在「跨越方塊」時處理，降低開銷
        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockY() == event.getTo().getBlockY()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        Player p = event.getPlayer();
        UUID id = p.getUniqueId();

        if (!plugin.flyEnabled().contains(id)) return;
        if (!plugin.chargedModes().contains(p.getGameMode())) return;

        boolean allowed = plugin.flightAllowedAt(p);

        if (!allowed) {
            // 禁飛區：安全放下、收回飛行能力，並提示一次（系統造成的墜落 → 免傷）
            if (p.isFlying()) {
                p.setFlying(false);
                plugin.protectNextFall(id);
            }
            p.setAllowFlight(false);
            if (plugin.blockedFlight().add(id)) {
                p.sendMessage(plugin.lang().component("no-fly-here"));
            }
            return;
        }

        // 可飛行區域
        if (plugin.blockedFlight().remove(id)) {
            // 剛從禁飛區回到可飛區 → 自動恢復飛行能力
            p.setAllowFlight(true);
            p.sendMessage(plugin.lang().component("flight-resumed"));
            return;
        }

        // 維持跨界飛行（在 Residence 等外掛關閉飛行後搶回）
        if (plugin.keepFlyingAcrossRegions()) {
            if (!p.getAllowFlight()) p.setAllowFlight(true);
            if (!p.isFlying() && !p.isOnGround()) p.setFlying(true);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.blockedFlight().remove(event.getPlayer().getUniqueId());
    }

    /** 玩家「自行」雙擊關閉飛行 → 記錄為自願停飛，後續墜落不予保護。 */
    @EventHandler
    public void onToggleFlight(PlayerToggleFlightEvent event) {
        Player p = event.getPlayer();
        if (!plugin.flyEnabled().contains(p.getUniqueId())) return;
        if (!event.isFlying() && p.isFlying()) {
            plugin.markIntentionalStop(p.getUniqueId());
        }
    }

    /** 僅對「系統造成的墜落」免除摔落傷害；玩家自願往下跳的墜落照常受傷。 */
    @EventHandler(ignoreCancelled = true)
    public void onFallDamage(EntityDamageEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) return;
        if (!(event.getEntity() instanceof Player p)) return;
        if (plugin.consumeFallProtection(p.getUniqueId())) {
            event.setCancelled(true);
        }
    }
}
