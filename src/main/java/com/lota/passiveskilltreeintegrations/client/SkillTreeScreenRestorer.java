package com.lota.passiveskilltreeintegrations.client;

import com.lota.passiveskilltreeintegrations.PassiveSkillTreeIntegrationsMod;
import com.lota.passiveskilltreeintegrations.data.SkillGuideRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = PassiveSkillTreeIntegrationsMod.MOD_ID, value = Dist.CLIENT)
public final class SkillTreeScreenRestorer {
    private static Screen pendingScreen;

    private SkillTreeScreenRestorer() {
    }

    public static void schedule(Screen screen) {
        pendingScreen = screen;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || pendingScreen == null) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen == null) {
            Screen screenToRestore = pendingScreen;
            pendingScreen = null;
            minecraft.setScreen(screenToRestore);
        }
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        pendingScreen = null;
        SkillGuideRegistry.clearClientGuides();
    }
}
