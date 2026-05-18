package fuzs.fantasticwings.common.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import fuzs.fantasticwings.common.flight.Flight;
import fuzs.fantasticwings.common.init.ModRegistry;
import net.minecraft.network.Connection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerGamePacketListenerImpl.class)
abstract class ServerGamePacketListenerImplMixin extends ServerCommonPacketListenerImpl {
    @Shadow
    public ServerPlayer player;

    public ServerGamePacketListenerImplMixin(MinecraftServer server, Connection connection, CommonListenerCookie cookie) {
        super(server, connection, cookie);
    }

    @ModifyExpressionValue(method = "handleMovePlayer",
                           at = @At(value = "INVOKE",
                                    target = "Lnet/minecraft/server/level/ServerPlayer;isFallFlying()Z"))
    public boolean handleMovePlayer(boolean isFallFlying) {
        // Disables server-side movement checks when flying just like for elytra gliding.
        return isFallFlying || ModRegistry.FLIGHT_ATTACHMENT_TYPE.getOrDefault(this.player, Flight.VOID).isFlying();
    }
}
