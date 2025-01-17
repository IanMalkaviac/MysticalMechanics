package mysticalmechanics.api;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;

import javax.annotation.Nullable;

public interface IGearBehavior {
    IGearBehavior NO_BEHAVIOR = new IGearBehavior() {
        @Override
        public double transformPower(TileEntity tile, @Nullable Direction facing, ItemStack gear, IGearData data, double power) {
            return power;
        }
    };

    default void onAttach(TileEntity tile, @Nullable Direction facing, ItemStack gear, IGearData data, PlayerEntity player) {
        //NOOP
    }

    default ItemStack onDetach(TileEntity tile, @Nullable Direction facing, ItemStack gear, IGearData data, PlayerEntity player) {
        return gear;
    }

    /**
     * Implement to modify how much power this gear can transmit. This should be suitable to implement all manner of mechanics like low/high-pass filters, friction and power limits.
     *
     * @param tile the TileEntity this gear is attached to
     * @param facing which face the gear is attached to
     * @param gear the ItemStack representing the attached gear
     * @param power how much power would be returned at full efficiency (100%) (note: for the standard gearbox the division by the number of connections for output gears is already included here)
     * @return how much power will actually be returned. Should absolutely NEVER be greater than the power passed into this method, or the gear can lead to power loops!
     */
    default double transformPower(TileEntity tile, @Nullable Direction facing, ItemStack gear, double power) {
        return transformPower(tile, facing, gear, null, power);
    }

    /**
     * Works like {@link #transformPower(TileEntity, Direction, ItemStack, double)}, but also accepts gear data
     *
     * @param data the gear's data container
     */
    default double transformPower(TileEntity tile, @Nullable Direction facing, ItemStack gear, IGearData data, double power) {
        return transformPower(tile,facing,gear,power);
    }

    /**
     * Implement to modify how fast the gear spins when rendered.
     *
     * @param tile the TileEntity this gear is attached to
     * @param facing which face the gear is attached to
     * @param gear the ItemStack representing the attached gear
     * @param power how much power would be returned at full efficiency (100%)
     * @return how much power will actually be returned.
     */
    default double transformVisualPower(TileEntity tile, @Nullable Direction facing, ItemStack gear, double power) {
        return transformPower(tile,facing,gear,power);
    }

    /**
     * Works like {@link #transformVisualPower(TileEntity, Direction, ItemStack, double)}, but also accepts gear data
     *
     * @param data the gear's data container
     */
    default double transformVisualPower(TileEntity tile, @Nullable Direction facing, ItemStack gear, IGearData data, double power) {
        return transformVisualPower(tile, facing, gear, power);
    }

    /**
     * Implement this to provide special visual effects while the gear is attached to a gearbox or machine.
     *
     * @param tile the TileEntity this gear is attached to
     * @param facing which face the gear is attached to
     * @param gear the ItemStack representing the attached gear
     */
    @Deprecated
    default void visualUpdate(TileEntity tile, @Nullable Direction facing, ItemStack gear) {
        //NOOP
    }

    /**
     * Works like {@link #visualUpdate(TileEntity, Direction, ItemStack)}, but also accepts gear data
     *
     * @param data the gear's data container
     */
    default void visualUpdate(TileEntity tile, @Nullable Direction facing, ItemStack gear, IGearData data, double powerIn, double powerOut) {
        visualUpdate(tile, facing, gear);
    }

    default boolean canTick(ItemStack gear) {
        return false;
    }

    @Deprecated
    default void tick(TileEntity tile, @Nullable Direction facing, ItemStack gear, double power) {
        //NOOP
    }

    default void tick(TileEntity tile, @Nullable Direction facing, ItemStack gear, IGearData data, double powerIn, double powerOut) {
        tick(tile, facing, gear, powerIn);
    }

    default boolean hasData() {
        return false;
    }

    default IGearData createData() {
        return null;
    }
}
