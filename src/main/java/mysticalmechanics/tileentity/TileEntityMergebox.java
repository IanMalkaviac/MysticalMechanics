package mysticalmechanics.tileentity;

import java.util.ArrayList;
import java.util.List;

import mysticalmechanics.api.*;
import mysticalmechanics.block.BlockGearbox;
import mysticalmechanics.util.Misc;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;

public class TileEntityMergebox extends TileEntityGearbox {
	//public int connections = 0;
	
    @Override
    public DefaultMechCapability createCapability() {
        return new MergeboxMechCapability();
    }

    @Override
    public void update() {
        super.update();
        ((MergeboxMechCapability)capability).reduceWait();
    }

    @Override
    protected double getInternalPower(Direction facing) {
        return ((MergeboxMechCapability)capability).getInternalPower(facing);
    }

    @Override
    protected double getExternalPower(Direction facing) {
        return ((MergeboxMechCapability)capability).getExternalPower(facing);
    }

    @Override
    public void updateNeighbors() {
        BlockState state = world.getBlockState(getPos());
        
        //manages Mergeboxes input;
        for (Direction f : Direction.VALUES) {
            MysticalMechanicsAPI.IMPL.pullPower(this, f, capability, !getGear(f).isEmpty());
        }
        
        connections = 0;
        List<Direction> toUpdate = new ArrayList<>();
        for (Direction f : Direction.VALUES) {
            if (f != from) {
                TileEntity t = world.getTileEntity(getPos().offset(f));
                if (t != null && t.hasCapability(MysticalMechanicsAPI.MECH_CAPABILITY, f.getOpposite())) {
                    if (!getGear(f).isEmpty() && !toUpdate.contains(f)) {
                    	toUpdate.add(f);
                    	connections++;                    	
                    }else if(getGear(f).isEmpty() && toUpdate.contains(f)) {
                    	toUpdate.remove(f);
                    	connections--;
                    }                   
                }
            }                  
        }
        
        //manages Mergeboxes output
        if (state.getBlock() instanceof BlockGearbox) {
            from = state.getValue(BlockGearbox.facing);
            MysticalMechanicsAPI.IMPL.pushPower(this, from, capability, !getGear(from).isEmpty());
        }        
        markDirty();
    }
    
    //repurposing this for inputTracking;
    @Override
    public int getConnections() {
        return connections;
    }

    private class MergeboxMechCapability extends DefaultMechCapability {
        public double[] powerValues = {0,0,0,0,0,0};
        public double[] powerValuesExternal = {0,0,0,0,0,0};
        public int waitTime;

        public void reduceWait() {
            if (waitTime > 0) {
                waitTime--;
                if (waitTime <= 0) {
                    updateNeighbors();
                }
            }
        }

        @Override
        public void onPowerChange() {
            TileEntityGearbox box = TileEntityMergebox.this;
            box.shouldUpdate = true;
            box.markDirty();
        }

        @Override
        public double getPower(Direction from) {
            GearHelper gearHelper = getGearHelper(from);
            if (gearHelper != null && gearHelper.isEmpty()) {
                return 0;
            }

            double unchangedPower = getInternalPower(from);

            if (gearHelper == null)
                return unchangedPower;

            IGearBehavior behavior = gearHelper.getBehavior();
            return behavior.transformPower(TileEntityMergebox.this, from, gearHelper.getGear(), gearHelper.getData(), unchangedPower);
        }

        @Override
        public double getVisualPower(Direction from) {
            GearHelper gearHelper = getGearHelper(from);
            if (gearHelper != null && gearHelper.isEmpty()) {
                return 0;
            }

            double unchangedPower;
            if(isInput(from))
                unchangedPower = getExternalPower(from);
            else
                unchangedPower = getInternalPower(from);

            if (gearHelper == null)
                return unchangedPower;

            IGearBehavior behavior = gearHelper.getBehavior();
            return behavior.transformVisualPower(TileEntityMergebox.this, from, gearHelper.getGear(), gearHelper.getData(), unchangedPower);
        }

        private double getInternalPower(Direction from) {
            //need to work out solution for null checks that aren't the renderer.
            if (isOutput(from) && !getGear(from).isEmpty() && getConnections() != 0) {
                return Math.max(0, getPowerInternal());
            } else if (from != null && !getGear(from).isEmpty()) {
                return powerValues[from.getIndex()];
            } else {
                return 0;
            }
        }

        private double getExternalPower(Direction from) {
            if(from != null && isInput(from))
                return powerValuesExternal[from.getIndex()];
            else
                return 0;
        }

        @Override
        public void setPower(double value, Direction from) {
            GearHelper gearHelper = getGearHelper(from);
            if(from == null) {
                for (int i = 0; i < powerValues.length; i++) {
                    powerValues[i] = 0;
                }
                onPowerChange();
            }
        	if(from != null && gearHelper != null && !isOutput(from)) {
        		double oldPower = powerValues[from.getIndex()];
        		powerValuesExternal[from.getIndex()] = value;
        		if(!gearHelper.isEmpty()) {
                    IGearBehavior behavior = gearHelper.getBehavior();
                    value = behavior.transformPower(TileEntityMergebox.this,from,gearHelper.getGear(),gearHelper.getData(),value);
                }
        		if(oldPower != value && (value == 0 || !gearHelper.isEmpty())) {
        			powerValues[from.getIndex()] = value;
                    waitTime = 20;
        			onPowerChange();
        		}
        	} else if(from == null && TileEntityMergebox.this.isBroken) {
        		for(Direction face : Direction.values()) {
        			powerValues[face.getIndex()] = 0;
        			onPowerChange();
        		}
        	}           
        }

        private double getPowerInternal() {
            double adjustedPower;
            if(waitTime > 0)
                adjustedPower = 0;
            else {
                adjustedPower = 0;
                double equalPower = Double.POSITIVE_INFINITY;
                for (Direction facing : Direction.VALUES) {
                    if (isOutput(facing))
                        continue;
                    double power = powerValues[facing.getIndex()];
                    if (power > 0)
                        equalPower = Math.min(equalPower, power);
                }
                for (Direction face : Direction.values()) {
                    double power = powerValues[face.getIndex()];
                    if (!isOutput(face) && Misc.isRoughlyEqual(equalPower, power)) {
                        adjustedPower += power;
                    }
                }
            }
            if(power != adjustedPower) {
                power = adjustedPower;
                markDirty();
            }
            return power;
        }

        @Override
        public boolean isInput(Direction from) {
            return TileEntityMergebox.this.from != from;
        }

        @Override
        public boolean isOutput(Direction from) {
            return TileEntityMergebox.this.from == from;
        }

        @Override
        public void readFromNBT(CompoundNBT tag) {
            super.readFromNBT(tag);
            waitTime = tag.getInteger("waitTime");
            powerValues[Direction.UP.getIndex()] = tag.getDouble("mechPowerUp");
            powerValues[Direction.DOWN.getIndex()] = tag.getDouble("mechPowerDown");
            powerValues[Direction.NORTH.getIndex()] = tag.getDouble("mechPowerNorth");
            powerValues[Direction.SOUTH.getIndex()] = tag.getDouble("mechPowerSouth");
            powerValues[Direction.EAST.getIndex()] = tag.getDouble("mechPowerEast");
            powerValues[Direction.WEST.getIndex()] = tag.getDouble("mechPowerWest");
            powerValuesExternal[Direction.UP.getIndex()] = tag.getDouble("mechPowerExternalUp");
            powerValuesExternal[Direction.DOWN.getIndex()] = tag.getDouble("mechPowerExternalDown");
            powerValuesExternal[Direction.NORTH.getIndex()] = tag.getDouble("mechPowerExternalNorth");
            powerValuesExternal[Direction.SOUTH.getIndex()] = tag.getDouble("mechPowerExternalSouth");
            powerValuesExternal[Direction.EAST.getIndex()] = tag.getDouble("mechPowerExternalEast");
            powerValuesExternal[Direction.WEST.getIndex()] = tag.getDouble("mechPowerExternalWest");
        }

        @Override
        public void writeToNBT(CompoundNBT tag) {
            super.writeToNBT(tag);
            tag.setInteger("waitTime",waitTime);
            tag.putDouble("mechPowerUp",powerValues[Direction.UP.getIndex()]);
            tag.putDouble("mechPowerDown",powerValues[Direction.DOWN.getIndex()]);
            tag.putDouble("mechPowerNorth",powerValues[Direction.NORTH.getIndex()]);
            tag.putDouble("mechPowerSouth", powerValues[Direction.SOUTH.getIndex()]);
            tag.putDouble("mechPowerEast",powerValues[Direction.EAST.getIndex()]);
            tag.putDouble("mechPowerWest",powerValues[Direction.WEST.getIndex()]);
            tag.putDouble("mechPowerExternalUp",powerValuesExternal[Direction.UP.getIndex()]);
            tag.putDouble("mechPowerExternalDown",powerValuesExternal[Direction.DOWN.getIndex()]);
            tag.putDouble("mechPowerExternalNorth",powerValuesExternal[Direction.NORTH.getIndex()]);
            tag.putDouble("mechPowerExternalSouth", powerValuesExternal[Direction.SOUTH.getIndex()]);
            tag.putDouble("mechPowerExternalEast",powerValuesExternal[Direction.EAST.getIndex()]);
            tag.putDouble("mechPowerExternalWest",powerValuesExternal[Direction.WEST.getIndex()]);
        }
    }
}
