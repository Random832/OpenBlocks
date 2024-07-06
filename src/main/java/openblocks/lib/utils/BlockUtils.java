package openblocks.lib.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public class BlockUtils {
    public static Direction get3dOrientation(LivingEntity entity, BlockPos pos) {
        if (Mth.abs((float) entity.getX() - pos.getX()) < 2.0F && Mth.abs((float) entity.getZ() - pos.getZ()) < 2.0F) {
            final double entityEyes = entity.getY() + entity.getEyeHeight();
            if (entityEyes - pos.getY() > 2.0D) return Direction.DOWN;
            if (pos.getY() - entityEyes > 0.0D) return Direction.UP;
        }

        return entity.getDirection();
    }

    public static void playSoundAtPos(Level level, BlockPos worldPosition, SoundEvent sound, SoundSource category, float volume, float pitch) {
        level.playSound(null, worldPosition, sound, category, volume, pitch);
    }

    public static AABB singleBlock(BlockPos pos) {
        return new AABB(pos);
    }

    public static AABB aabbOffset(BlockPos pos, double x1, double y1, double z1, double x2, double y2, double z2) {
        return new AABB(pos.getX() + x1, pos.getY() + y1, pos.getZ() + z1, pos.getX() + x2, pos.getY() + y2, pos.getZ() + z2);
    }

    public static boolean isAir(Level level, BlockPos pos) {
        return level.getBlockState(pos).isAir();
    }
}
