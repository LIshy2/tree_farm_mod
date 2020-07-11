package net.minecraft.client.renderer.entity.model;

import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.passive.PolarBearEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PolarBearModel<T extends PolarBearEntity> extends QuadrupedModel<T> {
   public PolarBearModel() {
      super(12, 0.0F, true, 16.0F, 4.0F, 2.25F, 2.0F, 24);
      this.textureWidth = 128;
      this.textureHeight = 64;
      this.headModel = new ModelRenderer(this, 0, 0);
      this.headModel.func_228301_a_(-3.5F, -3.0F, -3.0F, 7.0F, 7.0F, 7.0F, 0.0F);
      this.headModel.setRotationPoint(0.0F, 10.0F, -16.0F);
      this.headModel.func_78784_a(0, 44).func_228301_a_(-2.5F, 1.0F, -6.0F, 5.0F, 3.0F, 3.0F, 0.0F);
      this.headModel.func_78784_a(26, 0).func_228301_a_(-4.5F, -4.0F, -1.0F, 2.0F, 2.0F, 1.0F, 0.0F);
      ModelRenderer modelrenderer = this.headModel.func_78784_a(26, 0);
      modelrenderer.mirror = true;
      modelrenderer.func_228301_a_(2.5F, -4.0F, -1.0F, 2.0F, 2.0F, 1.0F, 0.0F);
      this.body = new ModelRenderer(this);
      this.body.func_78784_a(0, 19).func_228301_a_(-5.0F, -13.0F, -7.0F, 14.0F, 14.0F, 11.0F, 0.0F);
      this.body.func_78784_a(39, 0).func_228301_a_(-4.0F, -25.0F, -7.0F, 12.0F, 12.0F, 10.0F, 0.0F);
      this.body.setRotationPoint(-2.0F, 9.0F, 12.0F);
      int i = 10;
      this.legBackRight = new ModelRenderer(this, 50, 22);
      this.legBackRight.func_228301_a_(-2.0F, 0.0F, -2.0F, 4.0F, 10.0F, 8.0F, 0.0F);
      this.legBackRight.setRotationPoint(-3.5F, 14.0F, 6.0F);
      this.legBackLeft = new ModelRenderer(this, 50, 22);
      this.legBackLeft.func_228301_a_(-2.0F, 0.0F, -2.0F, 4.0F, 10.0F, 8.0F, 0.0F);
      this.legBackLeft.setRotationPoint(3.5F, 14.0F, 6.0F);
      this.legFrontRight = new ModelRenderer(this, 50, 40);
      this.legFrontRight.func_228301_a_(-2.0F, 0.0F, -2.0F, 4.0F, 10.0F, 6.0F, 0.0F);
      this.legFrontRight.setRotationPoint(-2.5F, 14.0F, -7.0F);
      this.legFrontLeft = new ModelRenderer(this, 50, 40);
      this.legFrontLeft.func_228301_a_(-2.0F, 0.0F, -2.0F, 4.0F, 10.0F, 6.0F, 0.0F);
      this.legFrontLeft.setRotationPoint(2.5F, 14.0F, -7.0F);
      --this.legBackRight.rotationPointX;
      ++this.legBackLeft.rotationPointX;
      this.legBackRight.rotationPointZ += 0.0F;
      this.legBackLeft.rotationPointZ += 0.0F;
      --this.legFrontRight.rotationPointX;
      ++this.legFrontLeft.rotationPointX;
      --this.legFrontRight.rotationPointZ;
      --this.legFrontLeft.rotationPointZ;
   }

   public void func_225597_a_(T p_225597_1_, float p_225597_2_, float p_225597_3_, float p_225597_4_, float p_225597_5_, float p_225597_6_) {
      super.func_225597_a_(p_225597_1_, p_225597_2_, p_225597_3_, p_225597_4_, p_225597_5_, p_225597_6_);
      float f = p_225597_4_ - (float)p_225597_1_.ticksExisted;
      float f1 = p_225597_1_.getStandingAnimationScale(f);
      f1 = f1 * f1;
      float f2 = 1.0F - f1;
      this.body.rotateAngleX = ((float)Math.PI / 2F) - f1 * (float)Math.PI * 0.35F;
      this.body.rotationPointY = 9.0F * f2 + 11.0F * f1;
      this.legFrontRight.rotationPointY = 14.0F * f2 - 6.0F * f1;
      this.legFrontRight.rotationPointZ = -8.0F * f2 - 4.0F * f1;
      this.legFrontRight.rotateAngleX -= f1 * (float)Math.PI * 0.45F;
      this.legFrontLeft.rotationPointY = this.legFrontRight.rotationPointY;
      this.legFrontLeft.rotationPointZ = this.legFrontRight.rotationPointZ;
      this.legFrontLeft.rotateAngleX -= f1 * (float)Math.PI * 0.45F;
      if (this.isChild) {
         this.headModel.rotationPointY = 10.0F * f2 - 9.0F * f1;
         this.headModel.rotationPointZ = -16.0F * f2 - 7.0F * f1;
      } else {
         this.headModel.rotationPointY = 10.0F * f2 - 14.0F * f1;
         this.headModel.rotationPointZ = -16.0F * f2 - 3.0F * f1;
      }

      this.headModel.rotateAngleX += f1 * (float)Math.PI * 0.15F;
   }
}