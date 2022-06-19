package betterwithmods.api.tile.multiblock;

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

public interface IExternalCapability {
    boolean hasExternalCapability(BlockPos pos, Capability<?> capability, @Nullable Direction facing);

    <T> T getExternalCapability(BlockPos pos, Capability<?> capability, @Nullable Direction facing);
}
