package xyz.acrylicstyle.boss.tabCompleter;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import util.ICollectionList;
import xyz.acrylicstyle.boss.api.utils.BossDefinitionAPI;
import xyz.acrylicstyle.boss.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SummonBossTC implements TabCompleter {
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> emptyList = new ArrayList<>();
        if (args.length == 0) return Utils.getBossDefinitionsCached().map(BossDefinitionAPI::getId);
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (args.length == 1)
                return Utils.filterArgsList(Utils.getBossDefinitionsCached().map(BossDefinitionAPI::getId), args[0]);
            if (args.length == 2)
                return Utils.filterArgsList(Arrays.asList(Double.toString(player.getLocation().getX()), args[1]), args[1]);
            if (args.length == 3)
                return Utils.filterArgsList(Arrays.asList(Double.toString(player.getLocation().getY()), args[2]), args[2]);
            if (args.length == 4)
                return Utils.filterArgsList(Arrays.asList(Double.toString(player.getLocation().getZ()), args[3]), args[3]);
        } else if (sender instanceof BlockCommandSender) {
            BlockCommandSender commandBlock = (BlockCommandSender) sender;
            if (args.length == 1)
                return Utils.filterArgsList(Utils.getBossDefinitionsCached().map(BossDefinitionAPI::getId), args[0]);
            if (args.length == 2)
                return Utils.filterArgsList(Arrays.asList(Double.toString(commandBlock.getBlock().getLocation().getX()), args[1]), args[1]);
            if (args.length == 3)
                return Utils.filterArgsList(Arrays.asList(Double.toString(commandBlock.getBlock().getLocation().getX()), args[2]), args[2]);
            if (args.length == 4)
                return Utils.filterArgsList(Arrays.asList(Double.toString(commandBlock.getBlock().getLocation().getX()), args[3]), args[3]);
        }
        if (args.length == 5) return Utils.filterArgsList(ICollectionList.asList(Bukkit.getWorlds()).map(World::getName), args[4]);
        return emptyList;
    }
}
