package openblocks.rubbish;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;
import openblocks.OpenBlocks;
import openblocks.advancements.Criterions;
import openblocks.events.GuideActionEvent;
import openblocks.events.PlayerActionEvent;

public class BrickManager {
    public static class BowelContents {
        public static final Codec<BowelContents> CODEC = RecordCodecBuilder.<BowelContents>mapCodec(
                instance -> instance.group(
                        Codec.INT.fieldOf("brick_count").forGetter(s -> s.brickCount)
                ).apply(instance, BowelContents::new)
        ).codec();

        private BowelContents(int brickCount) {
            this.brickCount = brickCount;
        }

        public BowelContents() {
            this.brickCount = 0;
        }

        public int brickCount;
    }

    public static BowelContents getProperty(Entity entity) {
        return entity.getData(OpenBlocks.BRICK_ATTACHMENT);
    }

    @SubscribeEvent
    public void onEntityDeath(LivingDropsEvent evt) {
        if (evt.getEntity().level().isClientSide) return;

        BowelContents tag = getProperty(evt.getEntity());

        for (int i = 0; i < Math.min(tag.brickCount, 16); i++) {
            ItemEntity entityItem = createBrick(evt.getEntity());
            evt.getDrops().add(entityItem);
        }
    }

    private static boolean tryDecrementBrick(Player player) {
        if (player.getAbilities().instabuild) return true;

        BowelContents tag = getProperty(player);
        if (tag.brickCount > 0) {
            tag.brickCount--;
            return true;
        }

        return false;
    }

    public static void onPlayerScared(PlayerActionEvent evt, IPayloadContext context) {
        if(evt.actionType() == PlayerActionEvent.ActionType.BOO && context.player() instanceof ServerPlayer player) {
            player.level().playSound(null, player.blockPosition(), OpenBlocks.SOUND_BEST_FEATURE_EVER_FART.get(), SoundSource.PLAYERS, 1, 1);
            if (tryDecrementBrick(player)) {
                ItemEntity drop = createBrick(player);
                drop.setDefaultPickUpDelay();
                player.level().addFreshEntity(drop);
                Criterions.BRICK_DROPPED.get().trigger(player);
                //player.awardStat(, 1)
            }
        }
    }

    private static ItemEntity createBrick(Entity dropper) {
        ItemStack brick = new ItemStack(Items.BRICK);
        ItemEntity drop = new ItemEntity(dropper.level(), dropper.getX(), dropper.getY(), dropper.getZ(), brick);
        double rotation = Math.toRadians(dropper.getYRot()) - Math.PI / 2;
        double dx = Math.cos(rotation);
        double dz = Math.sin(rotation);
        drop.move(MoverType.SELF, new Vec3(0.75 * dx, 0.5, 0.75 * dz));
        drop.setDeltaMovement(0.5 * dx, 0.2, 0.5 * dz);
        return drop;
    }

}