package net.Indyuce.mmoitems.api.interaction;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.item.LiveMMOItem;
import net.Indyuce.mmoitems.api.item.MMOItem;
import net.Indyuce.mmoitems.api.util.message.Message;
import net.Indyuce.mmoitems.stat.data.GemSocketsData;
import net.Indyuce.mmoitems.stat.data.GemstoneData;
import net.Indyuce.mmoitems.stat.data.type.Mergeable;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.Indyuce.mmoitems.stat.type.ProperStat;
import net.mmogroup.mmolib.api.item.NBTItem;

public class GemStone extends UseItem {

	public GemStone(Player player, NBTItem item, Type type) {
		super(player, item, type);
	}

	public ApplyResult applyOntoItem(NBTItem target, Type targetType) {

		/*
		 * loads all stats and calculates EVERY piece of the lore again.
		 */
		MMOItem targetMMO = new LiveMMOItem(target);
		if (!targetMMO.hasData(ItemStat.GEM_SOCKETS))
			return new ApplyResult(ResultType.NONE);

		String gemType = getNBTItem().getString("MMOITEMS_GEM_COLOR");

		GemSocketsData sockets = (GemSocketsData) targetMMO.getData(ItemStat.GEM_SOCKETS);
		if (!sockets.canReceive(gemType))
			return new ApplyResult(ResultType.NONE);

		/*
		 * checks if the gem supports the item type, or the item set, or a
		 * weapon
		 */
		String appliableTypes = getNBTItem().getString("MMOITEMS_ITEM_TYPE_RESTRICTION");
		if (!appliableTypes.equals("") && (!targetType.isWeapon() || !appliableTypes.contains("WEAPON"))
				&& !appliableTypes.contains(targetType.getItemSet().name()) && !appliableTypes.contains(targetType.getId()))
			return new ApplyResult(ResultType.NONE);

		// check for success rate
		double successRate = getNBTItem().getStat(ItemStat.SUCCESS_RATE);
		if (successRate != 0)
			if (random.nextDouble() < 1 - successRate / 100) {
				player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
				Message.GEM_STONE_BROKE
						.format(ChatColor.RED, "#gem#", MMOUtils.getDisplayName(getItem()), "#item#", MMOUtils.getDisplayName(target.getItem()))
						.send(player);
				return new ApplyResult(ResultType.FAILURE);
			}

		/*
		 * gem stone can be successfully applied. apply stats then abilities and
		 * permanent effects. also REGISTER gem stone in the item gem stone
		 * list.
		 */
		GemstoneData gemData = new GemstoneData(mmoitem);
		sockets.apply(gemType, gemData);

		/*
		 * only applies NON PROPER and MERGEABLE item stats
		 */
		for (ItemStat stat : mmoitem.getStats())
			if (!(stat instanceof ProperStat)) {
				StatData data = mmoitem.getData(stat);
				if (data instanceof Mergeable) {
					if (targetMMO.hasData(stat))
						((Mergeable) targetMMO.getData(stat)).merge(mmoitem.getData(stat));
					else
						targetMMO.setData(stat, mmoitem.getData(stat));
				}
			}

		player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 2);
		Message.GEM_STONE_APPLIED
				.format(ChatColor.YELLOW, "#gem#", MMOUtils.getDisplayName(getItem()), "#item#", MMOUtils.getDisplayName(target.getItem()))
				.send(player);

		return new ApplyResult(targetMMO.newBuilder().build());
	}

	public class ApplyResult {
		private final ResultType type;
		private final ItemStack result;

		public ApplyResult(ResultType type) {
			this(null, type);
		}

		public ApplyResult(ItemStack result) {
			this(result, ResultType.SUCCESS);
		}

		public ApplyResult(ItemStack result, ResultType type) {
			this.type = type;
			this.result = result;
		}

		public ResultType getType() {
			return type;
		}

		public ItemStack getResult() {
			return result;
		}
	}

	public enum ResultType {

		/*
		 * when the gem stone is not successfully applied onto the item and when
		 * it needs to be destroyed
		 */
		FAILURE,

		/*
		 * when a gem stone, for some reason, cannot be applied onto an item (if
		 * it has no more empty gem socket), but when the gem must not be
		 * destroyed
		 */
		NONE,

		/*
		 * when a gem stone is successfully applied onto an item without any
		 * error
		 */
		SUCCESS;
	}
}
