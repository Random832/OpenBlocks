package openblocks.common.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import openblocks.OpenBlocks;
import openblocks.lib.utils.EnchantmentUtils;
import org.jetbrains.annotations.Nullable;

@EventBusSubscriber(modid = OpenBlocks.MODID)
public class OpenBlocksCommands {

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("openblocks")
                .then(Commands.literal("xp")
                        .then(Commands.literal("print")
                                .executes(c -> printExperienceCommand(c.getSource())))
                        .then(Commands.literal("set")
                                .then(Commands.argument("xp", IntegerArgumentType.integer())
                                        .executes(c -> setExperienceCommand(c.getSource(), IntegerArgumentType.getInteger(c, "xp")))))
                        .then(Commands.literal("add")
                                .then(Commands.argument("xp", IntegerArgumentType.integer())
                                        .executes(c -> addExperienceCommand(c.getSource(), IntegerArgumentType.getInteger(c, "xp")))))));
    }

    private static int notPlayerError(CommandSourceStack source) {
        Entity entity = source.getEntity();
        Component name = entity != null ? entity.getDisplayName() : Component.literal("null");
        if(name == null) name = Component.literal("[unknown " + entity.getType().getDescription() + "]");
        source.sendFailure(Component.literal("Entity ").append(name).append(" is not a player."));
        return 0;
    }

    private static int printExperienceCommand(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if(player == null) return notPlayerError(source);
        source.sendSuccess(() ->
                {
                    final Component name = player.getDisplayName();
                    return Component.literal("Player ")
                            .append(name != null ? name : Component.literal("[unknown]"))
                            .append(String.format(" has %f levels, %d total experience points.",
                                            EnchantmentUtils.getLevelAsFloat(player),
                                            EnchantmentUtils.getPlayerXP(player)));
                },
                false);
        return EnchantmentUtils.getPlayerXP(player);
    }

    private static int setExperienceCommand(CommandSourceStack source, int amount) {
        ServerPlayer player = source.getPlayer();
        if(player == null) return notPlayerError(source);
        player.experienceLevel = 0;
        player.experienceProgress = 0;
        EnchantmentUtils.addPlayerXP(player, amount);
        return amount;
    }

    private static int addExperienceCommand(CommandSourceStack source, int amount) {
        ServerPlayer player = source.getPlayer();
        if(player == null) return notPlayerError(source);
        EnchantmentUtils.addPlayerXP(player, amount);
        return amount;
    }

}
