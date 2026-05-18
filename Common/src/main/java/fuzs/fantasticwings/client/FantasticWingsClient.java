package fuzs.fantasticwings.client;

import fuzs.fantasticwings.FantasticWings;
import fuzs.fantasticwings.client.flight.apparatus.WingFormRegistry;
import fuzs.fantasticwings.client.handler.ClientEventHandler;
import fuzs.fantasticwings.client.handler.FlyingCrouchHandler;
import fuzs.fantasticwings.client.handler.PotionItemModelHandler;
import fuzs.fantasticwings.client.init.ClientModRegistry;
import fuzs.fantasticwings.client.model.ModelWingsAvian;
import fuzs.fantasticwings.client.model.ModelWingsInsectoid;
import fuzs.fantasticwings.client.renderer.entity.layers.LayerWings;
import fuzs.fantasticwings.flight.FlightCapability;
import fuzs.fantasticwings.flight.apparatus.FlightApparatusImpl;
import fuzs.fantasticwings.init.ModRegistry;
import fuzs.fantasticwings.network.ServerboundControlFlyingMessage;
import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import fuzs.puzzleslib.api.client.core.v1.context.*;
import fuzs.puzzleslib.api.client.event.v1.*;
import fuzs.puzzleslib.api.client.screen.v2.KeyMappingActivationHelper;
import fuzs.puzzleslib.api.core.v1.context.AddReloadListenersContext;
import fuzs.puzzleslib.api.event.v1.entity.player.PlayerTickEvents;
import fuzs.puzzleslib.api.network.v3.PlayerSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;

public class FantasticWingsClient implements ClientModConstructor {
    public static final ResourceLocation BAT_BLOOD_BOTTLE_TEXTURE_LOCATION = FantasticWings.id("bat_blood_bottle");

    @Override
    public void onConstructMod() {
        ClientModRegistry.touch();
        registerEventHandlers();
    }

    private static void registerEventHandlers() {
        ComputeCameraAnglesCallback.EVENT.register(ClientEventHandler::onComputeCameraAngles);
        ClientEntityLevelEvents.LOAD.register(ClientEventHandler::onEntityLoad);
        PlayerTickEvents.END.register(ClientEventHandler::onEndPlayerTick);
        RenderHandCallback.EVENT.register(ClientEventHandler::onRenderHand);
        RenderPlayerEvents.BEFORE.register(FlyingCrouchHandler::onBeforeRenderPlayer);
        RenderPlayerEvents.AFTER.register(FlyingCrouchHandler::onAfterRenderPlayer);
        ModelEvents.MODIFY_UNBAKED_MODEL.register(PotionItemModelHandler::onModifyUnbakedModel);
        ClientTickEvents.START.register(FantasticWingsClient::onStartClientTick);
    }

    private static void onStartClientTick(Minecraft minecraft) {
        if (minecraft.player != null) {
            while (ClientModRegistry.FLY_KEY_MAPPING.consumeClick()) {
                FlightCapability flightCapability = ModRegistry.FLIGHT_CAPABILITY.get(minecraft.player);
                if (flightCapability.canFly()) {
                    flightCapability.toggleIsFlying(PlayerSet.ofNone());
                    FantasticWings.NETWORK.sendToServer(new ServerboundControlFlyingMessage(flightCapability.isFlying()));
                }
            }
        }
    }

    @Override
    public void onClientSetup() {
        WingFormRegistry.INSTANCE.registerAll();
    }

    @Override
    public void onRegisterResourcePackReloadListeners(AddReloadListenersContext context) {
        context.registerReloadListener("wing_models", WingFormRegistry.INSTANCE);
    }

    @Override
    public void onRegisterKeyMappings(KeyMappingsContext context) {
        context.registerKeyMapping(ClientModRegistry.FLY_KEY_MAPPING,
                KeyMappingActivationHelper.KeyActivationContext.GAME);
    }

    @Override
    public void onRegisterAdditionalModels(AdditionalModelsContext context) {
//        context.registerAdditionalModel(AbstractModelProvider.decorateItemModelLocation(
//                BAT_BLOOD_BOTTLE_TEXTURE_LOCATION));
        FlightApparatusImpl.forEach(flightApparatus -> {
            context.registerAdditionalModel(flightApparatus.modelLocation());
        });
    }

    @Override
    public void onRegisterItemModelProperties(ItemModelPropertiesContext context) {
//        registerPotionItemModelProperty(context,
//                BAT_BLOOD_BOTTLE_TEXTURE_LOCATION,
//                ModRegistry.BAT_BLOOD_POTION.value()
//        );
        FlightApparatusImpl.forEach(flightApparatus -> {
            registerPotionItemModelProperty(context, flightApparatus.textureLocation(), flightApparatus.getPotion());
        });
    }

    private static void registerPotionItemModelProperty(ItemModelPropertiesContext context, ResourceLocation itemModelProperty, Potion potion) {
        context.registerItemProperty(itemModelProperty,
                (ItemStack itemStack, ClientLevel clientLevel, LivingEntity livingEntity, int i) -> {
                    return PotionUtils.getPotion(itemStack) == potion ? 1.0F : 0.0F;
                },
                PotionItemModelHandler.POTION_ITEMS);
    }

    @Override
    public void onRegisterLayerDefinitions(LayerDefinitionsContext context) {
        context.registerLayerDefinition(ClientModRegistry.INSECTOID_WINGS, ModelWingsInsectoid::createWingsLayer);
        context.registerLayerDefinition(ClientModRegistry.AVIAN_WINGS, ModelWingsAvian::createWingsLayer);
    }

    @Override
    public void onRegisterLivingEntityRenderLayers(LivingEntityRenderLayersContext context) {
        context.registerRenderLayer(EntityType.PLAYER, LayerWings::new);
    }
}
