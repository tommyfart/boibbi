package com.example.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;

import org.lwjgl.glfw.GLFW;

public class ExampleClient implements ClientModInitializer {

    public static KeyMapping OPEN_MENU;

    @Override
    public void onInitializeClient() {

        OPEN_MENU = KeyMappingHelper.registerKeyMapping(
                new KeyMapping(
                        "key.modid.openmenu",
                        InputConstants.Type.KEYSYM,
                        GLFW.GLFW_KEY_GRAVE_ACCENT,
                        KeyMapping.Category.MISC
                )
        );

        ClientEvents.register();
        BlockESPRenderer.register();
    }
}
