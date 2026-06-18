package fuzs.fantasticwings.common.client;

import fuzs.fantasticwings.common.FantasticWings;
import fuzs.fantasticwings.common.client.flight.FlightView;
import fuzs.fantasticwings.common.client.flight.apparatus.WingFormRegistry;
import fuzs.fantasticwings.common.client.handler.ClientEventHandler;
import fuzs.fantasticwings.common.client.init.ClientModRegistry;
import fuzs.fantasticwings.common.client.model.AvianWingsModel;
import fuzs.fantasticwings.common.client.model.InsectoidWingsModel;
import fuzs.fantasticwings.common.client.model.geom.ModModelLayers;
import fuzs.fantasticwings.common.client.renderer.entity.layers.ModWingsLayer;
import fuzs.fantasticwings.common.client.renderer.item.properties.select.FlightApparatusProperty;
import fuzs.fantasticwings.common.flight.Flight;
import fuzs.fantasticwings.common.init.ModRegistry;
import fuzs.fantasticwings.common.network.ServerboundControlFlyingMessage;
import fuzs.fantasticwings.common.world.item.WithDescriptionItem;
import fuzs.puzzleslib.common.api.client.core.v1.ClientModConstructor;
import fuzs.puzzleslib.common.api.client.core.v1.context.ItemModelsContext;
import fuzs.puzzleslib.common.api.client.core.v1.context.KeyMappingsContext;
import fuzs.puzzleslib.common.api.client.core.v1.context.LayerDefinitionsContext;
import fuzs.puzzleslib.common.api.client.core.v1.context.ResourcePackReloadListenersContext;
import fuzs.puzzleslib.common.api.client.event.v1.entity.ClientEntityEvents;
import fuzs.puzzleslib.common.api.client.event.v1.renderer.AddLivingEntityRenderLayersCallback;
import fuzs.puzzleslib.common.api.client.event.v1.renderer.ComputeCameraAnglesCallback;
import fuzs.puzzleslib.common.api.client.event.v1.renderer.ExtractEntityRenderStateCallback;
import fuzs.puzzleslib.common.api.client.event.v1.renderer.RenderHandEvents;
import fuzs.puzzleslib.common.api.client.gui.v2.tooltip.ItemTooltipRegistry;
import fuzs.puzzleslib.common.api.client.key.v1.KeyActivationHandler;
import fuzs.puzzleslib.common.api.event.v1.entity.player.PlayerTickEvents;
import fuzs.puzzleslib.common.api.network.v4.MessageSender;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public class FantasticWingsClient implements ClientModConstructor {

    @Override
    public void onConstructMod() {
        ClientModRegistry.bootstrap();
        registerEventHandlers();
    }

    private static void registerEventHandlers() {
        ComputeCameraAnglesCallback.EVENT.register(ClientEventHandler::onComputeCameraAngles);
        ClientEntityEvents.LOAD.register(ClientEventHandler::onEntityLoad);
        PlayerTickEvents.END.register(FlightView::onEndPlayerTick);
        ExtractEntityRenderStateCallback.EVENT.register(ClientEventHandler::onExtractEntityRenderState);
        RenderHandEvents.OFF_HAND.register(ClientEventHandler::onRenderOffHand);
        AddLivingEntityRenderLayersCallback.EVENT.register(ModWingsLayer::addLivingEntityRenderLayers);
    }

    @Override
    public void onClientSetup() {
        ItemTooltipRegistry.ITEM.registerItemTooltip(WithDescriptionItem.class,
                WithDescriptionItem::getDescriptionComponent);
    }

    @Override
    public void onRegisterKeyMappings(KeyMappingsContext context) {
        context.registerKeyMapping(ClientModRegistry.FLY_KEY_MAPPING,
                KeyActivationHandler.forGame((Minecraft minecraft) -> {
                    Player player = minecraft.player;
                    Flight flight = ModRegistry.FLIGHT_ATTACHMENT_TYPE.getOrDefault(player, Flight.VOID);
                    if (flight.canFly(player)) {
                        flight = flight.toggleIsFlying(player);
                        ModRegistry.FLIGHT_ATTACHMENT_TYPE.set(player, flight);
                        MessageSender.broadcast(new ServerboundControlFlyingMessage(flight.isFlying()));
                    }
                }));
    }

    @Override
    public void onRegisterItemModels(ItemModelsContext context) {
        context.registerSelectItemModelProperty(FantasticWings.id("wings"), FlightApparatusProperty.TYPE);
    }

    @Override
    public void onRegisterLayerDefinitions(LayerDefinitionsContext context) {
        context.registerLayerDefinition(ModModelLayers.INSECTOID_WINGS_MODEL_LAYER,
                InsectoidWingsModel::createWingsLayer);
        context.registerLayerDefinition(ModModelLayers.AVIAN_WINGS_MODEL_LAYER, AvianWingsModel::createWingsLayer);
    }

    @Override
    public void onAddResourcePackReloadListeners(ResourcePackReloadListenersContext context) {
        context.registerReloadListener(FantasticWings.id("wing_models"), WingFormRegistry.INSTANCE);
    }
}
