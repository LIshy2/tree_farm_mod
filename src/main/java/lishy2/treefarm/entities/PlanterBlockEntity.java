package lishy2.treefarm.entities;

import lishy2.treefarm.blocks.PlanterBlock;
import lishy2.treefarm.containers.PlanterContainer;
import lishy2.treefarm.util.RegistryHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.InvWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PlanterBlockEntity extends LockableLootTileEntity implements ITickableTileEntity {
    protected NonNullList<ItemStack> entityContent;
    protected int numPlayersUsing;
    private IItemHandlerModifiable items = createHandler();
    private LazyOptional<IItemHandlerModifiable> itemHandler = LazyOptional.of(() -> items);
    private int size;

    public PlanterBlockEntity() {
        super(RegistryHandler.PLANTER_BLOCK_ENTITY.get());
        size = 1;
        entityContent = NonNullList.withSize(size, ItemStack.EMPTY);
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

    public NonNullList<ItemStack> getItems() {

        return entityContent;
    }

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

    protected void onOpenOrClose() {
        Block block = this.getBlockState().getBlock();
        this.world.addBlockEvent(this.pos, block, 1, this.numPlayersUsing);
        this.world.notifyNeighborsOfStateChange(this.pos, block);
    }

    public static int getPlayersUsing(IBlockReader reader, BlockPos pos) {
        BlockState blockState = reader.getBlockState(pos);
        if (blockState.hasTileEntity()) {
            TileEntity tileentity = reader.getTileEntity(pos);
            return ((PlanterBlockEntity) tileentity).numPlayersUsing;
        }
        return 0;
    }

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
