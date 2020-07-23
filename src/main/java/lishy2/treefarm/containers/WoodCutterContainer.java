package lishy2.treefarm.containers;


import lishy2.treefarm.entities.WoodCutterBlockEntity;
import lishy2.treefarm.util.RegistryHandler;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShearsItem;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IWorldPosCallable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

@MethodsReturnNonnullByDefault
public class WoodCutterContainer extends Container {

    private final IWorldPosCallable canInteractWithCallable;


    public WoodCutterContainer(final int windowId, final PlayerInventory playerInventory, final WoodCutterBlockEntity tileEntity) {
        super(RegistryHandler.WOOD_CUTTER_CONTAINER.get(), windowId);
        this.canInteractWithCallable = IWorldPosCallable.of(Objects.requireNonNull(tileEntity.getWorld()), tileEntity.getPos());
        // Main Inventory
        int slotSizePlus2 = 18;
        this.addSlot(new Slot(tileEntity, 0, 35, 20) {
            public boolean isItemValid(ItemStack stack) {
                return stack.getItem() instanceof AxeItem;
            }
        });
        this.addSlot(new Slot(tileEntity, 1, 121, 20) {
            public boolean isItemValid(ItemStack stack) {
                return stack.getItem() instanceof ShearsItem;
            }
        });

        // Main Player Inventory
        int startPlayerInvY = 51;
        int startPlayerInvX = 8;
        for (int row = 0; row < 3; ++row) {
            for (int column = 0; column < 9; ++column) {
                this.addSlot(new Slot(playerInventory, 9 + (row * 9) + column, startPlayerInvX + (column * slotSizePlus2),
                        startPlayerInvY + (row * slotSizePlus2)));
            }
        }
        // Hotbar
        int hotbarY = 109;
        for (int column = 0; column < 9; ++column) {
            this.addSlot(new Slot(playerInventory, column, startPlayerInvX + (column * slotSizePlus2), hotbarY));
        }
    }

    private static WoodCutterBlockEntity getTileEntity(final PlayerInventory playerInventory, final PacketBuffer data) {
        Objects.requireNonNull(playerInventory, "playerInventory cannot be null");
        Objects.requireNonNull(data, "data cannot be null");
        final TileEntity tileAtPos = playerInventory.player.world.getTileEntity(data.readBlockPos());
        if (tileAtPos instanceof WoodCutterBlockEntity) {
            return (WoodCutterBlockEntity) tileAtPos;
        }
        throw new IllegalStateException("Tile entity is not correct! " + tileAtPos);
    }

    public WoodCutterContainer(final int windowId, final PlayerInventory playerInventory, final PacketBuffer data) {
        this(windowId, playerInventory, getTileEntity(playerInventory, data));
    }


    @ParametersAreNonnullByDefault
    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return isWithinUsableDistance(canInteractWithCallable, playerIn, RegistryHandler.WOOD_CUTTER_BLOCK.get());
    }

    @Override
    public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);
        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();
            if (index < 2) {
                if (!this.mergeItemStack(itemstack1, 2, this.inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.mergeItemStack(itemstack1, 0, 2, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }
        }
        return itemstack;
    }

}
