package betterwithmods.api.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public interface IRenderRotationPlacement {
    BlockState getRenderState(World world, BlockPos pos, Direction facing, float flX, float flY, float flZ, int meta, LivingEntity placer);
    RenderFunction getRenderFunction();
    interface RenderFunction {
        void render(World world, Block block, BlockPos pos, ItemStack stack, PlayerEntity player, Direction side, RayTraceResult target, double partial);
    }
}
