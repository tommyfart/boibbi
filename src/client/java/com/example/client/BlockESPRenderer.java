package com.example.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.example.SelectedBlocks;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BlockESPRenderer {

    private static final int SCAN_RADIUS = 32;
    private static final int MAX_RENDERED_BLOCKS = 256;

    private static final List<BlockPos> HIGHLIGHTED_BLOCKS = new ArrayList<>();

    private static int ticksUntilScan = 0;

    public static void register() {

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (--ticksUntilScan > 0) return;

            ticksUntilScan = 20;
            rebuildHighlightedBlocks(client);
        });

        LevelRenderEvents.AFTER_TRANSLUCENT_FEATURES.register(context -> {

            Minecraft mc = Minecraft.getInstance();

            if (mc.player == null ||
                mc.level == null ||
                HIGHLIGHTED_BLOCKS.isEmpty()) {
                return;
            }

            PoseStack matrices = context.matrixStack();

            MultiBufferSource consumers = context.consumers();

            Camera camera = context.camera();

            Vec3 camPos = camera.getPosition();

            VertexConsumer lines =
                    consumers.getBuffer(RenderType.lines());

            matrices.pushPose();

            matrices.translate(
                    -camPos.x,
                    -camPos.y,
                    -camPos.z
            );

            for (BlockPos pos : HIGHLIGHTED_BLOCKS) {

                Vec3 center = pos.getCenter();

                double dist =
                        mc.player.position().distanceTo(center);

                AABB box =
                        new AABB(pos).inflate(0.002);

                LevelRenderer.renderLineBox(
                        matrices,
                        lines,
                        box,
                        0F,
                        1F,
                        0F,
                        1F
                );

                renderTracer(
                        matrices,
                        lines,
                        center
                );

                if (dist <= 30) {
                    renderDistanceLabel(
                            mc.font,
                            matrices,
                            consumers,
                            camera,
                            dist,
                            center
                    );
                }
            }

            matrices.popPose();
        });
    }

    private static void rebuildHighlightedBlocks(
            Minecraft mc
    ) {

        HIGHLIGHTED_BLOCKS.clear();

        Player player = mc.player;

        if (player == null ||
            mc.level == null ||
            SelectedBlocks.BLOCKS.isEmpty()) {
            return;
        }

        BlockPos playerPos =
                player.blockPosition();

        for (int x = -SCAN_RADIUS;
             x <= SCAN_RADIUS &&
             HIGHLIGHTED_BLOCKS.size() < MAX_RENDERED_BLOCKS;
             x++) {

            for (int y = -SCAN_RADIUS;
                 y <= SCAN_RADIUS &&
                 HIGHLIGHTED_BLOCKS.size() < MAX_RENDERED_BLOCKS;
                 y++) {

                for (int z = -SCAN_RADIUS;
                     z <= SCAN_RADIUS &&
                     HIGHLIGHTED_BLOCKS.size() < MAX_RENDERED_BLOCKS;
                     z++) {

                    BlockPos pos =
                            playerPos.offset(x,y,z);

                    String id =
                            BuiltInRegistries.BLOCK
                                    .getKey(
                                            mc.level
                                                    .getBlockState(pos)
                                                    .getBlock()
                                    )
                                    .toString();

                    if (SelectedBlocks.BLOCKS.contains(id)) {
                        HIGHLIGHTED_BLOCKS.add(pos);
                    }
                }
            }
        }
    }

    private static void renderTracer(
            PoseStack matrices,
            VertexConsumer lines,
            Vec3 target
    ) {

        lines.addVertex(
                        matrices.last().pose(),
                        0F,
                        0F,
                        0F
                )
                .setColor(
                        0F,
                        1F,
                        0F,
                        0.85F
                );

        lines.addVertex(
                        matrices.last().pose(),
                        (float) target.x,
                        (float) target.y,
                        (float) target.z
                )
                .setColor(
                        0F,
                        1F,
                        0F,
                        0.85F
                );
    }

    private static void renderDistanceLabel(
            Font font,
            PoseStack matrices,
            MultiBufferSource consumers,
            Camera camera,
            double distance,
            Vec3 target
    ) {

        Component text =
                Component.literal(
                        String.format(
                                Locale.ROOT,
                                "%.1fm",
                                distance
                        )
                );

        float width =
                (float) font.width(text);

        matrices.pushPose();

        matrices.translate(
                target.x,
                target.y + 1.15,
                target.z
        );

        matrices.mulPose(
                camera.rotation()
        );

        matrices.scale(
                -0.025F,
                -0.025F,
                0.025F
        );

        font.drawInBatch(
                text,
                -width / 2F,
                0,
                0xFF00FF00,
                false,
                matrices.last().pose(),
                consumers,
                Font.DisplayMode.NORMAL,
                0,
                15728880
        );

        matrices.popPose();
    }
}
