package xyz.acrylicstyle.boss.utils;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import util.Collection;
import util.CollectionList;
import util.ICollection;
import util.ICollectionList;
import xyz.acrylicstyle.boss.api.utils.BossDefinitionAPI;
import xyz.acrylicstyle.tomeito_core.providers.ConfigProvider;
import xyz.acrylicstyle.tomeito_core.utils.Log;

import java.io.File;
import java.util.List;
import java.util.Map;

public final class Utils {
    private Utils() {}

    public static CollectionList<String> getBossDefinitionFiles() {
        File itemsDir = new File("./plugins/Boss/defs/");
        String[] itemsArray = itemsDir.list();
        if (itemsArray == null) return new CollectionList<>();
        return ICollectionList.asList(itemsArray);
    }

    public static CollectionList<BossDefinitionAPI> bossDefinitions = null;

    public static CollectionList<BossDefinitionAPI> getBossDefinitions() {
        ICollectionList<String> files = getBossDefinitionFiles();
        return files.map(file -> {
            Log.debug("Processing file: " + file);
            ConfigProvider config;
            try {
                config = new ConfigProvider("./plugins/Boss/defs/" + file);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            String id = config.getString("id");
            if (id == null) throw new NullPointerException("Item ID must be specified.");
            double maxHealth = config.getDouble("maxHealth", 100);
            String customName = config.getString("customName");
            int level = config.getInt("level", 1);
            CollectionList<ItemStack> rewards = null;
            if (config.get("reward") != null) {
                rewards = new CollectionList<>();
                CollectionList<ItemStack> finalRewards = rewards;
                ICollectionList.asList(config.getMapList("reward"))
                        .map(map -> ICollection.asCollection(map).map((k, v) -> (String) k, (k, v) -> (Object) v))
                        .forEach(map -> {
                            Material rewardType = Material.valueOf((String) map.getOrDefault("material", "STONE"));
                            int rewardAmount = (int) map.getOrDefault("amount", 1);
                            @SuppressWarnings("unchecked") Map<String, Object> enchantmentsRaw = (Map<String, Object>) map.get("enchantments");
                            Collection<Enchantment, Integer> enchantments = enchantmentsRaw == null ? null : ICollection.asCollection(enchantmentsRaw)
                                    .map((s, o) -> Enchantment.getByKey(NamespacedKey.minecraft(s)), (s, o) -> (int) o);
                            ItemStack reward = new ItemStack(rewardType, rewardAmount);
                            ItemMeta meta = reward.getItemMeta();
                            assert meta != null;
                            if (enchantments != null) enchantments.forEach((ench, eLevel) -> meta.addEnchant(ench, eLevel, true));
                            reward.setItemMeta(meta);
                            finalRewards.add(reward);
                        });
                rewards = finalRewards;
            }
            return new BossDefinition(id,
                    EntityType.valueOf(config.getString("entityType", "ZOMBIE")),
                    maxHealth,
                    customName,
                    level,
                    rewards);
        });
    }

    public static CollectionList<BossDefinitionAPI> getBossDefinitionsCached() {
        if (bossDefinitions == null) bossDefinitions = Utils.getBossDefinitions();
        return bossDefinitions;
    }

    public static BossDefinitionAPI getBossDefinitionById(String id) {
        try {
            return getBossDefinitionsCached().filter(b -> b.getId().equals(id)).first();
        } catch (IndexOutOfBoundsException ignored) {
            return null;
        }
    }

    public static CollectionList<String> filterArgsList(CollectionList<String> list, String s) {
        return list.filter(s2 -> s2.toLowerCase().replaceAll(".*:(.*)", "$1").startsWith(s.toLowerCase().replaceAll(".*:(.*)", "$1")));
    }

    public static CollectionList<String> filterArgsList(List<String> list, String s) {
        return filterArgsList(ICollectionList.asList(list), s);
    }
}
