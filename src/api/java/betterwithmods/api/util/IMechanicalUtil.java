package betterwithmods.api.util;

import betterwithmods.api.tile.IAxle;
import betterwithmods.api.tile.IMechanicalPower;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public interface IMechanicalUtil {

    IMechanicalPower getMechanicalPower(World world, BlockPos pos, Direction facing);

    IAxle getAxle(IBlockAccess world, BlockPos pos, Direction facing);

    boolean isRedstonePowered(World world, BlockPos pos);

    boolean canInput(World world, BlockPos pos, Direction facing);

    boolean isAxle(IBlockAccess world, BlockPos pos, Direction facing);

    int getPowerOutput(World world, BlockPos pos, Direction facing);
}
