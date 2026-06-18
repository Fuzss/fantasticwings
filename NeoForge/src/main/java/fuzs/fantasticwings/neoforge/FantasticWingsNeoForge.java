package fuzs.fantasticwings.neoforge;

import fuzs.fantasticwings.common.FantasticWings;
import fuzs.fantasticwings.common.data.ModRecipeProvider;
import fuzs.fantasticwings.common.data.tags.ModEntityTypeTagsProvider;
import fuzs.fantasticwings.common.data.tags.ModItemTagsProvider;
import fuzs.fantasticwings.common.init.ModRegistry;
import fuzs.puzzleslib.common.api.core.v1.ModConstructor;
import fuzs.puzzleslib.neoforge.api.data.v2.core.DataProviderHelper;
import net.neoforged.fml.common.Mod;

@Mod(FantasticWings.MOD_ID)
public class FantasticWingsNeoForge {

    public FantasticWingsNeoForge() {
        ModConstructor.construct(FantasticWings.MOD_ID, FantasticWings::new);
        DataProviderHelper.registerDataProviders(FantasticWings.MOD_ID,
                ModRegistry.REGISTRY_SET_BUILDER,
                ModEntityTypeTagsProvider::new,
                ModItemTagsProvider::new,
                ModRecipeProvider::new);
    }
}
