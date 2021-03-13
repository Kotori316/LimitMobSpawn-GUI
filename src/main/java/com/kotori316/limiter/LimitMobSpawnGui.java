package com.kotori316.limiter;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import com.kotori316.limiter.capability.Caps;
import com.kotori316.limiter.gui.packet.LMSHandlerMessage;
import com.kotori316.limiter.gui.packet.PacketHandler;

@Mod(LimitMobSpawnGui.MOD_ID)
public class LimitMobSpawnGui {
    public static final String MOD_ID = "limit-mob-spawn-gui";

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class Register {
        public static final Item GUI_ITEM = new GuiItem();

        @SubscribeEvent
        public static void setup(FMLCommonSetupEvent event) {
            PacketHandler.init();
        }

        @SubscribeEvent
        public static void registerItem(RegistryEvent.Register<Item> event) {
            event.getRegistry().register(GUI_ITEM);
        }
    }

    static class GuiItem extends Item {

        public GuiItem() {
            super(new Item.Properties().group(ItemGroup.MISC));
            setRegistryName(LimitMobSpawnGui.MOD_ID, "gui-item");
        }

        @Override
        public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
            ItemStack stack = playerIn.getHeldItem(handIn);
            if (!worldIn.isRemote) {
                ServerPlayerEntity serverPlayerEntity = ((ServerPlayerEntity) playerIn);
                if (serverPlayerEntity.hasPermissionLevel(2)) {
                    // Allow op users to change config.
                    worldIn.getCapability(Caps.getLmsCapability())
                        .map(LMSHandlerMessage::createClientMessageToOpenGui)
                        .ifPresent(m -> PacketHandler.openGuiInClient(m, (ServerPlayerEntity) playerIn));
                }
            }
            return ActionResult.resultSuccess(stack);
        }
    }

}
