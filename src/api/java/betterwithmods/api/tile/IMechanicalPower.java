package betterwithmods.api.tile;

import betterwithmods.api.block.IOverpower;
import net.minecraft.block.Block;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IMechanicalPower {
    int getMechanicalOutput(Direction facing);

    int getMechanicalInput(Direction facing);

    int getMaximumInput(Direction facing);

    int getMinimumInput(Direction facing);

    Block getBlock();

    World getBlockWorld();

    BlockPos getBlockPos();

    default int calculateInput() {
        int findPower = 0;
        for (Direction facing : Direction.values()) {
            int power = getMechanicalInput(facing);
            if (power > findPower) {
                findPower = power;
            }
        }
        if (overpowerChance() && findPower > getMaximumInput(Direction.UP)) {
            overpower();
            return 0;
        }
        return findPower;
    }

    default boolean overpowerChance() {
        return getBlockWorld().rand.nextInt(10) == 0;
    }

    default void overpower() {
        if (getBlock() instanceof IOverpower)
            ((IOverpower) getBlock()).overpower(getBlockWorld(), getBlockPos());
    }

}
