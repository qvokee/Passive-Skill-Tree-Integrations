package com.lota.passiveskilltreeintegrations.mixin;

import com.lota.passiveskilltreeintegrations.PassiveSkillTreeIntegrationsMod;
import com.lota.passiveskilltreeintegrations.client.GuideScreenOpener;
import com.lota.passiveskilltreeintegrations.data.SkillGuideRegistry;
import daripher.skilltree.client.widget.SkillTreeWidgets;
import daripher.skilltree.client.widget.skill.SkillButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = SkillTreeWidgets.class, remap = false)
public abstract class SkillTreeWidgetsMixin {
    @Shadow(remap = false)
    protected abstract void skillButtonPressed(SkillButton button);

    @Redirect(
        method = "m_6375_",
        at = @At(
            value = "INVOKE",
            target = "Ldaripher/skilltree/client/widget/SkillTreeWidgets;skillButtonPressed(Ldaripher/skilltree/client/widget/skill/SkillButton;)V"
        ),
        remap = false
    )
    private void redirectSkillButtonPressed(SkillTreeWidgets widgetController, SkillButton skillButton) {
        if (!Screen.hasShiftDown()) {
            skillButtonPressed(skillButton);
            return;
        }

        PassiveSkillTreeIntegrationsMod.LOGGER.info("Intercepted click for skill: {}", skillButton.skill.getId());
        ItemStack guideStack = SkillGuideRegistry.getClientGuide(skillButton.skill.getId());
        if (guideStack.isEmpty()) {
            PassiveSkillTreeIntegrationsMod.LOGGER.info("No linked guide for skill {}, fallback to default click", skillButton.skill.getId());
            skillButtonPressed(skillButton);
            return;
        }

        Screen returnScreen = Minecraft.getInstance().screen;
        if (returnScreen == null) {
            PassiveSkillTreeIntegrationsMod.LOGGER.info("No current screen for guide open, fallback to default click");
            skillButtonPressed(skillButton);
            return;
        }

        PassiveSkillTreeIntegrationsMod.LOGGER.info("Linked guide found for skill {}, trying to open", skillButton.skill.getId());
        if (GuideScreenOpener.openGuide(guideStack, returnScreen)) {
            Minecraft minecraft = Minecraft.getInstance();
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            PassiveSkillTreeIntegrationsMod.LOGGER.info("Guide open call executed for {}", skillButton.skill.getId());
            return;
        }

        PassiveSkillTreeIntegrationsMod.LOGGER.info("Guide open failed for {}, fallback to default click", skillButton.skill.getId());
        skillButtonPressed(skillButton);
    }
}
