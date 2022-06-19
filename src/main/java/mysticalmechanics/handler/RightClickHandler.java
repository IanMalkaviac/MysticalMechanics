package mysticalmechanics.handler;

import mysticalmechanics.MysticalMechanics;
import mysticalmechanics.api.IGearbox;
import mysticalmechanics.api.MysticalMechanicsAPI;
import mysticalmechanics.api.lubricant.ILubricantCapability;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class RightClickHandler {
    @SubscribeEvent
    public void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        PlayerEntity player = event.getPlayerEntity();
        World world = event.getWorld();
        BlockPos pos = event.getPos();
        Direction side = event.getFace();
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof IGearbox) {
            IGearbox gearbox = (IGearbox) tile;
            ItemStack stack = event.getItemStack();
            if (gearbox.canAttachGear(side) && MysticalMechanicsAPI.IMPL.isValidGear(stack)) {
                event.setUseBlock(Event.Result.ALLOW);
            }
        }
    }

    /*@SubscribeEvent
    public void onApplyLube(PlayerInteractEvent.RightClickBlock event) {
        World world = event.getWorld();
        BlockPos pos = event.getPos();
        Direction side = event.getFace();
        TileEntity tile = world.getTileEntity(pos);
        if(tile != null && event.getItemStack().getItem().equals(Items.SLIME_BALL)) {
            ILubricantCapability capability = tile.getCapability(MysticalMechanicsAPI.LUBRICANT_CAPABILITY, side);
            if(capability != null) {
                capability.lubricate(MysticalMechanics.TEST_LUBRICANT, 500, false);
                event.setCancellationResult(EnumActionResult.SUCCESS);
            }
        }
    }*/
}
