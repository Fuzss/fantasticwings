package fuzs.fantasticwings.forge.client;

import fuzs.fantasticwings.FantasticWings;
import fuzs.fantasticwings.client.FantasticWingsClient;
import fuzs.fantasticwings.data.client.ModLanguageProvider;
import fuzs.fantasticwings.data.client.ModModelProvider;
import fuzs.fantasticwings.forge.data.client.ModSoundProvider;
import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import fuzs.puzzleslib.api.data.v2.core.DataProviderHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;

@Mod.EventBusSubscriber(modid = FantasticWings.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class FantasticWingsForgeClient {

    @SubscribeEvent
    public static void onConstructMod(final FMLConstructModEvent evt) {
        ClientModConstructor.construct(FantasticWings.MOD_ID, FantasticWingsClient::new);
        DataProviderHelper.registerDataProviders(FantasticWings.MOD_ID,
                ModLanguageProvider::new,
                ModModelProvider::new,
                ModSoundProvider::new);
    }
}
