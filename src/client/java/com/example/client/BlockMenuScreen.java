package com.example.client;

import com.example.SelectedBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BlockMenuScreen extends Screen {

    private EditBox searchBox;

    protected BlockMenuScreen() {
        super(Component.literal("Choose blocks"));
    }

    @Override
    protected void init() {
        int panelWidth = 260;
        int panelX = (this.width - panelWidth) / 2;
        int y = 48;

        this.addRenderableWidget(new StringWidget(0, 20, this.width, 20, this.title, this.font));

        this.searchBox = new EditBox(this.font, panelX, y, 180, 20, Component.literal("Block id"));
        this.searchBox.setHint(Component.literal("minecraft:diamond_ore"));
        this.addRenderableWidget(this.searchBox);
        this.setInitialFocus(this.searchBox);

        this.addRenderableWidget(Button.builder(Component.literal("Add"), button -> addBlock())
                .bounds(panelX + 186, y, 74, 20)
                .build());

        y += 32;

        List<String> blocks = new ArrayList<>(SelectedBlocks.BLOCKS);
        blocks.sort(Comparator.naturalOrder());

        for (String block : blocks) {
            if (y > this.height - 48) {
                break;
            }

            this.addRenderableWidget(Button.builder(Component.literal("Remove"), button -> {
                        SelectedBlocks.BLOCKS.remove(block);
                        refreshWidgets();
                    })
                    .bounds(panelX + 186, y - 4, 74, 20)
                    .build());
            this.addRenderableWidget(new StringWidget(panelX, y - 4, 180, 20, Component.literal(block), this.font));

            y += 24;
        }

        if (blocks.isEmpty()) {
            this.addRenderableWidget(new StringWidget(panelX, y, panelWidth, 20, Component.literal("No blocks selected"), this.font));
        }

        this.addRenderableWidget(Button.builder(Component.literal("Done"), button -> onClose())
                .bounds((this.width - 100) / 2, this.height - 32, 100, 20)
                .build());
    }

    private void addBlock() {
        String blockId = this.searchBox.getValue().trim();
        if (blockId.isEmpty()) {
            return;
        }

        SelectedBlocks.BLOCKS.add(blockId);
        this.searchBox.setValue("");
        refreshWidgets();
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (this.searchBox != null && this.searchBox.isFocused()
                && (event.key() == GLFW.GLFW_KEY_ENTER || event.key() == GLFW.GLFW_KEY_KP_ENTER)) {
            addBlock();
            return true;
        }

        return super.keyPressed(event);
    }

    private void refreshWidgets() {
        Minecraft.getInstance().setScreen(new BlockMenuScreen());
    }

}
