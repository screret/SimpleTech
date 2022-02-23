package io.github.screret.simpletech.packets;

import io.github.screret.simpletech.multiblock.blockentity.StructureBlockEntity;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;
import slimeknights.mantle.network.packet.IThreadsafePacket;
import slimeknights.mantle.util.BlockEntityHelper;

import javax.annotation.Nullable;

/**
 * Packet to tell a multiblock to render a specific position as the cause of the error
 */
@RequiredArgsConstructor
public class StructureErrorPositionPacket implements IThreadsafePacket {
    private final BlockPos controllerPos;
    @Nullable
    private final BlockPos errorPos;

    public StructureErrorPositionPacket(FriendlyByteBuf buffer) {
        this.controllerPos = buffer.readBlockPos();
        if (buffer.readBoolean()) {
            this.errorPos = buffer.readBlockPos();
        } else {
            this.errorPos = null;
        }
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(controllerPos);
        if (errorPos != null) {
            buffer.writeBoolean(true);
            buffer.writeBlockPos(errorPos);
        } else {
            buffer.writeBoolean(false);
        }
    }

    @Override
    public void handleThreadsafe(Context context) {
        HandleClient.handle(this);
    }

    private static class HandleClient {
        private static void handle(StructureErrorPositionPacket packet) {
            BlockEntityHelper.get(StructureBlockEntity.class, Minecraft.getInstance().level, packet.controllerPos)
                    .ifPresent(te -> te.setErrorPos(packet.errorPos));
        }
    }
}