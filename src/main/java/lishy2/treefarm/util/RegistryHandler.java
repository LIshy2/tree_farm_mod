package lishy2.treefarm.util;

import lishy2.treefarm.Treefarm;
import lishy2.treefarm.util.blocks.GardenCraftingTable;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class RegistryHandler {
    private static final DeferredRegister<Block> BLOCKS = new DeferredRegister<>(ForgeRegistries.BLOCKS, Treefarm.MOD_ID);
    private static final DeferredRegister<Item> ITEMS = new DeferredRegister<>(ForgeRegistries.ITEMS, Treefarm.MOD_ID);

    public static void init() {
        BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    //Blocks
    public static final RegistryObject<Block> GARDEN_CRAFTING_TABLE = BLOCKS.register("garden_crafting_table", GardenCraftingTable::new);
    //Block items
    public static final RegistryObject<Item> GARDEN_CRAFTING_TABLE_ITEM = ITEMS.register("garden_crafting_table", () -> new BlockItem(GARDEN_CRAFTING_TABLE.get(), new Item.Properties().group(ItemGroup.DECORATIONS)));
    //Items
}
