package fuzs.fantasticwings.flight.apparatus;

import fuzs.fantasticwings.FantasticWings;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

public interface FlightApparatus {
    FlightApparatus NONE = new FlightApparatus() {

        @Override
        public void onFlying(Player player, Vec3 direction) {
            // NO-OP
        }

        @Override
        public void onSlowlyDescending(Player player, Vec3 direction) {
            // NO-OP
        }

        @Override
        public boolean isUsableForFlying(Player player) {
            return false;
        }

        @Override
        public boolean isUsableForSlowlyDescending(Player player) {
            return false;
        }
    };

    void onFlying(Player player, Vec3 direction);

    void onSlowlyDescending(Player player, Vec3 direction);

    boolean isUsableForFlying(Player player);

    boolean isUsableForSlowlyDescending(Player player);

    record FlightApparatusHolder(FlightApparatus flightApparatus, boolean isEmpty) {
        private static final String KEY_IS_EMPTY = FantasticWings.id("is_empty").toString();
        private static final String KEY_FLIGHT_APPARATUS = FantasticWings.id("flight_apparatus").toString();
        private static final FlightApparatusHolder EMPTY = new FlightApparatusHolder(NONE, true);

        public FlightApparatusHolder {
            Objects.requireNonNull(flightApparatus, "flight apparatus is null");
        }

        public static FlightApparatusHolder of(FlightApparatus flightApparatus) {
            return new FlightApparatusHolder(flightApparatus, false);
        }

        public static FlightApparatusHolder empty() {
            return EMPTY;
        }

        public boolean is(FlightApparatus otherFlightApparatus) {
            return this.flightApparatus == otherFlightApparatus;
        }

        public boolean is(FlightApparatusHolder other) {
            return this.flightApparatus == other.flightApparatus();
        }

        public CompoundTag writeToNbtTag() {
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.putBoolean(KEY_IS_EMPTY, this.isEmpty);
            if (!this.isEmpty) {
                compoundTag.putByte(KEY_FLIGHT_APPARATUS, (byte) ((FlightApparatusImpl) this.flightApparatus).ordinal());
            }
            return compoundTag;
        }

        public static FlightApparatusHolder readFromNbtTag(CompoundTag compoundTag) {
            boolean isEmpty = compoundTag.getBoolean(KEY_IS_EMPTY);
            if (!isEmpty) {
                return new FlightApparatusHolder(FlightApparatusImpl.byId(compoundTag.getByte(KEY_FLIGHT_APPARATUS)), false);
            } else {
                return empty();
            }
        }
    }
}
