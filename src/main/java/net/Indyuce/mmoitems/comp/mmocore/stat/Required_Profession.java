package net.Indyuce.mmoitems.comp.mmocore.stat;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;

import net.Indyuce.mmocore.api.experience.Profession;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import net.Indyuce.mmoitems.api.util.message.AddonMessage;
import net.Indyuce.mmoitems.comp.mmocore.MMOCoreHook;
import net.Indyuce.mmoitems.stat.type.DoubleStat;
import net.Indyuce.mmoitems.stat.type.GemStoneStat;
import net.Indyuce.mmoitems.stat.type.ItemRestriction;
import io.lumine.mythic.lib.api.item.NBTItem;

public class Required_Profession extends DoubleStat implements ItemRestriction, GemStoneStat {
    private final Profession profession;

    public Required_Profession(Profession profession) {
        super("PROFESSION_" + profession.getId().toUpperCase().replace("-", "_"), Material.PINK_DYE, profession.getName() + " Requirement (MMOCore)", new String[] { "Amount of " + profession.getName() + " levels the", "player needs to use the item." }, new String[] { "!block", "all" });
        this.profession = profession;
    }

    @Override
    public boolean canUse(RPGPlayer player, NBTItem item, boolean message) {
        MMOCoreHook.MMOCoreRPGPlayer mmocore = (MMOCoreHook.MMOCoreRPGPlayer) player;
        if (mmocore.getData().getCollectionSkills().getLevel(this.profession) < item.getStat(getId())) {
            if (message) {
                new AddonMessage("not-enough-profession").format(ChatColor.RED, "#profession#", profession.getName()).send(player.getPlayer(), "cant-use-item");
                player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1.5f);
            }
            return false;
        }
        return true;
    }
}
