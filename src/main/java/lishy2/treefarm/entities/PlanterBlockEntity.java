package lishy2.treefarm.entities;

import lishy2.treefarm.blocks.PlanterBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

import static lishy2.treefarm.util.RegistryHandler.PLANTER_BLOCK;

public class PlanterBlockEntity extends TileEntity implements ITickableTileEntity {
    public BlockState saplings;

    public PlanterBlockEntity() {
        super(TileEntityType.Builder.create(PlanterBlockEntity::new, PLANTER_BLOCK.get()).build(null));
        saplings = Blocks.OAK_SAPLING.getDefaultState();
    }

    @Override
    public void tick() {
        if (world instanceof ServerWorld) {
            BlockPos forwardPos = getPos().add(this.getBlockState().get(PlanterBlock.HORIZONTAL_FACING).getDirectionVec());
            BlockState forwardBlock = world.getBlockState(forwardPos);
            if (forwardBlock.isAir(world, forwardPos)) {
                world.setBlockState(forwardPos, saplings);
            }
        }
    }
}
