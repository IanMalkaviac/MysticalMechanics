package betterwithmods.api.block;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;

public interface IAdvancedRotationPlacement {

    BlockState getStateForAdvancedRotationPlacement(BlockState defaultState, Direction facing, float hitX, float hitY, float hitZ);



    default boolean isMax(double hit1, double hit2) {
        return Math.max(Math.abs(hit1), Math.abs(hit2)) == Math.abs(hit1);
    }

    default boolean inCenter(float hit1, float hit2, float max) {
        return Math.abs(hit1) <= max && Math.abs(hit2) <= max;
    }

}