package mysticalmechanics.tileentity;

import mysticalmechanics.MysticalMechanics;
import mysticalmechanics.api.MysticalMechanicsAPI;
import mysticalmechanics.block.BlockGearbox;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.awt.*;

public class TileEntityConverterBWMRenderer extends TileEntitySpecialRenderer<TileEntityConverterBWM> {
    public TileEntityConverterBWMRenderer() {
        super();
        MinecraftForge.EVENT_BUS.register(getClass());
    }

    static int tick;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START)
            tick++;
    }

    @Override
    public void render(TileEntityConverterBWM tile, double x, double y, double z, float partialTicks, int destroyStage, float tileAlpha) {
        if (tile != null) {
            EntityPlayer player = Minecraft.getMinecraft().player;
            ItemStack heldItem = player.getHeldItemMainhand();
            RayTraceResult result = Minecraft.getMinecraft().objectMouseOver;
            boolean correctHit = result != null && result.typeOfHit == RayTraceResult.Type.BLOCK && result.getBlockPos().equals(tile.getPos());
            boolean isHoldingGear = MysticalMechanicsAPI.IMPL.isValidGear(heldItem);
            EnumFacing direction = tile.getSideMystMech();
            ItemStack gear = tile.gear;
            boolean hitSide = false;
            if (correctHit) {
                EnumFacing sideHit = result.sideHit;
                if (player.isSneaking())
                    sideHit = sideHit.getOpposite();
                hitSide = sideHit == direction;
            }
            if (hitSide) {
                boolean gearFits = isHoldingGear && tile.canAttachGear(direction, heldItem);
                if (gear.isEmpty() && !gearFits) {
                    hitSide = false;
                }
            }
            if (!gear.isEmpty() || hitSide) {
                //double powerRatio = tile.capability.getPower(direction);

                GlStateManager.pushMatrix();
                GlStateManager.translate(x + 0.5, y + 0.5, z + 0.5);

                //rotate gear model items to fit the sides of the gearbox.
                switch (direction) {
                    case DOWN:
                        GlStateManager.rotate(-90, 1, 0, 0);
                        break;
                    case UP:
                        GlStateManager.rotate(90, 1, 0, 0);
                        break;
                    case NORTH:
                        break;
                    case WEST:
                        GlStateManager.rotate(90, 0, 1, 0);
                        break;
                    case SOUTH:
                        GlStateManager.rotate(180, 0, 1, 0);
                        break;
                    case EAST:
                        GlStateManager.rotate(270, 0, 1, 0);
                        break;
                    default:
                        break;

                }

                double angle = tile.angle;
                double lastAngle = tile.lastAngle;

                double totalTick = tick + partialTicks;

                //render hologram
                double offset = -0.375;
                if (hitSide && MysticalMechanics.RENDER_GEAR_HOLOGRAM) {
                    double startOffset;
                    double endOffset;
                    if (gear.isEmpty()) {
                        startOffset = -0.5;
                        endOffset = offset;
                        gear = heldItem;
                    } else {
                        startOffset = offset;
                        endOffset = -0.5;
                    }
                    offset = MathHelper.clampedLerp(startOffset, endOffset, (totalTick / 10) % 1);
                }
                //render the gears and rotate them based on how much power they have.
                GlStateManager.translate(0, 0, offset);
                GlStateManager.scale(0.875, 0.875, 0.875);
                GlStateManager.rotate(
                        ((float) (partialTicks * angle) + (1 - partialTicks) * (float) lastAngle), 0, 0, 1);
                GlStateManager.color(0.5f, 0.5f, 0.5f, 0.5f);
                IBakedModel model = Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(gear, tile.getWorld(), null);
                Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
                renderItem(gear, model);
                GlStateManager.popMatrix();
                GlStateManager.disableBlend();

            }
        }

    }

    public void renderItem(ItemStack stack, IBakedModel model) {
        float r = 1f;
        float g = 1f;
        float b = 1f;
        float a = 0.5f;
        if (!stack.isEmpty()) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(-0.5F, -0.5F, -0.5F);

            if (model.isBuiltInRenderer()) {
                GlStateManager.color(r, g, b, a);
                GlStateManager.enableRescaleNormal();
                stack.getItem().getTileEntityItemStackRenderer().renderByItem(stack);
            } else {
                Color color = new Color(r, g, b, a);
                Minecraft.getMinecraft().getRenderItem().renderModel(model, color.getRGB(), stack);
            }

            GlStateManager.popMatrix();
        }
    }
}