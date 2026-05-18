package fuzs.fantasticwings.fabric;

import fuzs.fantasticwings.FantasticWings;
import fuzs.fantasticwings.commands.WingsArgument;
import fuzs.puzzleslib.api.core.v1.ModConstructor;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;

public class FantasticWingsFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        ArgumentTypeRegistry.registerArgumentType(FantasticWings.id("wings"),
                WingsArgument.class,
                SingletonArgumentInfo.contextFree(WingsArgument::wings));
        ModConstructor.construct(FantasticWings.MOD_ID, FantasticWings::new);
    }
}
