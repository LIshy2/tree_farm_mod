package net.minecraft.client.renderer.entity.model;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class IronGolemModel<T extends IronGolemEntity> extends SegmentedModel<T> {
   private final ModelRenderer ironGolemHead;
   private final ModelRenderer ironGolemBody;
   private final ModelRenderer ironGolemRightArm;
   private final ModelRenderer ironGolemLeftArm;
   private final ModelRenderer ironGolemLeftLeg;
   private final ModelRenderer ironGolemRightLeg;

   public IronGolemModel() {
      int i = 128;
      int j = 128;
      this.ironGolemHead = (new ModelRenderer(this)).func_78787_b(128, 128);
      this.ironGolemHead.setRotationPoint(0.0F, -7.0F, -2.0F);
      this.ironGolemHead.func_78784_a(0, 0).func_228301_a_(-4.0F, -12.0F, -5.5F, 8.0F, 10.0F, 8.0F, 0.0F);
      this.ironGolemHead.func_78784_a(24, 0).func_228301_a_(-1.0F, -5.0F, -7.5F, 2.0F, 4.0F, 2.0F, 0.0F);
      this.ironGolemBody = (new ModelRenderer(this)).func_78787_b(128, 128);
      this.ironGolemBody.setRotationPoint(0.0F, -7.0F, 0.0F);
      this.ironGolemBody.func_78784_a(0, 40).func_228301_a_(-9.0F, -2.0F, -6.0F, 18.0F, 12.0F, 11.0F, 0.0F);
      this.ironGolemBody.func_78784_a(0, 70).func_228301_a_(-4.5F, 10.0F, -3.0F, 9.0F, 5.0F, 6.0F, 0.5F);
      this.ironGolemRightArm = (new ModelRenderer(this)).func_78787_b(128, 128);
      this.ironGolemRightArm.setRotationPoint(0.0F, -7.0F, 0.0F);
      this.ironGolemRightArm.func_78784_a(60, 21).func_228301_a_(-13.0F, -2.5F, -3.0F, 4.0F, 30.0F, 6.0F, 0.0F);
      this.ironGolemLeftArm = (new ModelRenderer(this)).func_78787_b(128, 128);
      this.ironGolemLeftArm.setRotationPoint(0.0F, -7.0F, 0.0F);
      this.ironGolemLeftArm.func_78784_a(60, 58).func_228301_a_(9.0F, -2.5F, -3.0F, 4.0F, 30.0F, 6.0F, 0.0F);
      this.ironGolemLeftLeg = (new ModelRenderer(this, 0, 22)).func_78787_b(128, 128);
      this.ironGolemLeftLeg.setRotationPoint(-4.0F, 11.0F, 0.0F);
      this.ironGolemLeftLeg.func_78784_a(37, 0).func_228301_a_(-3.5F, -3.0F, -3.0F, 6.0F, 16.0F, 5.0F, 0.0F);
      this.ironGolemRightLeg = (new ModelRenderer(this, 0, 22)).func_78787_b(128, 128);
      this.ironGolemRightLeg.mirror = true;
      this.ironGolemRightLeg.func_78784_a(60, 0).setRotationPoint(5.0F, 11.0F, 0.0F);
      this.ironGolemRightLeg.func_228301_a_(-3.5F, -3.0F, -3.0F, 6.0F, 16.0F, 5.0F, 0.0F);
   }

   public Iterable<ModelRenderer> func_225601_a_() {
      return ImmutableList.of(this.ironGolemHead, this.ironGolemBody, this.ironGolemLeftLeg, this.ironGolemRightLeg, this.ironGolemRightArm, this.ironGolemLeftArm);
   }

   public void func_225597_a_(T p_225597_1_, float p_225597_2_, float p_225597_3_, float p_225597_4_, float p_225597_5_, float p_225597_6_) {
      this.ironGolemHead.rotateAngleY = p_225597_5_ * ((float)Math.PI / 180F);
      this.ironGolemHead.rotateAngleX = p_225597_6_ * ((float)Math.PI / 180F);
      this.ironGolemLeftLeg.rotateAngleX = -1.5F * this.triangleWave(p_225597_2_, 13.0F) * p_225597_3_;
      this.ironGolemRightLeg.rotateAngleX = 1.5F * this.triangleWave(p_225597_2_, 13.0F) * p_225597_3_;
      this.ironGolemLeftLeg.rotateAngleY = 0.0F;
      this.ironGolemRightLeg.rotateAngleY = 0.0F;
   }

   public void setLivingAnimations(T entityIn, float limbSwing, float limbSwingAmount, float partialTick) {
      int i = entityIn.getAttackTimer();
      if (i > 0) {
         this.ironGolemRightArm.rotateAngleX = -2.0F + 1.5F * this.triangleWave((float)i - partialTick, 10.0F);
         this.ironGolemLeftArm.rotateAngleX = -2.0F + 1.5F * this.triangleWave((float)i - partialTick, 10.0F);
      } else {
         int j = entityIn.getHoldRoseTick();
         if (j > 0) {
            this.ironGolemRightArm.rotateAngleX = -0.8F + 0.025F * this.triangleWave((float)j, 70.0F);
            this.ironGolemLeftArm.rotateAngleX = 0.0F;
         } else {
            this.ironGolemRightArm.rotateAngleX = (-0.2F + 1.5F * this.triangleWave(limbSwing, 13.0F)) * limbSwingAmount;
            this.ironGolemLeftArm.rotateAngleX = (-0.2F - 1.5F * this.triangleWave(limbSwing, 13.0F)) * limbSwingAmount;
         }
      }

   }

   private float triangleWave(float p_78172_1_, float p_78172_2_) {
      return (Math.abs(p_78172_1_ % p_78172_2_ - p_78172_2_ * 0.5F) - p_78172_2_ * 0.25F) / (p_78172_2_ * 0.25F);
   }

   public ModelRenderer func_205071_a() {
      return this.ironGolemRightArm;
   }
}