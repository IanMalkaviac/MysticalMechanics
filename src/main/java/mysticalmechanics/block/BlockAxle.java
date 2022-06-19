package mysticalmechanics.block;


import mysticalmechanics.tileentity.TileEntityAxle;
import net.minecraft.block.Block;
import net.minecraft.block.DirectionalBlock;
import net.minecraft.block.material.Material;
//import net.minecraft.block.properties.PropertyEnum;
//import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.extensions.IForgeBlockState;
//import net.minecraft.entity.LivingEntity;
//import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
//import net.minecraft.world.IBlockAccess;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Random;

public class BlockAxle extends DirectionalBlock {
	
	//public static final PropertyDirection facing = PropertyDirection.create("facing");
    //public static final PropertyEnum<Direction.Axis> axis = PropertyEnum.<Direction.Axis>create("axis", Direction.Axis.class);
	
    public BlockAxle(Material material) {
        super(Block.Properties.create(material));
    }

    //@Override
    //public BlockState createBlockState() {
    //	return new BlockState(this, FACING);
    //}

    //@Override
    //public int getMetaFromState(BlockState state) {
    //	return state.get(FACING).ordinal();
    //}

    //@Override
    //public BlockState getStateFromMeta(int meta) {
    //    Direction.Axis[] axisSet = Direction.Axis.values();
    //    return getDefaultState().with(FACING, Direction.byIndex((meta % axisSet.length)));
    //}

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
    	return this.getDefaultState().with(FACING, context.getNearestLookingDirection().getOpposite());
    }   

    @Override
    public boolean isSideSolid(BlockState state, IBlockAccess world, BlockPos pos, Direction side) {    	
        return side.getAxis() != state.getValue(axis);
    }

    @Override
    public boolean isOpaqueCube(BlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(BlockState state) {
        return false;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, BlockState state) {
        return new TileEntityAxle();
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
        TileEntityAxle tile = (TileEntityAxle) world.getTileEntity(pos);
        tile.neighborChanged(fromPos);
    }

    @Override
    public void updateTick(World world, BlockPos pos, BlockState state, Random rand) {        
    }

    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, BlockState state) {
        worldIn.scheduleUpdate(pos,this,0);       
    }
    
    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
    	//TileEntityAxle tile = (TileEntityAxle) world.getTileEntity(pos);
    	//tile.setConnection();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, BlockState state, PlayerEntity playerIn, EnumHand hand, Direction facing, float hitX, float hitY, float hitZ) {
        TileEntityAxle tile = (TileEntityAxle)world.getTileEntity(pos);
        return tile.activate(world, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
    }

    @Override
    public void onBlockHarvested(World world, BlockPos pos, BlockState state, PlayerEntity player){
        TileEntityAxle tile = (TileEntityAxle)world.getTileEntity(pos);
        tile.breakBlock(world,pos,state,player);
    }

    @Override
    public EnumBlockRenderType getRenderType(BlockState state) {
        return EnumBlockRenderType.INVISIBLE;
    }

    @Override
    public AxisAlignedBB getBoundingBox(BlockState state, IBlockAccess source, BlockPos pos) {
        switch (state.getValue(axis)) {
            case Y:
                return new AxisAlignedBB(0.375, 0, 0.375, 0.625, 1.0, 0.625);
            case Z:
                return new AxisAlignedBB(0.375, 0.375, 0, 0.625, 0.625, 1.0);
            case X:
                return new AxisAlignedBB(0, 0.375, 0.375, 1.0, 0.625, 0.625);
        }
        return new AxisAlignedBB(0.375, 0, 0.375, 0.625, 1.0, 0.625);
    }

    @Override
    public boolean rotateBlock(World world, BlockPos pos, Direction side) {
        BlockState state = world.getBlockState(pos);
        Direction.Axis currentAxis = state.getValue(axis);
        TileEntityAxle tile = (TileEntityAxle) world.getTileEntity(pos);

        if(side.getAxis() == currentAxis) {
            return false;
        }

        tile.rotateTile(world, pos, side);

        return true;
    }

    @Override
    public BlockState withRotation(BlockState state, Rotation rot) {
        Direction.Axis currentAxis = state.getValue(axis);
        currentAxis = rot.rotate(Direction.getFacingFromAxis(Direction.AxisDirection.POSITIVE,currentAxis)).getAxis();
        return state.withProperty(axis,currentAxis);
    }

    @Override
    public BlockState withMirror(BlockState state, Mirror mirrorIn) {
        Direction.Axis currentAxis = state.getValue(axis);
        currentAxis = mirrorIn.mirror(Direction.getFacingFromAxis(Direction.AxisDirection.POSITIVE,currentAxis)).getAxis();
        return state.withProperty(axis,currentAxis);
    }

    /*@Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState blockState, IProbeHitData data) {
        TileEntity tile = world.getTileEntity(data.getPos());
        if(tile instanceof TileEntityAxle)
            ((TileEntityAxle) tile).addProbeInfo(mode,probeInfo,player,world,blockState,data);
    }*/
}
