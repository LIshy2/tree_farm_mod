package net.minecraft.client.multiplayer;

import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.BooleanSupplier;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.SectionPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.LightType;
import net.minecraft.world.biome.BiomeContainer;
import net.minecraft.world.chunk.AbstractChunkProvider;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.EmptyChunk;
import net.minecraft.world.lighting.WorldLightManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class ClientChunkProvider extends AbstractChunkProvider {
   private static final Logger LOGGER = LogManager.getLogger();
   private final Chunk empty;
   private final WorldLightManager lightManager;
   private volatile ClientChunkProvider.ChunkArray array;
   private final ClientWorld world;

   public ClientChunkProvider(ClientWorld clientWorldIn, int viewDistance) {
      this.world = clientWorldIn;
      this.empty = new EmptyChunk(clientWorldIn, new ChunkPos(0, 0));
      this.lightManager = new WorldLightManager(this, true, clientWorldIn.getDimension().hasSkyLight());
      this.array = new ClientChunkProvider.ChunkArray(adjustViewDistance(viewDistance));
   }

   public WorldLightManager func_212863_j_() {
      return this.lightManager;
   }

   private static boolean isValid(@Nullable Chunk chunkIn, int x, int z) {
      if (chunkIn == null) {
         return false;
      } else {
         ChunkPos chunkpos = chunkIn.getPos();
         return chunkpos.x == x && chunkpos.z == z;
      }
   }

   public void unloadChunk(int x, int z) {
      if (this.array.inView(x, z)) {
         int i = this.array.getIndex(x, z);
         Chunk chunk = this.array.get(i);
         if (isValid(chunk, x, z)) {
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.world.ChunkEvent.Unload(chunk));
            this.array.unload(i, chunk, (Chunk)null);
         }

      }
   }

   @Nullable
   public Chunk getChunk(int chunkX, int chunkZ, ChunkStatus requiredStatus, boolean load) {
      if (this.array.inView(chunkX, chunkZ)) {
         Chunk chunk = this.array.get(this.array.getIndex(chunkX, chunkZ));
         if (isValid(chunk, chunkX, chunkZ)) {
            return chunk;
         }
      }

      return load ? this.empty : null;
   }

   public IBlockReader func_212864_k_() {
      return this.world;
   }

   @Nullable
   public Chunk func_228313_a_(int p_228313_1_, int p_228313_2_, @Nullable BiomeContainer p_228313_3_, PacketBuffer p_228313_4_, CompoundNBT p_228313_5_, int p_228313_6_) {
      if (!this.array.inView(p_228313_1_, p_228313_2_)) {
         LOGGER.warn("Ignoring chunk since it's not in the view range: {}, {}", p_228313_1_, p_228313_2_);
         return null;
      } else {
         int i = this.array.getIndex(p_228313_1_, p_228313_2_);
         Chunk chunk = this.array.chunks.get(i);
         if (!isValid(chunk, p_228313_1_, p_228313_2_)) {
            if (p_228313_3_ == null) {
               LOGGER.warn("Ignoring chunk since we don't have complete data: {}, {}", p_228313_1_, p_228313_2_);
               return null;
            }

            chunk = new Chunk(this.world, new ChunkPos(p_228313_1_, p_228313_2_), p_228313_3_);
            chunk.func_227073_a_(p_228313_3_, p_228313_4_, p_228313_5_, p_228313_6_);
            this.array.replace(i, chunk);
         } else {
            chunk.func_227073_a_(p_228313_3_, p_228313_4_, p_228313_5_, p_228313_6_);
         }

         ChunkSection[] achunksection = chunk.getSections();
         WorldLightManager worldlightmanager = this.func_212863_j_();
         worldlightmanager.func_215571_a(new ChunkPos(p_228313_1_, p_228313_2_), true);

         for(int j = 0; j < achunksection.length; ++j) {
            ChunkSection chunksection = achunksection[j];
            worldlightmanager.updateSectionStatus(SectionPos.of(p_228313_1_, j, p_228313_2_), ChunkSection.isEmpty(chunksection));
         }

         this.world.func_228323_e_(p_228313_1_, p_228313_2_);
         net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.world.ChunkEvent.Load(chunk));
         return chunk;
      }
   }

   public void tick(BooleanSupplier hasTimeLeft) {
   }

   public void setCenter(int x, int z) {
      this.array.centerX = x;
      this.array.centerZ = z;
   }

   public void setViewDistance(int viewDistance) {
      int i = this.array.viewDistance;
      int j = adjustViewDistance(viewDistance);
      if (i != j) {
         ClientChunkProvider.ChunkArray clientchunkprovider$chunkarray = new ClientChunkProvider.ChunkArray(j);
         clientchunkprovider$chunkarray.centerX = this.array.centerX;
         clientchunkprovider$chunkarray.centerZ = this.array.centerZ;

         for(int k = 0; k < this.array.chunks.length(); ++k) {
            Chunk chunk = this.array.chunks.get(k);
            if (chunk != null) {
               ChunkPos chunkpos = chunk.getPos();
               if (clientchunkprovider$chunkarray.inView(chunkpos.x, chunkpos.z)) {
                  clientchunkprovider$chunkarray.replace(clientchunkprovider$chunkarray.getIndex(chunkpos.x, chunkpos.z), chunk);
               }
            }
         }

         this.array = clientchunkprovider$chunkarray;
      }

   }

   private static int adjustViewDistance(int p_217254_0_) {
      return Math.max(2, p_217254_0_) + 3;
   }

   public String makeString() {
      return "Client Chunk Cache: " + this.array.chunks.length() + ", " + this.getLoadedChunksCount();
   }

   public int getLoadedChunksCount() {
      return this.array.loaded;
   }

   public void markLightChanged(LightType type, SectionPos pos) {
      Minecraft.getInstance().worldRenderer.markForRerender(pos.getSectionX(), pos.getSectionY(), pos.getSectionZ());
   }

   public boolean canTick(BlockPos pos) {
      return this.chunkExists(pos.getX() >> 4, pos.getZ() >> 4);
   }

   public boolean isChunkLoaded(ChunkPos pos) {
      return this.chunkExists(pos.x, pos.z);
   }

   public boolean isChunkLoaded(Entity entityIn) {
      return this.chunkExists(MathHelper.floor(entityIn.func_226277_ct_()) >> 4, MathHelper.floor(entityIn.func_226281_cx_()) >> 4);
   }

   @OnlyIn(Dist.CLIENT)
   final class ChunkArray {
      private final AtomicReferenceArray<Chunk> chunks;
      private final int viewDistance;
      private final int sideLength;
      private volatile int centerX;
      private volatile int centerZ;
      private int loaded;

      private ChunkArray(int viewDistanceIn) {
         this.viewDistance = viewDistanceIn;
         this.sideLength = viewDistanceIn * 2 + 1;
         this.chunks = new AtomicReferenceArray<>(this.sideLength * this.sideLength);
      }

      private int getIndex(int x, int z) {
         return Math.floorMod(z, this.sideLength) * this.sideLength + Math.floorMod(x, this.sideLength);
      }

      protected void replace(int chunkIndex, @Nullable Chunk chunkIn) {
         Chunk chunk = this.chunks.getAndSet(chunkIndex, chunkIn);
         if (chunk != null) {
            --this.loaded;
            ClientChunkProvider.this.world.onChunkUnloaded(chunk);
         }

         if (chunkIn != null) {
            ++this.loaded;
         }

      }

      protected Chunk unload(int chunkIndex, Chunk chunkIn, @Nullable Chunk replaceWith) {
         if (this.chunks.compareAndSet(chunkIndex, chunkIn, replaceWith) && replaceWith == null) {
            --this.loaded;
         }

         ClientChunkProvider.this.world.onChunkUnloaded(chunkIn);
         return chunkIn;
      }

      private boolean inView(int x, int z) {
         return Math.abs(x - this.centerX) <= this.viewDistance && Math.abs(z - this.centerZ) <= this.viewDistance;
      }

      @Nullable
      protected Chunk get(int chunkIndex) {
         return this.chunks.get(chunkIndex);
      }
   }
}