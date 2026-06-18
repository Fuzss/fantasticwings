package fuzs.fantasticwings.common.handler;

import fuzs.fantasticwings.common.flight.Flight;
import fuzs.fantasticwings.common.init.ModRegistry;
import fuzs.puzzleslib.common.api.event.v1.core.EventResult;
import fuzs.puzzleslib.common.api.item.v2.ItemHelper;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class ServerEventHandler {

    public static EventResult onAttackEntity(Player player, Level level, InteractionHand interactionHand, Entity entity) {
        ItemStack itemInHand = player.getItemInHand(interactionHand);
        if (entity.is(ModRegistry.BAT_BLOOD_TARGETS_ENTITY_TYPE_TAG) && itemInHand.is(Items.GLASS_BOTTLE)) {
            level.playSound(player,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    SoundEvents.BOTTLE_FILL,
                    SoundSource.NEUTRAL,
                    1.0F,
                    1.0F);
            ItemStack originalItemInHand = itemInHand.copy();
            itemInHand.consume(1, player);
            player.awardStat(Stats.ITEM_USED.get(Items.GLASS_BOTTLE));
            ItemStack itemStack = new ItemStack(ModRegistry.BOTTLED_BAT_BLOOD_ITEM);
            if (itemInHand.isEmpty()) {
                ItemHelper.onPlayerDestroyItem(player, originalItemInHand, interactionHand);
                player.setItemInHand(interactionHand, itemStack);
            } else if (!player.getInventory().add(itemStack)) {
                player.drop(itemStack, false);
            }
        }

        return EventResult.PASS;
    }

    public static EventResult onStartRiding(Level level, Entity passengerEntity, Entity vehicleEntity) {
        return ModRegistry.FLIGHT_ATTACHMENT_TYPE.getOrDefault(passengerEntity, Flight.VOID).isFlying() ?
                EventResult.INTERRUPT : EventResult.PASS;
    }

    public static void onEndPlayerTick(Player player) {
        Flight flight = ModRegistry.FLIGHT_ATTACHMENT_TYPE.getOrDefault(player, Flight.VOID);
        flight = Flight.tick(flight, player);
        ModRegistry.FLIGHT_ATTACHMENT_TYPE.set(player, flight);
    }

    public static boolean onUpdateBodyRotation(LivingEntity living, float movementYaw) {
        if (living instanceof Player player && ModRegistry.FLIGHT_ATTACHMENT_TYPE.getOrDefault(player, Flight.VOID)
                .isFlying()) {
            living.yBodyRot += Mth.wrapDegrees(movementYaw - living.yBodyRot) * 0.3F;
            float theta = Mth.clamp(Mth.wrapDegrees(living.getYRot() - living.yBodyRot), -50.0F, 50.0F);
            living.yBodyRot = living.getYRot() - theta;
            return true;
        } else {
            return false;
        }
    }
}
