package fuzs.fantasticwings.mixin.client;

import fuzs.fantasticwings.init.ModRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.PotionUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(PotionUtils.class)
abstract class PotionUtilsMixin {

    @Inject(method = "addPotionTooltip(Ljava/util/List;Ljava/util/List;F)V", at = @At("HEAD"), cancellable = true)
    private static void addPotionTooltip(List<MobEffectInstance> effects, List<Component> tooltipLines, float durationFactor, CallbackInfo callback) {
        // Prevent our effects from showing up on potion item tooltips, hides the implementation of the wing bottles a bit.
        // Also hides the fact that effects with an amplifier past 5 have no translation key.
        if (effects.size() == 1) {
            MobEffect mobEffect = effects.get(0).getEffect();
            if (mobEffect == ModRegistry.GROW_WINGS_MOB_EFFECT.value()
                    || mobEffect == ModRegistry.SHED_WINGS_MOB_EFFECT.value()) {
                callback.cancel();
            }
        }
    }
}
