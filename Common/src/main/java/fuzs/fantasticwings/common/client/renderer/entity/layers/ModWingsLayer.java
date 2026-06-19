package fuzs.fantasticwings.common.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import fuzs.fantasticwings.common.client.flight.apparatus.WingForm;
import fuzs.fantasticwings.common.client.handler.ClientEventHandler;
import fuzs.fantasticwings.common.init.ModRegistry;
import fuzs.puzzleslib.common.api.client.renderer.v1.RenderStateExtraData;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.entity.EntityType;

import java.util.Optional;

public class ModWingsLayer extends RenderLayer<AvatarRenderState, PlayerModel> {

    public ModWingsLayer(RenderLayerParent<AvatarRenderState, PlayerModel> renderLayerParent, EntityRendererProvider.Context context) {
        super(renderLayerParent);
    }

    public static void addLivingEntityRenderLayers(EntityType<?> entityType, LivingEntityRenderer<?, ?, ?> entityRenderer, EntityRendererProvider.Context context) {
        if (entityRenderer instanceof AvatarRenderer<?> playerRenderer) {
            playerRenderer.addLayer(new ModWingsLayer(playerRenderer, context));
        }
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector nodeCollector, int packedLight, AvatarRenderState renderState, float yRot, float xRot) {
        if (!renderState.isInvisible && !renderState.chestEquipment.is(ModRegistry.WING_OBSTRUCTIONS)) {
            RenderStateExtraData.getOrDefault(renderState, ClientEventHandler.WING_FORM_KEY, Optional.empty())
                    .ifPresent((WingForm.FormRendererState<?> form) -> {
                        poseStack.pushPose();
                        poseStack.translate(0.0, -0.0625, 0.0);
                        if (!renderState.chestEquipment.isEmpty()) {
                            poseStack.translate(0.0, 0.0, 0.0625);
                        }

                        this.getParentModel().body.translateAndRotate(poseStack);
                        form.submitModel(poseStack,
                                nodeCollector,
                                RenderTypes::entityCutoutCull,
                                packedLight,
                                renderState.outlineColor);
                        poseStack.popPose();
                    });
        }
    }
}
