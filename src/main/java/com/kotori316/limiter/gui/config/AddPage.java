package com.kotori316.limiter.gui.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import com.feed_the_beast.mods.ftbguilibrary.config.ConfigGroup;
import com.feed_the_beast.mods.ftbguilibrary.config.gui.GuiEditConfig;
import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import com.feed_the_beast.mods.ftbguilibrary.misc.GuiButtonListBase;
import com.feed_the_beast.mods.ftbguilibrary.utils.Key;
import com.feed_the_beast.mods.ftbguilibrary.utils.MouseButton;
import com.feed_the_beast.mods.ftbguilibrary.widget.GuiBase;
import com.feed_the_beast.mods.ftbguilibrary.widget.Panel;
import com.feed_the_beast.mods.ftbguilibrary.widget.SimpleTextButton;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import com.kotori316.limiter.SpawnConditionLoader;
import com.kotori316.limiter.TestSpawn;
import com.kotori316.limiter.capability.LMSHandler;
import com.kotori316.limiter.capability.RuleType;
import com.kotori316.limiter.conditions.And;
import com.kotori316.limiter.conditions.Not;
import com.kotori316.limiter.conditions.Or;
import com.kotori316.limiter.gui.packet.LMSHandlerMessage;
import com.kotori316.limiter.gui.packet.PacketHandler;

public class AddPage extends GuiButtonListBase {
    private final GuiBase parent;
    private final Consumer<TestSpawn> appender;
    private final Set<String> removedConditions;

    public AddPage(GuiBase parent, RuleType ruleType, LMSHandler lmsHandler) {
        this.parent = parent;
        this.appender = spawn -> {
            ruleType.add(lmsHandler, spawn);
            PacketHandler.sendChangesToServer(LMSHandlerMessage.createServer(lmsHandler));
        };
        this.removedConditions = Collections.emptySet();
    }

    public AddPage(GuiBase parent, Consumer<TestSpawn> appender, Set<String> removedConditions) {
        this.parent = parent;
        this.appender = appender;
        this.removedConditions = removedConditions;
    }

    @Override
    public Screen getPrevScreen() {
        parent.refreshWidgets();
        return parent.getWrapper();
    }

    @Override
    public boolean keyPressed(Key key) {
        if (super.keyPressed(key)) return true;
        if (onClosedByKey(key)) {
            closeGui(false);
            return true;
        }
        return false;
    }

    @Override
    public void addButtons(Panel panel) {
        List<String> strings = new ArrayList<>(SpawnConditionLoader.INSTANCE.serializeKeySet());
        strings.sort(Comparator.naturalOrder());
        for (String s : strings) {
            if (!removedConditions.contains(s))
                panel.add(new NewButton(panel, new StringTextComponent(s), Icon.EMPTY, s.equals("all"), SpawnConditionLoader.INSTANCE.getSerializer(s)));
        }
    }

    private class NewButton extends SimpleTextButton {
        private final boolean noProperty;
        private final TestSpawn.Serializer<?> serializer;

        public NewButton(Panel panel, ITextComponent txt, Icon icon, boolean noProperty, TestSpawn.Serializer<?> serializer) {
            super(panel, txt, icon);
            this.noProperty = noProperty;
            this.serializer = serializer;
        }

        @Override
        public void onClicked(MouseButton mouseButton) {
            this.playClickSound();
            if (noProperty) {
                TestSpawn spawn = serializer.from(new Dynamic<>(JsonOps.INSTANCE, JsonNull.INSTANCE));
                appender.accept(spawn);
                AddPage.this.onBack();
            } else if (serializer == Not.SERIALIZER) {
                AddPage page = new AddPage(AddPage.this.parent, t -> appender.accept(t.not()), Collections.singleton("not"));
                page.setTitle(new StringTextComponent("Not Condition"));
                page.openGui();
            } else if (serializer.equals(And.SERIALIZER)) {
                CombinedConditionPage page = new CombinedConditionPage(AddPage.this.parent, serializer.getType(),
                    And::new, appender);
                page.openGui();
            } else if (serializer.equals(Or.SERIALIZER)) {
                CombinedConditionPage page = new CombinedConditionPage(AddPage.this.parent, serializer.getType(),
                    Or::new, appender);
                page.openGui();
            } else {
                JsonObject object = new JsonObject();
                ConfigGroup group = LMSConfigGui.createConfigGroup(object, serializer);
                group.savedCallback = b -> {
                    if (b) {
                        try {
                            TestSpawn spawn = serializer.from(new Dynamic<>(JsonOps.INSTANCE, object));
                            appender.accept(spawn);
                        } catch (RuntimeException e) {
                            if (Minecraft.getInstance().player != null) {
                                Minecraft.getInstance().player.sendMessage(new StringTextComponent(e.getMessage()), Util.DUMMY_UUID);
                            }
                            return;
                        }
                    }
                    AddPage.this.onBack();
                };
                GuiEditConfig gui = new GuiEditConfig(group) {
                    @Override
                    public boolean onClosedByKey(Key key) {
                        return key.escOrInventory();
                    }

                    @Override
                    public boolean keyPressed(Key key) {
                        if (super.keyPressed(key)) return true;
                        if (onClosedByKey(key)) {
                            closeGui(false);
                            return true;
                        }
                        return false;
                    }
                };
                gui.openGui();
            }
        }
    }

}
