package com.example.client;

import com.example.SelectedBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BlockMenuScreen extends Screen {

    private static final int ROW_HEIGHT = 22;
    private static String searchText = "";
    private static int availableScroll = 0;
    private static int selectedScroll = 0;

    private EditBox searchBox;
    private int availableListX;
    private int selectedListX;
    private int listY;
    private int listWidth;
    private int visibleRows;

    public BlockMenuScreen() {
        super(Component.literal("Choose blocks"));
    }

    @Override
    protected void init() {
        this.listWidth = Math.max(170, Math.min(240, (this.width - 54) / 2));
        this.availableListX = 16;
        this.selectedListX = this.availableListX + this.listWidth + 22;
        this.listY = 78;
        this.visibleRows = Math.max(3, (this.height - this.listY - 44) / ROW_HEIGHT);

        this.addRenderableWidget(
                new StringWidget(
                        0,
                        20,
                        this.width,
                        20,
                        this.title,
                        this.font
                )
        );

        this.addRenderableWidget(
                new StringWidget(
                        this.availableListX,
                        46,
                        this.listWidth,
                        20,
                        Component.literal("All blocks"),
                        this.font
                )
        );

        this.addRenderableWidget(
                new StringWidget(
                        this.selectedListX,
                        46,
                        this.listWidth,
                        20,
                        Component.literal("Highlighted blocks"),
                        this.font
                )
        );

        this.searchBox = new EditBox(
                this.font,
                this.availableListX,
                58,
                this.listWidth,
                20,
                Component.literal("Search blocks")
        );

        this.searchBox.setHint(
                Component.literal("Search minecraft blocks")
        );

        this.searchBox.setValue(searchText);

        this.searchBox.setResponder(value -> {
            searchText = value;
            availableScroll = 0;
            refreshWidgets();
        });

        this.addRenderableWidget(this.searchBox);

        this.setInitialFocus(this.searchBox);

        addAvailableBlockRows();
        addSelectedBlockRows();

        this.addRenderableWidget(
                Button.builder(
                                Component.literal("Done"),
                                button -> onClose()
                        )
                        .bounds(
                                (this.width - 100) / 2,
                                this.height - 32,
                                100,
                                20
                        )
                        .build()
        );
    }

    private void addAvailableBlockRows() {

        List<String> blocks = getFilteredBlocks();

        availableScroll =
                clampScroll(
                        availableScroll,
                        blocks.size()
                );

        int end =
                Math.min(
                        blocks.size(),
                        availableScroll + this.visibleRows
                );

        for (int index = availableScroll;
             index < end;
             index++) {

            String block = blocks.get(index);

            int y =
                    this.listY +
                    (index - availableScroll) *
                    ROW_HEIGHT;

            this.addRenderableWidget(

                    Button.builder(

                                    Component.literal(
                                            fitText(
                                                    block,
                                                    this.listWidth - 8
                                            )
                                    ),

                                    button -> {

                                        if (!SelectedBlocks.BLOCKS.contains(block)) {
                                            SelectedBlocks.BLOCKS.add(block);
                                        }

                                        refreshWidgets();
                                    }

                            )

                            .bounds(
                                    this.availableListX,
                                    y,
                                    this.listWidth,
                                    20
                            )

                            .build()
            );
        }

        if (blocks.isEmpty()) {

            this.addRenderableWidget(

                    new StringWidget(
                            this.availableListX,
                            this.listY,
                            this.listWidth,
                            20,
                            Component.literal(
                                    "No matching blocks"
                            ),
                            this.font
                    )
            );
        }
    }

    private void addSelectedBlockRows() {

        List<String> blocks =
                new ArrayList<>(
                        SelectedBlocks.BLOCKS
                );

        blocks.sort(
                Comparator.naturalOrder()
        );

        selectedScroll =
                clampScroll(
                        selectedScroll,
                        blocks.size()
                );

        int end =
                Math.min(
                        blocks.size(),
                        selectedScroll + this.visibleRows
                );

        for (int index = selectedScroll;
             index < end;
             index++) {

            String block = blocks.get(index);

            int y =
                    this.listY +
                    (index - selectedScroll) *
                    ROW_HEIGHT;

            this.addRenderableWidget(

                    Button.builder(

                                    Component.literal("Remove"),

                                    button -> {

                                        SelectedBlocks.BLOCKS.remove(block);

                                        refreshWidgets();
                                    }

                            )

                            .bounds(
                                    this.selectedListX +
                                    this.listWidth -
                                    66,

                                    y,

                                    66,

                                    20
                            )

                            .build()
            );

            this.addRenderableWidget(

                    new StringWidget(

                            this.selectedListX,

                            y,

                            this.listWidth - 70,

                            20,

                            Component.literal(

                                    fitText(
                                            block,
                                            this.listWidth - 74
                                    )
                            ),

                            this.font
                    )
            );
        }

        if (blocks.isEmpty()) {

            this.addRenderableWidget(

                    new StringWidget(

                            this.selectedListX,

                            this.listY,

                            this.listWidth,

                            20,

                            Component.literal(
                                    "No highlighted blocks"
                            ),

                            this.font
                    )
            );
        }
    }

    private List<String> getFilteredBlocks() {

        String filter =
                searchText
                        .trim()
                        .toLowerCase();

        List<String> blocks =
                new ArrayList<>();

        BuiltInRegistries.BLOCK
                .keySet()
                .forEach(id -> {

                    String block =
                            id.toString();

                    if (filter.isEmpty()
                            ||
                            block.toLowerCase()
                                    .contains(filter)) {

                        blocks.add(block);
                    }
                });

        blocks.sort(
                Comparator.naturalOrder()
        );

        return blocks;
    }

    private int clampScroll(
            int scroll,
            int listSize
    ) {

        return Math.max(

                0,

                Math.min(

                        scroll,

                        Math.max(
                                0,
                                listSize - this.visibleRows
                        )
                )
        );
    }

    private String fitText(
            String text,
            int maxWidth
    ) {

        if (this.font.width(text)
                <= maxWidth) {

            return text;
        }

        String suffix = "...";

        int suffixWidth =
                this.font.width(suffix);

        StringBuilder fitted =
                new StringBuilder();

        for (int i = 0;
             i < text.length();
             i++) {

            String next =
                    fitted.toString()
                    + text.charAt(i);

            if (this.font.width(next)
                    + suffixWidth
                    > maxWidth) {

                break;
            }

            fitted.append(
                    text.charAt(i)
            );
        }

        return fitted + suffix;
    }

    @Override
    public boolean keyPressed(
            int keyCode,
            int scanCode,
            int modifiers
    ) {

        if (this.searchBox != null
                &&
                this.searchBox.isFocused()
                &&
                (
                        keyCode ==
                        GLFW.GLFW_KEY_ENTER

                        ||

                        keyCode ==
                        GLFW.GLFW_KEY_KP_ENTER
                )
        ) {

            List<String> blocks =
                    getFilteredBlocks();

            if (!blocks.isEmpty()) {

                String block =
                        blocks.get(0);

                if (!SelectedBlocks.BLOCKS.contains(block)) {
                    SelectedBlocks.BLOCKS.add(block);
                }

                refreshWidgets();
            }

            return true;
        }

        return super.keyPressed(
                keyCode,
                scanCode,
                modifiers
        );
    }

    @Override
    public boolean mouseScrolled(
            double mouseX,
            double mouseY,
            double horizontalAmount,
            double verticalAmount
    ) {

        if (

                isInside(

                        mouseX,

                        mouseY,

                        this.availableListX,

                        this.listY,

                        this.listWidth,

                        this.visibleRows *
                        ROW_HEIGHT
                )
        ) {

            availableScroll =
                    clampScroll(

                            availableScroll
                            -
                            (int)Math.signum(
                                    verticalAmount
                            ),

                            getFilteredBlocks()
                                    .size()
                    );

            refreshWidgets();

            return true;
        }

        if (

                isInside(

                        mouseX,

                        mouseY,

                        this.selectedListX,

                        this.listY,

                        this.listWidth,

                        this.visibleRows *
                        ROW_HEIGHT
                )
        ) {

            selectedScroll =
                    clampScroll(

                            selectedScroll
                            -
                            (int)Math.signum(
                                    verticalAmount
                            ),

                            SelectedBlocks.BLOCKS
                                    .size()
                    );

            refreshWidgets();

            return true;
        }

        return super.mouseScrolled(
                mouseX,
                mouseY,
                horizontalAmount,
                verticalAmount
        );
    }

    private boolean isInside(
            double mouseX,
            double mouseY,
            int x,
            int y,
            int width,
            int height
    ) {

        return mouseX >= x
                &&
                mouseX < x + width
                &&
                mouseY >= y
                &&
                mouseY < y + height;
    }

    private void refreshWidgets() {

        this.clearWidgets();

        this.init();
    }
}
