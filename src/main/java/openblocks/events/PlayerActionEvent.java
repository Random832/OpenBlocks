package openblocks.events;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import openblocks.OpenBlocks;

public record PlayerActionEvent(ActionType actionType) implements CustomPacketPayload {
    public static final StreamCodec<ByteBuf, PlayerActionEvent> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8.map(ActionType::valueOf, Enum::toString),
                    PlayerActionEvent::actionType,
                    PlayerActionEvent::new
            );

    public enum ActionType {
        BOO
    }

    public static final CustomPacketPayload.Type<PlayerActionEvent> TYPE = new CustomPacketPayload.Type<>(OpenBlocks.modLoc("player_action"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}