package betterwithmods.api.util;

import net.minecraft.block.BlockState;

public interface IWoodProvider {

    boolean match(BlockState state);

    IWood getWood(BlockState state);

}
