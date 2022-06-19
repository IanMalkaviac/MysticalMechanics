package mysticalmechanics.api.lubricant;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;

import java.awt.*;

public interface ILubricant {
    ResourceLocation getType();

    String getUnlocalizedName();

    Color getColor();

    Object getParameter(String name);

    default double getDouble(String name, double _default) {
        Object value = getParameter(name);
        if(value instanceof Number)
            return ((Number) value).doubleValue();
        else
            return _default;
    }

    default double getSpeedMod() {
        return getDouble("speed",1);
    }

    default double getFrictionMod() {
        return getDouble("friction",1);
    }

    default double getHeatMod() {
        return getDouble("heat",1);
    }

    default CompoundNBT writeToNBT(CompoundNBT tag) {
        tag.putString("type",getType().toString());
        return tag;
    }

    default void readFromNBT(CompoundNBT tag) {}
}
