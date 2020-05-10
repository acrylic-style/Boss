package xyz.acrylicstyle.boss;

import com.google.common.util.concurrent.AtomicDouble;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import util.Collection;
import util.CollectionList;
import util.ICollectionList;
import xyz.acrylicstyle.boss.api.BossPluginAPI;
import xyz.acrylicstyle.boss.api.events.BossDamageByEntityEvent;
import xyz.acrylicstyle.boss.api.events.BossDamageEvent;
import xyz.acrylicstyle.boss.api.events.BossDeathEvent;
import xyz.acrylicstyle.boss.api.utils.BossAPI;
import xyz.acrylicstyle.boss.api.utils.BossDefinitionAPI;
import xyz.acrylicstyle.boss.commands.ReloadBoss;
import xyz.acrylicstyle.boss.commands.SummonBoss;
import xyz.acrylicstyle.boss.commands.TeleportBoss;
import xyz.acrylicstyle.boss.tabCompleter.SummonBossTC;
import xyz.acrylicstyle.boss.utils.Boss;
import xyz.acrylicstyle.boss.utils.Utils;
import xyz.acrylicstyle.tomeito_api.utils.Log;

import java.util.*;

public class BossPlugin extends JavaPlugin implements Listener, BossPluginAPI {
    private static BossPlugin plugin;
    public static Collection<UUID, Boss> boss = new Collection<>();

    @Override
    public void onLoad() {
        plugin = this;
    }

    @Override
    public void onEnable() {
        Bukkit.getServicesManager().register(BossPluginAPI.class, this, this, ServicePriority.Normal);
        Bukkit.getPluginManager().registerEvents(this, this);
        if (Bukkit.getPluginCommand("summonboss") == null) throw new NullPointerException("summonboss cannot be null");
        //noinspection ConstantConditions
        Bukkit.getPluginCommand("summonboss").setExecutor(new SummonBoss());
        //noinspection ConstantConditions
        Bukkit.getPluginCommand("summonboss").setTabCompleter(new SummonBossTC());
        Objects.requireNonNull(Bukkit.getPluginCommand("reloadboss")).setExecutor(new ReloadBoss());
        Objects.requireNonNull(Bukkit.getPluginCommand("teleportboss")).setExecutor(new TeleportBoss());
        Log.info("Enabled Boss");
    }

    @Override
    public void onDisable() {
        boss.forEach((uuid, b) -> {
            Log.info("Removing boss entity " + uuid + " as plugin is disabling");
            assert b.getBossEntity() != null;
            b.getBossEntity().remove();
        });
    }

    @EventHandler
    public void onWorldUnload(WorldUnloadEvent e) {
        boss.forEach((uuid, b) -> {
            assert b.getBossEntity() != null;
            if (!b.getBossEntity().getLocation().isWorldLoaded()) {
                Log.info("Removing boss entity " + uuid + " as world unloaded");
                b.getBossEntity().remove();
            }
            if (Objects.requireNonNull(b.getBossEntity().getLocation().getWorld()).getName().equals(e.getWorld().getName())) {
                Log.info("Removing boss entity " + uuid + " as world is unloading");
                b.getBossEntity().remove();
            }
        });
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {
        if (boss.containsKey(e.getEntity().getUniqueId())) {
            Bukkit.getPluginManager().callEvent(new BossDeathEvent(boss.get(e.getEntity().getUniqueId())));
            e.getDrops().clear();
            Boss boss2 = boss.get(e.getEntity().getUniqueId());
            boss2.setHealth(0);
            Bukkit.broadcastMessage("" + ChatColor.GREEN + ChatColor.BOLD + "----------------------------------------");
            Bukkit.broadcastMessage("");
            Bukkit.broadcastMessage(ChatColor.YELLOW + "ボス " + boss2.customName + ChatColor.YELLOW + " が倒されました！");
            List<Map.Entry<UUID, AtomicDouble>> list = boss2.participants.toEntryList();
            list.sort(Comparator.comparingDouble(entry -> entry.getValue().get()));
            Bukkit.broadcastMessage("");
            Bukkit.broadcastMessage(ChatColor.GOLD + "ダメージ数ランキング");
            Bukkit.broadcastMessage("");
            ICollectionList.asList(list).reverse().foreach((entry, index) -> {
                if ((index+1) <= 10) {
                    Bukkit.broadcastMessage("" + ChatColor.RED + (index+1) + ". " + ChatColor.YELLOW + Bukkit.getOfflinePlayer(entry.getKey()).getName() + ChatColor.WHITE + ": " + ChatColor.RED + String.format(Locale.US, "%,d", ((int) entry.getValue().get())));
                    Player player = Bukkit.getPlayer(entry.getKey());
                    if (player != null && boss2.getDefinition().getRewards() != null) {
                        HashMap<Integer, ItemStack> map = player.getInventory().addItem(boss2
                                .getDefinition()
                                .getRewards()
                                .map(item2 -> {
                                    ItemStack item = item2.clone();
                                    item.setAmount((int) ((float) item.getAmount() * (entry.getValue().get()/boss2.getMaxHealth())));
                                    return item;
                                })
                                .toArray(new ItemStack[0]));
                        if (map.size() != 0) {
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    player.sendMessage(ChatColor.RED + "持ち物に入りきらなかった討伐報酬はドロップされました。");
                                }
                            }.runTaskLater(this, 10);
                        }
                        map.forEach((i, item) -> player.getWorld().dropItemNaturally(player.getLocation(), item));
                    }
                }
            });
            Bukkit.broadcastMessage("");
            Bukkit.broadcastMessage("" + ChatColor.GREEN + ChatColor.BOLD + "----------------------------------------");
            Objects.requireNonNull(boss.remove(e.getEntity().getUniqueId()).getBossEntity()).remove();
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        double damage = e.getDamage();
        if (boss.containsKey(e.getEntity().getUniqueId()) && e.getDamager().getType() == EntityType.PLAYER) {
            Boss boss2 = boss.get(e.getEntity().getUniqueId());
            BossDamageByEntityEvent bossDamageByEntityEvent = new BossDamageByEntityEvent(boss2, damage);
            Bukkit.getPluginManager().callEvent(bossDamageByEntityEvent);
            if (bossDamageByEntityEvent.isCancelled()) {
                e.setCancelled(true);
                return;
            }
            damage = bossDamageByEntityEvent.getDamage();
            AtomicDouble atomicDouble = boss2.participants.getOrDefault(e.getDamager().getUniqueId(), new AtomicDouble());
            atomicDouble.addAndGet(damage);
            boss2.participants.add(e.getDamager().getUniqueId(), atomicDouble);
            boss2.decreaseHealth(bossDamageByEntityEvent.getFinalDamage() == 0 ? damage : bossDamageByEntityEvent.getFinalDamage());
            boss.add(e.getEntity().getUniqueId(), boss2);
            e.setDamage(0);
        } else if (boss.containsKey(e.getDamager().getUniqueId()) && e.getEntity().getType() == EntityType.PLAYER) {
            e.setDamage(boss.get(e.getDamager().getUniqueId()).getDamage(damage));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent e) {
        if (boss.containsKey(e.getEntity().getUniqueId())) {
            if (e.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK
                    && e.getCause() != EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK) {
                double damage = e.getDamage();
                BossDamageEvent bossDamageEvent = new BossDamageEvent(boss.get(e.getEntity().getUniqueId()), damage);
                Bukkit.getPluginManager().callEvent(bossDamageEvent);
                damage = bossDamageEvent.getDamage();
                if (bossDamageEvent.isCancelled()) {
                    e.setCancelled(true);
                    return;
                }
                boss.get(e.getEntity().getUniqueId()).decreaseHealth(damage);
                e.setDamage(0);
            }
        }
    }

    @EventHandler
    public void onEntityDamageByBlock(EntityDamageByBlockEvent e) {
        if (boss.containsKey(e.getEntity().getUniqueId())) {
            boss.get(e.getEntity().getUniqueId()).decreaseHealth(e.getDamage());
            e.setDamage(0);
        }
    }

    public static BossPlugin getInstance() {
        return plugin;
    }

    @Override
    public @NotNull CollectionList<String> getBossDefinitionFiles() {
        return Utils.getBossDefinitionFiles();
    }

    @Override
    public @NotNull CollectionList<BossDefinitionAPI> getBossDefinitions() {
        return Utils.getBossDefinitions();
    }

    @Override
    public @NotNull CollectionList<BossDefinitionAPI> getBossDefinitionsCached() {
        return Utils.getBossDefinitionsCached();
    }

    @Override
    public @Nullable BossDefinitionAPI getBossDefinitionById(String s) {
        return Utils.getBossDefinitionById(s);
    }

    @Override
    public @Nullable BossAPI getBossByUniqueId(UUID uuid) {
        return boss.get(uuid);
    }

    @Override
    public @NotNull Collection<UUID, BossAPI> getBosses() {
        return boss.map((k, v) -> k, (k, v) -> v);
    }
}
