package lishy2.treefarm.entities;

import lishy2.treefarm.blocks.PlanterBlock;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

import static lishy2.treefarm.util.RegistryHandler.PLANTER_BLOCK;

public class PlanterBlockEntity extends TileEntity implements ITickableTileEntity {
    public ItemStack saplings;

    public PlanterBlockEntity() {
        super(TileEntityType.Builder.create(PlanterBlockEntity::new, PLANTER_BLOCK.get()).build(null));
    }

    @Override
    public void tick() {
        if (world instanceof ServerWorld) {
            BlockPos forwardPos = getPos().add(this.getBlockState().get(PlanterBlock.HORIZONTAL_FACING).getDirectionVec());
            BlockState forwardBlock = world.getBlockState(forwardPos);
            if (forwardBlock.isAir(world, forwardPos) && saplings != null && saplings.isEmpty()) {
                BlockState saplingBlock = ((BlockItem) saplings.getItem()).getBlock().getDefaultState();
                world.setBlockState(forwardPos, saplingBlock);
                saplings.shrink(1);
            }
        }
    }
}
