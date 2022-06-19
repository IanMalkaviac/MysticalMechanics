package mysticalmechanics;

import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;

public class ServerProxy implements IProxy {
    @Override
    public void preInit() {
        //NOOP
    }

    @Override
    public void playMachineSound(TileEntity tile, int id, int playId, SoundEvent soundIn, SoundCategory categoryIn, boolean repeat, float volume, float pitch, float xIn, float yIn, float zIn) {
        //NOOP
    }

    @Override
    public boolean isGearHit(TileEntity tile, Direction facing) {
        return false;
    }

    @Override
    public void renderGear(ItemStack gear, ItemStack gearHologram, boolean renderHologram, float partialTicks, double offset, double scale, float angle) {
        //NOOP
    }

    @Override
    public void renderAxle(ModelResourceLocation resLoc, Direction.Axis axis, float angle) {
        //NOOP
    }
}
