package fuzs.fantasticwings.flight;

import fuzs.fantasticwings.FantasticWings;
import fuzs.fantasticwings.flight.apparatus.FlightApparatus;
import fuzs.fantasticwings.init.ModRegistry;
import fuzs.fantasticwings.network.ServerboundControlFlyingMessage;
import fuzs.fantasticwings.util.CubicBezier;
import fuzs.fantasticwings.util.MathHelper;
import fuzs.puzzleslib.api.capability.v3.data.CapabilityComponent;
import fuzs.puzzleslib.api.network.v3.PlayerSet;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class FlightCapability extends CapabilityComponent<Player> {
    private static final String KEY_IS_FLYING = FantasticWings.id("is_flying").toString();
    private static final String KEY_TIME_FLYING = FantasticWings.id("time_flying").toString();
    private static final String KEY_WING_TYPE = FantasticWings.id("wing_type").toString();
    private static final CubicBezier FLY_AMOUNT_CURVE = new CubicBezier(0.37F, 0.13F, 0.3F, 1.12F);
    private static final int INITIAL_TIME_FLYING = 0;
    private static final int MAX_TIME_FLYING = 20;
    private static final float MIN_SPEED = 0.03F;
    private static final float MAX_SPEED = 0.0715F;
    private static final float Y_BOOST = 0.05F;
    private static final float MANUAL_Y_BOOST = 0.06F;
    private static final float FALL_REDUCTION = 0.9F;
    private static final float PITCH_OFFSET = 30.0F;

    private int prevTimeFlying = INITIAL_TIME_FLYING;
    private int timeFlying = INITIAL_TIME_FLYING;
    private boolean isFlying;
    private FlightApparatus.Holder holder = FlightApparatus.Holder.empty();

    public void setIsFlying(boolean isFlying) {
        this.setIsFlying(isFlying, null);
    }

    public void setIsFlying(boolean isFlying, @Nullable PlayerSet playerSet) {
        if (this.isFlying != isFlying) {
            this.isFlying = isFlying;
            if (this.isFlying) {
                this.getHolder().unRide();
            }
            this.setChanged(playerSet);
        }
    }

    public boolean isFlying() {
        return this.isFlying;
    }

    public void toggleIsFlying(PlayerSet playerSet) {
        this.setIsFlying(!this.isFlying, playerSet);
    }

    public void setTimeFlying(int timeFlying) {
        if (this.timeFlying != timeFlying) {
            this.timeFlying = timeFlying;
            this.setChanged(PlayerSet.ofNone());
        }
    }

    public void setWings(FlightApparatus.Holder flightApparatus) {
        if (!this.holder.is(flightApparatus)) {
            this.holder = flightApparatus;
            this.setChanged();
        }
    }

    public FlightApparatus.Holder getWings() {
        return this.holder;
    }

    public float getFlyingAmount(float delta) {
        return FLY_AMOUNT_CURVE.eval(MathHelper.lerp(this.prevTimeFlying, this.timeFlying, delta) / MAX_TIME_FLYING);
    }

    public boolean canUseWings() {
        return !this.getHolder().getAbilities().flying && !this.getHolder()
                .getItemBySlot(EquipmentSlot.CHEST)
                .is(ModRegistry.WING_OBSTRUCTIONS);
    }

    public boolean canFly() {
        return this.holder.flightApparatus().isUsableForFlying(this.getHolder()) && this.canUseWings();
    }

    public boolean canSlowlyDescend() {
        return this.holder.flightApparatus().isUsableForSlowlyDescending(this.getHolder()) && this.canUseWings() && (
                this.isFlying() || !this.getHolder().isDescending());
    }

    private void onWornUpdate() {
        Player player = this.getHolder();
        if (!player.level().isClientSide && this.isFlying() && !this.canFly()) {
            this.setIsFlying(false);
        }
        if (player.isEffectiveAi()) {
            if (this.isFlying()) {
                float speed = Mth.clampedLerp(MIN_SPEED, MAX_SPEED, player.zza);
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
            if (this.canSlowlyDescend()) {
                Vec3 deltaMovement = player.getDeltaMovement();
                if (deltaMovement.y() < 0.0D) {
                    player.setDeltaMovement(deltaMovement.multiply(1.0D, FALL_REDUCTION, 1.0D));
                }
                player.fallDistance = 0.0F;
            }
        }
    }

    public void tick() {
        Player player = this.getHolder();
        if (!this.holder.isEmpty() || !player.isEffectiveAi()) {
            this.onWornUpdate();
        } else if (!player.level().isClientSide && this.isFlying()) {
            this.setIsFlying(false);
        }
        this.prevTimeFlying = this.timeFlying;
        if (this.isFlying()) {
            if (this.timeFlying < MAX_TIME_FLYING) {
                this.setTimeFlying(this.timeFlying + 1);
            } else if (player.isLocalPlayer() && player.onGround()) {
                this.setIsFlying(false, PlayerSet.ofNone());
                FantasticWings.NETWORK.sendToServer(new ServerboundControlFlyingMessage(false));
            }
        } else if (this.timeFlying > INITIAL_TIME_FLYING) {
            this.setTimeFlying(this.timeFlying - 1);
        }
    }

    public void onFlown(Vec3 direction) {
        Player player = this.getHolder();
        if (this.isFlying()) {
            this.holder.flightApparatus().onFlying(player, direction);
        } else if (this.canSlowlyDescend() && player.getDeltaMovement().y() < -0.5) {
            this.holder.flightApparatus().onSlowlyDescending(player, direction);
        }
    }

    @Override
    public void write(CompoundTag compoundTag, HolderLookup.Provider registries) {
        compoundTag.putBoolean(KEY_IS_FLYING, this.isFlying);
        compoundTag.putInt(KEY_TIME_FLYING, this.timeFlying);
        compoundTag.put(KEY_WING_TYPE, this.holder.writeToNbtTag());
    }

    @Override
    public void read(CompoundTag compoundTag, HolderLookup.Provider registries) {
        this.isFlying = compoundTag.getBoolean(KEY_IS_FLYING);
        this.timeFlying = compoundTag.getInt(KEY_TIME_FLYING);
        this.holder = FlightApparatus.Holder.readFromNbtTag(compoundTag.getCompound(KEY_WING_TYPE));
    }
}
