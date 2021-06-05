package com.kotori316.limiter.gui.config;

import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.SimpleTextButton;
import dev.ftb.mods.ftblibrary.ui.input.Key;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.ui.misc.ButtonListBaseScreen;
import net.minecraft.util.text.StringTextComponent;

import com.kotori316.limiter.capability.LMSHandler;
import com.kotori316.limiter.capability.RuleType;

public class TypePage extends ButtonListBaseScreen {
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
