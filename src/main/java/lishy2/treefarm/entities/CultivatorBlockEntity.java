package lishy2.treefarm.entities;

import lishy2.treefarm.Treefarm;
import lishy2.treefarm.blocks.CultivatorBlock;
import net.minecraft.block.Block;
import net.minecraft.block.SaplingBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BoneMealItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.server.ServerWorld;

import static lishy2.treefarm.util.RegistryHandler.CULTIVATOR_BLOCK;

public class CultivatorBlockEntity extends TileEntity implements ITickableTileEntity {
    private ItemStack bonemealStack;

    public CultivatorBlockEntity() {
        super(TileEntityType.Builder.create(CultivatorBlockEntity::new, CULTIVATOR_BLOCK.get()).build(null));
        bonemealStack = new ItemStack(Items.BONE_MEAL, 64);
    }


    @Override
    public void tick() {
        if (world instanceof ServerWorld) {
            Treefarm.LOGGER.info(world.getClosestPlayer(getPos().getX(), getPos().getY(), getPos().getZ()));
            boolean emptyArea = true;
            for (PlayerEntity p : ((ServerWorld) world).getPlayers()) {
                if (p.getPositionVec().distanceTo(new Vec3d(getPos().getX(), getPos().getY(), getPos().getZ())) < 3)
                    emptyArea = false;
            }
            if (emptyArea && !bonemealStack.isEmpty()) {
                BlockPos forwardPos = getPos().add(this.getBlockState().get(CultivatorBlock.HORIZONTAL_FACING).getDirectionVec());
                Block forwardBlock = world.getBlockState(forwardPos).getBlock();
                if (forwardBlock instanceof SaplingBlock) {
                    world.playSound(getPos().getX(), getPos().getY(), getPos().getZ(), SoundEvents.BLOCK_DISPENSER_DISPENSE, SoundCategory.BLOCKS, 100, 1, true);
                    BoneMealItem.applyBonemeal(bonemealStack, world, forwardPos);
                    bonemealStack.shrink(1);
                }
            }
        }
    }
}
