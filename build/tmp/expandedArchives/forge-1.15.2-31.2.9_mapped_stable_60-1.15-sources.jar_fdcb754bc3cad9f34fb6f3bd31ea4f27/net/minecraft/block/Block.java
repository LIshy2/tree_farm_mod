package net.minecraft.block;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import it.unimi.dsi.fastutil.objects.Object2ByteLinkedOpenHashMap;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.block.material.PushReaction;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.IFluidState;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.network.DebugPacketSender;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.StateContainer;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.Tag;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.Mirror;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ObjectIntIdentityMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.Explosion;
import net.minecraft.world.GameRules;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootParameterSets;
import net.minecraft.world.storage.loot.LootParameters;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraft.world.storage.loot.LootTables;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Block extends net.minecraftforge.registries.ForgeRegistryEntry<Block> implements IItemProvider, net.minecraftforge.common.extensions.IForgeBlock {
   protected static final Logger LOGGER = LogManager.getLogger();
   @Deprecated //Forge: Do not use, use GameRegistry
   public static final ObjectIntIdentityMap<BlockState> BLOCK_STATE_IDS = net.minecraftforge.registries.GameData.getBlockStateIDMap();
   private static final Direction[] UPDATE_ORDER = new Direction[]{Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH, Direction.DOWN, Direction.UP};
   private static final LoadingCache<VoxelShape, Boolean> OPAQUE_CACHE = CacheBuilder.newBuilder().maximumSize(512L).weakKeys().build(new CacheLoader<VoxelShape, Boolean>() {
      public Boolean load(VoxelShape p_load_1_) {
         return !VoxelShapes.compare(VoxelShapes.fullCube(), p_load_1_, IBooleanFunction.NOT_SAME);
      }
   });
   private static final VoxelShape field_220083_b = VoxelShapes.combineAndSimplify(VoxelShapes.fullCube(), makeCuboidShape(2.0D, 0.0D, 2.0D, 14.0D, 16.0D, 14.0D), IBooleanFunction.ONLY_FIRST);
   private static final VoxelShape field_220084_c = makeCuboidShape(7.0D, 0.0D, 7.0D, 9.0D, 10.0D, 9.0D);
   protected final int lightValue;
   protected final float blockHardness;
   protected final float blockResistance;
   protected final boolean ticksRandomly;
   protected final SoundType soundType;
   protected final Material material;
   protected final MaterialColor materialColor;
   private final float slipperiness;
   private final float field_226886_f_;
   private final float field_226887_g_;
   protected final StateContainer<Block, BlockState> stateContainer;
   private BlockState defaultState;
   protected final boolean blocksMovement;
   private final boolean variableOpacity;
   private final boolean field_226888_j_;
   @Nullable
   private ResourceLocation lootTable;
   @Nullable
   private String translationKey;
   @Nullable
   private Item item;
   private static final ThreadLocal<Object2ByteLinkedOpenHashMap<Block.RenderSideCacheKey>> SHOULD_SIDE_RENDER_CACHE = ThreadLocal.withInitial(() -> {
      Object2ByteLinkedOpenHashMap<Block.RenderSideCacheKey> object2bytelinkedopenhashmap = new Object2ByteLinkedOpenHashMap<Block.RenderSideCacheKey>(2048, 0.25F) {
         protected void rehash(int p_rehash_1_) {
         }
      };
      object2bytelinkedopenhashmap.defaultReturnValue((byte)127);
      return object2bytelinkedopenhashmap;
   });

   public static int getStateId(@Nullable BlockState state) {
      if (state == null) {
         return 0;
      } else {
         int i = BLOCK_STATE_IDS.get(state);
         return i == -1 ? 0 : i;
      }
   }

   public static BlockState getStateById(int id) {
      BlockState blockstate = BLOCK_STATE_IDS.getByValue(id);
      return blockstate == null ? Blocks.AIR.getDefaultState() : blockstate;
   }

   public static Block getBlockFromItem(@Nullable Item itemIn) {
      return itemIn instanceof BlockItem ? ((BlockItem)itemIn).getBlock() : Blocks.AIR;
   }

   public static BlockState nudgeEntitiesWithNewState(BlockState oldState, BlockState newState, World worldIn, BlockPos pos) {
      VoxelShape voxelshape = VoxelShapes.combine(oldState.getCollisionShape(worldIn, pos), newState.getCollisionShape(worldIn, pos), IBooleanFunction.ONLY_SECOND).withOffset((double)pos.getX(), (double)pos.getY(), (double)pos.getZ());

      for(Entity entity : worldIn.getEntitiesWithinAABBExcludingEntity((Entity)null, voxelshape.getBoundingBox())) {
         double d0 = VoxelShapes.getAllowedOffset(Direction.Axis.Y, entity.getBoundingBox().offset(0.0D, 1.0D, 0.0D), Stream.of(voxelshape), -1.0D);
         entity.setPositionAndUpdate(entity.func_226277_ct_(), entity.func_226278_cu_() + 1.0D + d0, entity.func_226281_cx_());
      }

      return newState;
   }

   public static VoxelShape makeCuboidShape(double x1, double y1, double z1, double x2, double y2, double z2) {
      return VoxelShapes.create(x1 / 16.0D, y1 / 16.0D, z1 / 16.0D, x2 / 16.0D, y2 / 16.0D, z2 / 16.0D);
   }

   @Deprecated
   public boolean canEntitySpawn(BlockState state, IBlockReader worldIn, BlockPos pos, EntityType<?> type) {
      return state.func_224755_d(worldIn, pos, Direction.UP) && state.getLightValue(worldIn, pos) < 14;
   }

   @Deprecated
   public boolean isAir(BlockState state) {
      return false;
   }

   @Deprecated
   public int getLightValue(BlockState state) {
      return this.lightValue;
   }

   @Deprecated
   public Material getMaterial(BlockState state) {
      return this.material;
   }

   @Deprecated
   public MaterialColor getMaterialColor(BlockState state, IBlockReader worldIn, BlockPos pos) {
      return this.materialColor;
   }

   @Deprecated
   public void updateNeighbors(BlockState stateIn, IWorld worldIn, BlockPos pos, int flags) {
      try (BlockPos.PooledMutable blockpos$pooledmutable = BlockPos.PooledMutable.func_185346_s()) {
         for(Direction direction : UPDATE_ORDER) {
            blockpos$pooledmutable.func_189533_g(pos).func_189536_c(direction);
            BlockState blockstate = worldIn.getBlockState(blockpos$pooledmutable);
            BlockState blockstate1 = blockstate.updatePostPlacement(direction.getOpposite(), stateIn, worldIn, blockpos$pooledmutable, pos);
            replaceBlock(blockstate, blockstate1, worldIn, blockpos$pooledmutable, flags);
         }
      }

   }

   public boolean isIn(Tag<Block> tagIn) {
      return tagIn.func_199685_a_(this);
   }

   public static BlockState getValidBlockForPosition(BlockState currentState, IWorld worldIn, BlockPos pos) {
      BlockState blockstate = currentState;
      BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();

      for(Direction direction : UPDATE_ORDER) {
         blockpos$mutable.func_189533_g(pos).func_189536_c(direction);
         blockstate = blockstate.updatePostPlacement(direction, worldIn.getBlockState(blockpos$mutable), worldIn, pos, blockpos$mutable);
      }

      return blockstate;
   }

   public static void replaceBlock(BlockState oldState, BlockState newState, IWorld worldIn, BlockPos pos, int flags) {
      if (newState != oldState) {
         if (newState.isAir()) {
            if (!worldIn.isRemote()) {
               worldIn.destroyBlock(pos, (flags & 32) == 0);
            }
         } else {
            worldIn.setBlockState(pos, newState, flags & -33);
         }
      }

   }

   @Deprecated
   public void updateDiagonalNeighbors(BlockState state, IWorld worldIn, BlockPos pos, int flags) {
   }

   @Deprecated
   public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
      return stateIn;
   }

   @Deprecated
   public BlockState rotate(BlockState state, Rotation rot) {
      return state;
   }

   @Deprecated
   public BlockState mirror(BlockState state, Mirror mirrorIn) {
      return state;
   }

   public Block(Block.Properties properties) {
      StateContainer.Builder<Block, BlockState> builder = new StateContainer.Builder<>(this);
      this.fillStateContainer(builder);
      this.material = properties.material;
      this.materialColor = properties.mapColor;
      this.blocksMovement = properties.blocksMovement;
      this.soundType = properties.soundType;
      this.lightValue = properties.lightValue;
      this.blockResistance = properties.resistance;
      this.blockHardness = properties.hardness;
      this.ticksRandomly = properties.ticksRandomly;
      this.harvestLevel = properties.harvestLevel;
      this.harvestTool = properties.harvestTool;
      final ResourceLocation lootTableCache = properties.lootTable;
      this.lootTableSupplier = lootTableCache != null ? () -> lootTableCache : properties.lootTableSupplier != null ? properties.lootTableSupplier : () -> new ResourceLocation(this.getRegistryName().getNamespace(), "blocks/" + this.getRegistryName().getPath());
      this.slipperiness = properties.slipperiness;
      this.field_226886_f_ = properties.field_226893_j_;
      this.field_226887_g_ = properties.field_226894_k_;
      this.variableOpacity = properties.variableOpacity;
      this.lootTable = properties.lootTable;
      this.field_226888_j_ = properties.field_226895_m_;
      this.stateContainer = builder.create(BlockState::new);
      this.setDefaultState(this.stateContainer.getBaseState());
   }

   public static boolean cannotAttach(Block blockIn) {
      return blockIn instanceof LeavesBlock || blockIn == Blocks.BARRIER || blockIn == Blocks.CARVED_PUMPKIN || blockIn == Blocks.JACK_O_LANTERN || blockIn == Blocks.MELON || blockIn == Blocks.PUMPKIN || blockIn.isIn(BlockTags.field_226150_J_);
   }

   @Deprecated
   public boolean isNormalCube(BlockState state, IBlockReader worldIn, BlockPos pos) {
      return state.getMaterial().isOpaque() && state.func_224756_o(worldIn, pos) && !state.canProvidePower();
   }

   @Deprecated
   public boolean func_229869_c_(BlockState p_229869_1_, IBlockReader p_229869_2_, BlockPos p_229869_3_) {
      return this.material.blocksMovement() && p_229869_1_.func_224756_o(p_229869_2_, p_229869_3_);
   }

   @Deprecated
   @OnlyIn(Dist.CLIENT)
   public boolean func_229870_f_(BlockState p_229870_1_, IBlockReader p_229870_2_, BlockPos p_229870_3_) {
      return p_229870_1_.func_229980_m_(p_229870_2_, p_229870_3_);
   }

   @Deprecated
   public boolean allowsMovement(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
      switch(type) {
      case LAND:
         return !state.func_224756_o(worldIn, pos);
      case WATER:
         return worldIn.getFluidState(pos).isTagged(FluidTags.WATER);
      case AIR:
         return !state.func_224756_o(worldIn, pos);
      default:
         return false;
      }
   }

   @Deprecated
   public BlockRenderType getRenderType(BlockState state) {
      return BlockRenderType.MODEL;
   }

   @Deprecated
   public boolean isReplaceable(BlockState state, BlockItemUseContext useContext) {
      return state.getMaterial().isReplaceable() && (useContext.getItem().isEmpty() || useContext.getItem().getItem() != this.asItem());
   }

   @Deprecated
   public boolean func_225541_a_(BlockState p_225541_1_, Fluid p_225541_2_) {
      return this.material.isReplaceable() || !this.material.isSolid();
   }

   @Deprecated
   public float getBlockHardness(BlockState blockState, IBlockReader worldIn, BlockPos pos) {
      return this.blockHardness;
   }

   public boolean ticksRandomly(BlockState state) {
      return this.ticksRandomly;
   }

   @Deprecated //Forge: New State sensitive version.
   public boolean hasTileEntity() {
      return hasTileEntity(getDefaultState());
   }

   @Deprecated
   public boolean needsPostProcessing(BlockState state, IBlockReader worldIn, BlockPos pos) {
      return false;
   }

   @Deprecated
   @OnlyIn(Dist.CLIENT)
   public boolean func_225543_m_(BlockState p_225543_1_) {
      return false;
   }

   @OnlyIn(Dist.CLIENT)
   public static boolean shouldSideBeRendered(BlockState adjacentState, IBlockReader blockState, BlockPos blockAccess, Direction pos) {
      BlockPos blockpos = blockAccess.offset(pos);
      BlockState blockstate = blockState.getBlockState(blockpos);
      if (adjacentState.isSideInvisible(blockstate, pos)) {
         return false;
      } else if (blockstate.isSolid()) {
         Block.RenderSideCacheKey block$rendersidecachekey = new Block.RenderSideCacheKey(adjacentState, blockstate, pos);
         Object2ByteLinkedOpenHashMap<Block.RenderSideCacheKey> object2bytelinkedopenhashmap = SHOULD_SIDE_RENDER_CACHE.get();
         byte b0 = object2bytelinkedopenhashmap.getAndMoveToFirst(block$rendersidecachekey);
         if (b0 != 127) {
            return b0 != 0;
         } else {
            VoxelShape voxelshape = adjacentState.func_215702_a(blockState, blockAccess, pos);
            VoxelShape voxelshape1 = blockstate.func_215702_a(blockState, blockpos, pos.getOpposite());
            boolean flag = VoxelShapes.compare(voxelshape, voxelshape1, IBooleanFunction.ONLY_FIRST);
            if (object2bytelinkedopenhashmap.size() == 2048) {
               object2bytelinkedopenhashmap.removeLastByte();
            }

            object2bytelinkedopenhashmap.putAndMoveToFirst(block$rendersidecachekey, (byte)(flag ? 1 : 0));
            return flag;
         }
      } else {
         return true;
      }
   }

   @Deprecated
   public final boolean isSolid(BlockState state) {
      return this.field_226888_j_;
   }

   @Deprecated
   @OnlyIn(Dist.CLIENT)
   public boolean isSideInvisible(BlockState state, BlockState adjacentBlockState, Direction side) {
      return false;
   }

   @Deprecated
   public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
      return VoxelShapes.fullCube();
   }

   @Deprecated
   public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
      return this.blocksMovement ? state.getShape(worldIn, pos) : VoxelShapes.empty();
   }

   @Deprecated
   public VoxelShape getRenderShape(BlockState state, IBlockReader worldIn, BlockPos pos) {
      return state.getShape(worldIn, pos);
   }

   @Deprecated
   public VoxelShape getRaytraceShape(BlockState state, IBlockReader worldIn, BlockPos pos) {
      return VoxelShapes.empty();
   }

   public static boolean hasSolidSideOnTop(IBlockReader worldIn, BlockPos pos) {
      BlockState blockstate = worldIn.getBlockState(pos);
      return !blockstate.isIn(BlockTags.LEAVES) && !VoxelShapes.compare(blockstate.getCollisionShape(worldIn, pos).project(Direction.UP), field_220083_b, IBooleanFunction.ONLY_SECOND);
   }

   public static boolean hasEnoughSolidSide(IWorldReader worldIn, BlockPos pos, Direction directionIn) {
      BlockState blockstate = worldIn.getBlockState(pos);
      return !blockstate.isIn(BlockTags.LEAVES) && !VoxelShapes.compare(blockstate.getCollisionShape(worldIn, pos).project(directionIn), field_220084_c, IBooleanFunction.ONLY_SECOND);
   }

   public static boolean hasSolidSide(BlockState state, IBlockReader worldIn, BlockPos pos, Direction side) {
      return !state.isIn(BlockTags.LEAVES) && doesSideFillSquare(state.getCollisionShape(worldIn, pos), side);
   }

   public static boolean doesSideFillSquare(VoxelShape shape, Direction side) {
      VoxelShape voxelshape = shape.project(side);
      return isOpaque(voxelshape);
   }

   public static boolean isOpaque(VoxelShape shape) {
      return OPAQUE_CACHE.getUnchecked(shape);
   }

   @Deprecated
   public final boolean isOpaqueCube(BlockState state, IBlockReader worldIn, BlockPos pos) {
      return state.isSolid() ? isOpaque(state.getRenderShape(worldIn, pos)) : false;
   }

   public boolean propagatesSkylightDown(BlockState state, IBlockReader reader, BlockPos pos) {
      return !isOpaque(state.getShape(reader, pos)) && state.getFluidState().isEmpty();
   }

   @Deprecated
   public int getOpacity(BlockState state, IBlockReader worldIn, BlockPos pos) {
      if (state.isOpaqueCube(worldIn, pos)) {
         return worldIn.getMaxLightLevel();
      } else {
         return state.propagatesSkylightDown(worldIn, pos) ? 0 : 1;
      }
   }

   @Deprecated
   public boolean func_220074_n(BlockState state) {
      return false;
   }

   @Deprecated
   public void func_225542_b_(BlockState p_225542_1_, ServerWorld p_225542_2_, BlockPos p_225542_3_, Random p_225542_4_) {
      this.func_225534_a_(p_225542_1_, p_225542_2_, p_225542_3_, p_225542_4_);
   }

   @Deprecated
   public void func_225534_a_(BlockState p_225534_1_, ServerWorld p_225534_2_, BlockPos p_225534_3_, Random p_225534_4_) {
   }

   @OnlyIn(Dist.CLIENT)
   public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
   }

   public void onPlayerDestroy(IWorld worldIn, BlockPos pos, BlockState state) {
   }

   @Deprecated
   public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
      DebugPacketSender.func_218806_a(worldIn, pos);
   }

   public int tickRate(IWorldReader worldIn) {
      return 10;
   }

   @Nullable
   @Deprecated
   public INamedContainerProvider getContainer(BlockState state, World worldIn, BlockPos pos) {
      return null;
   }

   @Deprecated
   public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
   }

   @Deprecated
   public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
      if (state.hasTileEntity() && (state.getBlock() != newState.getBlock() || !newState.hasTileEntity())) {
         worldIn.removeTileEntity(pos);
      }
   }

   @Deprecated
   public float getPlayerRelativeBlockHardness(BlockState state, PlayerEntity player, IBlockReader worldIn, BlockPos pos) {
      float f = state.getBlockHardness(worldIn, pos);
      if (f == -1.0F) {
         return 0.0F;
      } else {
         int i = net.minecraftforge.common.ForgeHooks.canHarvestBlock(state, player, worldIn, pos) ? 30 : 100;
         return player.getDigSpeed(state, pos) / f / (float)i;
      }
   }

   @Deprecated
   public void spawnAdditionalDrops(BlockState state, World worldIn, BlockPos pos, ItemStack stack) {
   }

   public ResourceLocation getLootTable() {
      if (this.lootTable == null) {
         this.lootTable = this.lootTableSupplier.get();
      }

      return this.lootTable;
   }

   @Deprecated
   public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
      ResourceLocation resourcelocation = this.getLootTable();
      if (resourcelocation == LootTables.EMPTY) {
         return Collections.emptyList();
      } else {
         LootContext lootcontext = builder.withParameter(LootParameters.BLOCK_STATE, state).build(LootParameterSets.BLOCK);
         ServerWorld serverworld = lootcontext.func_202879_g();
         LootTable loottable = serverworld.getServer().getLootTableManager().getLootTableFromLocation(resourcelocation);
         return loottable.generate(lootcontext);
      }
   }

   public static List<ItemStack> func_220070_a(BlockState state, ServerWorld worldIn, BlockPos pos, @Nullable TileEntity tileEntityIn) {
      LootContext.Builder lootcontext$builder = (new LootContext.Builder(worldIn)).withRandom(worldIn.rand).withParameter(LootParameters.POSITION, pos).withParameter(LootParameters.TOOL, ItemStack.EMPTY).withNullableParameter(LootParameters.BLOCK_ENTITY, tileEntityIn);
      return state.getDrops(lootcontext$builder);
   }

   public static List<ItemStack> func_220077_a(BlockState state, ServerWorld worldIn, BlockPos pos, @Nullable TileEntity tileEntityIn, @Nullable Entity entityIn, ItemStack stack) {
      LootContext.Builder lootcontext$builder = (new LootContext.Builder(worldIn)).withRandom(worldIn.rand).withParameter(LootParameters.POSITION, pos).withParameter(LootParameters.TOOL, stack).withNullableParameter(LootParameters.THIS_ENTITY, entityIn).withNullableParameter(LootParameters.BLOCK_ENTITY, tileEntityIn);
      return state.getDrops(lootcontext$builder);
   }

   public static void spawnDrops(BlockState state, World worldIn, BlockPos pos) {
      if (worldIn instanceof ServerWorld) {
         func_220070_a(state, (ServerWorld)worldIn, pos, (TileEntity)null).forEach((p_220079_2_) -> {
            spawnAsEntity(worldIn, pos, p_220079_2_);
         });
      }

      state.spawnAdditionalDrops(worldIn, pos, ItemStack.EMPTY);
   }

   public static void spawnDrops(BlockState state, World worldIn, BlockPos pos, @Nullable TileEntity tileEntityIn) {
      if (worldIn instanceof ServerWorld) {
         func_220070_a(state, (ServerWorld)worldIn, pos, tileEntityIn).forEach((p_220061_2_) -> {
            spawnAsEntity(worldIn, pos, p_220061_2_);
         });
      }

      state.spawnAdditionalDrops(worldIn, pos, ItemStack.EMPTY);
   }

   public static void spawnDrops(BlockState state, World worldIn, BlockPos pos, @Nullable TileEntity tileEntityIn, Entity entityIn, ItemStack stack) {
      if (worldIn instanceof ServerWorld) {
         func_220077_a(state, (ServerWorld)worldIn, pos, tileEntityIn, entityIn, stack).forEach((p_220057_2_) -> {
            spawnAsEntity(worldIn, pos, p_220057_2_);
         });
      }

      state.spawnAdditionalDrops(worldIn, pos, stack);
   }

   public static void spawnAsEntity(World worldIn, BlockPos pos, ItemStack stack) {
      if (!worldIn.isRemote && !stack.isEmpty() && worldIn.getGameRules().getBoolean(GameRules.DO_TILE_DROPS) && !worldIn.restoringBlockSnapshots) { // do not drop items while restoring blockstates, prevents item dupe
         float f = 0.5F;
         double d0 = (double)(worldIn.rand.nextFloat() * 0.5F) + 0.25D;
         double d1 = (double)(worldIn.rand.nextFloat() * 0.5F) + 0.25D;
         double d2 = (double)(worldIn.rand.nextFloat() * 0.5F) + 0.25D;
         ItemEntity itementity = new ItemEntity(worldIn, (double)pos.getX() + d0, (double)pos.getY() + d1, (double)pos.getZ() + d2, stack);
         itementity.setDefaultPickupDelay();
         worldIn.addEntity(itementity);
      }
   }

   public void dropXpOnBlockBreak(World worldIn, BlockPos pos, int amount) {
      if (!worldIn.isRemote && worldIn.getGameRules().getBoolean(GameRules.DO_TILE_DROPS) && !worldIn.restoringBlockSnapshots) { // do not drop items while restoring blockstates, prevents item dupe
         while(amount > 0) {
            int i = ExperienceOrbEntity.getXPSplit(amount);
            amount -= i;
            worldIn.addEntity(new ExperienceOrbEntity(worldIn, (double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, i));
         }
      }

   }

   @Deprecated //Forge: State sensitive version
   public float getExplosionResistance() {
      return this.blockResistance;
   }

   public void onExplosionDestroy(World worldIn, BlockPos pos, Explosion explosionIn) {
   }

   @Deprecated
   public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
      return true;
   }

   @Deprecated
   public ActionResultType func_225533_a_(BlockState p_225533_1_, World p_225533_2_, BlockPos p_225533_3_, PlayerEntity p_225533_4_, Hand p_225533_5_, BlockRayTraceResult p_225533_6_) {
      return ActionResultType.PASS;
   }

   public void onEntityWalk(World worldIn, BlockPos pos, Entity entityIn) {
   }

   @Nullable
   public BlockState getStateForPlacement(BlockItemUseContext context) {
      return this.getDefaultState();
   }

   @Deprecated
   public void onBlockClicked(BlockState state, World worldIn, BlockPos pos, PlayerEntity player) {
   }

   @Deprecated
   public int getWeakPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
      return 0;
   }

   @Deprecated
   public boolean canProvidePower(BlockState state) {
      return false;
   }

   @Deprecated
   public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn) {
   }

   @Deprecated
   public int getStrongPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
      return 0;
   }

   public void harvestBlock(World worldIn, PlayerEntity player, BlockPos pos, BlockState state, @Nullable TileEntity te, ItemStack stack) {
      player.addStat(Stats.BLOCK_MINED.get(this));
      player.addExhaustion(0.005F);
      spawnDrops(state, worldIn, pos, te, player, stack);
   }

   public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
   }

   public boolean canSpawnInBlock() {
      return !this.material.isSolid() && !this.material.isLiquid();
   }

   @OnlyIn(Dist.CLIENT)
   public ITextComponent getNameTextComponent() {
      return new TranslationTextComponent(this.getTranslationKey());
   }

   public String getTranslationKey() {
      if (this.translationKey == null) {
         this.translationKey = Util.makeTranslationKey("block", Registry.BLOCK.getKey(this));
      }

      return this.translationKey;
   }

   @Deprecated
   public boolean eventReceived(BlockState state, World worldIn, BlockPos pos, int id, int param) {
      return false;
   }

   @Deprecated
   public PushReaction getPushReaction(BlockState state) {
      return this.material.getPushReaction();
   }

   @Deprecated
   @OnlyIn(Dist.CLIENT)
   public float getAmbientOcclusionLightValue(BlockState state, IBlockReader worldIn, BlockPos pos) {
      return state.func_224756_o(worldIn, pos) ? 0.2F : 1.0F;
   }

   public void onFallenUpon(World worldIn, BlockPos pos, Entity entityIn, float fallDistance) {
      entityIn.func_225503_b_(fallDistance, 1.0F);
   }

   public void onLanded(IBlockReader worldIn, Entity entityIn) {
      entityIn.setMotion(entityIn.getMotion().mul(1.0D, 0.0D, 1.0D));
   }

   @Deprecated // Forge: Use more sensitive version below: getPickBlock
   public ItemStack getItem(IBlockReader worldIn, BlockPos pos, BlockState state) {
      return new ItemStack(this);
   }

   public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
      items.add(new ItemStack(this));
   }

   @Deprecated
   public IFluidState getFluidState(BlockState state) {
      return Fluids.EMPTY.getDefaultState();
   }

   @Deprecated //Forge: Use more sensitive version
   public float getSlipperiness() {
      return this.slipperiness;
   }

   public float func_226891_m_() {
      return this.field_226886_f_;
   }

   public float func_226892_n_() {
      return this.field_226887_g_;
   }

   @Deprecated
   @OnlyIn(Dist.CLIENT)
   public long getPositionRandom(BlockState state, BlockPos pos) {
      return MathHelper.getPositionRandom(pos);
   }

   public void onProjectileCollision(World worldIn, BlockState state, BlockRayTraceResult hit, Entity projectile) {
   }

   public void onBlockHarvested(World worldIn, BlockPos pos, BlockState state, PlayerEntity player) {
      worldIn.playEvent(player, 2001, pos, getStateId(state));
   }

   public void fillWithRain(World worldIn, BlockPos pos) {
   }

   @Deprecated //Forge: Use more sensitive version
   public boolean canDropFromExplosion(Explosion explosionIn) {
      return true;
   }

   @Deprecated
   public boolean hasComparatorInputOverride(BlockState state) {
      return false;
   }

   @Deprecated
   public int getComparatorInputOverride(BlockState blockState, World worldIn, BlockPos pos) {
      return 0;
   }

   protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
   }

   public StateContainer<Block, BlockState> getStateContainer() {
      return this.stateContainer;
   }

   protected final void setDefaultState(BlockState state) {
      this.defaultState = state;
   }

   public final BlockState getDefaultState() {
      return this.defaultState;
   }

   public Block.OffsetType getOffsetType() {
      return Block.OffsetType.NONE;
   }

   @Deprecated
   public Vec3d getOffset(BlockState state, IBlockReader worldIn, BlockPos pos) {
      Block.OffsetType block$offsettype = this.getOffsetType();
      if (block$offsettype == Block.OffsetType.NONE) {
         return Vec3d.ZERO;
      } else {
         long i = MathHelper.getCoordinateRandom(pos.getX(), 0, pos.getZ());
         return new Vec3d(((double)((float)(i & 15L) / 15.0F) - 0.5D) * 0.5D, block$offsettype == Block.OffsetType.XYZ ? ((double)((float)(i >> 4 & 15L) / 15.0F) - 1.0D) * 0.2D : 0.0D, ((double)((float)(i >> 8 & 15L) / 15.0F) - 0.5D) * 0.5D);
      }
   }

   @Deprecated //Forge: Use more sensitive version {@link IForgeBlockState#getSoundType(IWorldReader, BlockPos, Entity) }
   public SoundType getSoundType(BlockState state) {
      return this.soundType;
   }

   public Item asItem() {
      if (this.item == null) {
         this.item = Item.getItemFromBlock(this);
      }

      return this.item.delegate.get(); //Forge: Vanilla caches the items, update with registry replacements.
   }

   public boolean isVariableOpacity() {
      return this.variableOpacity;
   }

   public String toString() {
      return "Block{" + getRegistryName() + "}";
   }

   @OnlyIn(Dist.CLIENT)
   public void addInformation(ItemStack stack, @Nullable IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
   }

   /* ======================================== FORGE START =====================================*/
   protected Random RANDOM = new Random();
   private net.minecraftforge.common.ToolType harvestTool;
   private int harvestLevel;
   private final net.minecraftforge.common.util.ReverseTagWrapper<Block> reverseTags = new net.minecraftforge.common.util.ReverseTagWrapper<>(this, BlockTags::getGeneration, BlockTags::getCollection);
   private final java.util.function.Supplier<ResourceLocation> lootTableSupplier;

   @Override
   public float getSlipperiness(BlockState state, IWorldReader world, BlockPos pos, @Nullable Entity entity) {
      return this.slipperiness;
   }

   @Nullable
   @Override
   public net.minecraftforge.common.ToolType getHarvestTool(BlockState state) {
      return harvestTool; //TODO: RE-Evaluate
   }

   @Override
   public int getHarvestLevel(BlockState state) {
      return harvestLevel; //TODO: RE-Evaluate
   }

   @Override
   public boolean canSustainPlant(BlockState state, IBlockReader world, BlockPos pos, Direction facing, net.minecraftforge.common.IPlantable plantable) {
       BlockState plant = plantable.getPlant(world, pos.offset(facing));
       net.minecraftforge.common.PlantType type = plantable.getPlantType(world, pos.offset(facing));

       if (plant.getBlock() == Blocks.CACTUS)
           return this.getBlock() == Blocks.CACTUS || this.getBlock() == Blocks.SAND || this.getBlock() == Blocks.RED_SAND;

       if (plant.getBlock() == Blocks.SUGAR_CANE && this == Blocks.SUGAR_CANE)
           return true;

       if (plantable instanceof BushBlock && ((BushBlock)plantable).isValidGround(state, world, pos))
           return true;

       switch (type) {
           case Desert: return this.getBlock() == Blocks.SAND || this.getBlock() == Blocks.TERRACOTTA || this.getBlock() instanceof GlazedTerracottaBlock;
           case Nether: return this.getBlock() == Blocks.SOUL_SAND;
           case Crop:   return this.getBlock() == Blocks.FARMLAND;
           case Cave:   return Block.hasSolidSide(state, world, pos, Direction.UP);
           case Plains: return this.getBlock() == Blocks.GRASS_BLOCK || net.minecraftforge.common.Tags.Blocks.DIRT.func_199685_a_(this) || this.getBlock() == Blocks.FARMLAND;
           case Water:  return state.getMaterial() == Material.WATER; //&& state.getValue(BlockLiquidWrapper)
           case Beach:
               boolean isBeach = this.getBlock() == Blocks.GRASS_BLOCK || net.minecraftforge.common.Tags.Blocks.DIRT.func_199685_a_(this) || this.getBlock() == Blocks.SAND;
               boolean hasWater = (world.getBlockState(pos.east()).getMaterial() == Material.WATER ||
                       world.getBlockState(pos.west()).getMaterial() == Material.WATER ||
                       world.getBlockState(pos.north()).getMaterial() == Material.WATER ||
                       world.getBlockState(pos.south()).getMaterial() == Material.WATER);
               return isBeach && hasWater;
       }
       return false;
   }

   @Override
   public final java.util.Set<ResourceLocation> getTags() {
      return reverseTags.getTagNames();
   }

   static {
      net.minecraftforge.common.ForgeHooks.setBlockToolSetter((block, tool, level) -> {
         block.harvestTool = tool;
         block.harvestLevel = level;
      });
   }
   /* ========================================= FORGE END ======================================*/

   public static enum OffsetType {
      NONE,
      XZ,
      XYZ;
   }

   public static class Properties {
      private Material material;
      private MaterialColor mapColor;
      private boolean blocksMovement = true;
      private SoundType soundType = SoundType.STONE;
      private int lightValue;
      private float resistance;
      private float hardness;
      private boolean ticksRandomly;
      private float slipperiness = 0.6F;
      private float field_226893_j_ = 1.0F;
      private float field_226894_k_ = 1.0F;
      /** Sets loot table information */
      private ResourceLocation lootTable;
      private boolean field_226895_m_ = true;
      private boolean variableOpacity;
      private int harvestLevel = -1;
      private net.minecraftforge.common.ToolType harvestTool;
      private java.util.function.Supplier<ResourceLocation> lootTableSupplier;

      private Properties(Material materialIn, MaterialColor mapColorIn) {
         this.material = materialIn;
         this.mapColor = mapColorIn;
      }

      public static Block.Properties create(Material materialIn) {
         return create(materialIn, materialIn.getColor());
      }

      public static Block.Properties create(Material materialIn, DyeColor color) {
         return create(materialIn, color.getMapColor());
      }

      public static Block.Properties create(Material materialIn, MaterialColor mapColorIn) {
         return new Block.Properties(materialIn, mapColorIn);
      }

      public static Block.Properties from(Block blockIn) {
         Block.Properties block$properties = new Block.Properties(blockIn.material, blockIn.materialColor);
         block$properties.material = blockIn.material;
         block$properties.hardness = blockIn.blockHardness;
         block$properties.resistance = blockIn.blockResistance;
         block$properties.blocksMovement = blockIn.blocksMovement;
         block$properties.ticksRandomly = blockIn.ticksRandomly;
         block$properties.lightValue = blockIn.lightValue;
         block$properties.mapColor = blockIn.materialColor;
         block$properties.soundType = blockIn.soundType;
         block$properties.slipperiness = blockIn.getSlipperiness();
         block$properties.field_226893_j_ = blockIn.func_226891_m_();
         block$properties.variableOpacity = blockIn.variableOpacity;
         block$properties.field_226895_m_ = blockIn.field_226888_j_;
         block$properties.harvestLevel = blockIn.harvestLevel;
         block$properties.harvestTool = blockIn.harvestTool;
         return block$properties;
      }

      public Block.Properties doesNotBlockMovement() {
         this.blocksMovement = false;
         this.field_226895_m_ = false;
         return this;
      }

      public Block.Properties func_226896_b_() {
         this.field_226895_m_ = false;
         return this;
      }

      public Block.Properties slipperiness(float slipperinessIn) {
         this.slipperiness = slipperinessIn;
         return this;
      }

      public Block.Properties func_226897_b_(float p_226897_1_) {
         this.field_226893_j_ = p_226897_1_;
         return this;
      }

      public Block.Properties func_226898_c_(float p_226898_1_) {
         this.field_226894_k_ = p_226898_1_;
         return this;
      }

      public Block.Properties sound(SoundType soundTypeIn) {
         this.soundType = soundTypeIn;
         return this;
      }

      public Block.Properties lightValue(int lightValueIn) {
         this.lightValue = lightValueIn;
         return this;
      }

      public Block.Properties hardnessAndResistance(float hardnessIn, float resistanceIn) {
         this.hardness = hardnessIn;
         this.resistance = Math.max(0.0F, resistanceIn);
         return this;
      }

      protected Block.Properties zeroHardnessAndResistance() {
         return this.hardnessAndResistance(0.0F);
      }

      public Block.Properties hardnessAndResistance(float hardnessAndResistance) {
         this.hardnessAndResistance(hardnessAndResistance, hardnessAndResistance);
         return this;
      }

      public Block.Properties tickRandomly() {
         this.ticksRandomly = true;
         return this;
      }

      public Block.Properties variableOpacity() {
         this.variableOpacity = true;
         return this;
      }

      public Block.Properties harvestLevel(int harvestLevel) {
          this.harvestLevel = harvestLevel;
          return this;
      }

      public Block.Properties harvestTool(net.minecraftforge.common.ToolType harvestTool) {
          this.harvestTool = harvestTool;
          return this;
      }

      public Block.Properties noDrops() {
         this.lootTable = LootTables.EMPTY;
         return this;
      }

      public Block.Properties lootFrom(Block blockIn) {
         this.lootTableSupplier = () -> blockIn.delegate.get().getLootTable();
         return this;
      }
   }

   public static final class RenderSideCacheKey {
      private final BlockState state;
      private final BlockState adjacentState;
      private final Direction side;

      public RenderSideCacheKey(BlockState state, BlockState adjacentState, Direction side) {
         this.state = state;
         this.adjacentState = adjacentState;
         this.side = side;
      }

      public boolean equals(Object p_equals_1_) {
         if (this == p_equals_1_) {
            return true;
         } else if (!(p_equals_1_ instanceof Block.RenderSideCacheKey)) {
            return false;
         } else {
            Block.RenderSideCacheKey block$rendersidecachekey = (Block.RenderSideCacheKey)p_equals_1_;
            return this.state == block$rendersidecachekey.state && this.adjacentState == block$rendersidecachekey.adjacentState && this.side == block$rendersidecachekey.side;
         }
      }

      public int hashCode() {
         int i = this.state.hashCode();
         i = 31 * i + this.adjacentState.hashCode();
         i = 31 * i + this.side.hashCode();
         return i;
      }
   }
}