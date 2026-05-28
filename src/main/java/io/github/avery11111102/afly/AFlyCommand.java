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
 *   on / off / info / help → 所有玩家
 *   reload                 → 需 afly.admin
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
            case "reload" -> {
                if (!sender.hasPermission(AFly.ADMIN_PERM)) {
                    sender.sendMessage(plugin.lang().component("no-permission"));
                } else {
                    plugin.reloadAll();
                    sender.sendMessage(plugin.lang().component("reloaded"));
                }
            }
            default -> sender.sendMessage(plugin.lang().component("unknown-arg", Map.of("arg", sub)));
        }
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
            p.setAllowFlight(true);
            p.sendMessage(plugin.lang().component("fly-enabled"));
        } else {
            plugin.flyEnabled().remove(id);
            plugin.wasFlying().remove(id);
            plugin.lastZone().remove(id);
            p.setFlying(false);
            p.setAllowFlight(false);
            p.sendMessage(plugin.lang().component("fly-disabled"));
        }
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
        if (args.length != 1) return Collections.emptyList();
        List<String> opts = new ArrayList<>(Arrays.asList("on", "off", "info", "help"));
        if (sender.hasPermission(AFly.ADMIN_PERM)) opts.add("reload");

        String prefix = args[0].toLowerCase(Locale.ROOT);
        List<String> out = new ArrayList<>();
        for (String o : opts) {
            if (o.startsWith(prefix)) out.add(o);
        }
        return out;
    }
}
