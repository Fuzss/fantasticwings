package fuzs.fantasticwings;

import fuzs.fantasticwings.commands.WingsCommand;
import fuzs.fantasticwings.config.ServerConfig;
import fuzs.fantasticwings.flight.apparatus.FlightApparatusImpl;
import fuzs.fantasticwings.handler.ServerEventHandler;
import fuzs.fantasticwings.init.ModRegistry;
import fuzs.fantasticwings.network.ServerboundControlFlyingMessage;
import fuzs.puzzleslib.api.config.v3.ConfigHolder;
import fuzs.puzzleslib.api.core.v1.ModConstructor;
import fuzs.puzzleslib.api.core.v1.context.CreativeModeTabContext;
import fuzs.puzzleslib.api.event.v1.entity.EntityRidingEvents;
import fuzs.puzzleslib.api.event.v1.entity.player.PlayerInteractEvents;
import fuzs.puzzleslib.api.event.v1.entity.player.PlayerTickEvents;
import fuzs.puzzleslib.api.event.v1.server.RegisterCommandsCallback;
import fuzs.puzzleslib.api.item.v2.CreativeModeTabConfigurator;
import fuzs.puzzleslib.api.network.v3.NetworkHandlerV3;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Stream;

public class FantasticWings implements ModConstructor {
    public static final String MOD_ID = "fantasticwings";
    public static final String MOD_NAME = "Fantastic Wings";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    public static final NetworkHandlerV3 NETWORK = NetworkHandlerV3.builder(MOD_ID)
            .registerServerbound(ServerboundControlFlyingMessage.class);
    public static final ConfigHolder CONFIG = ConfigHolder.builder(MOD_ID).server(ServerConfig.class);

    @Override
    public void onConstructMod() {
        ModRegistry.touch();
        registerEventHandlers();
    }

    private static void registerEventHandlers() {
        RegisterCommandsCallback.EVENT.register(WingsCommand::register);
        EntityRidingEvents.START.register(ServerEventHandler::onStartRiding);
        PlayerTickEvents.END.register(ServerEventHandler::onEndPlayerTick);
        PlayerInteractEvents.ATTACK_ENTITY.register(ServerEventHandler::onAttackEntity);
    }

    @Override
    public void onCommonSetup() {
        FlightApparatusImpl.forEach(FlightApparatusImpl::registerBrewingRecipes);
    }

    @Override
    public void onRegisterCreativeModeTabs(CreativeModeTabContext context) {
        context.registerCreativeModeTab(CreativeModeTabConfigurator.from(MOD_ID).icon(() -> {
            return PotionUtils.setPotion(Items.POTION.getDefaultInstance(),
                    FlightApparatusImpl.MONARCH_BUTTERFLY.getPotion()
            );
        }).icons(() -> {
            return Stream.of(FlightApparatusImpl.values()).map(flightApparatus -> {
                return PotionUtils.setPotion(Items.POTION.getDefaultInstance(), flightApparatus.getPotion());
            }).toArray(ItemStack[]::new);
        }).displayItems((itemDisplayParameters, output) -> {
            output.accept(PotionUtils.setPotion(Items.POTION.getDefaultInstance(),
                    ModRegistry.BAT_BLOOD_POTION.value()
            ));
            FlightApparatusImpl.forEach(flightApparatus -> {
                output.accept(PotionUtils.setPotion(Items.POTION.getDefaultInstance(), flightApparatus.getPotion()));
            });
        }));
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
