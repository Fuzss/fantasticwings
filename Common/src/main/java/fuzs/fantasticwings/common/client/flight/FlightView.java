package fuzs.fantasticwings.common.client.flight;

import com.mojang.blaze3d.vertex.PoseStack;
import fuzs.fantasticwings.common.client.animator.Animator;
import fuzs.fantasticwings.common.client.animator.state.State;
import fuzs.fantasticwings.common.client.animator.state.StateIdle;
import fuzs.fantasticwings.common.client.flight.apparatus.WingForm;
import fuzs.fantasticwings.common.client.init.ClientModRegistry;
import fuzs.fantasticwings.common.flight.Flight;
import fuzs.fantasticwings.common.init.ModRegistry;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

import java.util.function.Consumer;
import java.util.function.Function;

public record FlightView(WingState state) {
    public static final FlightView VOID = new FlightView(PresentWingState.VOID);

    public void ifFormPresent(Consumer<WingForm.FormRenderer<?>> consumer) {
        this.state.ifFormPresent(consumer);
    }

    public static void onEndPlayerTick(Player player) {
        ClientModRegistry.FLIGHT_VIEW_ATTACHMENT_TYPE.apply(player, (FlightView flightView) -> {
            return flightView.tick(player);
        });
    }

    private FlightView tick(Player player) {
        WingState animator = this.getFlight(player)
                .wings()
                .flatMap(WingForm::get)
                .map(this.state::next)
                .orElseGet(this.state::nextAbsent);
        animator.update(this.getFlight(player), player);
        return new FlightView(animator);
    }

    public Flight getFlight(Player player) {
        return ModRegistry.FLIGHT_ATTACHMENT_TYPE.getOrDefault(player, Flight.VOID);
    }

    private interface Strategy {

        void update(Flight flight, Player player);

        void ifFormPresent(Consumer<WingForm.FormRenderer<?>> consumer);
    }

    interface WingState {

        WingState nextAbsent();

        WingState next(WingForm<?, ?> form);

        void update(Flight flight, Player player);

        void ifFormPresent(Consumer<WingForm.FormRenderer<?>> consumer);
    }

    private record PresentWingState(WingForm<?, ?> wing, Strategy behavior) implements WingState {
        static final WingState VOID = new WingState() {
            @Override
            public WingState nextAbsent() {
                return this;
            }

            @Override
            public WingState next(WingForm<?, ?> form) {
                return PresentWingState.newState(form);
            }

            @Override
            public void update(Flight flight, Player player) {
                // NO-OP
            }

            @Override
            public void ifFormPresent(Consumer<WingForm.FormRenderer<?>> consumer) {
                // NO-OP
            }
        };

        @Override
        public WingState nextAbsent() {
            return VOID;
        }

        @Override
        public WingState next(WingForm<?, ?> form) {
            if (this.wing.equals(form)) {
                return this;
            } else {
                return PresentWingState.newState(form);
            }
        }

        @Override
        public void update(Flight flight, Player player) {
            this.behavior.update(flight, player);
        }

        @Override
        public void ifFormPresent(Consumer<WingForm.FormRenderer<?>> consumer) {
            this.behavior.ifFormPresent(consumer);
        }

        public static <T extends Animator<S>, S> WingState newState(WingForm<T, S> shape) {
            return new PresentWingState(shape, new WingStrategy<>(shape));
        }

        private static class WingStrategy<T extends Animator<S>, S> implements Strategy {
            private final WingForm<T, S> shape;
            private final T animator;
            private State state;

            public WingStrategy(WingForm<T, S> shape) {
                this.shape = shape;
                this.animator = shape.createAnimator();
                this.state = new StateIdle();
            }

            @Override
            public void update(Flight flight, Player player) {
                this.animator.update();
                State state = this.state.update(flight,
                        player.getX() - player.xo,
                        player.getY() - player.yo,
                        player.getZ() - player.zo,
                        player);
                if (!this.state.equals(state)) {
                    state.beginAnimation(this.animator);
                }

                this.state = state;
            }

            @Override
            public void ifFormPresent(Consumer<WingForm.FormRenderer<?>> consumer) {
                consumer.accept(new WingForm.FormRenderer<S>() {
                    @Override
                    public Function<Identifier, RenderType> getRenderType() {
                        return WingStrategy.this.shape.getModel().renderType();
                    }

                    @Override
                    public Identifier getTextureLocation() {
                        return WingStrategy.this.shape.getTextureLocation();
                    }

                    @Override
                    public S createRenderState(float partialTick) {
                        S renderState = WingStrategy.this.shape.createRenderState();
                        WingStrategy.this.animator.extractRenderState(renderState, partialTick);
                        return renderState;
                    }

                    @Override
                    public void submitModel(S renderState, PoseStack poseStack, SubmitNodeCollector nodeCollector, RenderType renderType, int lightCoords, int outlineColor) {
                        nodeCollector.submitModel(WingStrategy.this.shape.getModel(),
                                renderState,
                                poseStack,
                                renderType,
                                lightCoords,
                                OverlayTexture.NO_OVERLAY,
                                outlineColor,
                                null);
                    }
                });
            }
        }
    }
}
