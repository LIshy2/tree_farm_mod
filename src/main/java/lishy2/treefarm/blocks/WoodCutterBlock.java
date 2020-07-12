package lishy2.treefarm.blocks;

import lishy2.treefarm.util.RegistryHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;

public class WoodCutterBlock extends Block {
    public WoodCutterBlock() {
        super(Properties.create(Material.EARTH).sound(SoundType.STONE).hardnessAndResistance(2.5f, 2.5f).harvestLevel(2));
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return RegistryHandler.WOOD_CUTTER_BLOCK_ENTITY.get().create();
    }
}