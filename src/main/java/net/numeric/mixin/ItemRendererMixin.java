package net.numeric.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.item.BundleItem;
import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemRenderer.class)
public class ItemRendererMixin {
    private String subscriptOfNumberString(String string) {
        return string.replace('0', '₀')
                .replace('1', '₁')
                .replace('2', '₂')
                .replace('3', '₃')
                .replace('4', '₄')
                .replace('5', '₅')
                .replace('6', '₆')
                .replace('7', '₇')
                .replace('8', '₈')
                .replace('9', '₉');
    }
    @Redirect(at = @At(ordinal = 0, value = "INVOKE",target = "Lnet/minecraft/client/render/item/ItemRenderer;renderGuiQuad(Lnet/minecraft/client/render/BufferBuilder;IIIIIIII)V"), method = "renderGuiItemOverlay(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V")
    private void renderGuiDurabilityUnderlay(ItemRenderer instance, BufferBuilder buffer, int x, int y, int width, int height, int red, int green, int blue, int alpha) {
        return; //don't need this
    }
    @Redirect(at = @At(ordinal = 1, value = "INVOKE",target = "Lnet/minecraft/client/render/item/ItemRenderer;renderGuiQuad(Lnet/minecraft/client/render/BufferBuilder;IIIIIIII)V"), method = "renderGuiItemOverlay(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V")
    private void renderGuiDurability(ItemRenderer instance, BufferBuilder buffer, int x, int y, int width, int height, int red, int green, int blue, int alpha, TextRenderer renderer, ItemStack stack, int _x, int _y, @Nullable String countLabel) {
        int i;
        if (stack.getItem() instanceof BundleItem) {
            i = (int)(BundleItem.getAmountFilled(stack) * 100);
        }
        else {
            i = stack.getMaxDamage() - stack.getDamage();
        }
        if (stack.getCount() != 1 || countLabel != null) { //catch stacked armor and such
            y -= 10;
        }
        MatrixStack matrixStack = new MatrixStack();
        matrixStack.translate(0.0, 0.0, instance.zOffset + 200.0f);
        VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
        String string = subscriptOfNumberString(String.valueOf(i));
        renderer.draw(string,
                (float) (x + Math.max(15 - renderer.getWidth(string),-3)), (float) (y - 5), ((alpha & 0xFF) << 24) | ((red & 0xFF) << 16) | ((green & 0xFF) << 8) | (blue & 0xFF), true, matrixStack.peek().getPositionMatrix(), immediate, false, 0, LightmapTextureManager.MAX_LIGHT_COORDINATE);
        immediate.draw();
    }
    @Redirect(at = @At(ordinal = 2, value = "INVOKE",target = "Lnet/minecraft/client/render/item/ItemRenderer;renderGuiQuad(Lnet/minecraft/client/render/BufferBuilder;IIIIIIII)V"), method = "renderGuiItemOverlay(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V")
    private void renderGuiQuadCooldown(ItemRenderer instance, BufferBuilder buffer, int x, int y, int width, int height, int red, int green, int blue, int alpha, TextRenderer renderer, ItemStack stack, int _x, int _y, @Nullable String countLabel) {
        MinecraftClient clientInstance = MinecraftClient.getInstance();
        ClientPlayerEntity clientPlayerEntity = clientInstance.player;
        ItemCooldownManager itemCooldownManager = clientPlayerEntity.getItemCooldownManager();
        y += height; //remove the cooldown's y offset
        if (stack.getCount() != 1 || countLabel != null || stack.isItemBarVisible()) {
            y -= 10;
            if ((stack.getCount() != 1 || countLabel != null) & stack.isItemBarVisible()) {
                y -= 7; //catch stacked and damaged stuff
            }
        }
        float start = itemCooldownManager.getCooldownProgress(stack.getItem(),0.1f);
        float end = itemCooldownManager.getCooldownProgress(stack.getItem(),0);
        float cooldownDuration = 0.1f/((end-start)); // hack but it has to be done
        float cooldownTicks = cooldownDuration * itemCooldownManager.getCooldownProgress(stack.getItem(),clientInstance.getTickDelta());
        String display = String.format("%.1f",(cooldownTicks)/20);
        MatrixStack matrixStack = new MatrixStack();
        matrixStack.translate(0.0, 0.0, instance.zOffset + 200.0f);
        VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
        String string = subscriptOfNumberString(display);
        renderer.draw(string,
                (float) (x + Math.max(17 - renderer.getWidth(string),-3)), (float) (y - 8), ((alpha & 0xFF) << 24) | ((red & 0xFF) << 16) | ((green & 0xFF) << 8) | (blue & 0xFF), true, matrixStack.peek().getPositionMatrix(), immediate, false, 0, LightmapTextureManager.MAX_LIGHT_COORDINATE);
        immediate.draw();
    }
}
