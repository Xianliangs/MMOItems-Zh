package net.Indyuce.mmoitems.command.mmoitems;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.util.SmartGive;
import io.lumine.mythic.lib.command.api.CommandTreeNode;
import io.lumine.mythic.lib.command.api.Parameter;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ItemTier;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.api.item.template.explorer.*;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Optional;
import java.util.Random;

public class GenerateCommandTreeNode extends CommandTreeNode {
    private static final Random random = new Random();

    public GenerateCommandTreeNode(CommandTreeNode parent) {
        super(parent, "generate");

        addParameter(Parameter.PLAYER);
        addParameter(new Parameter("(extra-args)", (explorer, list) -> list
                .addAll(Arrays.asList("-matchlevel", "-matchclass", "-level:", "-class:", "-type:", "-id:", "-tier:", "-tierset:", "-gimme"))));
    }

    @Override
    public CommandResult execute(CommandSender sender, String[] args) {
        try {
            if (args.length < 2) return CommandResult.THROW_USAGE;
            final Player target = Bukkit.getPlayer(args[1]);
            Validate.notNull(target, "找不到名为 " + args[1] + " 的玩家");

            GenerateCommandHandler handler = new GenerateCommandHandler(args);

            final Player give = handler.hasArgument("gimme") || handler.hasArgument("giveme") ? (sender instanceof Player ? (Player) sender : null)
                    : target;
            Validate.notNull(give, "不能使用 -gimme 参数");

            final RPGPlayer rpgPlayer = PlayerData.get(target).getRPG();
            final int itemLevel = handler.hasArgument("level:") ? Integer.parseInt(handler.getValue("level"))
                    : (handler.hasArgument("matchlevel") ? MMOItems.plugin.getTemplates().rollLevel(rpgPlayer.getLevel()) : 1 + random.nextInt(100));
            final @Nullable ItemTier itemTier = handler.hasArgument("tierset") ? null : handler.hasArgument("tier:")
                    ? MMOItems.plugin.getTiers().getOrThrow(handler.getValue("tier").toUpperCase().replace("-", "_"))
                    : MMOItems.plugin.getTemplates().rollTier();

            final TemplateExplorer builder = new TemplateExplorer();
            if (handler.hasArgument("matchclass"))
                builder.applyFilter(new ClassFilter(rpgPlayer));
            if (handler.hasArgument("class:"))
                builder.applyFilter(new ClassFilter(handler.getValue("class").replace("-", " ").replace("_", " ")));
            String type = null;
            if (handler.hasArgument("tierset:")) {
                String format = UtilityMethods.enumName(handler.getValue("tierset"));
                Validate.isTrue(MMOItems.plugin.getTiers().has(format), "找不到 ID 为 '" + format + "' 的层");
                builder.applyFilter(new TierFilter(format));
            }
            if (handler.hasArgument("type:")) {
                type = handler.getValue("type");
                Validate.isTrue(Type.isValid(type), "找不到 ID为 '" + type + "' 的类型");
                builder.applyFilter(new TypeFilter(Type.get(type)));
            }
            if (handler.hasArgument("id:")) {
                Validate.isTrue(type != null, "如果使用 id 选项, 您必须指定类型!");
                builder.applyFilter(new IDFilter(handler.getValue("id")));
            }

            Optional<MMOItemTemplate> optional = builder.rollLoot();
            Validate.isTrue(optional.isPresent(), "没有符合您条件的物品");

            ItemStack item = optional.get().newBuilder(itemLevel, itemTier).build().newBuilder().build();
            Validate.isTrue(item != null && item.getType() != Material.AIR, "无法生成 ID 为 '" + optional.get().getId() + "' 的物品");
            new SmartGive(give).give(item);
            return CommandResult.SUCCESS;

        } catch (IllegalArgumentException exception) {
            sender.sendMessage(ChatColor.RED + exception.getMessage());
            return CommandResult.FAILURE;
        }
    }
}
