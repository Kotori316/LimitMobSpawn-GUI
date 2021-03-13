package com.kotori316.limiter.gui.packet;

import java.util.function.Predicate;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import com.kotori316.limiter.LimitMobSpawnGui;

public class PacketHandler {
    private static final String PROTOCOL = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(LimitMobSpawnGui.MOD_ID, "main"),
        () -> PROTOCOL,
        Predicate.isEqual(PROTOCOL), Predicate.isEqual(PROTOCOL)
    );

    public static void init() {
        INSTANCE.registerMessage(0, LMSHandlerMessage.Client.class, LMSHandlerMessage::write,
            LMSHandlerMessage::readClient, LMSHandlerMessage::actionClient);
        INSTANCE.registerMessage(1, LMSHandlerMessage.Server.class, LMSHandlerMessage::write,
            LMSHandlerMessage::readServer, LMSHandlerMessage::actionServer);
    }

    public static void openGuiInClient(LMSHandlerMessage.Client message, ServerPlayerEntity player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    public static void sendChangesToServer(LMSHandlerMessage.Server message) {
        INSTANCE.send(PacketDistributor.SERVER.noArg(), message);
    }
}
