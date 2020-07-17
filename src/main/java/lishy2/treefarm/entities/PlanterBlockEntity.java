package lishy2.treefarm.entities;

import lishy2.treefarm.blocks.PlanterBlock;
import lishy2.treefarm.containers.PlanterContainer;
import net.minecraft.block.BlockState;
import net.minecraft.block.SaplingBlock;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;

import static lishy2.treefarm.util.RegistryHandler.PLANTER_BLOCK;

public class PlanterBlockEntity extends ItemHolderEntity implements ITickableTileEntity {

    public PlanterBlockEntity() {
        super(TileEntityType.Builder.create(PlanterBlockEntity::new, PLANTER_BLOCK.get()).build(null), (ItemStack it) -> ((BlockItem) it.getItem()).getBlock() instanceof SaplingBlock, 1);
    }

    @Override
    public void tick() {
        ItemStack saplings = entityContent.get(0);
        if (world instanceof ServerWorld) {
            BlockPos forwardPos = getPos().add(this.getBlockState().get(PlanterBlock.HORIZONTAL_FACING).getDirectionVec());
            BlockState forwardBlock = world.getBlockState(forwardPos);
            if (forwardBlock.isAir(world, forwardPos) && saplings != null && !saplings.isEmpty()) {
                BlockState saplingBlock = ((BlockItem) saplings.getItem()).getBlock().getDefaultState();
                world.setBlockState(forwardPos, saplingBlock);
                saplings.shrink(1);
            }
        }
    }

    @Override
    protected ITextComponent getDefaultName() {
        return new TranslationTextComponent("container.treefarm.planter_container");
    }

    @Override
    protected Container createMenu(int id, PlayerInventory player) {
        return new PlanterContainer(id, player, this);
    }
}
