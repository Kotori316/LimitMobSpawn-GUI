package com.kotori316.limiter.gui.packet;

import java.util.Optional;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkEvent;

import com.kotori316.limiter.capability.Caps;
import com.kotori316.limiter.capability.LMSConditionsHolder;
import com.kotori316.limiter.capability.LMSHandler;
import com.kotori316.limiter.capability.RuleType;
import com.kotori316.limiter.gui.config.LMSConfigGui;

public abstract class LMSHandlerMessage {
    LMSHandler worldHandler;

    public LMSHandlerMessage() {
    }

    public static LMSHandlerMessage.Client createClientMessageToOpenGui(LMSHandler lmsHandler) {
        Client message = new Client();
        message.worldHandler = lmsHandler;
        return message;
    }

    public static LMSHandlerMessage.Server createServer(LMSHandler lmsHandler) {
        Server message = new Server();
        message.worldHandler = lmsHandler;
        return message;
    }

    static void write(LMSHandlerMessage message, PacketBuffer buffer) {
        CompoundNBT nbt = message.worldHandler.serializeNBT();
        buffer.writeCompoundTag(nbt);
    }

    @Nonnull
    private static LMSHandler getLmsHandler(PacketBuffer buffer) {
        CompoundNBT nbt = buffer.readCompoundTag();
        LMSHandler handler = new LMSConditionsHolder();
        handler.deserializeNBT(nbt);
        return handler;
    }

    static LMSHandlerMessage.Client readClient(PacketBuffer buffer) {
        return createClientMessageToOpenGui(getLmsHandler(buffer));
    }

    static LMSHandlerMessage.Server readServer(PacketBuffer buffer) {
        return createServer(getLmsHandler(buffer));
    }

    static void actionClient(LMSHandlerMessage.Client message, Supplier<NetworkEvent.Context> supplier) {
        // In client.
        supplier.get().enqueueWork(() -> LMSConfigGui.openConfig(message.worldHandler));
        supplier.get().setPacketHandled(true);
    }

    static void actionServer(LMSHandlerMessage message, Supplier<NetworkEvent.Context> supplier) {
        // In server.
        supplier.get().enqueueWork(() ->
            Optional.ofNullable(supplier.get().getSender())
                .map(Entity::getServer)
                .ifPresent(s -> {
                    for (ServerWorld world : s.getWorlds()) {
                        world.getCapability(Caps.getLmsCapability()).ifPresent(h -> {
                            for (RuleType ruleType : RuleType.values()) {
                                ruleType.addAll(h, ruleType.getRules(message.worldHandler));
                            }
                        });
                    }
                }));
        supplier.get().setPacketHandled(true);
    }

    public static final class Client extends LMSHandlerMessage {
    }

    public static final class Server extends LMSHandlerMessage {
    }
}
