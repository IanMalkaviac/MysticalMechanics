package mysticalmechanics.api.lubricant;

import net.minecraft.nbt.INBT;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class TileLubricantCapability extends DefaultLubricantCapability {
    protected TileEntity tile;
    protected List<LubricantStack> lubricants = new ArrayList<>();
    protected int capacity;

    public TileLubricantCapability(TileEntity tile, int capacity) {
        this.tile = tile;
        this.capacity = capacity;
    }

    public TileEntity getTile() {
        return tile;
    }

    public void tick() {
        Iterator<LubricantStack> iterator = lubricants.iterator();
        while(iterator.hasNext()) {
            LubricantStack stack = iterator.next();
            stack.deplete(1);
            if(stack.isEmpty()) {
                iterator.remove();
            }
        }
    }

    @Override
    public int getCapacity() {
        return capacity;
    }

    @Override
    public int lubricate(ILubricant lubricant, int amount, boolean simulate) {
        int capacity = getCapacity();
        //Search for existing stack and increment
        for (LubricantStack existing : lubricants) {
            if(existing.getLubricant().equals(lubricant)) {
                int toAdd = Math.min(capacity - existing.getAmount(), amount);
                if(!simulate)
                    existing.increment(toAdd);
                return amount - toAdd;
            }
        }
        //No existing stack found, add new
        int toAdd = Math.min(capacity, amount);
        if(!simulate) {
            lubricants.add(new LubricantStack(lubricant, toAdd));
        }
        return amount - toAdd;
    }

    @Override
    public Collection<LubricantStack> getAppliedLubricant() {
        return lubricants;
    }

    @Override
    public CompoundNBT writeToNBT(CompoundNBT tag) {
        ListNBT tagList = new ListNBT();
        for (LubricantStack stack : lubricants) {
            tagList.add(stack.serializeNBT());
        }
        tag.put("lubricants", tagList);
        return tag;
    }

    @Override
    public void readFromNBT(CompoundNBT tag) {
        ListNBT tagList = tag.getList("lubricants", 10);
        lubricants.clear();
        for (INBT compound : tagList) {
            lubricants.add(new LubricantStack((CompoundNBT) compound));
        }
    }
}
