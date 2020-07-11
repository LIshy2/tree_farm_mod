package net.minecraft.inventory.container;

import com.mojang.datafixers.util.Pair;
import javax.annotation.Nullable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class Slot {
   private final int slotIndex;
   public final IInventory inventory;
   public int slotNumber;
   public final int xPos;
   public final int yPos;

   public Slot(IInventory inventoryIn, int index, int xPosition, int yPosition) {
      this.inventory = inventoryIn;
      this.slotIndex = index;
      this.xPos = xPosition;
      this.yPos = yPosition;
   }

   public void onSlotChange(ItemStack p_75220_1_, ItemStack p_75220_2_) {
      int i = p_75220_2_.getCount() - p_75220_1_.getCount();
      if (i > 0) {
         this.onCrafting(p_75220_2_, i);
      }

   }

   protected void onCrafting(ItemStack stack, int amount) {
   }

   protected void onSwapCraft(int p_190900_1_) {
   }

   protected void onCrafting(ItemStack stack) {
   }

   public ItemStack onTake(PlayerEntity thePlayer, ItemStack stack) {
      this.onSlotChanged();
      return stack;
   }

   public boolean isItemValid(ItemStack stack) {
      return true;
   }

   public ItemStack getStack() {
      return this.inventory.getStackInSlot(this.slotIndex);
   }

   public boolean getHasStack() {
      return !this.getStack().isEmpty();
   }

   public void putStack(ItemStack stack) {
      this.inventory.setInventorySlotContents(this.slotIndex, stack);
      this.onSlotChanged();
   }

   public void onSlotChanged() {
      this.inventory.markDirty();
   }

   public int getSlotStackLimit() {
      return this.inventory.getInventoryStackLimit();
   }

   public int getItemStackLimit(ItemStack stack) {
      return this.getSlotStackLimit();
   }

   @Nullable
   @OnlyIn(Dist.CLIENT)
   public Pair<ResourceLocation, ResourceLocation> func_225517_c_() {
      return backgroundPair;
   }

   public ItemStack decrStackSize(int amount) {
      return this.inventory.decrStackSize(this.slotIndex, amount);
   }

   public boolean canTakeStack(PlayerEntity playerIn) {
      return true;
   }

   @OnlyIn(Dist.CLIENT)
   public boolean isEnabled() {
      return true;
   }

   /**
    * Retrieves the index in the inventory for this slot, this value should typically not
    * be used, but can be useful for some occasions.
    *
    * @return Index in associated inventory for this slot.
    */
   public int getSlotIndex() {
      return slotIndex;
   }

   /**
    * Checks if the other slot is in the same inventory, by comparing the inventory reference.
    * @param other
    * @return true if the other slot is in the same inventory
    */
   public boolean isSameInventory(Slot other) {
      return this.inventory == other.inventory;
   }

   private Pair<ResourceLocation, ResourceLocation> backgroundPair;
   /**
    * Sets the background atlas and sprite location.
    *
    * @param atlas The atlas name
    * @param sprite The sprite located on that atlas.
    * @return this, to allow chaining.
    */
   public Slot setBackground(ResourceLocation atlas, ResourceLocation sprite) {
       this.backgroundPair = Pair.of(atlas, sprite);
       return this;
   }
}