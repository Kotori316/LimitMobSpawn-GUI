package com.kotori316.limiter.gui.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

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

import static com.kotori316.limiter.gui.config.ListPage.getAt;

public class CombinedConditionPage extends GuiButtonListBase {

    private final GuiBase parent;
    private final String ruleType;
    private final Function<List<TestSpawn>, TestSpawn> combiner;
    private final Consumer<TestSpawn> appender;
    private final List<TestSpawn> list;
    private final CombineButton addButton, removeButton, okButton;
    private int selectionIndex = -1;

    public CombinedConditionPage(GuiBase parent, String ruleType, Function<List<TestSpawn>, TestSpawn> combiner, Consumer<TestSpawn> appender) {
        super();
        this.parent = parent;
        this.ruleType = ruleType;
        this.combiner = combiner;
        this.appender = appender;
        this.list = new ArrayList<>();
        setTitle(new StringTextComponent(ruleType));
        addButton = new CombineButton(this, new StringTextComponent("Add"), 0);
        removeButton = new CombineButton(this, new StringTextComponent("Remove"), 1);
        okButton = new CombineButton(this, new StringTextComponent("OK"), 2);
    }

    @Override
    public void addButtons(Panel panel) {
        for (TestSpawn rule : list) {
            panel.add(new SimpleTextButton(panel, new StringTextComponent(rule.contentShort()), Icon.EMPTY) {
                @Override
                public void onClicked(MouseButton mouseButton) {
                    this.playClickSound();
                    getAt(panel.widgets, CombinedConditionPage.this.selectionIndex)
                        .flatMap(w -> w instanceof Button ? Optional.of((Button) w) : Optional.empty())
                        .ifPresent(b -> b.setIcon(Icon.EMPTY));
                    int selectionIndex = panel.widgets.indexOf(this);
                    if (selectionIndex != CombinedConditionPage.this.selectionIndex) {
                        CombinedConditionPage.this.selectionIndex = selectionIndex;
                        this.setIcon(GuiIcons.CHECK);
                    } else {
                        CombinedConditionPage.this.selectionIndex = -1;
                    }
                }
            });
        }
    }

    @Override
    public void addWidgets() {
        super.addWidgets();
        add(addButton);
        add(removeButton);
        add(okButton);
    }

    @Override
    public void alignWidgets() {
        super.alignWidgets();
        final int t = 9;
        int width = this.widgets.get(0).width;
        addButton.setPosAndSize(t, 144 + t, width / 2, 20);
        removeButton.setPosAndSize(t + width / 2, 144 + t, width / 2, 20);
        okButton.setPosAndSize(t, 144 + t + 20, width, 20);
        setHeight(this.height + 40);
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

    private class CombineButton extends SimpleTextButton {

        private final int id;

        /**
         * @param id 0-Add, 1-Remove, 2-OK
         */
        public CombineButton(Panel panel, ITextComponent txt, int id) {
            super(panel, txt, Icon.EMPTY);
            this.id = id;
        }

        @Override
        public boolean renderTitleInCenter() {
            return true;
        }

        @Override
        public void onClicked(MouseButton mouseButton) {
            this.playClickSound();
            switch (id) {
                case 0:
                    new AddPage(CombinedConditionPage.this, list::add, Collections.singleton(ruleType)).openGui();
                    break;
                case 1:
                    refreshWidgets();
                    if (0 <= selectionIndex && selectionIndex < list.size()) {
                        list.remove(selectionIndex);
                    }
                    refreshWidgets();
                    selectionIndex = -1;
                    break;
                case 2:
                default:
                    if (list.size() >= 2) {
                        TestSpawn t = combiner.apply(list);
                        appender.accept(t);
                        CombinedConditionPage.this.onBack();
                    }
                    break;
            }
        }

        @Override
        public boolean isEnabled() {
            switch (this.id) {
                case 1: // Remove
                    return CombinedConditionPage.this.selectionIndex != -1;
                case 2: // OK
                    return CombinedConditionPage.this.list.size() >= 2;
                default:
                    return super.isEnabled();
            }
        }

        @Override
        public WidgetType getWidgetType() {
            return isEnabled() ? super.getWidgetType() : WidgetType.DISABLED;
        }
    }
}
