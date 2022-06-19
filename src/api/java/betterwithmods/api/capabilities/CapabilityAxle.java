package betterwithmods.api.capabilities;

import betterwithmods.api.tile.IAxle;
import net.minecraft.block.Block;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

public class CapabilityAxle {
    @SuppressWarnings("CanBeFinal")
    @CapabilityInject(IAxle.class)
    public static Capability<IAxle> AXLE = null;

    public static class Impl implements Capability.IStorage<IAxle> {
        @Override
        public INBT writeNBT(Capability<IAxle> capability, IAxle mechanical, Direction side) {
            return null;
        }

        @Override
        public void readNBT(Capability<IAxle> capability, IAxle mechanical, Direction side, INBT nbt) {

        }
    }

    public static class Default implements IAxle {

        @Override
        public byte getSignal() {
            return 0;
        }

        @Override
        public byte getMaximumSignal() {
            return 0;
        }

        @Override
        public int getMaximumInput() {
            return 0;
        }

        @Override
        public int getMinimumInput() {
            return 0;
        }

        @Override
        public Direction[] getDirections() {
            return new Direction[0];
        }

        @Override
        public Direction.Axis getAxis() {
            return null;
        }

        @Override
        public int getMechanicalOutput(Direction facing) {
            return 0;
        }

        @Override
        public int getMechanicalInput(Direction facing) {
            return 0;
        }

        @Override
        public int getMaximumInput(Direction facing) {
            return 0;
        }

        @Override
        public int getMinimumInput(Direction facing) {
            return 0;
        }

        @Override
        public Block getBlock() {
            return null;
        }

        @Override
        public World getBlockWorld() {
            return null;
        }

        @Override
        public BlockPos getBlockPos() {
            return null;
        }
    }
}
