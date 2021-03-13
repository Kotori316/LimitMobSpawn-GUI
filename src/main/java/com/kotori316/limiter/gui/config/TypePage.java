package com.kotori316.limiter.gui.config;

import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import com.feed_the_beast.mods.ftbguilibrary.misc.GuiButtonListBase;
import com.feed_the_beast.mods.ftbguilibrary.utils.Key;
import com.feed_the_beast.mods.ftbguilibrary.utils.MouseButton;
import com.feed_the_beast.mods.ftbguilibrary.widget.Panel;
import com.feed_the_beast.mods.ftbguilibrary.widget.SimpleTextButton;
import net.minecraft.util.text.StringTextComponent;

import com.kotori316.limiter.capability.LMSHandler;
import com.kotori316.limiter.capability.RuleType;

public class TypePage extends GuiButtonListBase {
    private final LMSHandler lmsHandler;

    public TypePage(LMSHandler lmsHandler) {
        this.lmsHandler = lmsHandler;
    }

    @Override
    public void addButtons(Panel panel) {
        panel.add(new Button(panel, RuleType.DEFAULT, Icon.EMPTY));
        panel.add(new Button(panel, RuleType.DENY, Icon.EMPTY));
        panel.add(new Button(panel, RuleType.FORCE, Icon.EMPTY));
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
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

    private class Button extends SimpleTextButton {

        private final RuleType ruleType;

        public Button(Panel panel, RuleType ruleType, Icon icon) {
            super(panel, new StringTextComponent(ruleType.getText()), icon);
            this.ruleType = ruleType;
        }

        @Override
        public void onClicked(MouseButton mouseButton) {
            this.playClickSound();
            new ListPage(TypePage.this, ruleType, lmsHandler).openGui();
        }
    }
}
