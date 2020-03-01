package xyz.acrylicstyle.boss.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import xyz.acrylicstyle.boss.utils.Utils;

public class ReloadBoss implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Utils.bossDefinitions = null;
        Utils.getBossDefinitionsCached();
        sender.sendMessage(ChatColor.GREEN + "ボスを再読み込みしました。");
        return true;
    }
}
