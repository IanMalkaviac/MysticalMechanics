package mysticalmechanics.api;

import net.minecraft.nbt.CompoundNBT;

public interface IGearData {
    void readFromNBT(CompoundNBT tag);

    CompoundNBT writeToNBT(CompoundNBT tag);

    boolean isDirty();
}
