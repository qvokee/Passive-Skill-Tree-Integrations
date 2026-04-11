package com.lota.passiveskilltreeintegrations.network;

import com.lota.passiveskilltreeintegrations.data.SkillGuideRegistry;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

public class SyncSkillGuidesMessage {
    private final Map<ResourceLocation, ItemStack> guides;

    public SyncSkillGuidesMessage(Map<ResourceLocation, ItemStack> guides) {
        this.guides = new HashMap<>();
        guides.forEach((skillId, guideStack) -> this.guides.put(skillId, guideStack.copy()));
    }

    public static SyncSkillGuidesMessage decode(FriendlyByteBuf buffer) {
        int guideCount = buffer.readVarInt();
        Map<ResourceLocation, ItemStack> guides = new HashMap<>();
        for (int index = 0; index < guideCount; index++) {
            ResourceLocation skillId = buffer.readResourceLocation();
            guides.put(skillId, buffer.readItem());
        }
        return new SyncSkillGuidesMessage(guides);
    }

    public static void encode(SyncSkillGuidesMessage message, FriendlyByteBuf buffer) {
        buffer.writeVarInt(message.guides.size());
        message.guides.forEach((skillId, guideStack) -> {
            buffer.writeResourceLocation(skillId);
            buffer.writeItem(guideStack);
        });
    }

    public static void handle(SyncSkillGuidesMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> SkillGuideRegistry.setClientGuides(message.guides));
        context.setPacketHandled(true);
    }
}
