package mysticalmechanics.api;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;

import javax.annotation.Nullable;

public interface IGearbox {
    @Deprecated
    default void attachGear(Direction facing, ItemStack stack) {
        attachGear(facing, stack, null);
    }

    /**
     * @param facing the side to attach the gear to.
     * @param player the player attaching the gear, can be null
     * @param stack the gear to attach.
     */
    default void attachGear(Direction facing, ItemStack stack, @Nullable PlayerEntity player) {
        attachGear(facing, stack);
    }

    @Deprecated
    default ItemStack detachGear(Direction facing) {
        return detachGear(facing, null);
    }

    /**
     * @param facing the side to remove the gear from.
     * @param player the player removing the gear, can be null
     * @return the removed gear. Does not need to be the same gear as previously attached, but could be a damaged version or an empty stack.
     */
    default ItemStack detachGear(Direction facing, @Nullable PlayerEntity player) {
        return detachGear(facing);
    }

    /**
     * @param facing the side to check for.
     * @return the currently attached gear on this face, or ItemStack.EMPTY if none.
     */
    ItemStack getGear(Direction facing);

    /**
     * @param facing the side to check for.
     * @param stack an ItemStack representing a gear.
     * @return whether the specified stack can be attached on this face as a gear.
     */
    boolean canAttachGear(Direction facing, ItemStack stack);

    /**
     * @param facing the side to check for.
     * @return whether the specified face can even have a gear attached.
     */
    default boolean canAttachGear(Direction facing) {
        return true;
    }

    /**
     * Use/Implement this method for the number of connected outputs this gearbox has. Every connected output should only receive power divided by this value to prevent power loops.
     *
     * @return the number of directly connected mechanical blocks on output faces.
     */
    int getConnections();
}
