package xyz.acrylicstyle.boss.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.acrylicstyle.boss.BossPlugin;

import java.util.Objects;
import java.util.UUID;

public class TeleportBoss implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command cannot be invoked from console.");
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "/teleportboss <UUID>");
            return true;
        }
        try {
            UUID uuid = UUID.fromString(args[0]);
            Player player = (Player) sender;
            player.teleport(Objects.requireNonNull(BossPlugin.boss.get(uuid).getBossEntity()).getLocation());
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "/teleportboss <UUID>");
        }
        return true;
    }
}
