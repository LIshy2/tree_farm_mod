package net.minecraft.block;

import java.util.Random;
import net.minecraft.entity.EntityType;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class LeavesBlock extends Block implements net.minecraftforge.common.IShearable {
   public static final IntegerProperty DISTANCE = BlockStateProperties.DISTANCE_1_7;
   public static final BooleanProperty PERSISTENT = BlockStateProperties.PERSISTENT;

   public LeavesBlock(Block.Properties properties) {
      super(properties);
      this.setDefaultState(this.stateContainer.getBaseState().with(DISTANCE, Integer.valueOf(7)).with(PERSISTENT, Boolean.valueOf(false)));
   }

   public boolean ticksRandomly(BlockState state) {
      return state.get(DISTANCE) == 7 && !state.get(PERSISTENT);
   }

   public void func_225542_b_(BlockState p_225542_1_, ServerWorld p_225542_2_, BlockPos p_225542_3_, Random p_225542_4_) {
      if (!p_225542_1_.get(PERSISTENT) && p_225542_1_.get(DISTANCE) == 7) {
         spawnDrops(p_225542_1_, p_225542_2_, p_225542_3_);
         p_225542_2_.removeBlock(p_225542_3_, false);
      }

   }

   public void func_225534_a_(BlockState p_225534_1_, ServerWorld p_225534_2_, BlockPos p_225534_3_, Random p_225534_4_) {
      p_225534_2_.setBlockState(p_225534_3_, updateDistance(p_225534_1_, p_225534_2_, p_225534_3_), 3);
   }

   public int getOpacity(BlockState state, IBlockReader worldIn, BlockPos pos) {
      return 1;
   }

   public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
      int i = getDistance(facingState) + 1;
      if (i != 1 || stateIn.get(DISTANCE) != i) {
         worldIn.getPendingBlockTicks().scheduleTick(currentPos, this, 1);
      }

      return stateIn;
   }

   private static BlockState updateDistance(BlockState state, IWorld worldIn, BlockPos pos) {
      int i = 7;

      try (BlockPos.PooledMutable blockpos$pooledmutable = BlockPos.PooledMutable.func_185346_s()) {
         for(Direction direction : Direction.values()) {
            blockpos$pooledmutable.func_189533_g(pos).func_189536_c(direction);
            i = Math.min(i, getDistance(worldIn.getBlockState(blockpos$pooledmutable)) + 1);
            if (i == 1) {
               break;
            }
         }
      }

      return state.with(DISTANCE, Integer.valueOf(i));
   }

   private static int getDistance(BlockState neighbor) {
      if (BlockTags.LOGS.func_199685_a_(neighbor.getBlock())) {
         return 0;
      } else {
         return neighbor.getBlock() instanceof LeavesBlock ? neighbor.get(DISTANCE) : 7;
      }
   }

   @OnlyIn(Dist.CLIENT)
   public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
      if (worldIn.isRainingAt(pos.up())) {
         if (rand.nextInt(15) == 1) {
            BlockPos blockpos = pos.func_177977_b();
            BlockState blockstate = worldIn.getBlockState(blockpos);
            if (!blockstate.isSolid() || !blockstate.func_224755_d(worldIn, blockpos, Direction.UP)) {
               double d0 = (double)((float)pos.getX() + rand.nextFloat());
               double d1 = (double)pos.getY() - 0.05D;
               double d2 = (double)((float)pos.getZ() + rand.nextFloat());
               worldIn.addParticle(ParticleTypes.DRIPPING_WATER, d0, d1, d2, 0.0D, 0.0D, 0.0D);
            }
         }
      }
   }

   public boolean func_229869_c_(BlockState p_229869_1_, IBlockReader p_229869_2_, BlockPos p_229869_3_) {
      return false;
   }

   public boolean canEntitySpawn(BlockState state, IBlockReader worldIn, BlockPos pos, EntityType<?> type) {
      return type == EntityType.OCELOT || type == EntityType.PARROT;
   }

   protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
      builder.add(DISTANCE, PERSISTENT);
   }

   public BlockState getStateForPlacement(BlockItemUseContext context) {
      return updateDistance(this.getDefaultState().with(PERSISTENT, Boolean.valueOf(true)), context.getWorld(), context.getPos());
   }
}