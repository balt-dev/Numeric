package net.fabricmc.example.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.network.ClientPlayerEntity;
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
    @Redirect(at = @At("INVOKE"), method = "renderGuiItemOverlay")
    public void renderGuiItemOverlay(ItemRenderer itemRenderer, TextRenderer renderer, ItemStack stack, int x, int y, @Nullable String countLabel) {
        ClientPlayerEntity clientPlayerEntity;
        float f;
        int color = 0xFFFFFF;
        MinecraftClient clientInstance = MinecraftClient.getInstance();
        if (stack.isEmpty()) {
            return;
        }
        if (stack.getItem() instanceof BundleItem) {
            countLabel = String.valueOf((int) (BundleItem.getAmountFilled(stack) * 64));
            color = stack.getItemBarColor();
        }
        MatrixStack matrixStack = new MatrixStack();
        if (stack.getCount() != 1 || countLabel != null) {
            String string = countLabel == null ? String.valueOf(stack.getCount()) : countLabel;
            matrixStack.translate(0.0, 0.0, itemRenderer.zOffset + 200.0f);
            VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
            renderer.draw(string, (float)(x + 19 - 2 - renderer.getWidth(string)), (float)(y + 9), color, true, matrixStack.peek().getPositionMatrix(), immediate, false, 0, LightmapTextureManager.MAX_LIGHT_COORDINATE);
            immediate.draw();
        }
        if (stack.isItemBarVisible() & !(stack.getItem() instanceof BundleItem)) {
            color = stack.getItemBarColor();
            matrixStack.translate(0.0, 0.0, itemRenderer.zOffset + 200.0f);
            VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
            int i = stack.getMaxDamage() - stack.getDamage();
            String string = String.valueOf(i)
                    .replace('0', '₀')
                    .replace('1', '₁')
                    .replace('2', '₂')
                    .replace('3', '₃')
                    .replace('4', '₄')
                    .replace('5', '₅')
                    .replace('6', '₆')
                    .replace('7', '₇')
                    .replace('8', '₈')
                    .replace('9', '₉');
            renderer.draw(string,
                    (float)(x + 19 - 2 - renderer.getWidth(string)), (float)(y + 8), color, true, matrixStack.peek().getPositionMatrix(), immediate, false, 0, LightmapTextureManager.MAX_LIGHT_COORDINATE);
            immediate.draw();
        }
        f = (clientPlayerEntity = clientInstance.player) == null ? 0.0f : clientPlayerEntity.getItemCooldownManager().getCooldownProgress(stack.getItem(), clientInstance.getTickDelta());
        if (f > 0.0f) {
            ItemCooldownManager itemCooldownManager = clientPlayerEntity.getItemCooldownManager();
            float start = itemCooldownManager.getCooldownProgress(stack.getItem(),1);
            float end = itemCooldownManager.getCooldownProgress(stack.getItem(),0);
            float cooldownDuration = 1f/((end-start)); // hack but it has to be done
            float cooldownTicks = cooldownDuration * itemCooldownManager.getCooldownProgress(stack.getItem(),clientInstance.getTickDelta());
            String display;
            if (cooldownTicks >= 1200) {
                display = (int) (cooldownTicks / 1200) + ":" + String.format("%02d",(int)(cooldownTicks%1200)/20);
            } else {
                display = String.format("%.1f",(cooldownTicks)/20);
            }
            matrixStack.translate(0.0, 0.0, itemRenderer.zOffset + 200.0f);
            VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
            renderer.draw(display, (float)(x + 19 - 2 - renderer.getWidth(display)), (float)(y + (stack.getCount() != 1 ? 0 : 9)), 0x7fffffff, false, matrixStack.peek().getPositionMatrix(), immediate, false, 0, LightmapTextureManager.MAX_LIGHT_COORDINATE);
            immediate.draw();
        }
    }
}
