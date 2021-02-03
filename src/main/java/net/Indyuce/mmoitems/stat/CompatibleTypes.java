package net.Indyuce.mmoitems.stat;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.StringListData;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.util.AltChar;
import io.lumine.mythic.lib.version.VersionMaterial;

public class CompatibleTypes extends ItemStat {
	public CompatibleTypes() {
		super("COMPATIBLE_TYPES", VersionMaterial.COMMAND_BLOCK.toMaterial(), "Compatible Types",
				new String[] { "The item types this skin is", "compatible with." }, new String[] { "skin" });
	}

	@Override
	@SuppressWarnings("unchecked")
	public StringListData whenInitialized(Object object) {
		Validate.isTrue(object instanceof List<?>, "Must specify a string list");
		return new StringListData((List<String>) object);
	}

	@Override
	public void whenClicked(EditionInventory inv, InventoryClickEvent event) {
		if (event.getAction() == InventoryAction.PICKUP_ALL)
			new StatEdition(inv, ItemStats.COMPATIBLE_TYPES).enable("Write in the chat the name of the type you want to add.");

		if (event.getAction() == InventoryAction.PICKUP_HALF) {
			if (inv.getEditedSection().contains("compatible-types")) {
				List<String> lore = inv.getEditedSection().getStringList("compatible-types");
				if (lore.size() < 1)
					return;

				String last = lore.get(lore.size() - 1);
				lore.remove(last);
				inv.getEditedSection().set("compatible-types", lore);
				inv.registerTemplateEdition();
				inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Successfully removed '" + last + "'.");
			}
		}
	}

	@Override
	public void whenInput(EditionInventory inv, String message, Object... info) {
		List<String> lore = inv.getEditedSection().contains("compatible-types") ? inv.getEditedSection().getStringList("compatible-types")
				: new ArrayList<>();
		lore.add(message.toUpperCase());
		inv.getEditedSection().set("compatible-types", lore);
		inv.registerTemplateEdition();
		inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Compatible Types successfully added.");
	}

	@Override
	public void whenDisplayed(List<String> lore, Optional<RandomStatData> statData) {

		if (statData.isPresent()) {
			lore.add(ChatColor.GRAY + "Current Value:");
			((StringListData) statData.get()).getList().forEach(str -> lore.add(ChatColor.GRAY + str));

		} else
			lore.add(ChatColor.GRAY + "Current Value: " + ChatColor.RED + "Compatible with any item.");

		lore.add("");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Click to add a new type.");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to remove the last type.");
	}

	@Override
	public void whenApplied(ItemStackBuilder item, StatData data) {
		List<String> compatibleTypes = new ArrayList<>();
		JsonArray array = new JsonArray();
		((StringListData) data).getList().forEach(line -> {
			array.add(line);
			compatibleTypes.add(line);
		});
		item.getLore().insert("compatible-types", compatibleTypes);
		item.addItemTag(new ItemTag("MMOITEMS_COMPATIBLE_TYPES", array.toString()));
	}

	@Override
	public void whenLoaded(ReadMMOItem mmoitem) {
		if (mmoitem.getNBT().hasTag("MMOITEMS_COMPATIBLE_TYPES"))
			mmoitem.setData(ItemStats.COMPATIBLE_TYPES,
					new StringListData(new JsonParser().parse(mmoitem.getNBT().getString("MMOITEMS_COMPATIBLE_TYPES")).getAsJsonArray()));
	}
}
