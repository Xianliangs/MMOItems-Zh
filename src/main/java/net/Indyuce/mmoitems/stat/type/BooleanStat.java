package net.Indyuce.mmoitems.stat.type;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.BooleanData;
import net.Indyuce.mmoitems.stat.data.random.RandomBooleanData;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.util.AltChar;

public class BooleanStat extends ItemStat {
	private static final DecimalFormat digit = new DecimalFormat("0.#");

	public BooleanStat(String id, Material mat, String name, String[] lore, String[] types, Material... materials) {
		super(id, mat, name, lore, types, materials);
	}

	@Override
	public RandomStatData whenInitialized(Object object) {

		if (object instanceof Boolean)
			return new RandomBooleanData((boolean) object);

		if (object instanceof Number)
			return new RandomBooleanData(Double.parseDouble(object.toString()));

		throw new IllegalArgumentException("Must specify a number (chance) or true/false");
	}

	@Override
	public void whenApplied(ItemStackBuilder item, StatData data) {
		if (((BooleanData) data).isEnabled()) {
			item.addItemTag(new ItemTag(getNBTPath(), true));
			item.getLore().insert(getPath(), MMOItems.plugin.getLanguage().getStatFormat(getPath()));
		}
	}

	@Override
	public void whenClicked(EditionInventory inv, InventoryClickEvent event) {
		if (event.getAction() == InventoryAction.PICKUP_ALL) {
			inv.getEditedSection().set(getPath(), inv.getEditedSection().getBoolean(getPath()) ? null : true);
			inv.registerTemplateEdition();
		}

		else if (event.getAction() == InventoryAction.PICKUP_HALF)
			new StatEdition(inv, this).enable("Write in the chat the probability you want (a percentage)");
	}

	@Override
	public void whenInput(EditionInventory inv, String message, Object... info) {

		double probability = MMOUtils.parseDouble(message);
		Validate.isTrue(probability >= 0 && probability <= 100, "Chance must be between 0 and 100");

		inv.getEditedSection().set(getPath(), probability / 100);
		inv.registerTemplateEdition();
		inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + getName() + " successfully changed to " + ChatColor.GREEN
				+ digit.format(probability) + "% Chance" + ChatColor.GRAY + ".");
	}

	@Override
	public void whenLoaded(ReadMMOItem mmoitem) {
		if (mmoitem.getNBT().hasTag(getNBTPath()))
			mmoitem.setData(this, new BooleanData(mmoitem.getNBT().getBoolean(getNBTPath())));
	}

	@Override
	public void whenDisplayed(List<String> lore, Optional<RandomStatData> statData) {

		if (statData.isPresent()) {
			double chance = ((RandomBooleanData) statData.get()).getChance();
			lore.add(ChatColor.GRAY + "Current Value: " + (chance >= 1 ? ChatColor.GREEN + "True"
					: chance <= 0 ? ChatColor.RED + "False" : ChatColor.GREEN + digit.format(chance * 100) + "% Chance"));

		} else
			lore.add(ChatColor.GRAY + "Current Value: " + ChatColor.RED + "False");

		lore.add("");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Left click to switch this value.");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to choose a probability to have this option.");
	}
}
