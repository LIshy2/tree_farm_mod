package lishy2.treefarm.util;

import lishy2.treefarm.Treefarm;
import lishy2.treefarm.blocks.WoodCutterBlock;
import lishy2.treefarm.entities.WoodCutterBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

//import lishy2.treefarm.entities.WoodCutterBlockEntity;

//import lishy2.treefarm.blocks.WoodCutterBlock;
//import lishy2.treefarm.entities.WoodCutterBlockEntity;

public class RegistryHandler {


    private static final DeferredRegister<Block> BLOCKS = new DeferredRegister<>(ForgeRegistries.BLOCKS, Treefarm.MOD_ID);
    private static final DeferredRegister<Item> ITEMS = new DeferredRegister<>(ForgeRegistries.ITEMS, Treefarm.MOD_ID);
    private static final DeferredRegister<TileEntityType<?>> TILE_ENTITIES = new DeferredRegister<>(ForgeRegistries.TILE_ENTITIES, Treefarm.MOD_ID);



    public static void init() {
        Treefarm.LOGGER.info("Registration started");
        BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        TILE_ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
        Treefarm.LOGGER.info("Registration finished");
    }

    //Blocks
    public static final RegistryObject<Block> WOOD_CUTTER_BLOCK = BLOCKS.register("wood_cutter_block", WoodCutterBlock::new);
    //Block items
    public static final RegistryObject<Item> WOOD_CUTTER_BLOCK_ITEM = ITEMS.register("wood_cutter_block", () -> new BlockItem(WOOD_CUTTER_BLOCK.get(), new Item.Properties().group(ItemGroup.DECORATIONS)));

    //Items


    //Tile Entities

    public static final RegistryObject<TileEntityType<WoodCutterBlockEntity>> WOOD_CUTTER_BLOCK_ENTITY = TILE_ENTITIES.register("wood_cutter_entity", () -> TileEntityType.Builder.create(WoodCutterBlockEntity::new, WOOD_CUTTER_BLOCK.get()).build(null));


}
