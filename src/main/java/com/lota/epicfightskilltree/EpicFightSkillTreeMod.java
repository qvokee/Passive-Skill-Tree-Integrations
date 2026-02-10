package com.lota.epicfightskilltree;

import com.lota.epicfightskilltree.init.EFSTEventListeners;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(EpicFightSkillTreeMod.MOD_ID)
public class EpicFightSkillTreeMod {
    public static final String MOD_ID = "epicfightskilltree";
    public static final Logger LOGGER = LogManager.getLogger();

    public EpicFightSkillTreeMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        
        EFSTEventListeners.REGISTRY.register(modEventBus);
        MinecraftForge.EVENT_BUS.register(this);
        
        LOGGER.info("Epic Fight Skill Tree Compat initialized");
    }
}
