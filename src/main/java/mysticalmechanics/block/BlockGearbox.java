package mysticalmechanics.block;

import mysticalmechanics.tileentity.TileEntityGearbox;
import net.minecraft.block.Block;
import net.minecraft.block.DirectionalBlock;
import net.minecraft.block.material.Material;
//import net.minecraft.block.properties.PropertyDirection;
//import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
//import net.minecraft.world.IBlockAccess;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockGearbox extends DirectionalBlock {
    //public static final PropertyDirection facing = PropertyDirection.create("facing");

    public BlockGearbox(Material material) {
        super(Block.Properties.create(material));
        this.setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.NORTH);
    }

    //@Override
    //public BlockStateContainer createBlockState(){
    //     return new BlockStateContainer(this, facing);
    //}

    /*@Override
    public int getMetaFromState(BlockState state){
        return state.getValue(facing).getIndex();
    }

    @Override
    public BlockState getStateFromMeta(int meta){
        return getDefaultState().withProperty(facing, Direction.getFront(meta));
    }*/

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context){
        if(context.getPlayer().isSneaking())
            return this.getDefaultState().with(FACING, context.getNearestLookingDirection());
        else
            return this.getDefaultState().with(FACING, context.getNearestLookingDirection().getOpposite());
    }

    //@Override
    //public boolean isSideSolid(BlockState state, IBlockAccess world, BlockPos pos, Direction side){
    //    return side != state.getValue(facing);
    //}

    //@Override
    //public boolean isOpaqueCube(BlockState state) {
    //    return false;
    //}

    //@Override
    //public boolean isFullCube(BlockState state) {
    //    return false;
    //}


    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new TileEntityGearbox();
    }

    @Override
    public void onNeighborChange(BlockState state, IWorldReader worldIn, BlockPos pos, BlockPos fromPos){
        TileEntityGearbox tile = (TileEntityGearbox)worldIn.getTileEntity(pos);
        tile.shouldUpdate = true;
    }

    @Override
    public boolean onBlockActivated(BlockState state, World world, BlockPos pos,  PlayerEntity playerIn, Hand hand, BlockRayTraceResult hit) {
        hit.getHitVec().x;
        TileEntityGearbox tile = (TileEntityGearbox)world.getTileEntity(pos);
        return tile.activate(world, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
    }

    @Override
    public void onBlockHarvested(World world, BlockPos pos, BlockState state, PlayerEntity player){
        TileEntityGearbox tile = (TileEntityGearbox)world.getTileEntity(pos);
        tile.breakBlock(world,pos,state,player);
    }

    @Override
    public boolean rotateBlock(World world, BlockPos pos, Direction side) {
        TileEntityGearbox tile = (TileEntityGearbox)world.getTileEntity(pos);
        tile.rotateTile(world, pos, side);
        return true;
    }
}

