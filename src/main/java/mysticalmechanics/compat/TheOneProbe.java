package mysticalmechanics.compat;

import mcjty.theoneprobe.api.*;
import mysticalmechanics.MysticalMechanics;
import mysticalmechanics.api.IAxle;
import mysticalmechanics.api.IGearbox;
import mysticalmechanics.api.IMechCapability;
import mysticalmechanics.api.MysticalMechanicsAPI;
import mysticalmechanics.api.lubricant.ILubricant;
import mysticalmechanics.api.lubricant.ILubricantCapability;
import mysticalmechanics.api.lubricant.LubricantStack;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.event.FMLInterModComms;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TheOneProbe implements Function<ITheOneProbe, Void>, IProbeInfoProvider {
    public static ITheOneProbe probe;

    public static int ELEMENT_POWERUNIT;

    public static void init() {
        FMLInterModComms.sendFunctionMessage("theoneprobe", "getTheOneProbe", "mysticalmechanics.compat.TheOneProbe");
    }

    @Override
    public Void apply(ITheOneProbe theOneProbe) {
        probe = theOneProbe;
        ELEMENT_POWERUNIT = probe.registerElementFactory(new PowerUnit.Factory());
        probe.registerProvider(this);
        return null;
    }

    @Override
    public String getID() {
        return MysticalMechanics.MODID;
    }

    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState blockState, IProbeHitData data) {
        TileEntity tile = world.getTileEntity(data.getPos());
        IGearbox gearbox = null;

        if (tile instanceof IGearbox)
            gearbox = (IGearbox) tile;
        if (tile instanceof IAxle) {
            IAxle axle = (IAxle) tile;
            probeInfo.text(TextStyleClass.LABEL + IProbeInfo.STARTLOC + "mysticalmechanics.probe.axle_length" + IProbeInfo.ENDLOC + " " + TextStyleClass.INFO + axle.getLength());
        }

        Direction currentFacing = data.getSideHit();

        if (tile != null) {
            List<MechInfoStruct> info = new ArrayList<>();
            List<LubricantInfoStruct> infoLubricant = new ArrayList<>();
            boolean canLubricate = false;
            if(mode == ProbeMode.EXTENDED)
            for (Direction facing : Direction.VALUES) {
                boolean forceWrite = false;
                ItemStack gear = ItemStack.EMPTY;
                if (gearbox != null && gearbox.canAttachGear(facing)) {
                    gear = gearbox.getGear(facing);
                    if(gear.isEmpty())
                        continue;
                    forceWrite = true;
                }
                if (tile.hasCapability(MysticalMechanicsAPI.MECH_CAPABILITY, facing)) {
                    IMechCapability capability = tile.getCapability(MysticalMechanicsAPI.MECH_CAPABILITY, facing);
                    boolean input = capability.isInput(facing);
                    boolean output = capability.isOutput(facing);
                    double power = capability.getVisualPower(facing);
                    MechInfoStruct struct;
                    if (input && output)
                        struct = new MechInfoStruct(MechInfoType.Both, power, facing);
                    else if (input)
                        struct = new MechInfoStruct(MechInfoType.Input, power, facing);
                    else if (output)
                        struct = new MechInfoStruct(MechInfoType.Output, power, facing);
                    else
                        continue;
                    if(forceWrite)
                        addMechPowerData(mode,probeInfo,struct,gear,currentFacing);
                    else
                        info.add(struct);
                }
                if (tile.hasCapability(MysticalMechanicsAPI.LUBRICANT_CAPABILITY, facing)) {
                    canLubricate = true;
                    ILubricantCapability capability = tile.getCapability(MysticalMechanicsAPI.LUBRICANT_CAPABILITY, facing);
                    for (LubricantStack stack : capability.getAppliedLubricant()) {
                        infoLubricant.add(new LubricantInfoStruct(stack, capability.getCapacity()));
                    }
                }
            }

            Map<MechInfoType, List<MechInfoStruct>> grouped = info.stream().collect(Collectors.groupingBy(mechInfoStruct -> mechInfoStruct.type));
            if(grouped.entrySet().stream().allMatch(entry -> entry.getValue().stream().distinct().count() <= 1)) {
                for (List<MechInfoStruct> struct : grouped.values()) {
                    if(!struct.isEmpty())
                        addMechPowerData(mode,probeInfo,struct.get(0),ItemStack.EMPTY,currentFacing);
                }
            } else {
                for (MechInfoStruct struct : info) {
                    addMechPowerData(mode,probeInfo,struct,ItemStack.EMPTY,currentFacing);
                }
            }

            if(canLubricate)
                probeInfo.text(IProbeInfo.STARTLOC+"mysticalmechanics.probe.lubricant"+IProbeInfo.ENDLOC);
            Map<LubricantStack, List<LubricantInfoStruct>> groupedLubricants = infoLubricant.stream().collect(Collectors.groupingBy(LubricantInfoStruct::getStack));
            for (Map.Entry<LubricantStack, List<LubricantInfoStruct>> entry : groupedLubricants.entrySet()) {
                LubricantStack stack = entry.getKey();
                ILubricant lubricant = stack.getLubricant();
                int capacity = entry.getValue().stream().mapToInt(LubricantInfoStruct::getCapacity).max().getAsInt();
                IProbeInfo part = probeInfo.horizontal(probeInfo.defaultLayoutStyle().spacing(3).alignment(ElementAlignment.ALIGN_BOTTOMRIGHT));

                part.progress(stack.getAmount(), capacity, probeInfo.defaultProgressStyle().width(32).filledColor(lubricant.getColor().getRGB()).alternateFilledColor(lubricant.getColor().getRGB()));
                part.text(IProbeInfo.STARTLOC+stack.getUnlocalizedName()+IProbeInfo.ENDLOC);
            }
        }
    }

    private void addMechPowerData(ProbeMode mode, IProbeInfo probeInfo, MechInfoStruct struct, ItemStack gear, Direction facing) {
        boolean input = struct.type == MechInfoType.Input || struct.type == MechInfoType.Both;
        boolean output = struct.type == MechInfoType.Output || struct.type == MechInfoType.Both;
        probeInfo.element(new PowerUnit(struct.power,input,output,gear,struct.facing));
    }

    enum MechInfoType {
        Input,
        Output,
        Both,
    }

    class MechInfoStruct {
        MechInfoType type;
        double power;
        Direction facing;

        public MechInfoStruct(MechInfoType type, double power, Direction facing) {
            this.type = type;
            this.power = power;
            this.facing = facing;
        }

        public MechInfoType getType() {
            return type;
        }

        public double getPower() {
            return power;
        }

        public Direction getFacing() {
            return facing;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof MechInfoStruct)
                return equals((MechInfoStruct) obj);
            return super.equals(obj);
        }

        private boolean equals(MechInfoStruct struct) {
            return type.equals(struct.type) && power == struct.power;
        }

        @Override
        public int hashCode() {
            return type.hashCode() ^ Double.hashCode(power);
        }
    }

    class LubricantInfoStruct {
        LubricantStack stack;
        int capacity;

        public LubricantInfoStruct(LubricantStack stack, int capacity) {
            this.stack = stack;
            this.capacity = capacity;
        }

        public LubricantStack getStack() {
            return stack;
        }

        public int getCapacity() {
            return capacity;
        }
    }
}
