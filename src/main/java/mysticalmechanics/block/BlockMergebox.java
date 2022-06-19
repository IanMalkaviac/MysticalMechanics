package mysticalmechanics.block;

import mysticalmechanics.tileentity.TileEntityMergebox;
import net.minecraft.block.material.Material;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockMergebox extends BlockGearbox {
    public BlockMergebox(Material material) {
        super(material);
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, BlockState state) {
        return new TileEntityMergebox();
    }
}

