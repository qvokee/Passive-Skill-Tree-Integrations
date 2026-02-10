package com.lota.epicfightskilltree.handler;

import com.lota.epicfightskilltree.EpicFightSkillTreeMod;
import com.lota.epicfightskilltree.event.*;
import daripher.skilltree.skill.bonus.EventListenerBonus;
import daripher.skilltree.skill.bonus.SkillBonus;
import daripher.skilltree.skill.bonus.SkillBonusHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.entity.eventlistener.*;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener.EventType;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = EpicFightSkillTreeMod.MOD_ID)
public class EpicFightEventHandler {
    
    private static final UUID DODGE_SUCCESS_UUID = UUID.fromString("ef570001-0001-0001-0001-000000000001");
    private static final UUID DEAL_DAMAGE_UUID = UUID.fromString("ef570001-0002-0001-0001-000000000001");
    private static final UUID TAKE_DAMAGE_UUID = UUID.fromString("ef570001-0003-0001-0001-000000000001");
    private static final UUID SKILL_CAST_UUID = UUID.fromString("ef570001-0004-0001-0001-000000000001");
    private static final UUID BASIC_ATTACK_UUID = UUID.fromString("ef570001-0005-0001-0001-000000000001");

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        
        ServerPlayerPatch playerPatch = EpicFightCapabilities.getEntityPatch(player, ServerPlayerPatch.class);
        if (playerPatch == null) return;
        
        PlayerEventListener eventListener = playerPatch.getEventListener();
        
        eventListener.addEventListener(
            EventType.DODGE_SUCCESS_EVENT,
            DODGE_SUCCESS_UUID,
            epicEvent -> applyDodgeSuccessBonuses(player, epicEvent)
        );
        
        eventListener.addEventListener(
            EventType.DEAL_DAMAGE_EVENT_DAMAGE,
            DEAL_DAMAGE_UUID,
            epicEvent -> applyDealDamageBonuses(player, epicEvent)
        );
        
        eventListener.addEventListener(
            EventType.TAKE_DAMAGE_EVENT_DAMAGE,
            TAKE_DAMAGE_UUID,
            epicEvent -> applyTakeDamageBonuses(player, epicEvent)
        );
        
        eventListener.addEventListener(
            EventType.SKILL_CAST_EVENT,
            SKILL_CAST_UUID,
            epicEvent -> applySkillCastBonuses(player, epicEvent)
        );
        
        eventListener.addEventListener(
            EventType.BASIC_ATTACK_EVENT,
            BASIC_ATTACK_UUID,
            epicEvent -> applyBasicAttackBonuses(player, epicEvent)
        );
        
        EpicFightSkillTreeMod.LOGGER.debug("Registered Epic Fight Skill Tree event listeners for player: {}", player.getName().getString());
    }
    
    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        
        ServerPlayerPatch playerPatch = EpicFightCapabilities.getEntityPatch(player, ServerPlayerPatch.class);
        if (playerPatch == null) return;
        
        PlayerEventListener eventListener = playerPatch.getEventListener();
        
        eventListener.removeListener(EventType.DODGE_SUCCESS_EVENT, DODGE_SUCCESS_UUID);
        eventListener.removeListener(EventType.DEAL_DAMAGE_EVENT_DAMAGE, DEAL_DAMAGE_UUID);
        eventListener.removeListener(EventType.TAKE_DAMAGE_EVENT_DAMAGE, TAKE_DAMAGE_UUID);
        eventListener.removeListener(EventType.SKILL_CAST_EVENT, SKILL_CAST_UUID);
        eventListener.removeListener(EventType.BASIC_ATTACK_EVENT, BASIC_ATTACK_UUID);
    }

    private static void applyDodgeSuccessBonuses(ServerPlayer player, DodgeSuccessEvent epicEvent) {
        for (EventListenerBonus<?> bonus : SkillBonusHandler.getMergedSkillBonuses(player, EventListenerBonus.class)) {
            if (!(bonus.getEventListener() instanceof EpicDodgeSuccessEventListener listener)) continue;
            SkillBonus<? extends EventListenerBonus<?>> copy = bonus.copy();
            listener.onEvent(player, (EventListenerBonus<?>) copy);
        }
    }

    private static void applyDealDamageBonuses(ServerPlayer player, DealDamageEvent.Damage epicEvent) {
        LivingEntity target = epicEvent.getTarget();
        for (EventListenerBonus<?> bonus : SkillBonusHandler.getMergedSkillBonuses(player, EventListenerBonus.class)) {
            if (!(bonus.getEventListener() instanceof EpicDealDamageEventListener listener)) continue;
            SkillBonus<? extends EventListenerBonus<?>> copy = bonus.copy();
            listener.onEvent(player, target, (EventListenerBonus<?>) copy);
        }
    }

    private static void applyTakeDamageBonuses(ServerPlayer player, TakeDamageEvent.Damage epicEvent) {
        LivingEntity attacker = null;
        if (epicEvent.getDamageSource().getEntity() instanceof LivingEntity living) {
            attacker = living;
        }
        for (EventListenerBonus<?> bonus : SkillBonusHandler.getMergedSkillBonuses(player, EventListenerBonus.class)) {
            if (!(bonus.getEventListener() instanceof EpicTakeDamageEventListener listener)) continue;
            SkillBonus<? extends EventListenerBonus<?>> copy = bonus.copy();
            listener.onEvent(player, attacker, (EventListenerBonus<?>) copy);
        }
    }

    private static void applySkillCastBonuses(ServerPlayer player, SkillCastEvent epicEvent) {
        if (epicEvent.getSkillContainer().getSkill().getCategory() != yesman.epicfight.skill.SkillCategories.WEAPON_INNATE) {
            return;
        }

        for (EventListenerBonus<?> bonus : SkillBonusHandler.getMergedSkillBonuses(player, EventListenerBonus.class)) {
            if (!(bonus.getEventListener() instanceof EpicSkillCastEventListener listener)) continue;
            SkillBonus<? extends EventListenerBonus<?>> copy = bonus.copy();
            listener.onEvent(player, (EventListenerBonus<?>) copy);
        }
    }

    private static void applyBasicAttackBonuses(ServerPlayer player, BasicAttackEvent epicEvent) {
        for (EventListenerBonus<?> bonus : SkillBonusHandler.getMergedSkillBonuses(player, EventListenerBonus.class)) {
            if (!(bonus.getEventListener() instanceof EpicBasicAttackEventListener listener)) continue;
            SkillBonus<? extends EventListenerBonus<?>> copy = bonus.copy();
            listener.onEvent(player, (EventListenerBonus<?>) copy);
        }
    }
}
