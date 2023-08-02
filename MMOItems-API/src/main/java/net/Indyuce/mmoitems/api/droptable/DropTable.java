package net.Indyuce.mmoitems.api.droptable;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.droptable.item.BlockDropItem;
import net.Indyuce.mmoitems.api.droptable.item.DropItem;
import net.Indyuce.mmoitems.api.droptable.item.MMOItemDropItem;
import net.Indyuce.mmoitems.api.player.PlayerData;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.*;
import java.util.logging.Level;

public class DropTable {
	private final List<String> subtablesList = new ArrayList<>();
	private final Map<String, Subtable> subtables = new HashMap<>();

	private static final Random random = new Random();

	public DropTable(ConfigurationSection config) {
		Validate.notNull(config, "无法读取掉落物配置");
		for (String key : config.getKeys(false))
			try {

				// Add subtable to list & then to map
				for (int j = 0; j < config.getInt(key + ".coef"); j++)
					subtablesList.add(key);

				// Include parsed subtable
				subtables.put(key, new Subtable(config.getConfigurationSection(key)));

			} catch (IllegalArgumentException exception) {
				MMOItems.plugin.getLogger().log(Level.WARNING, "无法读取子表 '" + key + "' 来自掉落物表 '" + config.getName() + "': " + exception.getMessage());
			}

		Validate.notEmpty(subtablesList, "您的 droptable 必须至少包含一个子表");
	}

	public String getRandomSubtable() {
		return subtablesList.get(random.nextInt(subtablesList.size()));
	}

	public List<ItemStack> read(@Nullable PlayerData player, boolean silkTouch) {
		List<ItemStack> dropped = new ArrayList<>();

		String randomSubtable = getRandomSubtable();
		for (DropItem dropItem : getSubtable(randomSubtable).getDropItems(silkTouch))
			if (dropItem.rollDrop()) {
				ItemStack drop = dropItem.getItem(player);
				if (drop == null)
					MMOItems.plugin.getLogger().log(Level.WARNING, "无法读取子表项 " + dropItem.getKey());
				else
					dropped.add(drop);
			}

		return dropped;
	}

	public Collection<Subtable> getSubtables() {
		return subtables.values();
	}

	public Subtable getSubtable(String key) {
		return subtables.get(key);
	}

	public static class Subtable {
		private final List<DropItem> items = new ArrayList<>();

		/**
		 * Options to prevent players from abusing drop tables.
		 */
		private final boolean disableSilkTouch;

		public Subtable(ConfigurationSection subtable) {
			Validate.notNull(subtable, "无法读取子表配置");

			Validate.isTrue(subtable.contains("coef"), "无法读取子表系数。");
			Validate.isTrue(subtable.contains("items") || subtable.contains("blocks"), "找不到 物品/方块 列表");

			if (subtable.contains("items"))
				for (String typeFormat : subtable.getConfigurationSection("items").getKeys(false)) {
					Type type = MMOItems.plugin.getTypes().getOrThrow(typeFormat.toUpperCase().replace("-", "_"));
					for (String id : subtable.getConfigurationSection("items." + typeFormat).getKeys(false))
						items.add(new MMOItemDropItem(type, id, subtable.getString("items." + typeFormat + "." + id)));
				}

			if (subtable.contains("blocks"))
				for (String idFormat : subtable.getConfigurationSection("blocks").getKeys(false)) {
					int id = Integer.parseInt(idFormat);
					Validate.isTrue(id > 0 && id != 54 && id <= 160, id + " 不是有效的块 ID");
					items.add(new BlockDropItem(id, subtable.getString("blocks." + idFormat)));
				}

			disableSilkTouch = subtable.getBoolean("disable-silk-touch");
		}

		public List<DropItem> getDropItems(boolean silkTouch) {
			return silkTouch && disableSilkTouch ? new ArrayList<>() : items;
		}
	}
}
