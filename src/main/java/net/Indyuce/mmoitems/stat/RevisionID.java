package net.Indyuce.mmoitems.stat;

import java.util.List;
import java.util.Optional;

import net.Indyuce.mmoitems.stat.type.GemStoneStat;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;

import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.api.util.NumericStatFormula;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.DoubleData;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.util.AltChar;

public class RevisionID extends ItemStat implements GemStoneStat {
	public RevisionID() {
		super("REVISION_ID", Material.ITEM_FRAME, "Revision ID", new String[] { "The Revision ID is used to determine",
		"if an item is outdated or not. You", "should increase this whenever", "you make changes to your item!"},
				new String[] { "all" });
	}

	@Override
	public RandomStatData whenInitialized(Object object) {
		if (object instanceof Integer)
			return new NumericStatFormula((Integer) object, 0, 0, 0);

		throw new IllegalArgumentException("Must specify a whole number");
	}

	@Override
	public void whenApplied(ItemStackBuilder item, StatData data) {
		item.addItemTag(new ItemTag("MMOITEMS_REVISION_ID", (int) ((DoubleData) data).getValue()));
	}

	@Override
	public void whenClicked(EditionInventory inv, InventoryClickEvent event) {
		int id = inv.getEditedSection().getInt(getPath(), 1);
		if (event.getAction() == InventoryAction.PICKUP_HALF) {
			inv.getEditedSection().set(getPath(), Math.max(id - 1, 1));
			inv.registerTemplateEdition();
			return;
		}

		inv.getEditedSection().set(getPath(), Math.min(id + 1, Integer.MAX_VALUE));
		inv.registerTemplateEdition();
	}

	@Override
	public void whenInput(EditionInventory inv, String message, Object... info) {}

	@Override
	public void whenLoaded(ReadMMOItem mmoitem) {
		if (mmoitem.getNBT().hasTag("MMOITEMS_REVISION_ID"))
			mmoitem.setData(this, new DoubleData(mmoitem.getNBT().getInteger("MMOITEMS_REVISION_ID")));
	}

	@Override
	public void whenDisplayed(List<String> lore, Optional<RandomStatData> statData) {
		if (statData.isPresent()) {
			NumericStatFormula data = (NumericStatFormula) statData.get();
			lore.add(ChatColor.GRAY + "Current Revision ID: " + ChatColor.GREEN + ((int) data.getBase()));
		} else
			lore.add(ChatColor.GRAY + "Current Revision ID: " + ChatColor.GREEN + "1");

		lore.add("");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Left click to increase this value.");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to decrease this value.");
	}
}
