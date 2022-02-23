package io.github.screret.simpletech.registry;

import io.github.screret.simpletech.SimpleTech;
import io.github.screret.simpletech.packets.StructureErrorPositionPacket;
import io.github.screret.simpletech.packets.StructureUpdatePacket;
import io.github.screret.simpletech.packets.TankUpdatePacket;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import slimeknights.mantle.network.NetworkWrapper;

import javax.annotation.Nullable;
import java.util.function.Function;

public class PacketManager extends NetworkWrapper {
    private static PacketManager instance;

    public PacketManager() {
        super(new ResourceLocation(SimpleTech.MOD_ID, "network"));
    }

    /** Gets the instance of the network */
    public static PacketManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Attempt to call network getInstance before network is setup");
        }
        return instance;
    }

    /**
     * Called during mod construction to setup the network
     */
    public static void setup() {
        if (instance != null) {
            return;
        }
        instance = new PacketManager();

        instance.registerPacket(StructureErrorPositionPacket.class, StructureErrorPositionPacket::new, NetworkDirection.PLAY_TO_CLIENT);
        instance.registerPacket(StructureUpdatePacket.class, StructureUpdatePacket::new, NetworkDirection.PLAY_TO_CLIENT);
        instance.registerPacket(TankUpdatePacket.class, TankUpdatePacket::new, NetworkDirection.PLAY_TO_CLIENT);
    }

    /**
     * Sends a vanilla packet to the given player
     * @param player  Player
     * @param packet  Packet
     */
    public void sendVanillaPacket(Entity player, Packet<?> packet) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(packet);
        }
    }

    /**
     * Same as {@link #sendToClientsAround(Object, ServerLevel, BlockPos)}, but checks that the world is a serverworld
     * @param msg       Packet to send
     * @param world     World instance
     * @param position  Target position
     */
    public void sendToClientsAround(Object msg, @Nullable LevelAccessor world, BlockPos position) {
        if (world instanceof ServerLevel server) {
            super.sendToClientsAround(msg, server, position);
        }
    }

    /**
     * Sends a packet to all entities tracking the given entity
     * @param msg     Packet
     * @param entity  Entity to check
     */
    @Override
    public void sendToTrackingAndSelf(Object msg, Entity entity) {
        this.network.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity), msg);
    }

    /**
     * Sends a packet to all entities tracking the given entity
     * @param msg     Packet
     * @param entity  Entity to check
     */
    @Override
    public void sendToTracking(Object msg, Entity entity) {
        this.network.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), msg);
    }

    /**
     * Sends a packet to the whole player list
     * @param targetedPlayer  Main player to target, if null uses whole list
     * @param playerList      Player list to use if main player is null
     * @param msg             Message to send
     */
    public void sendToPlayerList(@Nullable ServerPlayer targetedPlayer, PlayerList playerList, Object msg) {
        if (targetedPlayer != null) {
            sendTo(msg, targetedPlayer);
        } else {
            for (ServerPlayer player : playerList.getPlayers()) {
                sendTo(msg, player);
            }
        }
    }

    /** Creates a new client block entity data packet with better generics than the vanilla method */
    public static <B extends BlockEntity> ClientboundBlockEntityDataPacket createBEPacket(B be, Function<? super B, CompoundTag> tagFunction) {
        return new ClientboundBlockEntityDataPacket(be.getBlockPos(), be.getType(), tagFunction.apply(be));
    }
}
