package net.minecraft.world.server;

import java.util.Comparator;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3i;

public class TicketType<T> {
   private final String name;
   private final Comparator<T> typeComparator;
   private final long lifespan;
   public static final TicketType<Unit> START = func_219484_a("start", (p_219486_0_, p_219486_1_) -> {
      return 0;
   });
   public static final TicketType<Unit> DRAGON = func_219484_a("dragon", (p_219485_0_, p_219485_1_) -> {
      return 0;
   });
   public static final TicketType<ChunkPos> PLAYER = func_219484_a("player", Comparator.comparingLong(ChunkPos::asLong));
   public static final TicketType<ChunkPos> FORCED = func_219484_a("forced", Comparator.comparingLong(ChunkPos::asLong));
   public static final TicketType<ChunkPos> LIGHT = func_219484_a("light", Comparator.comparingLong(ChunkPos::asLong));
   public static final TicketType<BlockPos> PORTAL = func_223183_a("portal", Vec3i::compareTo, 300);
   public static final TicketType<Integer> POST_TELEPORT = func_223183_a("post_teleport", Integer::compareTo, 5);
   public static final TicketType<ChunkPos> UNKNOWN = func_223183_a("unknown", Comparator.comparingLong(ChunkPos::asLong), 1);

   public static <T> TicketType<T> func_219484_a(String nameIn, Comparator<T> comparator) {
      return new TicketType<>(nameIn, comparator, 0L);
   }

   public static <T> TicketType<T> func_223183_a(String nameIn, Comparator<T> comparator, int lifespanIn) {
      return new TicketType<>(nameIn, comparator, (long)lifespanIn);
   }

   protected TicketType(String nameIn, Comparator<T> comparator, long lifespanIn) {
      this.name = nameIn;
      this.typeComparator = comparator;
      this.lifespan = lifespanIn;
   }

   public String toString() {
      return this.name;
   }

   public Comparator<T> getComparator() {
      return this.typeComparator;
   }

   public long getLifespan() {
      return this.lifespan;
   }
}