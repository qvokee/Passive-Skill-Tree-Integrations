package com.lota.passiveskilltreeintegrations.mixin;

import com.lota.passiveskilltreeintegrations.data.SkillGuideRegistry;
import daripher.skilltree.client.widget.skill.SkillButton;
import daripher.skilltree.skill.PassiveSkill;
import daripher.skilltree.skill.PassiveSkillTree;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = SkillButton.class, remap = false)
public class SkillButtonMixin {
    @Shadow(remap = false) @Final public PassiveSkill skill;

    @Inject(method = "getSkillTooltip", at = @At("RETURN"), remap = false)
    private void addGuideHint(PassiveSkillTree skillTree, CallbackInfoReturnable<List<MutableComponent>> cir) {
        if (!SkillGuideRegistry.hasClientGuide(skill.getId())) {
            return;
        }

        List<MutableComponent> tooltip = cir.getReturnValue();
        if (!tooltip.isEmpty()) {
            MutableComponent lastLine = tooltip.get(tooltip.size() - 1);
            if (!lastLine.getString().isEmpty()) {
                tooltip.add(Component.empty());
            }
        }
        tooltip.add(Component.translatable("tooltip.passive_skill_tree_integrations.skill_guide_hint").withStyle(ChatFormatting.GOLD));
    }
}
