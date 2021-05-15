package com.kotori316.limiter;

import java.util.List;

import javax.annotation.Nullable;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
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
                int level = Config.getInstance().getPermission();
                if (serverPlayerEntity.hasPermissionLevel(level)) {
                    // Allow op users to change config.
                    worldIn.getCapability(Caps.getLmsCapability())
                        .map(LMSHandlerMessage::createClientMessageToOpenGui)
                        .ifPresent(m -> PacketHandler.openGuiInClient(m, (ServerPlayerEntity) playerIn));
                } else {
                    playerIn.sendStatusMessage(new TranslationTextComponent("chat.limit-mob-spawn-gui.permission-error", level), false);
                }
            }
            return ActionResult.resultSuccess(stack);
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
            super.addInformation(stack, worldIn, tooltip, flagIn);
            tooltip.add(new TranslationTextComponent("tooltip.limit-mob-spawn-gui.permission", Config.getInstance().getPermission()));
        }
    }
}
