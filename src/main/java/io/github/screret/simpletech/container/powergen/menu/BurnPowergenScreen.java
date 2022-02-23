package io.github.screret.simpletech.container.powergen.menu;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.screret.simpletech.SimpleTech;
import io.github.screret.simpletech.container.BaseMachineContainer;
import io.github.screret.simpletech.container.menu.BaseMachineScreen;
import io.github.screret.simpletech.container.powergen.BurnPowergenContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class BurnPowergenScreen extends AbstractContainerScreen<BurnPowergenContainer> {

    private final ResourceLocation GUI = new ResourceLocation(SimpleTech.MOD_ID, "textures/gui/base_gui.png");

    public BurnPowergenScreen(BurnPowergenContainer container, Inventory inv, Component name) {
        super(container, inv, name);
        this.imageWidth = 176;
        this.imageHeight = 186;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.renderBars(matrixStack, partialTicks, mouseX, mouseY);
        this.renderTooltip(matrixStack, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(PoseStack matrixStack, int mouseX, int mouseY) {
        super.renderLabels(matrixStack, mouseX, mouseY);
        drawString(matrixStack, Minecraft.getInstance().font, "Energy: " + menu.getEnergy(), 10, 10, 0xffffff);
    }

    @Override
    protected void renderBg(PoseStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, GUI);
        int relX = (this.width - this.imageWidth) / 2;
        int relY = (this.height - this.imageHeight) / 2;
        this.blit(matrixStack, relX, relY, 0, 0, this.imageWidth, this.imageHeight);
    }

    protected void renderBars(PoseStack matrixStack, float partialTicks, int mouseX, int mouseY){
        RenderSystem.setShaderTexture(0, GUI);
        /*if (this.menu.isOn()) {
            int k = this.menu.getProgress();
            this.blit(matrixStack, this.leftPos + 67, this.topPos + 37 + 12 - k, 201, 16 - k, 14, k + 1);
        }*/
        if(this.menu.getEnergy() > 0){
            int k = this.menu.getProgress();
            this.blit(matrixStack, this.leftPos + 8, this.topPos + 18 + 69 - k, 0, 186 + 69 - k, 16, k + 1);
        }
    }

}