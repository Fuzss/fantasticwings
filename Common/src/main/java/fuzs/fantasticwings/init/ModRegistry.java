package fuzs.fantasticwings.init;

import fuzs.fantasticwings.FantasticWings;
import fuzs.fantasticwings.commands.WingsArgument;
import fuzs.fantasticwings.flight.FlightCapability;
import fuzs.fantasticwings.flight.apparatus.FlightApparatusImpl;
import fuzs.fantasticwings.world.effect.WingsMobEffect;
import fuzs.puzzleslib.api.capability.v3.CapabilityController;
import fuzs.puzzleslib.api.capability.v3.data.EntityCapabilityKey;
import fuzs.puzzleslib.api.capability.v3.data.SyncStrategy;
import fuzs.puzzleslib.api.core.v1.ModLoader;
import fuzs.puzzleslib.api.init.v3.RegistryManager;
import fuzs.puzzleslib.api.init.v3.tags.BoundTagFactory;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;

public class ModRegistry {
    static final RegistryManager REGISTRY = RegistryManager.from(FantasticWings.MOD_ID);
    public static final Holder.Reference<MobEffect> GROW_WINGS_MOB_EFFECT = REGISTRY.registerMobEffect("grow_wings",
            () -> new WingsMobEffect(MobEffectCategory.BENEFICIAL, 0x97CAE4));
    public static final Holder.Reference<MobEffect> SHED_WINGS_MOB_EFFECT = REGISTRY.registerMobEffect("shed_wings",
            () -> new WingsMobEffect(MobEffectCategory.HARMFUL, 0x9B172D));
    public static final Holder.Reference<Potion> BAT_BLOOD_POTION = REGISTRY.registerPotion("bat_blood",
            () -> new Potion(new MobEffectInstance(SHED_WINGS_MOB_EFFECT.value(), 1)));
    public static final Holder.Reference<SoundEvent> ITEM_ARMOR_EQUIP_WINGS = REGISTRY.registerSoundEvent(
            "item.armor.equip_wings");
    public static final Holder.Reference<SoundEvent> ITEM_WINGS_FLYING = REGISTRY.registerSoundEvent("item.wings.flying");
    public static final Holder.Reference<ArgumentTypeInfo<?, ?>> WINGS_ARGUMENT_TYPE = REGISTRY.whenNotOn(ModLoader.FABRIC)
            .register(Registries.COMMAND_ARGUMENT_TYPE,
                    "wings",
                    () -> SingletonArgumentInfo.contextFree(WingsArgument::wings));
    static final BoundTagFactory TAGS = BoundTagFactory.make(FantasticWings.MOD_ID);
    public static final TagKey<Item> WING_OBSTRUCTIONS = TAGS.registerItemTag("wing_obstructions");
    static final CapabilityController CAPABILITIES = CapabilityController.from(FantasticWings.MOD_ID);
    public static final EntityCapabilityKey<Player, FlightCapability> FLIGHT_CAPABILITY = CAPABILITIES.registerEntityCapability(
            "flight",
            FlightCapability.class,
            FlightCapability::new,
            Player.class).setSyncStrategy(SyncStrategy.TRACKING);

    public static void touch() {
        FlightApparatusImpl.forEach(flightApparatus -> {
            flightApparatus.registerPotion(REGISTRY::registerPotion);
        });
    }
}
