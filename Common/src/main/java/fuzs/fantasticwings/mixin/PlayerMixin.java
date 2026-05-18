package fuzs.fantasticwings.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import fuzs.fantasticwings.init.ModRegistry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
abstract class PlayerMixin extends LivingEntity {

    protected PlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @ModifyExpressionValue(method = "updatePlayerPose",
                           at = @At(value = "INVOKE",
                                    target = "Lnet/minecraft/world/entity/player/Player;isFallFlying()Z"))
    protected boolean updatePlayerPose$0(boolean isFallFlying) {
        return isFallFlying || ModRegistry.FLIGHT_CAPABILITY.get(Player.class.cast(this)).isFlying();
    }

    @ModifyExpressionValue(method = "updatePlayerPose",
                           at = @At(value = "INVOKE",
                                    target = "Lnet/minecraft/world/entity/player/Player;isShiftKeyDown()Z"))
    protected boolean updatePlayerPose$1(boolean isShiftKeyDown) {
        // Crouching increases falling speed when not flying but having wings.
        // Treat this just like creative mode descending where both pose and therefore eye height are not offset for crouching.
        if (!ModRegistry.FLIGHT_CAPABILITY.get(Player.class.cast(this)).getWings().isEmpty()
                && this.getDeltaMovement().y() < -0.5) {
            return false;
        } else {
            return isShiftKeyDown;
        }
    }

    @Inject(method = "checkMovementStatistics",
            at = @At(value = "INVOKE", target = "Ljava/lang/Math;round(F)I"),
            slice = @Slice(from = @At(value = "FIELD",
                                      target = "Lnet/minecraft/stats/Stats;AVIATE_ONE_CM:Lnet/minecraft/resources/ResourceLocation;",
                                      opcode = Opcodes.GETSTATIC)))
    public void checkMovementStatistics(double dx, double dy, double dz, CallbackInfo callback) {
        ModRegistry.FLIGHT_CAPABILITY.get(Player.class.cast(this)).onFlown(new Vec3(dx, dy, dz));
    }
}
