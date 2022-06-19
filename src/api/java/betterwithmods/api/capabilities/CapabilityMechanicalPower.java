package betterwithmods.api.capabilities;

import betterwithmods.api.tile.IMechanicalPower;
import net.minecraft.block.Block;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

public class CapabilityMechanicalPower {
    @SuppressWarnings("CanBeFinal")
    @CapabilityInject(IMechanicalPower.class)
    public static Capability<IMechanicalPower> MECHANICAL_POWER = null;

    public static class Impl implements Capability.IStorage<IMechanicalPower> {
        @Override
        public INBT writeNBT(Capability<IMechanicalPower> capability, IMechanicalPower mechanical, Direction side) {
            return null;
        }

        @Override
        public void readNBT(Capability<IMechanicalPower> capability, IMechanicalPower mechanical, Direction side, INBT nbt) {

        }
    }

    public static class Default implements IMechanicalPower {
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
