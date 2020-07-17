package lishy2.treefarm.entities;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@FunctionalInterface
interface ValidatorItemStack {
    boolean validate(ItemStack it);
}

public abstract class ItemHolderEntity extends LockableLootTileEntity {
    protected NonNullList<ItemStack> entityContent;
    protected int numPlayersUsing;
    private IItemHandlerModifiable items = createHandler();
    private LazyOptional<IItemHandlerModifiable> itemHandler = LazyOptional.of(() -> items);
    ValidatorItemStack validator;
    private int size;

    public ItemHolderEntity(TileEntityType<?> typeIn, ValidatorItemStack validator, int size) {
        super(typeIn);
        this.size = size;
        this.validator = validator;
        entityContent = NonNullList.withSize(size, ItemStack.EMPTY);
    }


    @Override
    public NonNullList<ItemStack> getItems() {

        return entityContent;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> itemsIn) {
        entityContent = itemsIn;
    }

    @Override
    protected abstract ITextComponent getDefaultName();

    @Override
    protected abstract Container createMenu(int id, PlayerInventory player);


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
            return ((ItemHolderEntity) tileentity).numPlayersUsing;
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
        return new ItemStackHandler() {
            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                // check if item is valid for slot, just return true for now(all items will be valid)
                return validator.validate(stack);
            }
        };
    }

    @Override
    public void remove() {
        super.remove();
        if (itemHandler != null) {
            itemHandler.invalidate();
        }
    }

}
