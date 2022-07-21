package net.fabricmc.example.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.item.ItemRenderer;

@Mixin(ItemRenderer.class)
public interface RenderGUIQuadInvoker {
    @Invoker("renderGuiQuad")
    void renderGuiQuadInvoker(BufferBuilder buffer, int x, int y, int width, int height, int red, int green, int blue, int alpha);
}