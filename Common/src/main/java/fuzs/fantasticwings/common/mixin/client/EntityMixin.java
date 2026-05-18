package fuzs.fantasticwings.common.mixin.client;

import fuzs.fantasticwings.common.client.handler.ClientEventHandler;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
abstract class EntityMixin {

    @Inject(method = "turn", at = @At("TAIL"))
    public void turn(double xo, double yo, CallbackInfo callback) {
        // the method is only ever called client-side
        float deltaYaw = (float) xo * 0.15F;
        ClientEventHandler.onTurn(Entity.class.cast(this), deltaYaw);
    }
}
