package openblocks.advancements;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import openblocks.OpenBlocks;

import java.util.function.Supplier;

public class Criterions {
    public static final DeferredRegister<CriterionTrigger<?>> REGISTRY = DeferredRegister.create(Registries.TRIGGER_TYPE, OpenBlocks.MODID);

    //DeferredHolder<CriterionTrigger<?>, PlayerTrigger> DEV_NULL_STACK = register("dev_null_stacked", PlayerTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, PlayerTrigger> BRICK_DROPPED = register("brick_dropped", PlayerTrigger::new);

    static private <T extends CriterionTrigger<?>> DeferredHolder<CriterionTrigger<?>, T> register(String id, Supplier<T> make) {
        return REGISTRY.register(id, make);
    }
}