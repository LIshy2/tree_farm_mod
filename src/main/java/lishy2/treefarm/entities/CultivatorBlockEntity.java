package lishy2.treefarm.entities;

import lishy2.treefarm.blocks.CultivatorBlock;
import lishy2.treefarm.containers.CultivatorContainer;
import net.minecraft.block.Block;
import net.minecraft.block.SaplingBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.BoneMealItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static lishy2.treefarm.util.RegistryHandler.CULTIVATOR_BLOCK;

public class CultivatorBlockEntity extends ItemHolderEntity implements ITickableTileEntity {

    public static final Logger LOGGER = LogManager.getLogger();

    public CultivatorBlockEntity() {
        super(TileEntityType.Builder.create(CultivatorBlockEntity::new, CULTIVATOR_BLOCK.get()).build(null), (ItemStack it) -> it.getItem() instanceof BoneMealItem, 2);
    }


    @Override
    public void tick() {
        ItemStack bonemealStack = entityContent.get(0);
        if (world instanceof ServerWorld) {
            boolean emptyArea = true;
            for (PlayerEntity p : ((ServerWorld) world).getPlayers()) {
                if (p.getPositionVec().distanceTo(new Vec3d(getPos().getX(), getPos().getY(), getPos().getZ())) <= 4)
                    emptyArea = false;
            }
            if (emptyArea && bonemealStack != null && !bonemealStack.isEmpty()) {
                BlockPos forwardPos = getPos().add(this.getBlockState().get(CultivatorBlock.HORIZONTAL_FACING).getDirectionVec());
                Block forwardBlock = world.getBlockState(forwardPos).getBlock();
                if (forwardBlock instanceof SaplingBlock) {
                    world.playSound(getPos().getX(), getPos().getY(), getPos().getZ(), SoundEvents.BLOCK_DISPENSER_DISPENSE, SoundCategory.BLOCKS, 100, 1, true);
                    BoneMealItem.applyBonemeal(bonemealStack, world, forwardPos);
                    LOGGER.info("Bonemeal applied");
                    bonemealStack.shrink(1);
                }
            }
        }
    }


    @Override
    protected ITextComponent getDefaultName() {
        return new TranslationTextComponent("container.treefarm.cultivator_container");
    }

    @Override
    protected Container createMenu(int id, PlayerInventory player) {
        return new CultivatorContainer(id, player, this);
    }
}
