package com.example.client;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class BlockMenuScreen extends Screen {

    protected BlockMenuScreen() {
        super(Component.literal("Choose blocks"));
    }

}
