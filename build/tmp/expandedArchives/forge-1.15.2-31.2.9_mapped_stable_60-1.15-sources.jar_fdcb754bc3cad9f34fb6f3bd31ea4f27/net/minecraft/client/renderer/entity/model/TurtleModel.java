package net.minecraft.client.renderer.entity.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.passive.TurtleEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TurtleModel<T extends TurtleEntity> extends QuadrupedModel<T> {
   private final ModelRenderer pregnant;

   public TurtleModel(float p_i48834_1_) {
      super(12, p_i48834_1_, true, 120.0F, 0.0F, 9.0F, 6.0F, 120);
      this.textureWidth = 128;
      this.textureHeight = 64;
      this.headModel = new ModelRenderer(this, 3, 0);
      this.headModel.func_228301_a_(-3.0F, -1.0F, -3.0F, 6.0F, 5.0F, 6.0F, 0.0F);
      this.headModel.setRotationPoint(0.0F, 19.0F, -10.0F);
      this.body = new ModelRenderer(this);
      this.body.func_78784_a(7, 37).func_228301_a_(-9.5F, 3.0F, -10.0F, 19.0F, 20.0F, 6.0F, 0.0F);
      this.body.func_78784_a(31, 1).func_228301_a_(-5.5F, 3.0F, -13.0F, 11.0F, 18.0F, 3.0F, 0.0F);
      this.body.setRotationPoint(0.0F, 11.0F, -10.0F);
      this.pregnant = new ModelRenderer(this);
      this.pregnant.func_78784_a(70, 33).func_228301_a_(-4.5F, 3.0F, -14.0F, 9.0F, 18.0F, 1.0F, 0.0F);
      this.pregnant.setRotationPoint(0.0F, 11.0F, -10.0F);
      int i = 1;
      this.legBackRight = new ModelRenderer(this, 1, 23);
      this.legBackRight.func_228301_a_(-2.0F, 0.0F, 0.0F, 4.0F, 1.0F, 10.0F, 0.0F);
      this.legBackRight.setRotationPoint(-3.5F, 22.0F, 11.0F);
      this.legBackLeft = new ModelRenderer(this, 1, 12);
      this.legBackLeft.func_228301_a_(-2.0F, 0.0F, 0.0F, 4.0F, 1.0F, 10.0F, 0.0F);
      this.legBackLeft.setRotationPoint(3.5F, 22.0F, 11.0F);
      this.legFrontRight = new ModelRenderer(this, 27, 30);
      this.legFrontRight.func_228301_a_(-13.0F, 0.0F, -2.0F, 13.0F, 1.0F, 5.0F, 0.0F);
      this.legFrontRight.setRotationPoint(-5.0F, 21.0F, -4.0F);
      this.legFrontLeft = new ModelRenderer(this, 27, 24);
      this.legFrontLeft.func_228301_a_(0.0F, 0.0F, -2.0F, 13.0F, 1.0F, 5.0F, 0.0F);
      this.legFrontLeft.setRotationPoint(5.0F, 21.0F, -4.0F);
   }

   protected Iterable<ModelRenderer> func_225600_b_() {
      return Iterables.concat(super.func_225600_b_(), ImmutableList.of(this.pregnant));
   }

   public void func_225597_a_(T p_225597_1_, float p_225597_2_, float p_225597_3_, float p_225597_4_, float p_225597_5_, float p_225597_6_) {
      super.func_225597_a_(p_225597_1_, p_225597_2_, p_225597_3_, p_225597_4_, p_225597_5_, p_225597_6_);
      this.legBackRight.rotateAngleX = MathHelper.cos(p_225597_2_ * 0.6662F * 0.6F) * 0.5F * p_225597_3_;
      this.legBackLeft.rotateAngleX = MathHelper.cos(p_225597_2_ * 0.6662F * 0.6F + (float)Math.PI) * 0.5F * p_225597_3_;
      this.legFrontRight.rotateAngleZ = MathHelper.cos(p_225597_2_ * 0.6662F * 0.6F + (float)Math.PI) * 0.5F * p_225597_3_;
      this.legFrontLeft.rotateAngleZ = MathHelper.cos(p_225597_2_ * 0.6662F * 0.6F) * 0.5F * p_225597_3_;
      this.legFrontRight.rotateAngleX = 0.0F;
      this.legFrontLeft.rotateAngleX = 0.0F;
      this.legFrontRight.rotateAngleY = 0.0F;
      this.legFrontLeft.rotateAngleY = 0.0F;
      this.legBackRight.rotateAngleY = 0.0F;
      this.legBackLeft.rotateAngleY = 0.0F;
      this.pregnant.rotateAngleX = ((float)Math.PI / 2F);
      if (!p_225597_1_.isInWater() && p_225597_1_.onGround) {
         float f = p_225597_1_.isDigging() ? 4.0F : 1.0F;
         float f1 = p_225597_1_.isDigging() ? 2.0F : 1.0F;
         float f2 = 5.0F;
         this.legFrontRight.rotateAngleY = MathHelper.cos(f * p_225597_2_ * 5.0F + (float)Math.PI) * 8.0F * p_225597_3_ * f1;
         this.legFrontRight.rotateAngleZ = 0.0F;
         this.legFrontLeft.rotateAngleY = MathHelper.cos(f * p_225597_2_ * 5.0F) * 8.0F * p_225597_3_ * f1;
         this.legFrontLeft.rotateAngleZ = 0.0F;
         this.legBackRight.rotateAngleY = MathHelper.cos(p_225597_2_ * 5.0F + (float)Math.PI) * 3.0F * p_225597_3_;
         this.legBackRight.rotateAngleX = 0.0F;
         this.legBackLeft.rotateAngleY = MathHelper.cos(p_225597_2_ * 5.0F) * 3.0F * p_225597_3_;
         this.legBackLeft.rotateAngleX = 0.0F;
      }

      this.pregnant.showModel = !this.isChild && p_225597_1_.hasEgg();
   }

   public void func_225598_a_(MatrixStack p_225598_1_, IVertexBuilder p_225598_2_, int p_225598_3_, int p_225598_4_, float p_225598_5_, float p_225598_6_, float p_225598_7_, float p_225598_8_) {
      boolean flag = this.pregnant.showModel;
      if (flag) {
         p_225598_1_.func_227860_a_();
         p_225598_1_.func_227861_a_(0.0D, (double)-0.08F, 0.0D);
      }

      super.func_225598_a_(p_225598_1_, p_225598_2_, p_225598_3_, p_225598_4_, p_225598_5_, p_225598_6_, p_225598_7_, p_225598_8_);
      if (flag) {
         p_225598_1_.func_227865_b_();
      }

   }
}