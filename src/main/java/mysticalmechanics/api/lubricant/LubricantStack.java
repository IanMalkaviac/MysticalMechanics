package mysticalmechanics.api.lubricant;

import mysticalmechanics.api.MysticalMechanicsAPI;
import net.minecraft.nbt.CompoundNBT;

public class LubricantStack {
    private ILubricant lubricant;
    private int amount;

    public LubricantStack(ILubricant lubricant, int amount) {
        this.lubricant = lubricant;
        this.amount = amount;
    }

    public LubricantStack(CompoundNBT tag) {
        readFromNBT(tag);
    }

    public ILubricant getLubricant() {
        return lubricant;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public boolean isEmpty() {
        return amount <= 0;
    }

    public void increment(int n) {
        amount += n;
    }

    public void deplete(int n) {
        amount -= n;
    }

    CompoundNBT writeToNBT(CompoundNBT tag) {
        lubricant.writeToNBT(tag);
        tag.putInt("amount", amount);
        return tag;
    }

    void readFromNBT(CompoundNBT tag) {
        lubricant = MysticalMechanicsAPI.IMPL.deserializeLubricant(tag);
        amount = tag.getInt("amount");
    }

    public CompoundNBT serializeNBT() {
        return writeToNBT(new CompoundNBT());
    }

    public String getUnlocalizedName() {
        return lubricant.getUnlocalizedName();
    }
}
