package lishy2.treefarm.entities;

import net.minecraft.block.Block;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.LogBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.ArrayList;

public class WoodCutterBlockEntity extends TileEntity implements ITickableTileEntity, INamedContainerProvider {

    private Tree cuttingTreeNow;
    private AxeItem axe;

    public WoodCutterBlockEntity(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }

    @Override
    public ITextComponent getDisplayName() {
        return null;
    }

    @Nullable
    @Override
    public Container createMenu(int p_createMenu_1_, PlayerInventory p_createMenu_2_, PlayerEntity p_createMenu_3_) {
        return null;
    }

    @Override
    public void tick() {
        if (cuttingTreeNow.isEmpty()) {
            cuttingTreeNow = new Tree(this.getPos().up(), world);
        }
        world.destroyBlock(cuttingTreeNow.pop(), true);
    }

    public final ItemStackHandler inventory = new ItemStackHandler(1) {

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return true;
        }
    };

}

class Tree {
    private ArrayList<BlockPos> logs;
    private ArrayList<BlockPos> leaves;

    public Tree(BlockPos one, World w) {
        logs = new ArrayList<>();
        leaves = new ArrayList<>();
        ArrayDeque<BlockPos> q = new ArrayDeque<>();
        q.add(one);
        while (!q.isEmpty()) {
            BlockPos v = q.pollFirst();
            Block vBlock = w.getBlockState(v).getBlock();
            if (vBlock instanceof LogBlock) {
                logs.add(v);
            } else if (vBlock instanceof LeavesBlock) {
                leaves.add(v);
            } else {
                break;
            }
            for (int dx = -1; dx <= 1; ++dx) {
                for (int dy = -1; dy <= 1; ++dy) {
                    for (int dz = -1; dz <= 1; ++dz) {
                        BlockPos newBlock = v.add(dx, dy, dz);
                        q.add(newBlock);
                    }
                }
            }
        }
    }

    BlockPos peek() {
        if (!logs.isEmpty()) return logs.get(logs.size() - 1);
        if (!leaves.isEmpty()) return leaves.get(leaves.size() - 1);
        return null;
    }

    BlockPos pop() {
        if (!logs.isEmpty()) return logs.remove(logs.size() - 1);
        if (!leaves.isEmpty()) return leaves.remove(leaves.size() - 1);
        return null;
    }

    boolean isEmpty() {
        return logs.isEmpty() && leaves.isEmpty();
    }
}