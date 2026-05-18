package fuzs.fantasticwings.client.init;

import com.mojang.blaze3d.platform.InputConstants;
import fuzs.fantasticwings.FantasticWings;
import fuzs.fantasticwings.client.flight.FlightViewCapability;
import fuzs.puzzleslib.api.capability.v3.CapabilityController;
import fuzs.puzzleslib.api.capability.v3.data.EntityCapabilityKey;
import fuzs.puzzleslib.api.client.init.v1.ModelLayerFactory;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.player.AbstractClientPlayer;

public class ClientModRegistry {
    public static final KeyMapping FLY_KEY_MAPPING = new KeyMapping(FantasticWings.id("toggle_flight")
            .toLanguageKey("key"),
            InputConstants.UNKNOWN.getValue(),
            FantasticWings.id("main").toLanguageKey("key.category"));
    static final CapabilityController CAPABILITIES = CapabilityController.from(FantasticWings.MOD_ID);
    public static final EntityCapabilityKey<AbstractClientPlayer, FlightViewCapability> FLIGHT_VIEW_CAPABILITY = CAPABILITIES.registerEntityCapability(
            "flight_view",
            FlightViewCapability.class,
            FlightViewCapability::new,
            AbstractClientPlayer.class);
    static final ModelLayerFactory MODEL_LAYERS = ModelLayerFactory.from(FantasticWings.MOD_ID);
    public static final ModelLayerLocation INSECTOID_WINGS = MODEL_LAYERS.register("insectoid_wings");
    public static final ModelLayerLocation AVIAN_WINGS = MODEL_LAYERS.register("avian_wings");

    public static void touch() {
        // NO-OP
    }
}
