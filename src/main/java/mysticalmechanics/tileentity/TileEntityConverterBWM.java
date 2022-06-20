package mysticalmechanics.tileentity;

import betterwithmods.api.BWMAPI;
import betterwithmods.api.capabilities.CapabilityMechanicalPower;
import mysticalmechanics.api.*;
import mysticalmechanics.block.BlockConverterBWM;
import mysticalmechanics.util.Misc;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

public class TileEntityConverterBWM extends TileEntity implements ITickableTileEntity, IGearbox {
    protected boolean isBroken;

    ConverterMystMechCapability capabilityMystMech;
    ConverterBWMCapability capabilityBWM;

    GearHelperTile gear;
    public boolean shouldUpdate;

    public TileEntityConverterBWM() {
        capabilityBWM = new ConverterBWMCapability();
        capabilityMystMech = new ConverterMystMechCapability();
        gear = new GearHelperTile(this, null){
            @Override
            public Direction getFacing() {
                return getSideMystMech();
            }
        };
    }

    public boolean canConvertToBWM() {
        BlockState state = world.getBlockState(pos);
        if(state.getBlock() instanceof BlockConverterBWM)
            return !state.getValue(BlockConverterBWM.on);
        else
            return false;
    }

    public boolean canConvertToMM() {
        BlockState state = world.getBlockState(pos);
        if(state.getBlock() instanceof BlockConverterBWM)
            return state.getValue(BlockConverterBWM.on);
        else
            return false;
    }

    private int convertToBWM() {
        if(canConvertToBWM() && capabilityMystMech.power >= 1)
            return (int) Math.ceil(Math.log(capabilityMystMech.power*0.1+1.0) / Math.log(2));
        else
            return 0;
    }

    private double convertToMystMech() {
        if(canConvertToMM())
            return (Math.pow(2,capabilityBWM.power)-1);
        else
            return 0;
    }

    private double getGearInPower(Direction facing) {
        if(capabilityMystMech.isInput(facing))
            return capabilityMystMech.getExternalPower(facing);
        else
            return capabilityMystMech.getInternalPower(facing);
    }

    private double getGearOutPower(Direction facing) {
        if(capabilityMystMech.isOutput(facing))
            return capabilityMystMech.getExternalPower(facing);
        else
            return capabilityMystMech.getInternalPower(facing);
    }

    public Direction getSideBWM() {
        return getFacing().getOpposite();
    }

    public Direction getFacing() {
        BlockState state = world.getBlockState(pos);
        return state.getValue(BlockConverterBWM.facing);
    }

    public Direction getSideMystMech() {
        return getFacing();
    }

    @Override
    public void tick() {
        if(shouldUpdate) {
            updateNeighbors();
            shouldUpdate = false;
        }
        if(!world.isRemote) {
            int powerBWM = capabilityBWM.calculateInput();
            if (capabilityBWM.power != powerBWM) {
                capabilityBWM.power = powerBWM;
                capabilityMystMech.onPowerChange();
            }
        }
        double powerIn = getGearInPower(gear.getFacing());
        double powerOut = getGearOutPower(gear.getFacing());
        gear.tick(powerIn, powerOut);
        if(gear.isDirty())
            shouldUpdate = true;
        if(world.isRemote)
            gear.visualUpdate(powerIn, capabilityMystMech.getVisualPower(getSideMystMech()));
    }

    @Override
    public CompoundNBT writeToNBT(CompoundNBT tag) {
        super.writeToNBT(tag);
        capabilityMystMech.writeToNBT(tag);
        tag.setInteger("bwmPower",capabilityBWM.power);
        tag.setTag("side", gear.writeToNBT(new CompoundNBT()));
        return tag;
    }

    @Override
    public void readFromNBT(CompoundNBT tag) {
        super.readFromNBT(tag);
        capabilityMystMech.readFromNBT(tag);
        capabilityBWM.power = tag.getInteger("bwmPower");
        gear.readFromNBT(tag.getCompoundTag("side"));
        if(tag.hasKey("gear"))
            gear.setGear(new ItemStack(tag.getCompoundTag("gear")));
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return writeToNBT(new CompoundNBT());
    }

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(getPos(), 0, getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        readFromNBT(pkt.getNbtCompound());
    }

    @Override
    public boolean hasCapability(Capability<?> capability, Direction facing) {
        if (capability == MysticalMechanicsAPI.MECH_CAPABILITY && (facing == null || facing == getSideMystMech())) {
            return true;
        }
        if(capability == CapabilityMechanicalPower.MECHANICAL_POWER && facing == getSideBWM()) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, Direction facing) {
        if (capability == MysticalMechanicsAPI.MECH_CAPABILITY && (facing == null || facing == getSideMystMech())) {
            return MysticalMechanicsAPI.MECH_CAPABILITY.cast(capabilityMystMech);
        }
        if(capability == CapabilityMechanicalPower.MECHANICAL_POWER && facing == getSideBWM()) {
            return CapabilityMechanicalPower.MECHANICAL_POWER.cast(capabilityBWM);
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, BlockState oldState, BlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }

    public void updateNeighbors() {
        Direction from = getSideMystMech();

        if(capabilityMystMech.isInput(from))
            MysticalMechanicsAPI.IMPL.pullPower(this, from, capabilityMystMech, !getGear(from).isEmpty());
        if(capabilityMystMech.isOutput(from))
            MysticalMechanicsAPI.IMPL.pushPower(this, from, capabilityMystMech, !getGear(from).isEmpty());

        markDirty();
    }

    public boolean activate(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand,
                            Direction side, float hitX, float hitY, float hitZ) {
        ItemStack heldItem = player.getHeldItem(hand);
        Direction attachSide = side;
        if(hand == Hand.OFF_HAND)
            return false;
        if(side == getSideBWM()) {
            capabilityBWM.power = 0;
            capabilityMystMech.power = 0;
            capabilityMystMech.onPowerChange();
            boolean newOn = !state.getValue(BlockConverterBWM.on);
            if(newOn)
                player.sendStatusMessage(new TextComponentTranslation("mysticalmechanics.tooltip.bwm_converter.on"),true);
            else
                player.sendStatusMessage(new TextComponentTranslation("mysticalmechanics.tooltip.bwm_converter.off"),true);
            world.setBlockState(pos,state.withProperty(BlockConverterBWM.on,newOn));
            return true;
        }
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
                world.spawnEntity(new ItemEntity(world, player.posX, player.posY + player.height / 2.0f, player.posZ, gear));
            }
            return true;
        }
        return false;
    }

    public void rotateTile(World world, BlockPos pos, Direction side) {
        BlockState state = world.getBlockState(pos);
        Direction currentFacing = state.getValue(BlockConverterBWM.facing);

        capabilityMystMech.setPower(0,null);
        world.setBlockState(pos,state.withProperty(BlockConverterBWM.facing,currentFacing.rotateAround(side.getAxis())));
        capabilityMystMech.onPowerChange();
    }

    public void breakBlock(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        ItemStack stack = gear.detach(player);
        if (!world.isRemote) {
            world.spawnEntity(new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack));
        }
        isBroken = true;
        capabilityMystMech.setPower(0, null);
        updateNeighbors();
    }

    @Override
    public void markDirty() {
        super.markDirty();
        Misc.syncTE(this, isBroken);
    }

    @Override
    public void attachGear(Direction facing, ItemStack stack, PlayerEntity player) {
        if (!canAttachGear(facing, stack))
            return;
        gear.attach(null, stack);
        capabilityMystMech.onPowerChange();
        world.neighborChanged(pos.offset(getSideMystMech()),blockType,pos);
    }

    @Override
    public ItemStack detachGear(Direction facing, PlayerEntity player) {
        if (!canAttachGear(facing))
            return ItemStack.EMPTY;
        ItemStack removed = gear.detach(null);
        capabilityMystMech.onPowerChange();
        world.neighborChanged(pos.offset(getSideMystMech()),blockType,pos);
        return removed;
    }

    @Override
    public ItemStack getGear(Direction facing) {
        return gear.getGear();
    }

    @Override
    public boolean canAttachGear(Direction facing, ItemStack stack) {
        return canAttachGear(facing) && gear.canAttach(stack);
    }

    @Override
    public boolean canAttachGear(Direction facing) {
        return facing == getSideMystMech();
    }

    @Override
    public int getConnections() {
        return 1;
    }

    private class ConverterBWMCapability extends CapabilityMechanicalPower.Default {
        int power;

        @Override
        public int getMechanicalInput(Direction facing) {
            if(facing == getSideBWM())
                return BWMAPI.IMPLEMENTATION.getPowerOutput(world, pos.offset(facing), facing.getOpposite());
            return 0;
        }

        @Override
        public int getMechanicalOutput(Direction facing) {
            if(facing == getSideBWM())
                return convertToBWM();
            return -1;
        }

        @Override
        public int getMaximumInput(Direction facing) {
            return Integer.MAX_VALUE;
        }

        @Override
        public int getMinimumInput(Direction facing) {
            return 0;
        }

        @Override
        public Block getBlock() {
            return getBlockType();
        }

        @Override
        public BlockPos getBlockPos() {
            return getPos();
        }

        @Override
        public World getBlockWorld() {
            return getWorld();
        }
    }

    private class ConverterMystMechCapability extends DefaultMechCapability {
        double powerExternal;

        @Override
        public double getPower(Direction from) {
            if (from == null)
                return super.getPower(from);

            GearHelper gearHelper = gear;

            if (gearHelper.isEmpty() || !canConvertToMM())
                return 0;

            IGearBehavior behavior = gearHelper.getBehavior();
            return behavior.transformPower(TileEntityConverterBWM.this, from, gearHelper.getGear(), gearHelper.getData(), getInternalPower(from));
        }

        private double getInternalPower(Direction from) {
            if (from == getSideMystMech())
                return canConvertToMM() ? convertToMystMech() : power;
            else
                return 0;
        }

        public double getExternalPower(Direction from) {
            if (from == getSideMystMech())
                return powerExternal;
            else
                return 0;
        }

        @Override
        public void setPower(double value, Direction from) {
            if (from == null)
                super.setPower(value, from);

            if (from == getSideMystMech()) {
                powerExternal = value;
                GearHelper gearHelper = gear;
                double transformedPower;
                if (gearHelper.isEmpty())
                    transformedPower = 0;
                else {
                    IGearBehavior behavior = gearHelper.getBehavior();
                    transformedPower = behavior.transformPower(TileEntityConverterBWM.this, from, gearHelper.getGear(), gearHelper.getData(), value);
                }

                if(transformedPower != power) {
                    super.setPower(transformedPower, from);
                    world.neighborChanged(pos.offset(getSideBWM()),blockType,pos);
                    //world.notifyNeighborsOfStateChange(pos, blockType, false);
                }
            }
        }

        @Override
        public double getVisualPower(Direction from) {
            if (from == null)
                return super.getPower(from);
            if (from != getSideMystMech())
                return 0;

            GearHelper gearHelper = gear;
            if (gearHelper.isEmpty())
                return 0;

            IGearBehavior behavior = gearHelper.getBehavior();
            return behavior.transformVisualPower(TileEntityConverterBWM.this, from, gearHelper.getGear(), gearHelper.getData(), getInternalPower(from));
        }

        @Override
        public void onPowerChange() {
            shouldUpdate = true;
            markDirty();
        }

        @Override
        public boolean isInput(Direction from) {
            return from == getSideMystMech() && canConvertToBWM();
        }

        @Override
        public boolean isOutput(Direction from) {
            return from == getSideMystMech() && canConvertToMM();
        }

        @Override
        public void readFromNBT(CompoundNBT tag) {
            super.readFromNBT(tag);
            powerExternal = tag.getDouble("powerExternal");
        }

        @Override
        public void writeToNBT(CompoundNBT tag) {
            super.writeToNBT(tag);
            tag.putDouble("powerExternal", powerExternal);
        }
    }
}
