package com.lota.passiveskilltreeintegrations.event;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.lota.passiveskilltreeintegrations.init.EFSTEventListeners;
import daripher.skilltree.client.tooltip.TooltipHelper;
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
 * Event listener that triggers when a player deals damage using Epic Fight combat system.
 */
public class EpicDealDamageEventListener implements SkillEventListener {
    private LivingEntityPredicate playerCondition = NoneLivingEntityPredicate.INSTANCE;
    private LivingEntityPredicate enemyCondition = NoneLivingEntityPredicate.INSTANCE;
    private LivingMultiplier playerMultiplier = NoneLivingMultiplier.INSTANCE;
    private LivingMultiplier enemyMultiplier = NoneLivingMultiplier.INSTANCE;
    private SkillBonus.Target target = SkillBonus.Target.ENEMY;

    public void onEvent(@Nonnull Player player, @Nonnull LivingEntity enemy, @Nonnull EventListenerBonus<?> skill) {
        if (!playerCondition.test(player)) return;
        if (!enemyCondition.test(enemy)) return;
        LivingEntity targetEntity = target == SkillBonus.Target.PLAYER ? player : enemy;
        skill.multiply(playerMultiplier.getValue(player) * enemyMultiplier.getValue(enemy)).applyEffect(targetEntity);
    }

    @Override
    public MutableComponent getTooltip(Component bonusTooltip) {
        MutableComponent eventTooltip = Component.translatable(getDescriptionId(), bonusTooltip);
        eventTooltip = playerCondition.getTooltip(eventTooltip, SkillBonus.Target.PLAYER);
        eventTooltip = enemyCondition.getTooltip(eventTooltip, SkillBonus.Target.ENEMY);
        eventTooltip = playerMultiplier.getTooltip(eventTooltip, SkillBonus.Target.PLAYER);
        eventTooltip = enemyMultiplier.getTooltip(eventTooltip, SkillBonus.Target.ENEMY);
        return eventTooltip;
    }

    @Override
    public SkillEventListener.Serializer getSerializer() {
        return EFSTEventListeners.EPIC_DEAL_DAMAGE.get();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EpicDealDamageEventListener listener = (EpicDealDamageEventListener) o;
        return Objects.equals(playerCondition, listener.playerCondition)
            && Objects.equals(enemyCondition, listener.enemyCondition)
            && Objects.equals(playerMultiplier, listener.playerMultiplier)
            && Objects.equals(enemyMultiplier, listener.enemyMultiplier)
            && target == listener.target;
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerCondition, enemyCondition, playerMultiplier, enemyMultiplier, target);
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
        editor.addLabel(0, 0, "Enemy Condition", ChatFormatting.GREEN);
        editor.increaseHeight(19);
        editor
            .addSelectionMenu(0, 0, 200, enemyCondition)
            .setResponder(condition -> selectEnemyCondition(editor, consumer, condition))
            .setMenuInitFunc(() -> addEnemyConditionWidgets(editor, consumer));
        editor.increaseHeight(19);
        editor.addLabel(0, 0, "Player Multiplier", ChatFormatting.GREEN);
        editor.increaseHeight(19);
        editor
            .addSelectionMenu(0, 0, 200, playerMultiplier)
            .setResponder(multiplier -> selectPlayerMultiplier(editor, consumer, multiplier))
            .setMenuInitFunc(() -> addPlayerMultiplierWidgets(editor, consumer));
        editor.increaseHeight(19);
        editor.addLabel(0, 0, "Enemy Multiplier", ChatFormatting.GREEN);
        editor.increaseHeight(19);
        editor
            .addSelectionMenu(0, 0, 200, enemyMultiplier)
            .setResponder(multiplier -> selectEnemyMultiplier(editor, consumer, multiplier))
            .setMenuInitFunc(() -> addEnemyMultiplierWidgets(editor, consumer));
        editor.increaseHeight(19);
        editor.addLabel(0, 0, "Target", ChatFormatting.GREEN);
        editor.increaseHeight(19);
        editor
            .addSelection(0, 0, 80, 1, target)
            .setNameGetter(TooltipHelper::getTargetName)
            .setResponder(t -> selectTarget(consumer, t));
        editor.increaseHeight(29);
    }

    private void selectTarget(Consumer<SkillEventListener> consumer, SkillBonus.Target t) {
        setTarget(t);
        consumer.accept(this);
    }

    private void addPlayerConditionWidgets(SkillTreeEditor editor, Consumer<SkillEventListener> consumer) {
        playerCondition.addEditorWidgets(editor, condition -> { setPlayerCondition(condition); consumer.accept(this); });
    }

    private void selectPlayerCondition(SkillTreeEditor editor, Consumer<SkillEventListener> consumer, LivingEntityPredicate condition) {
        setPlayerCondition(condition); consumer.accept(this); editor.rebuildWidgets();
    }

    private void addEnemyConditionWidgets(SkillTreeEditor editor, Consumer<SkillEventListener> consumer) {
        enemyCondition.addEditorWidgets(editor, condition -> { setEnemyCondition(condition); consumer.accept(this); });
    }

    private void selectEnemyCondition(SkillTreeEditor editor, Consumer<SkillEventListener> consumer, LivingEntityPredicate condition) {
        setEnemyCondition(condition); consumer.accept(this); editor.rebuildWidgets();
    }

    private void addPlayerMultiplierWidgets(SkillTreeEditor editor, Consumer<SkillEventListener> consumer) {
        playerMultiplier.addEditorWidgets(editor, multiplier -> { setPlayerMultiplier(multiplier); consumer.accept(this); });
    }

    private void selectPlayerMultiplier(SkillTreeEditor editor, Consumer<SkillEventListener> consumer, LivingMultiplier multiplier) {
        setPlayerMultiplier(multiplier); consumer.accept(this); editor.rebuildWidgets();
    }

    private void addEnemyMultiplierWidgets(SkillTreeEditor editor, Consumer<SkillEventListener> consumer) {
        enemyMultiplier.addEditorWidgets(editor, multiplier -> { setEnemyMultiplier(multiplier); consumer.accept(this); });
    }

    private void selectEnemyMultiplier(SkillTreeEditor editor, Consumer<SkillEventListener> consumer, LivingMultiplier multiplier) {
        setEnemyMultiplier(multiplier); consumer.accept(this); editor.rebuildWidgets();
    }

    @Override
    public SkillBonus.Target getTarget() { return target; }

    public void setPlayerCondition(LivingEntityPredicate playerCondition) { this.playerCondition = playerCondition; }
    public void setEnemyCondition(LivingEntityPredicate enemyCondition) { this.enemyCondition = enemyCondition; }
    public void setPlayerMultiplier(LivingMultiplier playerMultiplier) { this.playerMultiplier = playerMultiplier; }
    public void setEnemyMultiplier(LivingMultiplier enemyMultiplier) { this.enemyMultiplier = enemyMultiplier; }
    public void setTarget(SkillBonus.Target target) { this.target = target; }

    public static class Serializer implements SkillEventListener.Serializer {
        @Override
        public SkillEventListener deserialize(JsonObject json) throws JsonParseException {
            EpicDealDamageEventListener listener = new EpicDealDamageEventListener();
            listener.setPlayerCondition(SerializationHelper.deserializeLivingCondition(json, "player_condition"));
            listener.setEnemyCondition(SerializationHelper.deserializeLivingCondition(json, "enemy_condition"));
            listener.setPlayerMultiplier(SerializationHelper.deserializeLivingMultiplier(json, "player_multiplier"));
            listener.setEnemyMultiplier(SerializationHelper.deserializeLivingMultiplier(json, "enemy_multiplier"));
            if (json.has("target")) listener.setTarget(SkillBonus.Target.valueOf(json.get("target").getAsString().toUpperCase()));
            return listener;
        }

        @Override
        public void serialize(JsonObject json, SkillEventListener listener) {
            if (!(listener instanceof EpicDealDamageEventListener aListener)) throw new IllegalArgumentException();
            SerializationHelper.serializeLivingCondition(json, aListener.playerCondition, "player_condition");
            SerializationHelper.serializeLivingCondition(json, aListener.enemyCondition, "enemy_condition");
            SerializationHelper.serializeLivingMultiplier(json, aListener.playerMultiplier, "player_multiplier");
            SerializationHelper.serializeLivingMultiplier(json, aListener.enemyMultiplier, "enemy_multiplier");
            json.addProperty("target", aListener.target.name().toLowerCase());
        }

        @Override
        public SkillEventListener deserialize(CompoundTag tag) {
            EpicDealDamageEventListener listener = new EpicDealDamageEventListener();
            listener.setPlayerCondition(SerializationHelper.deserializeLivingCondition(tag, "player_condition"));
            listener.setEnemyCondition(SerializationHelper.deserializeLivingCondition(tag, "enemy_condition"));
            listener.setPlayerMultiplier(SerializationHelper.deserializeLivingMultiplier(tag, "player_multiplier"));
            listener.setEnemyMultiplier(SerializationHelper.deserializeLivingMultiplier(tag, "enemy_multiplier"));
            if (tag.contains("target")) listener.setTarget(SkillBonus.Target.valueOf(tag.getString("target").toUpperCase()));
            return listener;
        }

        @Override
        public CompoundTag serialize(SkillEventListener listener) {
            if (!(listener instanceof EpicDealDamageEventListener aListener)) throw new IllegalArgumentException();
            CompoundTag tag = new CompoundTag();
            SerializationHelper.serializeLivingCondition(tag, aListener.playerCondition, "player_condition");
            SerializationHelper.serializeLivingCondition(tag, aListener.enemyCondition, "enemy_condition");
            SerializationHelper.serializeLivingMultiplier(tag, aListener.playerMultiplier, "player_multiplier");
            SerializationHelper.serializeLivingMultiplier(tag, aListener.enemyMultiplier, "enemy_multiplier");
            tag.putString("target", aListener.target.name().toLowerCase());
            return tag;
        }

        @Override
        public SkillEventListener deserialize(FriendlyByteBuf buf) {
            EpicDealDamageEventListener listener = new EpicDealDamageEventListener();
            listener.setPlayerCondition(NetworkHelper.readLivingCondition(buf));
            listener.setEnemyCondition(NetworkHelper.readLivingCondition(buf));
            listener.setPlayerMultiplier(NetworkHelper.readLivingMultiplier(buf));
            listener.setEnemyMultiplier(NetworkHelper.readLivingMultiplier(buf));
            listener.setTarget(SkillBonus.Target.values()[buf.readInt()]);
            return listener;
        }

        @Override
        public void serialize(FriendlyByteBuf buf, SkillEventListener listener) {
            if (!(listener instanceof EpicDealDamageEventListener aListener)) throw new IllegalArgumentException();
            NetworkHelper.writeLivingCondition(buf, aListener.playerCondition);
            NetworkHelper.writeLivingCondition(buf, aListener.enemyCondition);
            NetworkHelper.writeLivingMultiplier(buf, aListener.playerMultiplier);
            NetworkHelper.writeLivingMultiplier(buf, aListener.enemyMultiplier);
            buf.writeInt(aListener.target.ordinal());
        }

        @Override
        public SkillEventListener createDefaultInstance() { return new EpicDealDamageEventListener(); }
    }
}
