package com.lota.passiveskilltreeintegrations;

import com.lota.passiveskilltreeintegrations.init.EFSTEventListeners;
import com.lota.passiveskilltreeintegrations.network.NetworkHandler;
import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(PassiveSkillTreeIntegrationsMod.MOD_ID)
public class PassiveSkillTreeIntegrationsMod {
    public static final String MOD_ID = "passive_skill_tree_integrations";
    public static final Logger LOGGER = LogUtils.getLogger();

    public PassiveSkillTreeIntegrationsMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        EFSTEventListeners.REGISTRY.register(modEventBus);
        modEventBus.addListener(NetworkHandler::register);
        LOGGER.info("Passive Skill Tree Integrations initialized");
    }
}
