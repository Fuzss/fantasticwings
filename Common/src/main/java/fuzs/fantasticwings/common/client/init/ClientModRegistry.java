package fuzs.fantasticwings.common.client.init;

import fuzs.fantasticwings.common.FantasticWings;
import fuzs.fantasticwings.common.client.flight.FlightView;
import fuzs.puzzleslib.common.api.attachment.v4.DataAttachmentRegistry;
import fuzs.puzzleslib.common.api.attachment.v4.DataAttachmentType;
import fuzs.puzzleslib.common.api.client.key.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;

public class ClientModRegistry {
    public static final KeyMapping FLY_KEY_MAPPING = KeyMappingHelper.registerUnboundKeyMapping(FantasticWings.id(
            "toggle_flight"));

    public static final DataAttachmentType<Entity, FlightView> FLIGHT_VIEW_ATTACHMENT_TYPE = DataAttachmentRegistry.<FlightView>entityBuilder()
            .defaultValue(EntityTypes.PLAYER, FlightView.VOID)
            .build(FantasticWings.id("flight_view"));

    public static void bootstrap() {
        // NO-OP
    }
}
