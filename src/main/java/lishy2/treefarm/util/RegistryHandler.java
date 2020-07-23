package lishy2.treefarm.util;

import lishy2.treefarm.Treefarm;
import lishy2.treefarm.blocks.CultivatorBlock;
import lishy2.treefarm.blocks.PlanterBlock;
import lishy2.treefarm.blocks.WoodCutterBlock;
import lishy2.treefarm.containers.CultivatorContainer;
import lishy2.treefarm.containers.PlanterContainer;
import lishy2.treefarm.containers.WoodCutterContainer;
import lishy2.treefarm.entities.CultivatorBlockEntity;
import lishy2.treefarm.entities.PlanterBlockEntity;
import lishy2.treefarm.entities.WoodCutterBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;



public class RegistryHandler {


    private static final DeferredRegister<Block> BLOCKS = new DeferredRegister<>(ForgeRegistries.BLOCKS, Treefarm.MOD_ID);
    private static final DeferredRegister<Item> ITEMS = new DeferredRegister<>(ForgeRegistries.ITEMS, Treefarm.MOD_ID);
    private static final DeferredRegister<TileEntityType<?>> TILE_ENTITY_TYPES = new DeferredRegister<>(ForgeRegistries.TILE_ENTITIES, Treefarm.MOD_ID);
    private static final DeferredRegister<ContainerType<?>> CONTAINER_TYPES = new DeferredRegister<>(ForgeRegistries.CONTAINERS, Treefarm.MOD_ID);

    public static void init() {
        Treefarm.LOGGER.info("Registration started");
        BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        TILE_ENTITY_TYPES.register(FMLJavaModLoadingContext.get().getModEventBus());
        CONTAINER_TYPES.register(FMLJavaModLoadingContext.get().getModEventBus());
        Treefarm.LOGGER.info("Registration finished");
    }

    //Blocks
    public static final RegistryObject<Block> WOOD_CUTTER_BLOCK = BLOCKS.register("wood_cutter_block", WoodCutterBlock::new);
    public static final RegistryObject<Block> CULTIVATOR_BLOCK = BLOCKS.register("cultivator_block", CultivatorBlock::new);
    public static final RegistryObject<Block> PLANTER_BLOCK = BLOCKS.register("planter_block", PlanterBlock::new);

    //Block items
    public static final RegistryObject<Item> WOOD_CUTTER_BLOCK_ITEM = ITEMS.register("wood_cutter_block", () -> new BlockItem(WOOD_CUTTER_BLOCK.get(), new Item.Properties().group(ItemGroup.REDSTONE)));
    public static final RegistryObject<Item> CULTIVATOR_BLOCK_ITEM = ITEMS.register("cultivator_block", () -> new BlockItem(CULTIVATOR_BLOCK.get(), new Item.Properties().group(ItemGroup.REDSTONE)));
    public static final RegistryObject<Item> PLANTER_BLOCK_ITEM = ITEMS.register("planter_block", () -> new BlockItem(PLANTER_BLOCK.get(), new Item.Properties().group(ItemGroup.REDSTONE)));

    //Items


    //Tile Entity types

    public static final RegistryObject<TileEntityType<WoodCutterBlockEntity>> WOOD_CUTTER_BLOCK_ENTITY = TILE_ENTITY_TYPES.register("wood_cutter_entity", () -> TileEntityType.Builder.create(WoodCutterBlockEntity::new, WOOD_CUTTER_BLOCK.get()).build(null));
    public static final RegistryObject<TileEntityType<CultivatorBlockEntity>> CULTIVATOR_BLOCK_ENTITY = TILE_ENTITY_TYPES.register("cultivator_entity", () -> TileEntityType.Builder.create(CultivatorBlockEntity::new, CULTIVATOR_BLOCK.get()).build(null));
    public static final RegistryObject<TileEntityType<PlanterBlockEntity>> PLANTER_BLOCK_ENTITY = TILE_ENTITY_TYPES.register("planter_entity", () -> TileEntityType.Builder.create(PlanterBlockEntity::new, PLANTER_BLOCK.get()).build(null));


    //Container types

    public static final RegistryObject<ContainerType<PlanterContainer>> PLANTER_CONTAINER = CONTAINER_TYPES
            .register("planter_container", () -> IForgeContainerType.create(PlanterContainer::new));
    public static final RegistryObject<ContainerType<CultivatorContainer>> CULTIVATOR_CONTAINER = CONTAINER_TYPES
            .register("cultivator_container", () -> IForgeContainerType.create(CultivatorContainer::new));
    public static final RegistryObject<ContainerType<WoodCutterContainer>> WOOD_CUTTER_CONTAINER = CONTAINER_TYPES
            .register("wood_cutter_container", () -> IForgeContainerType.create(WoodCutterContainer::new));


}
