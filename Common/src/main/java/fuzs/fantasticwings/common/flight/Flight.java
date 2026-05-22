package fuzs.fantasticwings.common.flight;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fuzs.fantasticwings.common.flight.apparatus.FlightApparatus;
import fuzs.fantasticwings.common.init.ModRegistry;
import fuzs.fantasticwings.common.network.ServerboundControlFlyingMessage;
import fuzs.fantasticwings.common.util.CubicBezier;
import fuzs.fantasticwings.common.util.MathHelper;
import fuzs.puzzleslib.common.api.network.v4.MessageSender;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public record Flight(Optional<Holder<FlightApparatus>> wings, boolean isFlying, int timeFlying, int prevTimeFlying) {
    public static final Codec<Flight> CODEC = RecordCodecBuilder.create(instance -> instance.group(FlightApparatus.CODEC.optionalFieldOf(
                    "wing_type").forGetter(Flight::wings),
            Codec.BOOL.optionalFieldOf("is_flying", false).forGetter(Flight::isFlying),
            Codec.INT.optionalFieldOf("time_flying", 0).forGetter(Flight::timeFlying)).apply(instance, Flight::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, Flight> STREAM_CODEC = StreamCodec.composite(
            FlightApparatus.STREAM_CODEC.apply(ByteBufCodecs::optional),
            Flight::wings,
            ByteBufCodecs.BOOL,
            Flight::isFlying,
            ByteBufCodecs.INT,
            Flight::timeFlying,
            Flight::new);
    public static final Flight VOID = new Flight(Optional.empty(), false, 0);
    private static final CubicBezier FLY_AMOUNT_CURVE = new CubicBezier(0.37F, 0.13F, 0.3F, 1.12F);
    private static final int MAX_TIME_FLYING = 20;
    private static final float MIN_SPEED = 0.03F;
    private static final float MAX_SPEED = 0.0715F;
    private static final float Y_BOOST = 0.05F;
    private static final float MANUAL_Y_BOOST = 0.06F;
    private static final float FALL_REDUCTION = 0.9F;
    private static final float PITCH_OFFSET = 30.0F;

    public Flight(Optional<Holder<FlightApparatus>> wings, boolean isFlying, int timeFlying) {
        this(wings, isFlying, timeFlying, timeFlying);
    }

    public Flight setIsFlying(Player player, boolean isFlying) {
        if (this.isFlying != isFlying) {
            if (isFlying) {
                player.unRide();
            }

            Mutable mutable = this.mutable();
            mutable.isFlying = isFlying;
            return mutable.immutable();
        } else {
            return this;
        }
    }

    public Flight toggleIsFlying(Player player) {
        return this.setIsFlying(player, !this.isFlying);
    }

    public Flight setWings(@Nullable Holder<FlightApparatus> wings) {
        if (wings == null && this.wings.isPresent() || wings != null && !this.is(wings)) {
            Mutable mutable = this.mutable();
            mutable.wings = Optional.ofNullable(wings);
            return mutable.immutable();
        } else {
            return this;
        }
    }

    public boolean isEmpty() {
        return this.wings.isEmpty();
    }

    public boolean is(Holder<FlightApparatus> flightApparatus) {
        Objects.requireNonNull(flightApparatus, "flight apparatus is null");
        return this.wings.filter((Holder<FlightApparatus> holder) -> holder.is(flightApparatus)).isPresent();
    }

    public float getFlyingAmount(float partialTick) {
        return FLY_AMOUNT_CURVE.eval(
                MathHelper.lerp(this.prevTimeFlying, this.timeFlying, partialTick) / MAX_TIME_FLYING);
    }

    public boolean canUseWings(Player player) {
        return !player.getAbilities().flying && !player.getItemBySlot(EquipmentSlot.CHEST)
                .is(ModRegistry.WING_OBSTRUCTIONS);
    }

    public boolean canFly(Player player) {
        return this.canUseWings(player) && this.wings.filter((Holder<FlightApparatus> holder) -> holder.value()
                .isUsableForFlying(player)).isPresent();
    }

    public boolean canSlowlyDescend(Player player) {
        return this.canUseWings(player) && this.wings.filter((Holder<FlightApparatus> holder) -> holder.value()
                .isUsableForSlowlyDescending(player)).isPresent() && (this.isFlying() || !player.isDescending());
    }

    public static Flight tick(Flight flight, Player player) {
        Mutable mutable = flight.mutable();
        flight.tick(mutable, player);
        flight = mutable.immutable();
        if (player.isEffectiveAi()) {
            flight.onWornUpdate(player);
        }

        return flight;
    }

    private void tick(Mutable flight, Player player) {
        if (flight.isFlying && player.isEffectiveAi() && !this.canFly(player)) {
            flight.isFlying = false;
        }

        flight.prevTimeFlying = flight.timeFlying;
        if (flight.isFlying) {
            if (flight.timeFlying < MAX_TIME_FLYING) {
                flight.timeFlying++;
            } else if (player.isLocalPlayer() && player.onGround()) {
                flight.isFlying = false;
                MessageSender.broadcast(new ServerboundControlFlyingMessage(false));
            }
        } else if (flight.timeFlying > 0) {
            flight.timeFlying--;
        }
    }

    /**
     * @see Player#travel(Vec3)
     */
    private void onWornUpdate(Player player) {
        if (this.isFlying()) {
            float speed = Mth.clampedLerp(player.zza, MIN_SPEED, MAX_SPEED);
            float elevationBoost = MathHelper.transform(Math.abs(player.getXRot()), 45.0F, 90.0F, 1.0F, 0.0F);
            float pitch = -MathHelper.toRadians(player.getXRot() - PITCH_OFFSET * elevationBoost);
            float yaw = -MathHelper.toRadians(player.getYRot()) - MathHelper.PI;
            float vxz = -Mth.cos(pitch);
            float vy = Mth.sin(pitch);
            float vz = Mth.cos(yaw);
            float vx = Mth.sin(yaw);
            player.setDeltaMovement(player.getDeltaMovement()
                    .add(vx * vxz * speed,
                            vy * speed + Y_BOOST * (player.getXRot() > 0.0F ? elevationBoost : 1.0D),
                            vz * vxz * speed));
            // similar to swimming where jumping and sneaking help with ascending / descending
            if (player.jumping) {
                // with the elevation boost this can get quite crazy, so don't add as much as when descending
                player.setDeltaMovement(player.getDeltaMovement().add(0.0, MANUAL_Y_BOOST / 2.0F, 0.0));
            } else if (player.isDescending()) {
                player.setDeltaMovement(player.getDeltaMovement().add(0.0, -MANUAL_Y_BOOST, 0.0));
            }
        }

        if (this.canSlowlyDescend(player)) {
            Vec3 deltaMovement = player.getDeltaMovement();
            if (deltaMovement.y() < 0.0D) {
                player.setDeltaMovement(deltaMovement.multiply(1.0D, FALL_REDUCTION, 1.0D));
            }

            player.fallDistance = 0.0F;
        }
    }

    public void onFlown(Player player, Vec3 direction) {
        this.wings.ifPresent((Holder<FlightApparatus> holder) -> {
            if (this.isFlying()) {
                holder.value().onFlying(player, direction);
            } else if (this.canSlowlyDescend(player) && player.getDeltaMovement().y() < -0.5) {
                holder.value().onSlowlyDescending(player, direction);
            }
        });
    }

    private Mutable mutable() {
        return new Mutable(this);
    }

    private static class Mutable {
        public Optional<Holder<FlightApparatus>> wings;
        public boolean isFlying;
        public int timeFlying;
        public int prevTimeFlying;

        public Mutable(Flight flight) {
            this.wings = flight.wings();
            this.isFlying = flight.isFlying();
            this.timeFlying = flight.timeFlying();
            this.prevTimeFlying = flight.prevTimeFlying();
        }

        public Flight immutable() {
            return new Flight(this.wings, this.isFlying, this.timeFlying, this.prevTimeFlying);
        }
    }
}
