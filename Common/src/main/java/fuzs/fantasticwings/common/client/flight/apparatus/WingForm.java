package fuzs.fantasticwings.common.client.flight.apparatus;

import com.mojang.blaze3d.vertex.PoseStack;
import fuzs.fantasticwings.common.client.animator.Animator;
import fuzs.fantasticwings.common.client.model.WingsModel;
import fuzs.fantasticwings.common.flight.apparatus.FlightApparatus;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public final class WingForm<A extends Animator<S>, S> {
    private final ResourceKey<FlightApparatus> resourceKey;
    private final Supplier<A> animator;
    private final Supplier<S> state;
    private final Supplier<WingsModel<S>> model;
    private final Identifier textureLocation;

    public WingForm(ResourceKey<FlightApparatus> resourceKey, Supplier<A> animator, Supplier<S> state, Supplier<WingsModel<S>> model) {
        this.resourceKey = resourceKey;
        this.animator = animator;
        this.state = state;
        this.model = model;
        this.textureLocation = FlightApparatus.transformTextureLocation(FlightApparatus.getTextureLocation(resourceKey));
    }

    public A createAnimator() {
        return this.animator.get();
    }

    public S createRenderState() {
        return this.state.get();
    }

    public WingsModel<S> getModel() {
        return this.model.get();
    }

    public Identifier getTextureLocation() {
        return this.textureLocation;
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || obj instanceof WingForm<?, ?> wingForm && wingForm.resourceKey == this.resourceKey;
    }

    public static Optional<WingForm<?, ?>> get(Holder<FlightApparatus> holder) {
        return Optional.of(WingFormRegistry.INSTANCE.createWings(holder));
    }

    public interface FormRenderer<S> {

        Function<Identifier, RenderType> getRenderType();

        Identifier getTextureLocation();

        default RenderType renderType() {
            return this.getRenderType().apply(this.getTextureLocation());
        }

        S createRenderState(float partialTick);

        default FormRendererState<S> pack(float partialTick) {
            return new FormRendererState<>(this, this.createRenderState(partialTick));
        }

        void submitModel(S renderState, PoseStack poseStack, SubmitNodeCollector nodeCollector, RenderType renderType, int lightCoords, int outlineColor);
    }

    public record FormRendererState<S>(FormRenderer<S> form, S state) {

        public void submitModel(PoseStack poseStack, SubmitNodeCollector nodeCollector, int lightCoords, int outlineColor) {
            this.submitModel(poseStack, nodeCollector, this.form.renderType(), lightCoords, outlineColor);
        }

        public void submitModel(PoseStack poseStack, SubmitNodeCollector nodeCollector, RenderType renderType, int lightCoords, int outlineColor) {
            this.form.submitModel(this.state, poseStack, nodeCollector, renderType, lightCoords, outlineColor);
        }
    }
}
