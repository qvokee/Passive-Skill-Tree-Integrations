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
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Event listener that triggers when a player casts an Epic Fight skill.
 */
public class EpicSkillCastEventListener implements SkillEventListener {
    private LivingEntityPredicate playerCondition = NoneLivingEntityPredicate.INSTANCE;
    private LivingMultiplier playerMultiplier = NoneLivingMultiplier.INSTANCE;
    private SkillBonus.Target target = SkillBonus.Target.PLAYER;

    public void onEvent(@Nonnull Player player, @Nonnull EventListenerBonus<?> skill) {
        if (!playerCondition.test(player)) return;
        skill.multiply(playerMultiplier.getValue(player)).applyEffect(player);
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
        return EFSTEventListeners.EPIC_SKILL_CAST.get();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EpicSkillCastEventListener listener = (EpicSkillCastEventListener) o;
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
        editor.addSelectionMenu(0, 0, 200, playerCondition)
            .setResponder(c -> { setPlayerCondition(c); consumer.accept(this); editor.rebuildWidgets(); })
            .setMenuInitFunc(() -> playerCondition.addEditorWidgets(editor, c -> { setPlayerCondition(c); consumer.accept(this); }));
        editor.increaseHeight(19);
        editor.addLabel(0, 0, "Player Multiplier", ChatFormatting.GREEN);
        editor.increaseHeight(19);
        editor.addSelectionMenu(0, 0, 200, playerMultiplier)
            .setResponder(m -> { setPlayerMultiplier(m); consumer.accept(this); editor.rebuildWidgets(); })
            .setMenuInitFunc(() -> playerMultiplier.addEditorWidgets(editor, m -> { setPlayerMultiplier(m); consumer.accept(this); }));
        editor.increaseHeight(29);
    }

    @Override
    public SkillBonus.Target getTarget() { return target; }

    public void setPlayerCondition(LivingEntityPredicate playerCondition) { this.playerCondition = playerCondition; }
    public void setPlayerMultiplier(LivingMultiplier playerMultiplier) { this.playerMultiplier = playerMultiplier; }
    public void setTarget(SkillBonus.Target target) { this.target = target; }

    public static class Serializer implements SkillEventListener.Serializer {
        @Override
        public SkillEventListener deserialize(JsonObject json) throws JsonParseException {
            EpicSkillCastEventListener listener = new EpicSkillCastEventListener();
            listener.setPlayerCondition(SerializationHelper.deserializeLivingCondition(json, "player_condition"));
            listener.setPlayerMultiplier(SerializationHelper.deserializeLivingMultiplier(json, "player_multiplier"));
            if (json.has("target")) listener.setTarget(SkillBonus.Target.valueOf(json.get("target").getAsString().toUpperCase()));
            return listener;
        }

        @Override
        public void serialize(JsonObject json, SkillEventListener listener) {
            if (!(listener instanceof EpicSkillCastEventListener aListener)) throw new IllegalArgumentException();
            SerializationHelper.serializeLivingCondition(json, aListener.playerCondition, "player_condition");
            SerializationHelper.serializeLivingMultiplier(json, aListener.playerMultiplier, "player_multiplier");
            json.addProperty("target", aListener.target.name().toLowerCase());
        }

        @Override
        public SkillEventListener deserialize(CompoundTag tag) {
            EpicSkillCastEventListener listener = new EpicSkillCastEventListener();
            listener.setPlayerCondition(SerializationHelper.deserializeLivingCondition(tag, "player_condition"));
            listener.setPlayerMultiplier(SerializationHelper.deserializeLivingMultiplier(tag, "player_multiplier"));
            if (tag.contains("target")) listener.setTarget(SkillBonus.Target.valueOf(tag.getString("target").toUpperCase()));
            return listener;
        }

        @Override
        public CompoundTag serialize(SkillEventListener listener) {
            if (!(listener instanceof EpicSkillCastEventListener aListener)) throw new IllegalArgumentException();
            CompoundTag tag = new CompoundTag();
            SerializationHelper.serializeLivingCondition(tag, aListener.playerCondition, "player_condition");
            SerializationHelper.serializeLivingMultiplier(tag, aListener.playerMultiplier, "player_multiplier");
            tag.putString("target", aListener.target.name().toLowerCase());
            return tag;
        }

        @Override
        public SkillEventListener deserialize(FriendlyByteBuf buf) {
            EpicSkillCastEventListener listener = new EpicSkillCastEventListener();
            listener.setPlayerCondition(NetworkHelper.readLivingCondition(buf));
            listener.setPlayerMultiplier(NetworkHelper.readLivingMultiplier(buf));
            listener.setTarget(SkillBonus.Target.values()[buf.readInt()]);
            return listener;
        }

        @Override
        public void serialize(FriendlyByteBuf buf, SkillEventListener listener) {
            if (!(listener instanceof EpicSkillCastEventListener aListener)) throw new IllegalArgumentException();
            NetworkHelper.writeLivingCondition(buf, aListener.playerCondition);
            NetworkHelper.writeLivingMultiplier(buf, aListener.playerMultiplier);
            buf.writeInt(aListener.target.ordinal());
        }

        @Override
        public SkillEventListener createDefaultInstance() { return new EpicSkillCastEventListener(); }
    }
}
