package lishy2.treefarm.util;

import lishy2.treefarm.Treefarm;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

//import lishy2.treefarm.blocks.WoodCutterBlock;
//import lishy2.treefarm.entities.WoodCutterBlockEntity;

public class RegistryHandler {
    private static final DeferredRegister<Block> BLOCKS = new DeferredRegister<>(ForgeRegistries.BLOCKS, Treefarm.MOD_ID);
    private static final DeferredRegister<Item> ITEMS = new DeferredRegister<>(ForgeRegistries.ITEMS, Treefarm.MOD_ID);

    //    private static final DeferredRegister<> TILE_ENTITY_TYPES = new DeferredRegister<>(ForgeRegistries.ITEMS);
    public static void init() {
        BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    //Blocks
//    public static final RegistryObject<Block> WOOD_CUTTER_BLOCK = BLOCKS.register("wood_cutter_block", WoodCutterBlock::new);
    //Block items

    //Items

    //Entities
//    public static final TileEntityType<WoodCutterBlockEntity> WOOD_CUTTER_BLOCK_ENTITY = TileEntityType.register("wood_cutter_block", WOOD_CUTTER_BLOCK.get());

}
