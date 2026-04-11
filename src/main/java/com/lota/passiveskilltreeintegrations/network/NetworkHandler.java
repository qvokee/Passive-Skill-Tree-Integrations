package com.lota.passiveskilltreeintegrations.network;

import com.lota.passiveskilltreeintegrations.PassiveSkillTreeIntegrationsMod;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class NetworkHandler {
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(PassiveSkillTreeIntegrationsMod.MOD_ID, "main"),
        () -> "1",
        version -> true,
        version -> true
    );

    private NetworkHandler() {
    }

    public static void register(FMLCommonSetupEvent event) {
        CHANNEL.registerMessage(
            0,
            SyncSkillGuidesMessage.class,
            SyncSkillGuidesMessage::encode,
            SyncSkillGuidesMessage::decode,
            SyncSkillGuidesMessage::handle,
            Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
    }
}
