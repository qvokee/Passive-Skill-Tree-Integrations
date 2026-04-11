package com.lota.passiveskilltreeintegrations.event;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.lota.passiveskilltreeintegrations.init.EFSTEventListeners;
import daripher.skilltree.client.widget.editor.SkillTreeEditor;
import daripher.skilltree.data.serializers.SerializationHelper;
import daripher.skilltree.network.NetworkHelper;
import daripher.skilltree.skill.bonus.EventListenerBonus;
import daripher.skilltree.skill.bonus.SkillBonus;
import daripher.skilltree.skill.bonus.event.SkillEventListener;
import daripher.skilltree.skill.bonus.predicate.living.LivingEntityPredicate;
import daripher.skilltree.skill.bonus.predicate.living.NoneLivingEntityPredicate;
import daripher.skilltree.skill.bonus.multiplier.LivingMultiplier;
import daripher.skilltree.skill.bonus.multiplier.NoneLivingMultiplier;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Event listener that triggers when a player successfully dodges an attack in Epic Fight.
 */
public class EpicDodgeSuccessEventListener implements SkillEventListener {
    private LivingEntityPredicate playerCondition = NoneLivingEntityPredicate.INSTANCE;
    private LivingMultiplier playerMultiplier = NoneLivingMultiplier.INSTANCE;
    private SkillBonus.Target target = SkillBonus.Target.PLAYER;

    public void onEvent(@Nonnull Player player, @Nonnull EventListenerBonus<?> skill) {
        if (!playerCondition.test(player)) return;
        LivingEntity targetEntity = target == SkillBonus.Target.PLAYER ? player : null;
        if (targetEntity == null) return;
        skill.multiply(playerMultiplier.getValue(player)).applyEffect(targetEntity);
    }

    @Override
    public MutableComponent getTooltip(Component bonusTooltip) {
        MutableComponent eventTooltip = Component.translatable(getDescriptionId(), bonusTooltip);
        eventTooltip = playerCondition.getTooltip(eventTooltip, SkillBonus.Target.PLAYER);
        eventTooltip = playerMultiplier.getTooltip(eventTooltip, SkillBonus.Target.PLAYER);
        return eventTooltip;
    }

    @Override
    public SkillEventListener.Serializer getSerializer() {
        return EFSTEventListeners.EPIC_DODGE_SUCCESS.get();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EpicDodgeSuccessEventListener listener = (EpicDodgeSuccessEventListener) o;
        return Objects.equals(playerCondition, listener.playerCondition)
            && Objects.equals(playerMultiplier, listener.playerMultiplier)
            && target == listener.target;
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerCondition, playerMultiplier, target);
    }

    @Override
    public void addEditorWidgets(SkillTreeEditor editor, Consumer<SkillEventListener> consumer) {
        editor.addLabel(0, 0, "Player Condition", ChatFormatting.GREEN);
        editor.increaseHeight(19);
        editor
            .addSelectionMenu(0, 0, 200, playerCondition)
            .setResponder(condition -> selectPlayerCondition(editor, consumer, condition))
            .setMenuInitFunc(() -> addPlayerConditionWidgets(editor, consumer));
        editor.increaseHeight(19);
        editor.addLabel(0, 0, "Player Multiplier", ChatFormatting.GREEN);
        editor.increaseHeight(19);
        editor
            .addSelectionMenu(0, 0, 200, playerMultiplier)
            .setResponder(multiplier -> selectPlayerMultiplier(editor, consumer, multiplier))
            .setMenuInitFunc(() -> addPlayerMultiplierWidgets(editor, consumer));
        editor.increaseHeight(29);
    }

    private void addPlayerConditionWidgets(SkillTreeEditor editor, Consumer<SkillEventListener> consumer) {
        playerCondition.addEditorWidgets(editor, condition -> {
            setPlayerCondition(condition);
            consumer.accept(this);
        });
    }

    private void selectPlayerCondition(SkillTreeEditor editor, Consumer<SkillEventListener> consumer, LivingEntityPredicate condition) {
        setPlayerCondition(condition);
        consumer.accept(this);
        editor.rebuildWidgets();
    }

    private void addPlayerMultiplierWidgets(SkillTreeEditor editor, Consumer<SkillEventListener> consumer) {
        playerMultiplier.addEditorWidgets(editor, multiplier -> {
            setPlayerMultiplier(multiplier);
            consumer.accept(this);
        });
    }

    private void selectPlayerMultiplier(SkillTreeEditor editor, Consumer<SkillEventListener> consumer, LivingMultiplier multiplier) {
        setPlayerMultiplier(multiplier);
        consumer.accept(this);
        editor.rebuildWidgets();
    }

    @Override
    public SkillBonus.Target getTarget() {
        return target;
    }

    public void setPlayerCondition(LivingEntityPredicate playerCondition) {
        this.playerCondition = playerCondition;
    }

    public void setPlayerMultiplier(LivingMultiplier playerMultiplier) {
        this.playerMultiplier = playerMultiplier;
    }

    public void setTarget(SkillBonus.Target target) {
        this.target = target;
    }

    public static class Serializer implements SkillEventListener.Serializer {
        @Override
        public SkillEventListener deserialize(JsonObject json) throws JsonParseException {
            EpicDodgeSuccessEventListener listener = new EpicDodgeSuccessEventListener();
            listener.setPlayerCondition(SerializationHelper.deserializeLivingCondition(json, "player_condition"));
            listener.setPlayerMultiplier(SerializationHelper.deserializeLivingMultiplier(json, "player_multiplier"));
            if (json.has("target")) {
                listener.setTarget(SkillBonus.Target.valueOf(json.get("target").getAsString().toUpperCase()));
            }
            return listener;
        }

        @Override
        public void serialize(JsonObject json, SkillEventListener listener) {
            if (!(listener instanceof EpicDodgeSuccessEventListener aListener)) {
                throw new IllegalArgumentException();
            }
            SerializationHelper.serializeLivingCondition(json, aListener.playerCondition, "player_condition");
            SerializationHelper.serializeLivingMultiplier(json, aListener.playerMultiplier, "player_multiplier");
            json.addProperty("target", aListener.target.name().toLowerCase());
        }

        @Override
        public SkillEventListener deserialize(CompoundTag tag) {
            EpicDodgeSuccessEventListener listener = new EpicDodgeSuccessEventListener();
            listener.setPlayerCondition(SerializationHelper.deserializeLivingCondition(tag, "player_condition"));
            listener.setPlayerMultiplier(SerializationHelper.deserializeLivingMultiplier(tag, "player_multiplier"));
            if (tag.contains("target")) {
                listener.setTarget(SkillBonus.Target.valueOf(tag.getString("target").toUpperCase()));
            }
            return listener;
        }

        @Override
        public CompoundTag serialize(SkillEventListener listener) {
            if (!(listener instanceof EpicDodgeSuccessEventListener aListener)) {
                throw new IllegalArgumentException();
            }
            CompoundTag tag = new CompoundTag();
            SerializationHelper.serializeLivingCondition(tag, aListener.playerCondition, "player_condition");
            SerializationHelper.serializeLivingMultiplier(tag, aListener.playerMultiplier, "player_multiplier");
            tag.putString("target", aListener.target.name().toLowerCase());
            return tag;
        }

        @Override
        public SkillEventListener deserialize(FriendlyByteBuf buf) {
            EpicDodgeSuccessEventListener listener = new EpicDodgeSuccessEventListener();
            listener.setPlayerCondition(NetworkHelper.readLivingCondition(buf));
            listener.setPlayerMultiplier(NetworkHelper.readLivingMultiplier(buf));
            listener.setTarget(SkillBonus.Target.values()[buf.readInt()]);
            return listener;
        }

        @Override
        public void serialize(FriendlyByteBuf buf, SkillEventListener listener) {
            if (!(listener instanceof EpicDodgeSuccessEventListener aListener)) {
                throw new IllegalArgumentException();
            }
            NetworkHelper.writeLivingCondition(buf, aListener.playerCondition);
            NetworkHelper.writeLivingMultiplier(buf, aListener.playerMultiplier);
            buf.writeInt(aListener.target.ordinal());
        }

        @Override
        public SkillEventListener createDefaultInstance() {
            return new EpicDodgeSuccessEventListener();
        }
    }
}
