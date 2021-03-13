package com.kotori316.limiter.gui.config;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import com.feed_the_beast.mods.ftbguilibrary.misc.GuiButtonListBase;
import com.feed_the_beast.mods.ftbguilibrary.utils.Key;
import com.feed_the_beast.mods.ftbguilibrary.utils.MouseButton;
import com.feed_the_beast.mods.ftbguilibrary.widget.Button;
import com.feed_the_beast.mods.ftbguilibrary.widget.GuiBase;
import com.feed_the_beast.mods.ftbguilibrary.widget.GuiIcons;
import com.feed_the_beast.mods.ftbguilibrary.widget.Panel;
import com.feed_the_beast.mods.ftbguilibrary.widget.SimpleTextButton;
import com.feed_the_beast.mods.ftbguilibrary.widget.WidgetType;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import com.kotori316.limiter.TestSpawn;
import com.kotori316.limiter.capability.LMSHandler;
import com.kotori316.limiter.capability.RuleType;
import com.kotori316.limiter.gui.packet.LMSHandlerMessage;
import com.kotori316.limiter.gui.packet.PacketHandler;

public class ListPage extends GuiButtonListBase {
    private final GuiBase parent;
    private final RuleType ruleType;
    private final LMSHandler lmsHandler;
    private final List<TestSpawn> ruleList;
    private final ModifyButton addButton;
    private final ModifyButton removeButton;
    private int selectionIndex = -1;

    public ListPage(GuiBase parent, RuleType ruleType, LMSHandler lmsHandler) {
        this.parent = parent;
        this.ruleType = ruleType;
        this.lmsHandler = lmsHandler;
        this.ruleList = ruleType.getRules(lmsHandler).stream().sorted(Comparator.comparing(Object::toString)).collect(Collectors.toList());
        setTitle(new StringTextComponent(ruleType.getText()));
        addButton = new ModifyButton(this, new StringTextComponent("Add"), Icon.EMPTY, false);
        removeButton = new ModifyButton(this, new StringTextComponent("Remove"), Icon.EMPTY, true);
    }

    @Override
    public Screen getPrevScreen() {
        return parent.getWrapper();
    }

    @Override
    public void addButtons(Panel panel) {
        for (TestSpawn rule : ruleList) {
            panel.add(new SimpleTextButton(panel, new StringTextComponent(rule.contentShort()), Icon.EMPTY) {
                @Override
                public void onClicked(MouseButton mouseButton) {
                    this.playClickSound();
                    getAt(panel.widgets, ListPage.this.selectionIndex)
                        .flatMap(w -> w instanceof Button ? Optional.of((Button) w) : Optional.empty())
                        .ifPresent(b -> b.setIcon(Icon.EMPTY));
                    int selectionIndex = panel.widgets.indexOf(this);
                    if (selectionIndex != ListPage.this.selectionIndex) {
                        ListPage.this.selectionIndex = selectionIndex;
                        this.setIcon(GuiIcons.CHECK);
                    } else {
                        ListPage.this.selectionIndex = -1;
                    }
                }
            });
        }
    }

    static <T> Optional<T> getAt(List<T> list, int index) {
        if (0 <= index && index < list.size())
            return Optional.ofNullable(list.get(index));
        else
            return Optional.empty();
    }

    @Override
    public void addWidgets() {
        ruleList.clear();
        ruleType.getRules(lmsHandler).stream().sorted(Comparator.comparing(Object::toString)).forEach(ruleList::add);
        super.addWidgets();
        this.add(addButton);
        this.add(removeButton);
    }

    @Override
    public void alignWidgets() {
        super.alignWidgets();
        int width = this.widgets.get(0).width;
        addButton.setPosAndSize(9, 144 + 9, width / 2, 20);
        removeButton.setPosAndSize(9 + width / 2, 144 + 9, width / 2, 20);
        setHeight(this.height + 20);
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

    private class ModifyButton extends SimpleTextButton {

        private final boolean remove;

        public ModifyButton(Panel panel, ITextComponent txt, Icon icon, boolean remove) {
            super(panel, txt, icon);
            this.remove = remove;
        }

        @Override
        public void onClicked(MouseButton mouseButton) {
            this.playClickSound();
            if (remove) {
                if (0 <= ListPage.this.selectionIndex && ListPage.this.selectionIndex < ListPage.this.ruleList.size()) {
                    ListPage.this.ruleList.remove(ListPage.this.selectionIndex);
                    ListPage.this.ruleType.removeAll(ListPage.this.lmsHandler);
                    ListPage.this.ruleType.addAll(ListPage.this.lmsHandler, ListPage.this.ruleList);
                }
                ListPage.this.refreshWidgets();
                ListPage.this.selectionIndex = -1;
                PacketHandler.sendChangesToServer(LMSHandlerMessage.createServer(lmsHandler));
            } else {
                new AddPage(ListPage.this, ListPage.this.ruleType, ListPage.this.lmsHandler).openGui();
            }
        }

        @Override
        public boolean renderTitleInCenter() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            if (remove)
                return ListPage.this.selectionIndex != -1;
            else
                return super.isEnabled();
        }

        @Override
        public WidgetType getWidgetType() {
            return isEnabled() ? super.getWidgetType() : WidgetType.DISABLED;
        }
    }
}
