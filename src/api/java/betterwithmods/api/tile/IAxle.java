package betterwithmods.api.tile;

import net.minecraft.util.Direction;

public interface IAxle extends IMechanicalPower {
    byte getSignal();

    byte getMaximumSignal();

    int getMaximumInput();

    int getMinimumInput();

    Direction[] getDirections();

    Direction.Axis getAxis();

    default boolean isFacing(IAxle axle) {
        return axle.getAxis() == this.getAxis();
    }

}
