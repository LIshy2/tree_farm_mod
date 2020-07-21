package lishy2.treefarm.entities;

import lishy2.treefarm.Treefarm;
import lishy2.treefarm.blocks.PlanterBlock;
import lishy2.treefarm.blocks.WoodCutterBlock;
import lishy2.treefarm.containers.WoodCutterContainer;
import lishy2.treefarm.util.RegistryHandler;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootParameters;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.InvWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


public class WoodCutterBlockEntity extends LockableLootTileEntity implements ITickableTileEntity {
    protected NonNullList<ItemStack> entityContent;
    protected int numPlayersUsing;
    private IItemHandlerModifiable items = createHandler();
    private LazyOptional<IItemHandlerModifiable> itemHandler = LazyOptional.of(() -> items);
    private int size;


    private Tree cuttingTreeNow;
    private int cooldown;

    public WoodCutterBlockEntity() {
        super(RegistryHandler.WOOD_CUTTER_BLOCK_ENTITY.get());
        size = 2;
        entityContent = NonNullList.withSize(size, ItemStack.EMPTY);
    }


    @Override
    public void tick() {
        ItemStack axe = entityContent.get(0), scissors = entityContent.get(1);
        if (world instanceof ServerWorld && !axe.isEmpty() && !scissors.isEmpty()) {
            ++cooldown;
            if (cooldown == 120 / axe.getDestroySpeed(Blocks.OAK_LOG.getDefaultState())) {
                Vec3i forwardVector = (this.getBlockState().get(PlanterBlock.HORIZONTAL_FACING).getDirectionVec());
                BlockPos forwardPosition = getPos().add(forwardVector);
                Vec3d backVector = new Vec3d(-forwardVector.getX() / 2, -forwardVector.getY() / 2, -forwardVector.getZ() / 2);
                if (cuttingTreeNow == null || cuttingTreeNow.isEmpty()) {
                    cuttingTreeNow = new Tree(forwardPosition, world);
                }
                if (!cuttingTreeNow.isEmpty()) {
                    getBlockState().with(WoodCutterBlock.LIT, true);
                    ItemStack tool = world.getBlockState(cuttingTreeNow.peek()).getBlock() instanceof LogBlock ? axe : scissors;
                    Treefarm.LOGGER.info("Cutting block " + cuttingTreeNow.peek() + " by " + tool);
                    LootContext.Builder context = new LootContext.Builder((ServerWorld) world).withParameter(LootParameters.TOOL, tool).withParameter(LootParameters.POSITION, cuttingTreeNow.peek());
                    List<ItemStack> drop = world.getBlockState(cuttingTreeNow.peek()).getDrops(context);
                    Treefarm.LOGGER.info("Dropped " + drop);
                    world.destroyBlock(cuttingTreeNow.pop(), false);
                    Vec3d lootDropPlace = new Vec3d(getPos()).add(new Vec3d(0.5, 0.5, 0.5)).add(backVector);
                    for (ItemStack itemStack : drop) {
                        InventoryHelper.spawnItemStack(world, lootDropPlace.getX(), lootDropPlace.getY(), lootDropPlace.getZ(), itemStack);
                    }
                    tool.attemptDamageItem(1, world.rand, null);
                } else {
                    getBlockState().with(WoodCutterBlock.LIT, false);
                }
                cooldown = 0;
            }
        }
    }

    @Override
    protected ITextComponent getDefaultName() {
        return new TranslationTextComponent("container.treefarm.wood_cutter_container");
    }

    @Override
    protected Container createMenu(int id, PlayerInventory player) {
        return new WoodCutterContainer(id, player, this);
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
            return ((WoodCutterBlockEntity) tileentity).numPlayersUsing;
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

class Tree {
    private ArrayList<BlockPos> logs;
    private ArrayList<BlockPos> leaves;

    public Tree(BlockPos one, World w) {
        logs = new ArrayList<>();
        leaves = new ArrayList<>();
        ArrayDeque<BlockPos> q = new ArrayDeque<>();
        q.add(one);
        HashSet<BlockPos> was = new HashSet<>();
        while (!q.isEmpty() && logs.size() + leaves.size() < 100) {
            BlockPos v = q.pollFirst();
            Block vBlock = w.getBlockState(v).getBlock();
            if (vBlock instanceof LogBlock) {
                logs.add(v);
            } else if (vBlock instanceof LeavesBlock) {
                leaves.add(v);
            } else {
                continue;
            }
            was.add(v);
            for (int dx = -1; dx <= 1; ++dx) {
                for (int dy = -1; dy <= 1; ++dy) {
                    for (int dz = -1; dz <= 1; ++dz) {
                        BlockPos newBlock = v.add(dx, dy, dz);
                        if (!was.contains(newBlock)) {
                            was.add(newBlock);
                            q.add(newBlock);
                        }
                    }
                }
            }
        }
    }


    BlockPos peek() {
        if (!leaves.isEmpty()) return leaves.get(leaves.size() - 1);
        if (!logs.isEmpty()) return logs.get(logs.size() - 1);
        return null;
    }

    BlockPos pop() {
        if (!leaves.isEmpty()) return leaves.remove(leaves.size() - 1);
        if (!logs.isEmpty()) return logs.remove(logs.size() - 1);
        return null;
    }

    boolean isEmpty() {
        return logs.isEmpty() && leaves.isEmpty();
    }
}