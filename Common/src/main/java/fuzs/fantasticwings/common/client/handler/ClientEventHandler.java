package fuzs.fantasticwings.common.client.handler;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import fuzs.fantasticwings.common.FantasticWings;
import fuzs.fantasticwings.common.client.audio.WingsOnPlayerSoundInstance;
import fuzs.fantasticwings.common.client.flight.FlightView;
import fuzs.fantasticwings.common.client.flight.apparatus.WingForm;
import fuzs.fantasticwings.common.client.init.ClientModRegistry;
import fuzs.fantasticwings.common.flight.Flight;
import fuzs.fantasticwings.common.init.ModRegistry;
import fuzs.fantasticwings.common.util.MathHelper;
import fuzs.puzzleslib.common.api.client.renderer.v1.RenderStateExtraData;
import fuzs.puzzleslib.common.api.event.v1.core.EventResult;
import fuzs.puzzleslib.common.api.event.v1.data.MutableFloat;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.util.Mth;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Optional;

public class ClientEventHandler {
    public static final ContextKey<Optional<WingForm.FormRendererState<?>>> WING_FORM_KEY = new ContextKey<>(
            FantasticWings.id("wing_form"));
    public static final ContextKey<Float> FLYING_AMOUNT_KEY = new ContextKey<>(FantasticWings.id("flying_amount"));
    public static final ContextKey<Float> ROLL_KEY = new ContextKey<>(FantasticWings.id("roll"));
    public static final ContextKey<Float> PITCH_KEY = new ContextKey<>(FantasticWings.id("pitch"));

    public static void setupPlayerAnim(AvatarRenderState renderState, HumanoidModel<AvatarRenderState> model) {
        float flyingAmount = RenderStateExtraData.getOrDefault(renderState, FLYING_AMOUNT_KEY, 0.0F);
        if (flyingAmount != 0.0F) {
            model.head.xRot = MathHelper.toRadians(MathHelper.lerp(renderState.xRot,
                    renderState.xRot / 4.0F - 90.0F,
                    flyingAmount));
            model.leftArm.xRot = MathHelper.lerp(model.leftArm.xRot, -3.2F, flyingAmount);
            model.rightArm.xRot = MathHelper.lerp(model.rightArm.xRot, -3.2F, flyingAmount);
            model.leftLeg.xRot = MathHelper.lerp(model.leftLeg.xRot, 0.0F, flyingAmount);
            model.rightLeg.xRot = MathHelper.lerp(model.rightLeg.xRot, 0.0F, flyingAmount);
        }
    }

    public static void setupPlayerRotations(AvatarRenderState renderState, PoseStack poseStack) {
        float flyingAmount = RenderStateExtraData.getOrDefault(renderState, FLYING_AMOUNT_KEY, 0.0F);
        if (flyingAmount > 0.0F) {
            float roll = RenderStateExtraData.getOrDefault(renderState, ROLL_KEY, 0.0F);
            float pitch = RenderStateExtraData.getOrDefault(renderState, PITCH_KEY, 0.0F);
            poseStack.mulPose(Axis.ZP.rotationDegrees(MathHelper.lerpDegrees(0.0F, roll, flyingAmount)));
            poseStack.mulPose(Axis.XP.rotationDegrees(MathHelper.lerpDegrees(0.0F, pitch, flyingAmount)));
            poseStack.translate(0.0, -1.2 * MathHelper.easeInOut(flyingAmount), 0.0);
        }
    }

    public static void onComputeCameraAngles(Camera camera, float partialTick, MutableFloat pitch, MutableFloat yaw, MutableFloat roll) {
        Flight flight = ModRegistry.FLIGHT_ATTACHMENT_TYPE.getOrDefault(camera.entity(), Flight.VOID);
        float flyingAmount = flight.getFlyingAmount(partialTick);
        if (flyingAmount > 0.0F) {
            Player player = (Player) camera.entity();
            float newRoll = MathHelper.lerpDegrees(player.yBodyRotO - player.yRotO,
                    player.yBodyRot - player.getYRot(),
                    partialTick);
            roll.accept(MathHelper.lerpDegrees(0.0F, -newRoll * 0.25F, flyingAmount));
        }
    }

    public static EventResult onEntityLoad(Entity entity, ClientLevel level) {
        if (entity instanceof LocalPlayer player) {
            Minecraft.getInstance().getSoundManager().play(new WingsOnPlayerSoundInstance(player));
        }

        return EventResult.PASS;
    }

    public static void onExtractEntityRenderState(Entity entity, EntityRenderState entityRenderState, float partialTick) {
        if (entity instanceof AbstractClientPlayer player && entityRenderState instanceof AvatarRenderState state) {
            FlightView flightView = ClientModRegistry.FLIGHT_VIEW_ATTACHMENT_TYPE.getOrDefault(entity, FlightView.VOID);
            flightView.ifFormPresent((WingForm.FormRenderer<?> form) -> {
                RenderStateExtraData.set(entityRenderState, WING_FORM_KEY, Optional.of(form.pack(partialTick)));
            });
            RenderStateExtraData.set(entityRenderState,
                    FLYING_AMOUNT_KEY,
                    ModRegistry.FLIGHT_ATTACHMENT_TYPE.getOrDefault(player, Flight.VOID).getFlyingAmount(partialTick));
            float roll = MathHelper.lerpDegrees(player.yBodyRotO - player.yRotO,
                    player.yBodyRot - player.getYRot(),
                    partialTick);
            RenderStateExtraData.set(entityRenderState, ROLL_KEY, roll);
            float pitch = -MathHelper.lerpDegrees(player.xRotO, player.getXRot(), partialTick) - 90.0F;
            RenderStateExtraData.set(entityRenderState, PITCH_KEY, pitch);
            if (mustPreventCrouchingOffset(player)) {
                state.isCrouching = false;
            }

            if (flightView.getFlight(player).wings().isPresent()) {
                state.showCape = false;
            }
        }
    }

    private static boolean mustPreventCrouchingOffset(Player player) {
        if (player.isCrouching()) {
            Flight flight = ModRegistry.FLIGHT_ATTACHMENT_TYPE.getOrDefault(player, Flight.VOID);
            // crouching increases falling speed when not flying but having wings,
            // treat this just like creative mode descending where the player model is not offset for crouching
            return flight.isFlying() || !flight.isEmpty() && player.getDeltaMovement().y() < -0.5;
        }

        return false;
    }

    public static EventResult onRenderOffHand(ItemInHandRenderer itemInHandRenderer, InteractionHand interactionHand, AbstractClientPlayer player, HumanoidArm humanoidArm, ItemStack itemStack, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int combinedLight, float partialTick, float interpolatedPitch, float swingProgress, float equipProgress) {
        if (itemStack.isEmpty() && !player.isScoping() && !player.isInvisible()) {
            if (!itemInHandRenderer.mainHandItem.is(Items.FILLED_MAP)
                    && ModRegistry.FLIGHT_ATTACHMENT_TYPE.getOrDefault(player, Flight.VOID).isFlying()) {
                itemInHandRenderer.renderPlayerArm(poseStack,
                        submitNodeCollector,
                        combinedLight,
                        equipProgress,
                        swingProgress,
                        player.getMainArm().getOpposite());
                return EventResult.INTERRUPT;
            }
        }

        return EventResult.PASS;
    }

    public static void onTurn(Entity entity, float deltaYaw) {
        if (entity instanceof Player player && ModRegistry.FLIGHT_ATTACHMENT_TYPE.getOrDefault(player, Flight.VOID)
                .isFlying()) {
            float theta = Mth.wrapDegrees(player.getYRot() - player.yBodyRot);
            if (theta < -50.0F || theta > 50.0F) {
                player.yBodyRot += deltaYaw;
                player.yBodyRotO += deltaYaw;
            }
        }
    }
}
