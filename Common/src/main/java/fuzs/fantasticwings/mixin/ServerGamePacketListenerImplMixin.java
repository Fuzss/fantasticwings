package fuzs.fantasticwings.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import fuzs.fantasticwings.init.ModRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerGamePacketListenerImpl.class)
abstract class ServerGamePacketListenerImplMixin {
    @Shadow
    public ServerPlayer player;

    @ModifyExpressionValue(method = "handleMovePlayer",
                           at = @At(value = "INVOKE",
                                    target = "Lnet/minecraft/server/level/ServerPlayer;isFallFlying()Z"))
    public boolean handleMovePlayer(boolean isFallFlying) {
        // Disables server-side movement checks when flying just like for elytra gliding.
        return isFallFlying || ModRegistry.FLIGHT_CAPABILITY.get(this.player).isFlying();
    }
}
