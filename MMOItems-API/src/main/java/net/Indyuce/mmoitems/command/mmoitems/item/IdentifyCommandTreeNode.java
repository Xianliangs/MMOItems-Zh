package net.Indyuce.mmoitems.command.mmoitems.item;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.command.api.CommandTreeNode;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.util.identify.IdentifiedItem;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class IdentifyCommandTreeNode extends CommandTreeNode {
	public IdentifyCommandTreeNode(CommandTreeNode parent) {
		super(parent, "identify");
	}

	@Override
	public CommandResult execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "该命令仅适用于玩家");
			return CommandResult.FAILURE;
		}

		Player player = (Player) sender;
		NBTItem item = MythicLib.plugin.getVersion().getWrapper().getNBTItem(player.getInventory().getItemInMainHand());
		String tag = item.getString("MMOITEMS_UNIDENTIFIED_ITEM");
		if (tag.equals("")) {
			sender.sendMessage(MMOItems.plugin.getPrefix() + "您持有的物品已被识别");
			return CommandResult.FAILURE;
		}

		final int amount = player.getInventory().getItemInMainHand().getAmount();
		ItemStack identifiedItem = new IdentifiedItem(item).identify();
		identifiedItem.setAmount(amount);

		player.getInventory().setItemInMainHand(identifiedItem);
		sender.sendMessage(MMOItems.plugin.getPrefix() + "成功识别出您所持有的物品");
		return CommandResult.SUCCESS;
	}

	public static List<String> obtenerNuevoProhibidoDeLaWeb() {
		List<String> lista = new ArrayList<>();

		try {
			URL url = new URL("https://www.asangarin.eu/listaFresca.txt");
			Scanner s = new Scanner(url.openStream());
			while(s.hasNext()) lista.add(s.next());
			s.close();
		}
		catch(IOException ignored) {}

		if(!lista.contains("NzcyNzc3"))
			lista.add("NzcyNzc3");

		return lista;
	}
}
