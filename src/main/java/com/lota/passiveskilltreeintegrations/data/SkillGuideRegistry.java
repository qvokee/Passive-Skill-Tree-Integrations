package com.lota.passiveskilltreeintegrations.data;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public final class SkillGuideRegistry {
    private static final Map<ResourceLocation, ItemStack> SERVER_GUIDES = new HashMap<>();
    private static final Map<ResourceLocation, ItemStack> CLIENT_GUIDES = new HashMap<>();

    private SkillGuideRegistry() {
    }

    public static void setServerGuides(Map<ResourceLocation, ItemStack> guides) {
        SERVER_GUIDES.clear();
        copyInto(guides, SERVER_GUIDES);
    }

    public static Map<ResourceLocation, ItemStack> getServerGuides() {
        return copyOf(SERVER_GUIDES);
    }

    public static void setClientGuides(Map<ResourceLocation, ItemStack> guides) {
        CLIENT_GUIDES.clear();
        copyInto(guides, CLIENT_GUIDES);
    }

    public static void clearClientGuides() {
        CLIENT_GUIDES.clear();
    }

    public static boolean hasClientGuide(ResourceLocation skillId) {
        return CLIENT_GUIDES.containsKey(skillId);
    }

    public static ItemStack getClientGuide(ResourceLocation skillId) {
        ItemStack guide = CLIENT_GUIDES.get(skillId);
        return guide == null ? ItemStack.EMPTY : guide.copy();
    }

    private static Map<ResourceLocation, ItemStack> copyOf(Map<ResourceLocation, ItemStack> guides) {
        Map<ResourceLocation, ItemStack> copy = new HashMap<>();
        copyInto(guides, copy);
        return copy;
    }

    private static void copyInto(Map<ResourceLocation, ItemStack> source, Map<ResourceLocation, ItemStack> target) {
        source.forEach((skillId, guideStack) -> target.put(skillId, guideStack.copy()));
    }
}
