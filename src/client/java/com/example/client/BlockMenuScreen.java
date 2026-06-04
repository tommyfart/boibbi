package com.example.client;

import com.example.SelectedBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
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
                        rebuildWidgets();
                    })
                    .bounds(panelX + 186, y - 4, 74, 20)
                    .build());

            y += 24;
        }

        this.addRenderableWidget(Button.builder(Component.literal("Done"), button -> onClose())
                .bounds((this.width - 100) / 2, this.height - 32, 100, 20)
                .build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        int panelWidth = 280;
        int panelX = (this.width - panelWidth) / 2;

        graphics.drawCenteredString(this.font, this.title, this.width / 2, 24, 0xFFFFFF);

        int y = 80;
        List<String> blocks = new ArrayList<>(SelectedBlocks.BLOCKS);
        blocks.sort(Comparator.naturalOrder());

        if (blocks.isEmpty()) {
            graphics.drawCenteredString(this.font, Component.literal("No blocks selected"), this.width / 2, y, 0xA0A0A0);
            return;
        }

        for (String block : blocks) {
            if (y > this.height - 52) {
                graphics.drawString(this.font, Component.literal("..."), panelX + 10, y, 0xA0A0A0, false);
                break;
            }

            graphics.drawString(this.font, block, panelX + 10, y, 0xFFFFFF, false);
            y += 24;
        }
    }

    private void addBlock() {
        String blockId = this.searchBox.getValue().trim();
        if (blockId.isEmpty()) {
            return;
        }

        SelectedBlocks.BLOCKS.add(blockId);
        this.searchBox.setValue("");
        rebuildWidgets();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.searchBox != null && this.searchBox.isFocused()
                && (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER)) {
            addBlock();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void rebuildWidgets() {
        Minecraft.getInstance().setScreen(new BlockMenuScreen());
    }

}
