package fuzs.fantasticwings.data.client;

import fuzs.fantasticwings.FantasticWings;
import fuzs.fantasticwings.client.FantasticWingsClient;
import fuzs.fantasticwings.flight.apparatus.FlightApparatusImpl;
import fuzs.puzzlesaccessapi.api.client.data.v2.BlockModelBuilder;
import fuzs.puzzlesaccessapi.api.client.data.v2.ItemModelBuilder;
import fuzs.puzzleslib.api.client.data.v2.AbstractModelProvider;
import fuzs.puzzleslib.api.data.v2.core.DataProviderContext;
import net.minecraft.resources.ResourceLocation;

public class ModModelProvider extends AbstractModelProvider {

    public ModModelProvider(DataProviderContext context) {
        super(context);
    }

    @Override
    public void addBlockModels(BlockModelBuilder builder) {
        // NO-OP
    }

    @Override
    public void addItemModels(ItemModelBuilder builder) {
        ResourceLocation blankTextureLocation = decorateItemModelLocation(FantasticWings.id("blank"));
        builder.generateLayeredItem(decorateItemModelLocation(FantasticWingsClient.BAT_BLOOD_BOTTLE_TEXTURE_LOCATION),
                blankTextureLocation,
                decorateItemModelLocation(FantasticWingsClient.BAT_BLOOD_BOTTLE_TEXTURE_LOCATION));
        FlightApparatusImpl.forEach(flightApparatus -> {
            ResourceLocation textureLocation = decorateItemModelLocation(flightApparatus.textureLocation());
            // we need a blank layer 0 since it is tinted (in vanilla this is the potion bottle liquid)
            // this will mess with the enchantment glint as it will apply to both layers making it much stronger,
            // but that's a vanilla bug and since potions no longer have a glint by default ¯\_(ツ)_/¯
            builder.generateLayeredItem(textureLocation, blankTextureLocation, textureLocation);
        });
    }
}
