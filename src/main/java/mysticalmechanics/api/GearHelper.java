package mysticalmechanics.api;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GearHelper {
    ItemStack gear = ItemStack.EMPTY;
    IGearData data;

    public void setGear(ItemStack stack) {
        gear = stack;
        data = null;
        createData();
    }

    public ItemStack getGear() {
        return gear;
    }

    public IGearData getData() {
        return data;
    }

    @Nonnull
    public IGearBehavior getBehavior() {
        return MysticalMechanicsAPI.IMPL.getGearBehavior(gear);
    }

    public boolean isEmpty() {
        return gear.isEmpty();
    }

    private void createData() {
        IGearBehavior behavior = getBehavior();
        data = behavior.createData();
    }

    public boolean isDirty() {
        return data != null && data.isDirty();
    }

    public void attach(@Nullable PlayerEntity player, ItemStack stack) {
        gear = stack;
        createData();
    }

    public ItemStack detach(@Nullable PlayerEntity player) {
        ItemStack removed = gear;
        gear = ItemStack.EMPTY;
        data = null;
        return removed;
    }

    public boolean canAttach(ItemStack stack) {
        return true;
    }

    public boolean canDetach() {
        return true;
    }

    public void readFromNBT(CompoundNBT tag) {
        gear = new ItemStack(tag.getCompoundTag("gear"));
        data = null;
        createData();
        if(data != null)
            data.readFromNBT(tag.getCompoundTag("data"));
    }

    public CompoundNBT writeToNBT(CompoundNBT tag) {
        tag.setTag("gear", gear.serializeNBT());
        if(data != null)
            tag.setTag("data", data.writeToNBT(new CompoundNBT()));
        return tag;
    }
}
