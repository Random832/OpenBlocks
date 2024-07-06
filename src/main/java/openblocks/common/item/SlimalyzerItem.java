package openblocks.common.item;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import openblocks.OpenBlocks;
import org.jetbrains.annotations.Nullable;

public class SlimalyzerItem extends Item {

    private static final String TAG_ACTIVE = "active";

    public SlimalyzerItem(Properties properties) {
        super(properties);
    }
    public static boolean isActive(ItemStack stack) {
        return stack.getOrDefault(OpenBlocks.ACTIVE_COMPONENT, false);
    }

    private static boolean isInSlimeChunk(@Nullable ServerLevel world, @Nullable Entity entity) {
        if (world == null || entity == null || world.dimension() != Level.OVERWORLD) return false;
        //Reference Slime#checkSlimeSpawnRules
        ChunkPos chunkpos = new ChunkPos(entity.blockPosition());
        return WorldgenRandom.seedSlimeChunk(chunkpos.x, chunkpos.z, world.getSeed(), 987234911L).nextInt(10) == 0;
    }

    private static boolean update(ItemStack stack, ServerLevel world, Entity entity) {
        final boolean isActive = isActive(stack);
        final boolean isInSlimeChunk = isInSlimeChunk(world, entity);
        if (isActive != isInSlimeChunk) {
            if (isInSlimeChunk && entity instanceof ServerPlayer serverPlayer) {
                serverPlayer.playNotifySound(OpenBlocks.SOUND_SLIMALYZER_SIGNAL.get(), SoundSource.PLAYERS, 1F, 1F);
            }
            stack.set(OpenBlocks.ACTIVE_COMPONENT, isInSlimeChunk);
            return true;
        }
        return false;
    }


    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean isSelected) {
        if (world instanceof ServerLevel serverLevel)
            update(stack, serverLevel, entity);
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entityItem) {
        if (entityItem.level() instanceof ServerLevel serverLevel) {
            if (update(stack, serverLevel, entityItem)) entityItem.setItem(stack);
        }
        return super.onEntityItemUpdate(stack, entityItem);
    }
}
