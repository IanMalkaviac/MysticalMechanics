package betterwithmods.api.capabilities;

import betterwithmods.api.tile.ISteamPower;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

public class SteamCapability {
    @CapabilityInject(ISteamPower.class)
    public static Capability<ISteamPower> STEAM_CAPABILITY = null;

    public static class CapabilitySteamPower implements Capability.IStorage<ISteamPower> {
        @Override
        public INBT writeNBT(Capability<ISteamPower> capability, ISteamPower steam, Direction side) {
            return null;
        }

        @Override
        public void readNBT(Capability<ISteamPower> capability, ISteamPower steam, Direction side, INBT tag) {

        }
    }
}
