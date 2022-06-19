package mysticalmechanics.tileentity;

import mysticalmechanics.api.DefaultMechCapability;
import mysticalmechanics.api.IAxle;
import mysticalmechanics.api.IHasRotation;
import mysticalmechanics.api.MysticalMechanicsAPI;
import mysticalmechanics.block.BlockAxle;
import mysticalmechanics.util.Misc;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileEntityAxle extends TileEntity implements ITickable, IAxle, IHasRotation {
	//BlockPos front;
	//BlockPos back;
    public double angle, lastAngle;
    private boolean isBroken;
    //private Direction facing;
	//private Direction inputSide;
    
    public AxleCapability capability = new AxleCapability();

    public void updatePower() {
    	//setConnection();
		Direction frontFacing = getForward();
		Direction backFacing = getBackward();
		BlockPos front = pos.offset(frontFacing);
		BlockPos back = pos.offset(backFacing);
		TileEntity frontTile = world.getTileEntity(front);
		TileEntity backTile = world.getTileEntity(back);
		
		//input updates
		/*if (frontTile != null && frontTile.hasCapability(MysticalMechanicsAPI.MECH_CAPABILITY, backFacing)){
			if(frontTile.getCapability(MysticalMechanicsAPI.MECH_CAPABILITY, backFacing).isOutput(frontFacing)&&capability.isInput(backFacing)){				
				capability.setPower(frontTile.getCapability(MysticalMechanicsAPI.MECH_CAPABILITY, backFacing).getPower(backFacing), frontFacing);
			}
		}			
					
		if (backTile != null && backTile.hasCapability(MysticalMechanicsAPI.MECH_CAPABILITY, frontFacing)){			
			if(backTile.getCapability(MysticalMechanicsAPI.MECH_CAPABILITY, frontFacing).isOutput(backFacing)&&capability.isInput(frontFacing)) {				
				capability.setPower(backTile.getCapability(MysticalMechanicsAPI.MECH_CAPABILITY, frontFacing).getPower(frontFacing), backFacing);						
			}
		}*/
				
		//output updates
		if (frontTile != null && frontTile.hasCapability(MysticalMechanicsAPI.MECH_CAPABILITY, backFacing)){		
			if(frontTile.getCapability(MysticalMechanicsAPI.MECH_CAPABILITY, backFacing).isInput(backFacing)){
				frontTile.getCapability(MysticalMechanicsAPI.MECH_CAPABILITY, backFacing).setPower(capability.forwardPower,backFacing);
			}
		}			
					
		if (backTile != null && backTile.hasCapability(MysticalMechanicsAPI.MECH_CAPABILITY, frontFacing)){			
			if(backTile.getCapability(MysticalMechanicsAPI.MECH_CAPABILITY, frontFacing).isInput(frontFacing)) {
				backTile.getCapability(MysticalMechanicsAPI.MECH_CAPABILITY, frontFacing).setPower(capability.backwardPower,frontFacing);
			}
		}   	
       
    } 
    
    //required due to IAxle not currently used.
    public BlockPos getConnection(AxisDirection dir){
    	BlockPos connectPos;
    	if(dir == AxisDirection.POSITIVE)
			connectPos = pos.offset(getForward());
    	else
			connectPos = pos.offset(getBackward());
    	TileEntity connectTile = world.getTileEntity(connectPos);
    	if(connectTile instanceof IAxle)
    		return ((IAxle) connectTile).getConnection(dir);
    	else
    		return connectPos;
    }

    //also required by IAxle and not currently used.
    /*public TileEntityAxle getConnectionTile(AxisDirection dir) {
        TileEntity tile = world.getTileEntity(getConnection(dir));
        if(tile instanceof TileEntityAxle)
            return (TileEntityAxle) tile;
        return null;
    }
    
    public TileEntityAxle getConnectionTile(BlockPos pos, Direction facing) {		
		TileEntity tile = world.getTileEntity(pos);
		if (tile instanceof TileEntityAxle)
			if(tile.getCapability(MysticalMechanicsAPI.MECH_CAPABILITY, facing).isOutput(facing.getOpposite()))
			return (TileEntityAxle) tile;
		return null;
	}*/

    public void updateNeighbors(){
    	updatePower();        
    }

    public TileEntityAxle(){
        super();
    }
    
    @Override
	public CompoundNBT writeToNBT(CompoundNBT tag) {
		super.writeToNBT(tag);
		//switched facing and inputSide to a string value for until i can figure out the issue with reading int value.
		capability.writeToNBT(tag);
		/*if (facing != null) {
            tag.setString("facing", facing.getName());
        }		
		if (inputSide != null) {
            tag.setString("inputSide", inputSide.getName());
        }*/
		/*if(front != null) {
			int[] pos = {
				front.getX(), front.getY(),front.getZ()				
			};			
        	tag.setIntArray("front", pos);       	
        }
		if(back != null) {
			int[] pos = {
				back.getX(), back.getY(),back.getZ()				
			};			
        	tag.setIntArray("back", pos);       	
        }*/
		return tag;
	}  
    
    @Override
	public void readFromNBT(CompoundNBT tag) {
		super.readFromNBT(tag);
		//facing and inputSide aren't being read correctly by Direction.getFront I need to figure out why.
		capability.readFromNBT(tag);
		/*if (tag.hasKey("facing")) {
            this.facing = Direction.byName(tag.getString("from"));
        }		
		if(tag.hasKey("inputSide")) {
			this.inputSide = Direction.byName(tag.getString("inputSide"));
		}*/
		/*if(tag.hasKey("front")){
			int[] pos = tag.getIntArray("front");
			this.front = new BlockPos(pos[0],pos[1],pos[2]);
		}
		if(tag.hasKey("back")){
			int[] pos = tag.getIntArray("back");
			this.back = new BlockPos(pos[0],pos[1],pos[2]);
		}*/
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
			BlockState state = world.getBlockState(getPos());						
			if (state.getBlock() instanceof BlockAxle) {				
				if (facing != null && isValidSide(facing)) {
					return true;
				}
			}
		}
		return super.hasCapability(capability, facing);
	}   

    @Override
    public <T> T getCapability(Capability<T> capability, Direction facing){
        if (capability == MysticalMechanicsAPI.MECH_CAPABILITY){
            return MysticalMechanicsAPI.MECH_CAPABILITY.cast(this.capability);
        }
        return super.getCapability(capability, facing);
    }

    public boolean activate(World world, BlockPos pos, BlockState state, PlayerEntity player, EnumHand hand,
                            Direction side, float hitX, float hitY, float hitZ) {
        return false;
    }

    public void breakBlock(World world, BlockPos pos, BlockState state, PlayerEntity player) {
    	isBroken = true;
        capability.setPower(0f,null);
    }

    public Direction getForward() {
    	Direction.Axis axis = getAxis();
    	return Direction.getFacingFromAxis(AxisDirection.POSITIVE,axis);
	}

	public Direction getBackward() {
		Direction.Axis axis = getAxis();
		return Direction.getFacingFromAxis(AxisDirection.NEGATIVE,axis);
	}

    public boolean isForward(Direction facing) {
    	if(facing == null)
    		return false;
		return facing.getAxis() == getAxis() && facing.getAxisDirection() == AxisDirection.POSITIVE;
	}

	public boolean isBackward(Direction facing) {
		if(facing == null)
			return false;
		return facing.getAxis() == getAxis() && facing.getAxisDirection() == AxisDirection.NEGATIVE;
	}

	public Direction.Axis getAxis() {
		BlockState state = world.getBlockState(pos);
		return state.getValue(BlockAxle.axis);
	}
    
    public boolean isValidSide(Direction facing){
    	/*if(this.facing == null) {
    		setConnection();
    	}
		return facing == this.facing || facing == this.facing.getOpposite();*/
    	if(facing == null)
    		return false;
    	return getAxis() == facing.getAxis();
	}
    
    /*public boolean isValidSide(BlockPos from) {
		return from.equals(front) ||from.equals(back);
	}*/
    
    /*public void setConnection() {
		if(facing == null || front == null || back == null) {
			BlockState state = world.getBlockState(getPos());    	
			facing = state.getValue(BlockAxle.facing).getOpposite();
			front = getPos().offset(facing);
			back = getPos().offset(facing.getOpposite());					
			markDirty();
		}
	}*/
    
    /*private Direction comparePosToSides(BlockPos from) {
		if(from.equals(front)) {
			return world.getBlockState(this.pos).getValue(BlockAxle.facing).getOpposite();
		}else if(from.equals(back)) {				
			return world.getBlockState(this.pos).getValue(BlockAxle.facing);
		}
		return null;			
    }
    
    private void checkAndSetInput(Direction from) {
		if(from != null) {
			TileEntity t = world.getTileEntity(getPos().offset(from));
			if(t!=null && t.hasCapability(MysticalMechanicsAPI.MECH_CAPABILITY, from)) {				
				//if the tile entity is a output and we dont have power set the inputside. 
				if(t.getCapability(MysticalMechanicsAPI.MECH_CAPABILITY, from).isOutput(from.getOpposite()) && capability.power == 0) {					
					this.inputSide = from;					
				}
			}
		}
		
	}*/

	@Override
	public boolean shouldRefresh(World world, BlockPos pos, BlockState oldState, BlockState newState) {
		return oldState.getBlock() != newState.getBlock();
	}
    
    public void neighborChanged(BlockPos from) {		
		//if(isValidSide(from)) {
			//checkAndSetInput(comparePosToSides(from));
			updateNeighbors();
		//}
	}

    @Override
    public void update() {
        if(world.isRemote) {
			lastAngle = angle;
			angle += capability.getPower(null);
        }
    }
    
    @Override
    public void markDirty() {
        super.markDirty();       
        Misc.syncTE(this, isBroken);
    }
    
	public boolean isBroken() {		
		return this.isBroken;
	}

	public void rotateTile(World world, BlockPos pos, Direction side) {
		BlockState state = world.getBlockState(pos);
		Direction.Axis currentAxis = getAxis();
		currentAxis = Direction.getFacingFromAxis(Direction.AxisDirection.POSITIVE,currentAxis).rotateAround(side.getAxis()).getAxis();

		capability.setPower(0,null);
		world.setBlockState(pos,state.withProperty(BlockAxle.axis,currentAxis));
		updateNeighbors();
	}

	@Override
	public boolean hasRotation(@Nonnull Direction side) {
		return isValidSide(side);
	}

	@Override
	public double getAngle(@Nonnull Direction side) {
		return angle;
	}

	@Override
	public double getLastAngle(@Nonnull Direction side) {
		return lastAngle;
	}

	@Override
	public void setRotation(@Nonnull Direction side, double angle, double lastAngle) {
		this.angle = angle;
		this.lastAngle = lastAngle;
	}

	private class AxleCapability extends DefaultMechCapability {
		double forwardPower;
		double backwardPower;

		@Override
		public void onPowerChange(){
			updateNeighbors();
			markDirty();
		}

		@Override
		public double getPower(Direction from) {
			if(from == null || isValidSide(from)) {
				//this should only really be called on block break.
				return getActualPower();
			}/*else if (isForward(from)){
				return forwardPower;
			}else if (isBackward(from)){
				return backwardPower;
			}*/
			return 0;
		}

		private double getActualPower() {
			return Math.max(forwardPower,backwardPower);
		}

		@Override
        public void setPower(double value, Direction from) {
        	if(from == null /*&& isBroken()*/) {
        		forwardPower = 0;
        		backwardPower = 0;
    			onPowerChange();
    		}else if(isForward(from)){
    			double oldPower = backwardPower;
    			if (oldPower != value){
					backwardPower = value;
    				onPowerChange();
    			}
    		}else if(isBackward(from)){
				double oldPower = forwardPower;
				if (oldPower != value){
					forwardPower = value;
					onPowerChange();
				}
			}
        }

		@Override
		public boolean isInput(Direction from) {
			/*checkAndSetInput(from);
			if(inputSide != null && from != null) {
				return inputSide == from;
			}*/
			return isValidSide(from);
		}

		@Override
		public boolean isOutput(Direction from) {
			/*if(from != null && inputSide != null) {
				return inputSide.getOpposite() == from;
			}*/
			return isValidSide(from);
		}

		@Override
		public void writeToNBT(CompoundNBT tag) {
			tag.putDouble("forwardPower",forwardPower);
			tag.putDouble("backwardPower",backwardPower);
		}

		@Override
		public void readFromNBT(CompoundNBT tag) {
			forwardPower = tag.getDouble("forwardPower");
			backwardPower = tag.getDouble("backwardPower");
		}
	}
}
