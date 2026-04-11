package com.lota.passiveskilltreeintegrations.init;

import com.lota.passiveskilltreeintegrations.event.*;
import daripher.skilltree.skill.bonus.event.SkillEventListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;


public class EFSTEventListeners {
    public static final DeferredRegister<SkillEventListener.Serializer> REGISTRY =
        DeferredRegister.create(new ResourceLocation("skilltree", "event_listeners"), "passive_skill_tree_integrations");

    public static final RegistryObject<SkillEventListener.Serializer> EPIC_DODGE_SUCCESS =
        REGISTRY.register("epic_dodge_success", EpicDodgeSuccessEventListener.Serializer::new);
    
    public static final RegistryObject<SkillEventListener.Serializer> EPIC_DEAL_DAMAGE =
        REGISTRY.register("epic_deal_damage", EpicDealDamageEventListener.Serializer::new);
    
    public static final RegistryObject<SkillEventListener.Serializer> EPIC_TAKE_DAMAGE =
        REGISTRY.register("epic_take_damage", EpicTakeDamageEventListener.Serializer::new);
    
    public static final RegistryObject<SkillEventListener.Serializer> EPIC_SKILL_CAST =
        REGISTRY.register("epic_skill_cast", EpicSkillCastEventListener.Serializer::new);
    
    public static final RegistryObject<SkillEventListener.Serializer> EPIC_BASIC_ATTACK =
        REGISTRY.register("epic_basic_attack", EpicBasicAttackEventListener.Serializer::new);
}
