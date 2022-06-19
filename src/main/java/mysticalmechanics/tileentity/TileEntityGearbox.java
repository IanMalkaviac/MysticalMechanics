package mysticalmechanics.tileentity;

import mysticalmechanics.MysticalMechanics;
import mysticalmechanics.api.*;
import mysticalmechanics.api.lubricant.TileLubricantCapability;
import mysticalmechanics.block.BlockGearbox;
import mysticalmechanics.handler.RegistryHandler;
import mysticalmechanics.util.ISoundController;
import mysticalmechanics.util.Misc;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockState;
import net.minecraft.state.IStateHolder;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TileEntityGearbox extends TileEntity implements ITickable, IGearbox, ISoundController {
    Direction from = null;       
    protected boolean isBroken;
    public int connections = 0;
    public GearHelperTile[] gears = new GearHelperTile[6];

    public DefaultMechCapability capability;
    public TileLubricantCapability lubricant;

    //Don't look at me
    public static final int SOUND_SLOW_LV1 = 1;
    public static final int SOUND_SLOW_LV2 = 2;
    public static final int SOUND_SLOW_LV3 = 3;
    public static final int SOUND_MID_LV1 = 4;
    public static final int SOUND_MID_LV2 = 5;
    public static final int SOUND_MID_LV3 = 6;
    public static final int SOUND_FAST_LV1 = 7;
    public static final int SOUND_FAST_LV2 = 8;
    public static final int SOUND_FAST_LV3 = 9;
    public static final int[] SOUND_IDS = new int[]{SOUND_SLOW_LV1,SOUND_SLOW_LV2,SOUND_SLOW_LV3,SOUND_MID_LV1,SOUND_MID_LV2,SOUND_MID_LV3,SOUND_FAST_LV1,SOUND_FAST_LV2,SOUND_FAST_LV3};

    HashMap<Integer,Integer> soundsPlaying = new HashMap<>();
    public boolean shouldUpdate;

    public TileEntityGearbox() {
        super();
        capability = createCapability();
        for(int i = 0; i < gears.length; i++)
            gears[i] = new GearHelperTile(this, Direction.getFront(i));
        lubricant = new TileLubricantCapability(this, 1000);
    }

    public DefaultMechCapability createCapability() {
        return new GearboxMechCapability();
    }

    public void updateNeighbors() {
        BlockState state = world.getBlockState(getPos());
        
        //sets Gearbox Input.
        if (state.getBlock() instanceof BlockGearbox) {
            from = state..getValue(BlockGearbox.facing);
            MysticalMechanicsAPI.IMPL.pullPower(this, from, capability, !getGear(from).isEmpty());
        }
        
        connections = 0;
        List<Direction> toUpdate = new ArrayList<>();
        for (Direction f : Direction.values()) {
            if (f != null && f != from) {
                TileEntity t = world.getTileEntity(getPos().offset(f));
                if (t != null && t.hasCapability(MysticalMechanicsAPI.MECH_CAPABILITY, f.getOpposite())) {                	
                    if (!getGear(f).isEmpty() && capability.getPower(f) != 0) {
                    	connections++;
                    }
                    toUpdate.add(f);


                }
            }                  
        }
        
        //Manages Power Output.
        for (Direction f : toUpdate) {
            MysticalMechanicsAPI.IMPL.pushPower(this, f, capability, !getGear(f).isEmpty());
        }
        markDirty();
    }

    @Override
    public CompoundNBT writeToNBT(CompoundNBT tag) {
        super.writeToNBT(tag);
        capability.writeToNBT(tag);
        lubricant.writeToNBT(tag);
        if (from != null) {
            tag.setInteger("from", from.getIndex());
        }
        for (int i = 0; i < 6; i++) {
            tag.setTag("side" + i, gears[i].writeToNBT(new CompoundNBT()));
        }
        tag.setInteger("connections", connections);
        return tag;
    }

    @Override
    public void readFromNBT(CompoundNBT tag) {
        super.readFromNBT(tag);
        capability.readFromNBT(tag);
        lubricant.readFromNBT(tag);
        if (tag.hasKey("from")) {
            from = Direction.getFront(tag.getInteger("from"));
        }
        for (int i = 0; i < 6; i++) {
            gears[i].readFromNBT(tag.getCompoundTag("side" + i));
        }
        readLegacyGears(tag);
        connections = tag.getInteger("connections");
    }

    private void readLegacyGears(CompoundNBT tag) {

        for (int i = 0; i < 6; i++) {
            if(tag.hasKey("gear"+i))
                gears[i].setGear(new ItemStack(tag.getCompoundTag("gear" + i)));
        }
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return writeToNBT(new CompoundNBT());
    }

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(getPos(), 0, getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        readFromNBT(pkt.getNbtCompound());
    }

    @Override
    public boolean hasCapability(Capability<?> capability, Direction facing) {
        if (capability == MysticalMechanicsAPI.MECH_CAPABILITY) {
            return true;
        }
        if (capability == MysticalMechanicsAPI.LUBRICANT_CAPABILITY) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, Direction facing) {
        if (capability == MysticalMechanicsAPI.MECH_CAPABILITY) {
        	@SuppressWarnings("unchecked") 
			T result = (T) this.capability;
            return result;
        }
        if (capability == MysticalMechanicsAPI.LUBRICANT_CAPABILITY) {
            return (T) lubricant;
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public void attachGear(Direction facing, ItemStack stack, PlayerEntity player) {
        if (facing == null)
            return;
        int index = facing.getIndex();
        gears[index].attach(player, stack);
        capability.onPowerChange();
    }

    @Override
    public ItemStack detachGear(Direction facing, PlayerEntity player) {
        if (facing == null)
            return ItemStack.EMPTY;
        int index = facing.getIndex();
        ItemStack gear = gears[index].detach(player);
        capability.onPowerChange();
        
        return gear;
    }

    @Override
    public ItemStack getGear(Direction facing) {
        if (facing == null)
            return ItemStack.EMPTY;
        return gears[facing.getIndex()].getGear();
    }

    @Nullable
    public GearHelperTile getGearHelper(Direction facing) {
        if (facing == null)
            return null;
        return gears[facing.getIndex()];
    }

    @Override
    public boolean canAttachGear(Direction facing, ItemStack stack) {
        return gears[facing.getIndex()].canAttach(stack);
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, BlockState oldState, BlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }

    @Override
    public int getConnections() {
        return connections;
    }
    
    public boolean activate(World world, BlockPos pos, BlockState state, PlayerEntity player, EnumHand hand,
                            Direction side, float hitX, float hitY, float hitZ) {
        ItemStack heldItem = player.getHeldItem(hand);
        Direction attachSide = side;
        if(hand == EnumHand.OFF_HAND)
            return false;
        if(player.isSneaking())
            attachSide = attachSide.getOpposite();
        if (!heldItem.isEmpty() && canAttachGear(attachSide,heldItem) && getGear(attachSide).isEmpty() && MysticalMechanicsAPI.IMPL.isValidGear(heldItem)) {
                ItemStack gear = heldItem.copy();
                gear.setCount(1);
                attachGear(attachSide,gear,player);
                heldItem.shrink(1);
                if (heldItem.isEmpty()) {
                    player.setHeldItem(hand, ItemStack.EMPTY);
                }
                return true;
        } else if (!getGear(attachSide).isEmpty()) {
            ItemStack gear = detachGear(attachSide,player);
            if (!world.isRemote) {
                world.spawnEntity(new EntityItem(world, player.posX, player.posY + player.height / 2.0f, player.posZ, gear));
            }
            return true;
        }
        return false;
    }

    @Override
    public void playSound(int id) {
        int playId = -getPlayId(id) + 1;
        switch (id) {
            case SOUND_SLOW_LV1:
                MysticalMechanics.proxy.playMachineSound(this, SOUND_SLOW_LV1, playId, RegistryHandler.GEARBOX_SLOW_LV1, SoundCategory.BLOCKS, true, 1.0f, 1.0f, (float)pos.getX()+0.5f, (float)pos.getY()+0.5f, (float)pos.getZ()+0.5f);
                break;
            case SOUND_SLOW_LV2:
                MysticalMechanics.proxy.playMachineSound(this, SOUND_SLOW_LV2, playId, RegistryHandler.GEARBOX_SLOW_LV2, SoundCategory.BLOCKS, true, 1.0f, 1.0f, (float)pos.getX()+0.5f, (float)pos.getY()+0.5f, (float)pos.getZ()+0.5f);
                break;
            case SOUND_SLOW_LV3:
                MysticalMechanics.proxy.playMachineSound(this, SOUND_SLOW_LV3, playId, RegistryHandler.GEARBOX_SLOW_LV3, SoundCategory.BLOCKS, true, 1.0f, 1.0f, (float)pos.getX()+0.5f, (float)pos.getY()+0.5f, (float)pos.getZ()+0.5f);
                break;
            case SOUND_MID_LV1:
                MysticalMechanics.proxy.playMachineSound(this, SOUND_MID_LV1, playId, RegistryHandler.GEARBOX_MID_LV1, SoundCategory.BLOCKS, true, 1.0f, 1.0f, (float)pos.getX()+0.5f, (float)pos.getY()+0.5f, (float)pos.getZ()+0.5f);
                break;
            case SOUND_MID_LV2:
                MysticalMechanics.proxy.playMachineSound(this, SOUND_MID_LV2, playId, RegistryHandler.GEARBOX_MID_LV2, SoundCategory.BLOCKS, true, 1.0f, 1.0f, (float)pos.getX()+0.5f, (float)pos.getY()+0.5f, (float)pos.getZ()+0.5f);
                break;
            case SOUND_MID_LV3:
                MysticalMechanics.proxy.playMachineSound(this, SOUND_MID_LV3, playId, RegistryHandler.GEARBOX_MID_LV3, SoundCategory.BLOCKS, true, 1.0f, 1.0f, (float)pos.getX()+0.5f, (float)pos.getY()+0.5f, (float)pos.getZ()+0.5f);
                break;
            case SOUND_FAST_LV1:
                MysticalMechanics.proxy.playMachineSound(this, SOUND_FAST_LV1, playId, RegistryHandler.GEARBOX_FAST_LV1, SoundCategory.BLOCKS, true, 1.0f, 1.0f, (float)pos.getX()+0.5f, (float)pos.getY()+0.5f, (float)pos.getZ()+0.5f);
                break;
            case SOUND_FAST_LV2:
                MysticalMechanics.proxy.playMachineSound(this, SOUND_FAST_LV2, playId, RegistryHandler.GEARBOX_FAST_LV2, SoundCategory.BLOCKS, true, 1.0f, 1.0f, (float)pos.getX()+0.5f, (float)pos.getY()+0.5f, (float)pos.getZ()+0.5f);
                break;
            case SOUND_FAST_LV3:
                MysticalMechanics.proxy.playMachineSound(this, SOUND_FAST_LV2, playId, RegistryHandler.GEARBOX_FAST_LV3, SoundCategory.BLOCKS, true, 1.0f, 1.0f, (float)pos.getX()+0.5f, (float)pos.getY()+0.5f, (float)pos.getZ()+0.5f);
                break;
        }
        soundsPlaying.put(id,playId);
    }

    @Override
    public void stopSound(int id) {
        soundsPlaying.put(id,-getPlayId(id));
    }

    @Override
    public int getPlayId(int id) {
        if(!soundsPlaying.containsKey(id))
            return 0;
        return soundsPlaying.get(id);
    }

    @Override
    public int[] getSoundIDs() {
        return SOUND_IDS;
    }

    @Override
    public boolean shouldPlaySound(int id) {
        double power = capability.getVisualPower(null);
        int level = getSoundLevel();
        int speedindex = getSpeedindex(power);
        return speedindex > 0 && level > 0 && id == SOUND_IDS[speedindex - 1 + level];
    }

    private int getSpeedindex(double power) {
        int speedindex = 0;
        if(power > 50)
            speedindex = 3;
        else if(power > 25)
            speedindex = 2;
        else if(power > 0)
            speedindex = 1;
        return speedindex;
    }

    @Override
    public float getCurrentPitch(int id, float pitch) {
        double power =  capability.getVisualPower(null);
        int speedindex = getSpeedindex(power);
        if(speedindex == 1)
            return (float) (power*2 / 25.0);
        else if(speedindex == 2)
            return (float) (power*2 / 50.0);
        else if(speedindex == 3)
            return (float) (power*2 / 100.0);
        return 0;
    }

    private int getSoundLevel() {
        int level = 0;
        for (Direction.Axis axis : Direction.Axis.values()) {
            if(!getGear(Direction.getFacingFromAxis(Direction.AxisDirection.POSITIVE,axis)).isEmpty() || !getGear(Direction.getFacingFromAxis(Direction.AxisDirection.NEGATIVE,axis)).isEmpty())
                level++;
        }
        return level;
    }

    @Override
    public void markDirty() {
        super.markDirty();       
        Misc.syncTE(this, isBroken);
    }

    public void breakBlock(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        for (int i = 0; i < 6; i++) {
            ItemStack stack = gears[i].detach(player);
            if (!world.isRemote) {
                world.spawnEntity(new EntityItem(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack));
            }
        }
        isBroken = true;
        capability.setPower(0, this.from);
        updateNeighbors();
    }

    @Override
    public void update() {
        if (world.isRemote) {
            handleSound();
        }
        if(shouldUpdate) {
            updateNeighbors();
            shouldUpdate = false;
        }
        for(Direction facing : Direction.VALUES) {
            int i = facing.getIndex();
            if(world.isRemote) {
                gears[i].visualUpdate(getGearInPower(facing), capability.getVisualPower(facing));
            }
            gears[i].tick(getGearInPower(facing), getGearOutPower(facing));
            if(gears[i].isDirty())
                shouldUpdate = true;
        }
        lubricant.tick();
    }

    private double getGearInPower(Direction facing) {
        if(capability.isInput(facing))
            return getExternalPower(facing);
        else
            return getInternalPower(facing);
    }

    private double getGearOutPower(Direction facing) {
        if(capability.isOutput(facing))
            return capability.getPower(facing);
        else
            return getInternalPower(facing);
    }

    protected double getExternalPower(Direction facing) {
        return ((GearboxMechCapability)capability).getExternalPower(facing);
    }

    protected double getInternalPower(Direction facing) {
        return ((GearboxMechCapability)capability).getInternalPower(facing);
    }

    public void rotateTile(World world, BlockPos pos, Direction side) {
        BlockState state = world.getBlockState(pos);
        Direction currentFacing = state.getValue(BlockGearbox.facing);

        capability.setPower(0,null);
        GearHelperTile[] temp = new GearHelperTile[gears.length];
        for (int i = 0; i < gears.length; i++) {
            temp[i] = gears[i];
        }
        for (int i = 0; i < gears.length; i++) {
            Direction facing = Direction.getFront(i).rotateAround(side.getAxis());
            temp[i].setFacing(facing);
            gears[facing.getIndex()] = temp[i];
        }
        from = from.rotateAround(side.getAxis());
        world.setBlockState(pos,state.withProperty(BlockGearbox.facing,currentFacing.rotateAround(side.getAxis())));
        capability.onPowerChange();
        //markDirty();
    }

    private class GearboxMechCapability extends DefaultMechCapability {
        double powerExternal;

        @Override
        public void onPowerChange() {
            TileEntityGearbox box = TileEntityGearbox.this;
            box.shouldUpdate = true;
            //box.updateNeighbors();
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
            return behavior.transformPower(TileEntityGearbox.this, from, gearHelper.getGear(), gearHelper.getData(), unchangedPower);
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
            return behavior.transformVisualPower(TileEntityGearbox.this, from, gearHelper.getGear(), gearHelper.getData(), unchangedPower);
        }

        protected double getInternalPower(Direction from) {
            //need to work out solution for null checks that aren't the renderer.
            if (isInput(from))//|| from == null
                return capability.power;
            else
                return power / ((double) (Math.max(1, getConnections())));
        }

        protected double getExternalPower(Direction from) {
            if (isInput(from))
                return powerExternal;
            else
                return 0;
        }

        @Override
        public void setPower(double value, Direction from) {
            GearHelper gearHelper = getGearHelper(from);

            if(from == null) {
                this.power = 0;
                onPowerChange();
            }
            if (isInput(from) && (gearHelper == null || gearHelper.isEmpty())) {
                if(capability.power != 0) {
                    this.power = 0;
                    onPowerChange();
                }
            }
            if (gearHelper != null && isInput(from) && !gearHelper.isEmpty()) {
                powerExternal = value;
                IGearBehavior behavior = gearHelper.getBehavior();
                double oldPower = capability.power;
                value = behavior.transformPower(TileEntityGearbox.this,from,gearHelper.getGear(),gearHelper.getData(),value);
                if (oldPower != value) {
                    capability.power = value;
                    onPowerChange();
                }
            }
        }

        @Override
        public boolean isInput(Direction from) {
            return TileEntityGearbox.this.from == from;
        }

        @Override
        public boolean isOutput(Direction from) {
            return TileEntityGearbox.this.from != from;
        }

        @Override
        public void writeToNBT(CompoundNBT tag) {
            super.writeToNBT(tag);
            tag.putDouble("powerExternal", powerExternal);
        }

        @Override
        public void readFromNBT(CompoundNBT tag) {
            super.readFromNBT(tag);
            powerExternal = tag.getDouble("powerExternal");
        }
    }
}
