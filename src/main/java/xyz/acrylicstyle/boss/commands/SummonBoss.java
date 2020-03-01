package xyz.acrylicstyle.boss.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.acrylicstyle.boss.utils.BossDefinition;
import xyz.acrylicstyle.boss.utils.Utils;
import xyz.acrylicstyle.tomeito_core.utils.TypeUtil;

public class SummonBoss implements CommandExecutor {
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "/summonboss <Boss ID> [x] [y] [z]");
            return true;
        }
        // args[0] = id (1)
        // args[1] = x  (2)
        // args[2] = y  (3)
        // args[3] = z  (4)
        // args[4] = world (5)
        if (args.length < 5) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "This command cannot be invoked from console.");
                return true;
            }
            Player player = (Player) sender;
            String id = args[0];
            BossDefinition bossDefinition = Utils.getBossDefinitionById(id);
            if (bossDefinition == null) {
                sender.sendMessage(ChatColor.RED + "指定されたボスが見つかりません。");
                return true;
            }
            bossDefinition.summonBoss(player.getLocation());
            sender.sendMessage(ChatColor.GREEN + "ボスを召喚しました。");
            return true;
        }
        String id = args[0];
        BossDefinition bossDefinition = Utils.getBossDefinitionById(id);
        if (bossDefinition == null) {
            sender.sendMessage(ChatColor.RED + "指定されたボスが見つかりません。");
            return true;
        }
        if (TypeUtil.isDouble(args[1]) && TypeUtil.isDouble(args[2]) && TypeUtil.isDouble(args[3])) {
            World world = Bukkit.getWorld(args[4]);
            if (world == null) {
                sender.sendMessage(ChatColor.RED + "指定されたワールドが見つかりません。");
                return true;
            }
            bossDefinition.summonBoss(new Location(world, Double.parseDouble(args[1]), Double.parseDouble(args[2]), Double.parseDouble(args[3])));
            sender.sendMessage(ChatColor.GREEN + "ボスを召喚しました。");
        }
        return true;
    }
}
