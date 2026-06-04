package com.example.client;

import com.example.SelectedBlocks;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;

public class BlockESPRenderer {

    public static void register() {

        LevelRenderEvents.AFTER_TRANSLUCENT_FEATURES.register(context -> {

            if (SelectedBlocks.BLOCKS.isEmpty())
                return;

            /*
             render boxes
             render tracers
             render distance labels
            */

        });

    }

}
