package fuzs.fantasticwings.client.handler;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import fuzs.fantasticwings.client.audio.WingsSound;
import fuzs.fantasticwings.client.flight.FlightViewCapability;
import fuzs.fantasticwings.client.init.ClientModRegistry;
import fuzs.fantasticwings.flight.FlightCapability;
import fuzs.fantasticwings.init.ModRegistry;
import fuzs.fantasticwings.util.MathHelper;
import fuzs.puzzleslib.api.event.v1.core.EventResult;
import fuzs.puzzleslib.api.event.v1.data.MutableFloat;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ClientEventHandler {

    public static void onAnimatePlayerModel(Player player, PlayerModel<?> model, float ticksExisted, float pitch) {
        FlightCapability flightCapability = ModRegistry.FLIGHT_CAPABILITY.get(player);
        float delta = ticksExisted - player.tickCount;
        float flyingAmount = flightCapability.getFlyingAmount(delta);
        if (flyingAmount != 0.0F) {
            model.head.xRot = MathHelper.toRadians(MathHelper.lerp(pitch, pitch / 4.0F - 90.0F, flyingAmount));
            model.leftArm.xRot = MathHelper.lerp(model.leftArm.xRot, -3.2F, flyingAmount);
            model.rightArm.xRot = MathHelper.lerp(model.rightArm.xRot, -3.2F, flyingAmount);
            model.leftLeg.xRot = MathHelper.lerp(model.leftLeg.xRot, 0.0F, flyingAmount);
            model.rightLeg.xRot = MathHelper.lerp(model.rightLeg.xRot, 0.0F, flyingAmount);
            model.hat.copyFrom(model.head);
        }
    }

    public static void onApplyRotations(Player player, PoseStack poseStack, float delta) {
        FlightCapability flightCapability = ModRegistry.FLIGHT_CAPABILITY.get(player);
        float amt = flightCapability.getFlyingAmount(delta);
        if (amt > 0.0F) {
            float roll = MathHelper.lerpDegrees(player.yBodyRotO - player.yRotO,
                    player.yBodyRot - player.getYRot(),
                    delta);
            float pitch = -MathHelper.lerpDegrees(player.xRotO, player.getXRot(), delta) - 90.0F;
            poseStack.mulPose(Axis.ZP.rotationDegrees(MathHelper.lerpDegrees(0.0F, roll, amt)));
            poseStack.mulPose(Axis.XP.rotationDegrees(MathHelper.lerpDegrees(0.0F, pitch, amt)));
            poseStack.translate(0.0D, -1.2D * MathHelper.easeInOut(amt), 0.0D);
        }
    }

    public static void onComputeCameraAngles(GameRenderer renderer, Camera camera, float partialTick, MutableFloat pitch, MutableFloat yaw, MutableFloat roll) {
        LivingEntity cameraEntity = (LivingEntity) camera.getEntity();
        ModRegistry.FLIGHT_CAPABILITY.getIfProvided(cameraEntity).ifPresent(flightViewCapability -> {
            float flyingAmount = flightViewCapability.getFlyingAmount(partialTick);
            if (flyingAmount > 0.0F) {
                float newRoll = MathHelper.lerpDegrees(cameraEntity.yBodyRotO - cameraEntity.yRotO,
                        cameraEntity.yBodyRot - cameraEntity.getYRot(),
                        partialTick);
                roll.accept(MathHelper.lerpDegrees(0.0F, -newRoll * 0.25F, flyingAmount));
            }
        });
    }

    public static EventResult onEntityLoad(Entity entity, ClientLevel level) {
        if (entity instanceof LocalPlayer localPlayer) {
            FlightCapability flightCapability = ModRegistry.FLIGHT_CAPABILITY.get(localPlayer);
            Minecraft.getInstance().getSoundManager().play(new WingsSound(localPlayer, flightCapability));
        }

        return EventResult.PASS;
    }

    public static void onEndPlayerTick(Player player) {
        ClientModRegistry.FLIGHT_VIEW_CAPABILITY.getIfProvided(player).ifPresent(FlightViewCapability::tick);
    }

    public static EventResult onRenderHand(Player player, InteractionHand interactionHand, ItemStack itemStack, PoseStack poseStack, MultiBufferSource multiBufferSource, int lightCoords, float partialTick, float interpolatedPitch, float swingProgress, float equipProgress) {
        ItemInHandRenderer itemInHandRenderer = Minecraft.getInstance().gameRenderer.itemInHandRenderer;
        if (interactionHand == InteractionHand.OFF_HAND && itemStack.isEmpty() && !player.isScoping()
                && !player.isInvisible()) {
            if (!itemInHandRenderer.mainHandItem.is(Items.FILLED_MAP) && ModRegistry.FLIGHT_CAPABILITY.get(player)
                    .isFlying()) {
                itemInHandRenderer.renderPlayerArm(poseStack,
                        multiBufferSource,
                        lightCoords,
                        equipProgress,
                        swingProgress,
                        player.getMainArm().getOpposite());
                return EventResult.INTERRUPT;
            }
        }
        return EventResult.PASS;
    }

    public static void onTurn(Entity entity, float deltaYaw) {
        if (entity instanceof Player player && ModRegistry.FLIGHT_CAPABILITY.get(player).isFlying()) {
            float theta = Mth.wrapDegrees(player.getYRot() - player.yBodyRot);
            if (theta < -50.0F || theta > 50.0F) {
                player.yBodyRot += deltaYaw;
                player.yBodyRotO += deltaYaw;
            }
        }
    }
}
