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

    private static final int SCAN_RADIUS = 48;
    private static final int MAX_RENDERED_BLOCKS = 512;
    private static final int GREEN = 0xFF00FF00;
    private static final List<BlockPos> HIGHLIGHTED_BLOCKS = new ArrayList<>();
    private static int ticksUntilScan = 0;

    public static void register() {

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (ticksUntilScan > 0) {
                ticksUntilScan--;
                return;
            }

            ticksUntilScan = 10;
            rebuildHighlightedBlocks(client);
        });

        LevelRenderEvents.AFTER_TRANSLUCENT_FEATURES.register(context -> {

            Minecraft minecraft = Minecraft.getInstance();
            Player player = minecraft.player;
            if (player == null || minecraft.level == null || HIGHLIGHTED_BLOCKS.isEmpty()) {
                return;
            }

            PoseStack matrices = context.matrixStack();
            MultiBufferSource consumers = context.consumers();
            Camera camera = context.camera();
            Vec3 cameraPos = camera.getPosition();
            VertexConsumer lines = consumers.getBuffer(RenderType.lines());

            matrices.pushPose();
            matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

            for (BlockPos pos : HIGHLIGHTED_BLOCKS) {
                Vec3 center = pos.getCenter();
                AABB box = new AABB(pos).inflate(0.002);
                LevelRenderer.renderLineBox(matrices, lines, box, 0.0F, 1.0F, 0.0F, 1.0F);
                renderTracer(matrices, lines, cameraPos, center);
                renderDistanceLabel(minecraft.font, matrices, consumers, camera, player.position(), center);
            }

            matrices.popPose();

        });

    }

    private static void rebuildHighlightedBlocks(Minecraft minecraft) {
        HIGHLIGHTED_BLOCKS.clear();

        Player player = minecraft.player;
        if (player == null || minecraft.level == null || SelectedBlocks.BLOCKS.isEmpty()) {
            return;
        }

        BlockPos playerPos = player.blockPosition();
        for (int x = -SCAN_RADIUS; x <= SCAN_RADIUS && HIGHLIGHTED_BLOCKS.size() < MAX_RENDERED_BLOCKS; x++) {
            for (int y = -SCAN_RADIUS; y <= SCAN_RADIUS && HIGHLIGHTED_BLOCKS.size() < MAX_RENDERED_BLOCKS; y++) {
                for (int z = -SCAN_RADIUS; z <= SCAN_RADIUS && HIGHLIGHTED_BLOCKS.size() < MAX_RENDERED_BLOCKS; z++) {
                    BlockPos pos = playerPos.offset(x, y, z);
                    String blockId = BuiltInRegistries.BLOCK.getKey(minecraft.level.getBlockState(pos).getBlock()).toString();
                    if (SelectedBlocks.BLOCKS.contains(blockId)) {
                        HIGHLIGHTED_BLOCKS.add(pos);
                    }
                }
            }
        }
    }

    private static void renderTracer(PoseStack matrices, VertexConsumer lines, Vec3 cameraPos, Vec3 target) {
        lines.addVertex(matrices.last().pose(), (float) cameraPos.x, (float) cameraPos.y, (float) cameraPos.z)
                .setColor(0.0F, 1.0F, 0.0F, 0.85F);
        lines.addVertex(matrices.last().pose(), (float) target.x, (float) target.y, (float) target.z)
                .setColor(0.0F, 1.0F, 0.0F, 0.85F);
    }

    private static void renderDistanceLabel(Font font, PoseStack matrices, MultiBufferSource consumers, Camera camera, Vec3 playerPos, Vec3 target) {
        double distance = playerPos.distanceTo(target);
        Component label = Component.literal(String.format(Locale.ROOT, "%.1fm", distance));
        float textWidth = font.width(label);

        matrices.pushPose();
        matrices.translate(target.x, target.y + 1.15, target.z);
        matrices.mulPose(camera.rotation());
        matrices.scale(-0.025F, -0.025F, 0.025F);
        font.drawInBatch(label, -textWidth / 2.0F, 0.0F, GREEN, false, matrices.last().pose(), consumers, Font.DisplayMode.NORMAL, 0, 15728880);
        matrices.popPose();
    }

}
