package com.example.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;

public class ClientEvents {

    public static void register() {

        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            while (ExampleClient.OPEN_MENU.consumeClick()) {

                Minecraft.getInstance().setScreen(
                        new BlockMenuScreen()
                );

            }

        });

    }

}
