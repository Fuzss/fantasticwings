package fuzs.fantasticwings.handler;

import fuzs.fantasticwings.flight.FlightCapability;
import fuzs.fantasticwings.init.ModRegistry;
import fuzs.puzzleslib.api.core.v1.CommonAbstractions;
import fuzs.puzzleslib.api.event.v1.core.EventResult;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;

public class ServerEventHandler {

    public static EventResult onAttackEntity(Player player, Level level, InteractionHand interactionHand, Entity entity) {
        ItemStack itemInHand = player.getItemInHand(interactionHand);
        if (entity.getType() == EntityType.BAT && itemInHand.is(Items.GLASS_BOTTLE)) {
            level.playSound(
                player,
                player.getX(), player.getY(), player.getZ(),
                SoundEvents.BOTTLE_FILL,
                SoundSource.NEUTRAL,
                1.0F,
                1.0F
            );
            ItemStack itemStack = itemInHand.copy();
            if (!player.getAbilities().instabuild) {
                itemInHand.shrink(1);
            }
            player.awardStat(Stats.ITEM_USED.get(Items.GLASS_BOTTLE));
            ItemStack batBlood = PotionUtils.setPotion(Items.POTION.getDefaultInstance(),
                    ModRegistry.BAT_BLOOD_POTION.value()
            );
            if (itemInHand.isEmpty()) {
                CommonAbstractions.INSTANCE.onPlayerDestroyItem(player, itemStack, interactionHand);
                player.setItemInHand(interactionHand, batBlood);
            } else if (!player.getInventory().add(batBlood)) {
                player.drop(batBlood, false);
            }
        }
        return EventResult.PASS;
    }

    public static EventResult onStartRiding(Level level, Entity rider, Entity vehicle) {
        return ModRegistry.FLIGHT_CAPABILITY.getIfProvided(rider).filter(FlightCapability::isFlying).isPresent() ? EventResult.INTERRUPT : EventResult.PASS;
    }

    public static void onEndPlayerTick(Player player) {
        ModRegistry.FLIGHT_CAPABILITY.get(player).tick();
    }

    public static boolean onUpdateBodyRotation(LivingEntity living, float movementYaw) {
        if (living instanceof Player player && ModRegistry.FLIGHT_CAPABILITY.get(player).isFlying()) {
            living.yBodyRot += Mth.wrapDegrees(movementYaw - living.yBodyRot) * 0.3F;
            float theta = Mth.clamp(Mth.wrapDegrees(living.getYRot() - living.yBodyRot), -50.0F, 50.0F);
            living.yBodyRot = living.getYRot() - theta;
            return true;
        } else {
            return false;
        }
    }
}
