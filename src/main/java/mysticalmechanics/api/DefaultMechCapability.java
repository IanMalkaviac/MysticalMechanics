package mysticalmechanics.api;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;

public class DefaultMechCapability implements IMechCapability {
    public double power = 0;

    @Override
    public double getPower(Direction from) {
        return power;
    }

    @Override
    public void setPower(double value, Direction from) {
        double oldPower = power;
        this.power = value;
        if (oldPower != value) {
            onPowerChange();
        }
    }

    @Override
    public void onPowerChange() {

    }

    @Override
    public void readFromNBT(CompoundNBT tag) {
        power = tag.getDouble("mech_power");
    }

    @Override
    public void writeToNBT(CompoundNBT tag) {
        tag.putDouble("mech_power",power);
    }
}
