package com.lota.passiveskilltreeintegrations.client;

import com.lota.passiveskilltreeintegrations.PassiveSkillTreeIntegrationsMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class GuideScreenOpener {
    private GuideScreenOpener() {
    }

    public static boolean openGuide(ItemStack guideStack, Screen returnScreen) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            PassiveSkillTreeIntegrationsMod.LOGGER.info("Guide open skipped: player is null");
            return false;
        }

        ItemStack stackToOpen = guideStack.copy();
        if (stackToOpen.isEmpty()) {
            PassiveSkillTreeIntegrationsMod.LOGGER.info("Guide open skipped: guide stack is empty");
            return false;
        }

        PassiveSkillTreeIntegrationsMod.LOGGER.info(
            "Guide open requested: item={}, hasTag={}",
            net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(stackToOpen.getItem()),
            stackToOpen.hasTag()
        );

        if (stackToOpen.is(Items.WRITTEN_BOOK)) {
            SkillTreeScreenRestorer.schedule(returnScreen);
            minecraft.setScreen(new BookViewScreen(new BookViewScreen.WrittenBookAccess(stackToOpen)));
            PassiveSkillTreeIntegrationsMod.LOGGER.info("Guide open path: vanilla written book screen");
            return true;
        }

        SkillTreeScreenRestorer.schedule(returnScreen);
        Screen beforeUse = minecraft.screen;
        ItemStack originalMainHand = minecraft.player.getMainHandItem();
        minecraft.player.setItemInHand(InteractionHand.MAIN_HAND, stackToOpen);
        try {
            InteractionResultHolder<ItemStack> useResult =
                stackToOpen.use(minecraft.player.level(), minecraft.player, InteractionHand.MAIN_HAND);
            String beforeScreenName = beforeUse == null ? "null" : beforeUse.getClass().getName();
            String afterScreenName = minecraft.screen == null ? "null" : minecraft.screen.getClass().getName();
            PassiveSkillTreeIntegrationsMod.LOGGER.info(
                "Guide item use result: result={}, consumedStackEmpty={}, screenBefore={}, screenAfter={}",
                useResult.getResult(),
                useResult.getObject().isEmpty(),
                beforeScreenName,
                afterScreenName
            );
        } finally {
            minecraft.player.setItemInHand(InteractionHand.MAIN_HAND, originalMainHand);
        }
        return true;
    }
}
