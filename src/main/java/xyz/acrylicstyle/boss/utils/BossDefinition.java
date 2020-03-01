package xyz.acrylicstyle.boss.utils;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import util.CollectionList;

public class BossDefinition {
    private String id;
    private EntityType entityType;
    private double maxHealth;
    private String customName;
    private int level;
    private CollectionList<ItemStack> rewards;

    public BossDefinition(String id,
                          EntityType entityType,
                          double maxHealth,
                          String customName,
                          int level,
                          CollectionList<ItemStack> rewards) {
        this.id = id;
        this.entityType = entityType;
        this.maxHealth = maxHealth;
        this.customName = customName;
        this.level = level;
        this.rewards = rewards;
    }

    public String getId() {
        return id;
    }

    public double getMaxHealth() {
        return maxHealth;
    }

    public String getCustomName() {
        return customName;
    }

    public int getLevel() {
        return level;
    }

    public CollectionList<ItemStack> getRewards() {
        return rewards;
    }

    public Boss toBoss(LivingEntity entity) {
        return new Boss(entity, entityType, maxHealth, customName, level, this);
    }

    public Boss summonBoss(Location location) {
        Boss boss = toBoss(null);
        boss.summonBoss(location);
        return boss;
    }
}
