package lishy2.treefarm.entities;

import lishy2.treefarm.blocks.CultivatorBlock;
import net.minecraft.block.Block;
import net.minecraft.block.SaplingBlock;
import net.minecraft.item.BoneMealItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

import static lishy2.treefarm.util.RegistryHandler.CULTIVATOR_BLOCK;

public class CultivatorBlockEntity extends TileEntity implements ITickableTileEntity {
    public ItemStack bonemealStack;

    public CultivatorBlockEntity() {
        super(TileEntityType.Builder.create(WoodCutterBlockEntity::new, CULTIVATOR_BLOCK.get()).build(null));
        bonemealStack = new ItemStack(Items.BONE_MEAL, 64);
    }


    @Override
    public void tick() {
        if (world instanceof ServerWorld) {
            BlockPos forwardPos = getPos().add(this.getBlockState().get(CultivatorBlock.HORIZONTAL_FACING).getDirectionVec());
            Block forwardBlock = world.getBlockState(forwardPos).getBlock();
            if (forwardBlock instanceof SaplingBlock) {
                BoneMealItem.applyBonemeal(bonemealStack, world, forwardPos);
            }
        }
    }
}
