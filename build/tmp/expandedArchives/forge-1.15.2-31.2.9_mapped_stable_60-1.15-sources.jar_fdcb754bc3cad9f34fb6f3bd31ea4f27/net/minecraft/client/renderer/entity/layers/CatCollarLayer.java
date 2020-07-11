package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.model.CatModel;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CatCollarLayer extends LayerRenderer<CatEntity, CatModel<CatEntity>> {
   private static final ResourceLocation CAT_COLLAR = new ResourceLocation("textures/entity/cat/cat_collar.png");
   private final CatModel<CatEntity> model = new CatModel<>(0.01F);

   public CatCollarLayer(IEntityRenderer<CatEntity, CatModel<CatEntity>> p_i50948_1_) {
      super(p_i50948_1_);
   }

   public void func_225628_a_(MatrixStack p_225628_1_, IRenderTypeBuffer p_225628_2_, int p_225628_3_, CatEntity p_225628_4_, float p_225628_5_, float p_225628_6_, float p_225628_7_, float p_225628_8_, float p_225628_9_, float p_225628_10_) {
      if (p_225628_4_.isTamed()) {
         float[] afloat = p_225628_4_.getCollarColor().getColorComponentValues();
         func_229140_a_(this.getEntityModel(), this.model, CAT_COLLAR, p_225628_1_, p_225628_2_, p_225628_3_, p_225628_4_, p_225628_5_, p_225628_6_, p_225628_8_, p_225628_9_, p_225628_10_, p_225628_7_, afloat[0], afloat[1], afloat[2]);
      }
   }
}