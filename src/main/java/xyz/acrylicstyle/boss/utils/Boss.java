package xyz.acrylicstyle.boss.utils;

import com.google.common.util.concurrent.AtomicDouble;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import util.Collection;
import xyz.acrylicstyle.boss.BossPlugin;
import xyz.acrylicstyle.boss.api.utils.BossAPI;
import xyz.acrylicstyle.boss.api.utils.BossDefinitionAPI;

import java.util.Objects;
import java.util.UUID;

public class Boss implements BossAPI {
    private double maxHealth;
    private double health;
    public String customName;
    private LivingEntity bossEntity;
    private EntityType entityType;
    private int level;
    public Collection<UUID, AtomicDouble> participants = new Collection<>();
    private BossDefinition definition;

    public Boss(@Nullable LivingEntity bossEntity, @NotNull EntityType entityType, double maxHealth, String customName, int level, BossDefinition definition) {
        this.maxHealth = maxHealth;
        this.health = maxHealth;
        this.bossEntity = bossEntity;
        this.entityType = entityType;
        this.customName = ChatColor.translateAlternateColorCodes('&', customName);
        this.level = level;
        this.definition = definition;
        if (this.bossEntity != null) setBossEntity();
    }

    @NotNull
    public BossDefinitionAPI getDefinition() { return definition; }

    public void summonBoss(Location location) {
        if (this.bossEntity != null) throw new IllegalArgumentException("Boss has already summoned!");
        this.bossEntity = (LivingEntity) Objects.requireNonNull(location.getWorld()).spawnEntity(location, this.entityType);
        Bukkit.broadcastMessage(ChatColor.YELLOW + "ボス " + customName + ChatColor.YELLOW + " がワールド "
                + ChatColor.RED + location.getWorld().getName() + ChatColor.YELLOW + ", 座標: "
                + ChatColor.RED + location.getBlockX() + ChatColor.YELLOW + ", "
                + ChatColor.RED + location.getBlockY() + ChatColor.YELLOW + ", "
                + ChatColor.RED + location.getBlockZ() + ChatColor.YELLOW + "にスポーンしました！");
        TextComponent text = new TextComponent(ChatColor.GREEN + "ここをクリックするとテレポートします！");
        text.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/teleportboss " + bossEntity.getUniqueId()));
        Bukkit.spigot().broadcast(text);
        Bukkit.getWorlds().forEach(world -> world.playSound(location, Sound.ENTITY_WITHER_SPAWN, Float.MAX_VALUE, 1));
        setBossEntity();
    }

    private void setBossEntity() {
        this.bossEntity.setCustomName(this.customName);
        this.bossEntity.setCustomNameVisible(true);
        this.bossEntity.setHealth(20);
        this.bossEntity.setRemoveWhenFarAway(false);
        if (this.bossEntity.getEquipment() != null) {
            this.bossEntity.getEquipment().setHelmet(new ItemStack(Material.OAK_BUTTON));
            this.bossEntity.getEquipment().setHelmetDropChance(0.0F);
        }
        if (this.bossEntity instanceof Zombie) ((Zombie) this.bossEntity).setBaby(false);
        Objects.requireNonNull(this.bossEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH)).setBaseValue(100);
        BossPlugin.boss.add(this.bossEntity.getUniqueId(), this);
        applyHealth();
    }

    public void decreaseHealth(double health) {
        this.health = this.health - health;
        applyHealth();
    }

    public void setHealth(double health) {
        this.health = health;
        applyHealth();
    }

    public double getHealth() {
        return health;
    }

    public double getMaxHealth() {
        return maxHealth;
    }

    public LivingEntity getBossEntity() {
        return this.bossEntity;
    }

    private void applyHealth() {
        if (this.health <= 0) this.bossEntity.setHealth(0);
        this.bossEntity.setHealth(20 * (getHealth() / getMaxHealth()));
        this.bossEntity.setCustomName(ChatColor.YELLOW + this.customName + ChatColor.GREEN + " [" + ChatColor.GOLD + ((int) health) + ChatColor.YELLOW + "/" + ChatColor.GOLD + ((int) maxHealth) + ChatColor.GREEN + "] " + ChatColor.GRAY + "Lv" + level);
    }

    public double getDamage(double base) {
        return base + (level / base);
    }

    @Override
    public Collection<UUID, AtomicDouble> getParticipants() {
        return participants;
    }
}
