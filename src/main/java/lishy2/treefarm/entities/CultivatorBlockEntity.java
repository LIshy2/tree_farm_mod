package lishy2.treefarm.entities;

import lishy2.treefarm.blocks.CultivatorBlock;
import lishy2.treefarm.containers.CultivatorContainer;
import lishy2.treefarm.util.RegistryHandler;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.Block;
import net.minecraft.block.SaplingBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.BoneMealItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
public class CultivatorBlockEntity extends LockableLootTileEntity implements ITickableTileEntity {
    private static final Logger LOGGER = LogManager.getLogger();
    private NonNullList<ItemStack> entityContent;
    private int numPlayersUsing;
    private IItemHandlerModifiable items = createHandler();
    private LazyOptional<IItemHandlerModifiable> itemHandler = LazyOptional.of(() -> items);
    private int size;


    private int cooldown = 6;

    public CultivatorBlockEntity() {
        super(RegistryHandler.CULTIVATOR_BLOCK_ENTITY.get());
        this.size = 1;
        entityContent = NonNullList.withSize(size, ItemStack.EMPTY);
    }


    @Override
    public void tick() {
        ItemStack bonemealStack = entityContent.get(0);
        --cooldown;
        if (world instanceof ServerWorld && cooldown <= 0) {
            boolean emptyArea = true;
            for (PlayerEntity p : ((ServerWorld) world).getPlayers()) {
                if (p.getPositionVec().distanceTo(new Vec3d(getPos().getX(), getPos().getY(), getPos().getZ())) <= 4)
                    emptyArea = false;
            }
            if (emptyArea && !bonemealStack.isEmpty()) {
                BlockPos forwardPos = getPos().add(this.getBlockState().get(CultivatorBlock.HORIZONTAL_FACING).getDirectionVec());
                Block forwardBlock = world.getBlockState(forwardPos).getBlock();
                if (forwardBlock instanceof SaplingBlock) {
                    getBlockState().with(CultivatorBlock.LIT, true);
                    world.playSound(getPos().getX(), getPos().getY(), getPos().getZ(), SoundEvents.BLOCK_DISPENSER_DISPENSE, SoundCategory.BLOCKS, 100, 1, true);
                    BoneMealItem.applyBonemeal(bonemealStack, world, forwardPos);
                    LOGGER.info("Bonemeal applied");
                    bonemealStack.shrink(1);
                    cooldown = 6;

                    getBlockState().with(CultivatorBlock.LIT, false);
                }
            }
        }
    }

    @Override
    protected ITextComponent getDefaultName() {
        return new TranslationTextComponent("container.treefarm.cultivator_container");
    }

    @ParametersAreNonnullByDefault
    @Override
    protected Container createMenu(int id, PlayerInventory player) {
        return new CultivatorContainer(id, player, this);
    }


    @Override
    public NonNullList<ItemStack> getItems() {

        return entityContent;
    }


    @ParametersAreNonnullByDefault
    @Override
    protected void setItems(NonNullList<ItemStack> itemsIn) {
        entityContent = itemsIn;
    }


    @Override
    public int getSizeInventory() {
        return size;
    }

    @Override
    public void read(CompoundNBT compound) {
        super.read(compound);
        this.entityContent = NonNullList.withSize(size, ItemStack.EMPTY);
        if (!this.checkLootAndRead(compound)) {
            ItemStackHelper.loadAllItems(compound, entityContent);
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);
        if (!this.checkLootAndWrite(compound)) {
            ItemStackHelper.saveAllItems(compound, entityContent);
        }
        return compound;
    }

    @Override
    public boolean receiveClientEvent(int id, int type) {
        if (id == 1) {
            this.numPlayersUsing = type;
            return true;
        } else {
            return super.receiveClientEvent(id, type);
        }
    }

    @Override
    public void openInventory(PlayerEntity player) {
        if (!player.isSpectator()) {
            if (this.numPlayersUsing < 0) {
                this.numPlayersUsing = 0;
            }
            ++this.numPlayersUsing;
            this.onOpenOrClose();
        }

    }

    @Override
    public void closeInventory(PlayerEntity player) {
        if (!player.isSpectator()) {
            --this.numPlayersUsing;
            this.onOpenOrClose();
        }
    }

    private void onOpenOrClose() {
        Block block = this.getBlockState().getBlock();
        assert this.world != null;
        this.world.addBlockEvent(this.pos, block, 1, this.numPlayersUsing);
        this.world.notifyNeighborsOfStateChange(this.pos, block);
    }

//    public static int getPlayersUsing(IBlockReader reader, BlockPos pos) {
//        BlockState blockState = reader.getBlockState(pos);
//        if (blockState.hasTileEntity()) {
//            TileEntity tileentity = reader.getTileEntity(pos);
//            assert tileentity != null;
//            return ((CultivatorBlockEntity) tileentity).numPlayersUsing;
//        }
//        return 0;
//    }

    @Override
    public void updateContainingBlockInfo() {
        super.updateContainingBlockInfo();
        if (this.itemHandler != null) {
            this.itemHandler.invalidate();
            this.itemHandler = null;
        }
    }

    @Nullable
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return itemHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    private IItemHandlerModifiable createHandler() {
        return new InvWrapper(this);
    }

    @Override
    public void remove() {
        super.remove();
        if (itemHandler != null) {
            itemHandler.invalidate();
        }
    }
}
