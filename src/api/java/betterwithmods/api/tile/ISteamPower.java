package betterwithmods.api.tile;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;

import javax.annotation.Nullable;

public interface ISteamPower {
    void readSteamPower(CompoundNBT tag);

    CompoundNBT writeSteamPower(CompoundNBT tag);

    int getHeatUnits(Direction facing);

    void calculateHeatUnits();

    int getSteamPower(Direction facing);

    void calculateSteamPower(@Nullable Direction facing);

    void setSteamUpdate(boolean update);

    boolean canTransferItem();
}
