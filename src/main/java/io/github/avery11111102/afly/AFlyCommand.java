package io.github.avery11111102.afly;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * /afly 指令處理 + Tab 補全。
 *   on / off / info / help / summary / notify → 所有玩家
 *   reload / lang                             → 需 afly.admin
 */
public class AFlyCommand implements CommandExecutor, TabCompleter {

    private final AFly plugin;

    public AFlyCommand(AFly plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String sub = (args.length == 0) ? "help" : args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "help" -> sendHelp(sender);
            case "info" -> sendInfo(sender);
            case "on" -> toggle(sender, true);
            case "off" -> toggle(sender, false);
            case "summary" -> prefSummary(sender, args);
            case "notify" -> prefNotify(sender, args);
            case "check" -> {
                if (notAdmin(sender)) break;
                checkPlayers(sender, args);
            }
            case "adminnotify" -> {
                if (notAdmin(sender)) break;
                prefAdminNotify(sender, args);
            }
            case "reload" -> {
                if (notAdmin(sender)) break;
                plugin.reloadAll();
                sender.sendMessage(plugin.lang().component("reloaded"));
            }
            case "lang" -> {
                if (notAdmin(sender)) break;
                if (args.length < 2) {
                    sender.sendMessage(plugin.lang().component("lang-usage",
                            Map.of("langs", String.join(", ", plugin.availableLanguages()))));
                    break;
                }
                plugin.setLanguage(args[1]);
                sender.sendMessage(plugin.lang().component("lang-changed", Map.of("lang", args[1])));
            }
            default -> sender.sendMessage(plugin.lang().component("unknown-arg", Map.of("arg", sub)));
        }
        return true;
    }

    private boolean notAdmin(CommandSender sender) {
        if (sender.hasPermission(AFly.ADMIN_PERM)) return false;
        sender.sendMessage(plugin.lang().component("no-permission"));
        return true;
    }

    private void toggle(CommandSender sender, boolean on) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(plugin.lang().component("players-only"));
            return;
        }
        UUID id = p.getUniqueId();
        if (on) {
            plugin.flyEnabled().add(id);
            boolean allowed = plugin.flightAllowedAt(p);
            p.setAllowFlight(allowed);
            p.sendMessage(plugin.lang().component("fly-enabled"));
            if (allowed) {
                plugin.blockedFlight().remove(id);
            } else {
                plugin.blockedFlight().add(id);
                p.sendMessage(plugin.lang().component("no-fly-here"));
            }
            notifyAdminsToggle(p, true);
        } else {
            // 關閉前先結算明細
            plugin.endFlight(p);
            plugin.flyEnabled().remove(id);
            plugin.blockedFlight().remove(id);
            p.setFlying(false);
            p.setAllowFlight(false);
            p.sendMessage(plugin.lang().component("fly-disabled"));
            notifyAdminsToggle(p, false);
        }
    }

    private void notifyAdminsToggle(Player p, boolean on) {
        Component msg = plugin.lang().component(on ? "admin-notify-msg-on" : "admin-notify-msg-off", Map.of("player", p.getName()));
        for (Player admin : plugin.getServer().getOnlinePlayers()) {
            if (admin.hasPermission(AFly.ADMIN_PERM) && plugin.playerData().adminNotify(admin.getUniqueId())) {
                admin.sendMessage(msg);
            }
        }
    }

    private void checkPlayers(CommandSender sender, String[] args) {
        if (args.length < 2) {
            List<String> flyingPlayers = new ArrayList<>();
            for (UUID id : plugin.flyEnabled()) {
                Player p = plugin.getServer().getPlayer(id);
                if (p != null) flyingPlayers.add(p.getName());
            }
            if (flyingPlayers.isEmpty()) {
                sender.sendMessage(plugin.lang().component("check-none"));
            } else {
                sender.sendMessage(plugin.lang().component("check-list", Map.of("players", String.join(", ", flyingPlayers))));
            }
        } else {
            Player target = plugin.getServer().getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(plugin.lang().component("check-offline"));
                return;
            }
            boolean isFlying = plugin.flyEnabled().contains(target.getUniqueId());
            sender.sendMessage(plugin.lang().component(isFlying ? "check-player-on" : "check-player-off", Map.of("player", target.getName())));
        }
    }

    private void prefAdminNotify(CommandSender sender, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(plugin.lang().component("players-only"));
            return;
        }
        boolean value = resolveToggle(args, plugin.playerData().adminNotify(p.getUniqueId()));
        plugin.playerData().setAdminNotify(p.getUniqueId(), value);
        p.sendMessage(plugin.lang().component(value ? "admin-notify-on" : "admin-notify-off"));
    }

    private void prefSummary(CommandSender sender, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(plugin.lang().component("players-only"));
            return;
        }
        boolean value = resolveToggle(args, plugin.playerData().showSummary(p.getUniqueId()));
        plugin.playerData().setShowSummary(p.getUniqueId(), value);
        p.sendMessage(plugin.lang().component(value ? "summary-on" : "summary-off"));
    }

    private void prefNotify(CommandSender sender, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(plugin.lang().component("players-only"));
            return;
        }
        boolean value = resolveToggle(args, plugin.playerData().ownerNotify(p.getUniqueId()));
        plugin.playerData().setOwnerNotify(p.getUniqueId(), value);
        p.sendMessage(plugin.lang().component(value ? "notify-on" : "notify-off"));
    }

    /** 有給 on/off 就用它，否則切換目前值。 */
    private boolean resolveToggle(String[] args, boolean current) {
        if (args.length >= 2) {
            String a = args[1].toLowerCase(Locale.ROOT);
            if (a.equals("on")) return true;
            if (a.equals("off")) return false;
        }
        return !current;
    }

    private void sendHelp(CommandSender s) {
        for (Component c : plugin.lang().componentList("help", ratePlaceholders())) {
            s.sendMessage(c);
        }
    }

    private void sendInfo(CommandSender s) {
        for (Component c : plugin.lang().componentList("info", ratePlaceholders())) {
            s.sendMessage(c);
        }
        if (s instanceof Player p) {
            boolean on = plugin.flyEnabled().contains(p.getUniqueId());
            s.sendMessage(plugin.lang().component(on ? "status-on" : "status-off"));
        }
    }

    private Map<String, String> ratePlaceholders() {
        return Map.of(
                "cost_res", plugin.fmt(plugin.costRes()),
                "cost_wild", plugin.fmt(plugin.costWild())
        );
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> opts = new ArrayList<>(Arrays.asList("on", "off", "info", "help", "summary", "notify"));
            if (sender.hasPermission(AFly.ADMIN_PERM)) {
                opts.add("check");
                opts.add("adminnotify");
                opts.add("reload");
                opts.add("lang");
            }
            return filter(opts, args[0]);
        }
        if (args.length == 2) {
            String first = args[0].toLowerCase(Locale.ROOT);
            if (first.equals("summary") || first.equals("notify") || first.equals("adminnotify")) {
                return filter(Arrays.asList("on", "off"), args[1]);
            }
            if (first.equals("check") && sender.hasPermission(AFly.ADMIN_PERM)) {
                return null; // Return null to autocomplete online player names
            }
            if (first.equals("lang") && sender.hasPermission(AFly.ADMIN_PERM)) {
                return filter(plugin.availableLanguages(), args[1]);
            }
        }
        return Collections.emptyList();
    }

    private List<String> filter(List<String> options, String prefix) {
        String p = prefix.toLowerCase(Locale.ROOT);
        List<String> out = new ArrayList<>();
        for (String o : options) {
            if (o.toLowerCase(Locale.ROOT).startsWith(p)) out.add(o);
        }
        return out;
    }
}
