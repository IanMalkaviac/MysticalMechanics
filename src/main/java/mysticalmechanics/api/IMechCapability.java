package mysticalmechanics.api;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;

public interface IMechCapability {
    /**
     * Use to retrieve how much power is provided from this block on a certain face.
     * Expected behavior is that this method returns 0 if the face is not an output face.
     *
     * @param from the face from which power is retrieved. Use null for internal use.
     * @return how much power is provided from the specified face. Unitless. Convert using IMechUnit.
     */
    double getPower(Direction from);

    default double getVisualPower(Direction from) {
        return getPower(from);
    }

    /**
     * Use to provide power to a block/entity from a certain face.
     * Expected behavior is that this method does nothing if the face is not an input face.
     *
     * @param value how much power is provided. Unitless. Convert using IMechUnit.
     * @param from the face from which power is provided. Use null for internal use.
     */
    void setPower(double value, Direction from);

    /**
     * This method should be run when setPower changes the internal power to something other than what it was before.
     * Expected behavior is that this method is used to propagate power change through the network.
     */
    void onPowerChange();

    default boolean isInput(Direction from) {
        return true;
    }

    default boolean isOutput(Direction from) {
        return true;
    }

    default void writeToNBT(CompoundNBT tag) {}

    default void readFromNBT(CompoundNBT tag) {}
}
