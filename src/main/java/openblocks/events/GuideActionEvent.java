package openblocks.events;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import openblocks.OpenBlocks;

public record GuideActionEvent(ResourceKey<Level> dimension, BlockPos blockPos, String name) implements CustomPacketPayload {
    public static final StreamCodec<ByteBuf, GuideActionEvent> STREAM_CODEC =
           StreamCodec.composite(
                   ResourceKey.streamCodec(Registries.DIMENSION),
                   GuideActionEvent::dimension,
                   BlockPos.STREAM_CODEC,
                   GuideActionEvent::blockPos,
                   ByteBufCodecs.STRING_UTF8,
                   GuideActionEvent::name,
                   GuideActionEvent::new
           );

    public static final CustomPacketPayload.Type<GuideActionEvent> TYPE = new CustomPacketPayload.Type<>(OpenBlocks.modLoc("guide_action"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
