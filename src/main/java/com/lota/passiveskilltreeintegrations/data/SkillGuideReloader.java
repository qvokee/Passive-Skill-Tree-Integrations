package com.lota.passiveskilltreeintegrations.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lota.passiveskilltreeintegrations.PassiveSkillTreeIntegrationsMod;
import com.lota.passiveskilltreeintegrations.network.NetworkHandler;
import com.lota.passiveskilltreeintegrations.network.SyncSkillGuidesMessage;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = PassiveSkillTreeIntegrationsMod.MOD_ID)
public class SkillGuideReloader extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String DIRECTORY = "skill_guides";

    public SkillGuideReloader() {
        super(GSON, DIRECTORY);
    }

    @SubscribeEvent
    public static void addReloadListener(AddReloadListenerEvent event) {
        event.addListener(new SkillGuideReloader());
    }

    @SubscribeEvent
    public static void syncGuides(OnDatapackSyncEvent event) {
        SyncSkillGuidesMessage message = new SyncSkillGuidesMessage(SkillGuideRegistry.getServerGuides());
        ServerPlayer player = event.getPlayer();
        if (player != null) {
            NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
            return;
        }
        event.getPlayerList().getPlayers().forEach(serverPlayer ->
            NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer), message)
        );
    }

    @Override
    protected void apply(
        Map<ResourceLocation, JsonElement> map,
        ResourceManager resourceManager,
        ProfilerFiller profilerFiller
    ) {
        Map<ResourceLocation, ItemStack> loadedGuides = new HashMap<>();
        map.forEach((resourceId, jsonElement) -> readGuide(resourceId, jsonElement, loadedGuides));
        SkillGuideRegistry.setServerGuides(loadedGuides);
        PassiveSkillTreeIntegrationsMod.LOGGER.info("Loaded {} skill guide link(s)", loadedGuides.size());
    }

    private void readGuide(ResourceLocation resourceId, JsonElement jsonElement, Map<ResourceLocation, ItemStack> loadedGuides) {
        try {
            JsonObject json = GsonHelper.convertToJsonObject(jsonElement, resourceId.toString());
            ResourceLocation skillId = new ResourceLocation(GsonHelper.getAsString(json, "skill"));
            ItemStack guideStack = parseGuideStack(json);
            if (guideStack.isEmpty()) {
                throw new IllegalArgumentException("Guide stack is empty");
            }
            ItemStack previous = loadedGuides.put(skillId, guideStack);
            if (previous != null) {
                PassiveSkillTreeIntegrationsMod.LOGGER.warn("Overwriting guide link for skill {} from {}", skillId, resourceId);
            }
            PassiveSkillTreeIntegrationsMod.LOGGER.info(
                "Loaded guide link: skill={} item={} source={}",
                skillId,
                ForgeRegistries.ITEMS.getKey(guideStack.getItem()),
                resourceId
            );
        } catch (Exception exception) {
            PassiveSkillTreeIntegrationsMod.LOGGER.error("Could not load skill guide {}", resourceId, exception);
        }
    }

    private ItemStack parseGuideStack(JsonObject json) throws CommandSyntaxException {
        ResourceLocation itemId = new ResourceLocation(GsonHelper.getAsString(json, "item"));
        Item item = ForgeRegistries.ITEMS.getValue(itemId);
        if (item == null) {
            throw new IllegalArgumentException("Unknown item: " + itemId);
        }

        int count = GsonHelper.getAsInt(json, "count", 1);
        ItemStack stack = new ItemStack(item, Math.max(1, count));
        if (json.has("nbt")) {
            stack.setTag(net.minecraft.nbt.TagParser.parseTag(GsonHelper.getAsString(json, "nbt")));
        }
        if (json.has("hover_name")) {
            stack.setHoverName(Component.Serializer.fromJson(json.get("hover_name")));
        }
        return stack;
    }
}
