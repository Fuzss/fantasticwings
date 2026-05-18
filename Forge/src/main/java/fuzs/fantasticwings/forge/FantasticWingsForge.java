package fuzs.fantasticwings.forge;

import fuzs.fantasticwings.FantasticWings;
import fuzs.fantasticwings.commands.WingsArgument;
import fuzs.fantasticwings.data.ModItemTagProvider;
import fuzs.fantasticwings.init.ModRegistry;
import fuzs.puzzleslib.api.core.v1.ModConstructor;
import fuzs.puzzleslib.api.data.v2.core.DataProviderHelper;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;

@Mod(FantasticWings.MOD_ID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class FantasticWingsForge {

    @SubscribeEvent
    public static void onConstructMod(final FMLConstructModEvent evt) {
        ModConstructor.construct(FantasticWings.MOD_ID, FantasticWings::new);
        DataProviderHelper.registerDataProviders(FantasticWings.MOD_ID, ModItemTagProvider::new);
    }

    @SubscribeEvent
    public static void onCommonSetup(final FMLCommonSetupEvent evt) {
        ArgumentTypeInfos.registerByClass(WingsArgument.class,
                (ArgumentTypeInfo<WingsArgument, ?>) ModRegistry.WINGS_ARGUMENT_TYPE.value());
    }
}
