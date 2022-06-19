package betterwithmods.api.capabilities;

import betterwithmods.api.tile.IPollutant;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

public class PollutionCapability {
    @CapabilityInject(IPollutant.class)
    public static Capability<IPollutant> POLLUTION = null;

    public static class Impl implements Capability.IStorage<IPollutant> {
        @Override
        public INBT writeNBT(Capability<IPollutant> capability, IPollutant pollutant, Direction side) {
            return null;
        }

        @Override
        public void readNBT(Capability<IPollutant> capability, IPollutant pollutant, Direction side, INBT nbt) {

        }
    }

    public static class Default implements IPollutant {
        @Override
        public boolean isPolluting() {
            return false;
        }

        @Override
        public float getPollutionRate() {
            return 0F;
        }
    }
}
