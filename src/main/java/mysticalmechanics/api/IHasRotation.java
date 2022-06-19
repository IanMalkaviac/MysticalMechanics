package mysticalmechanics.api;

import net.minecraft.util.Direction;

import javax.annotation.Nonnull;

public interface IHasRotation {
    boolean hasRotation(@Nonnull Direction side);

    double getAngle(@Nonnull Direction side);

    double getLastAngle(@Nonnull Direction side);

    default void setRotation(@Nonnull Direction side, double angle, double lastAngle) {
        //NOOP
    }
}
