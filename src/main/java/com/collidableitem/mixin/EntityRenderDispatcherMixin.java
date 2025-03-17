package com.collidableitem.mixin;

import com.collidableitem.entity.CollidableItemEntity;
import com.collidableitem.entity.CollidableItemEntityRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {
    @Shadow public abstract <T extends Entity> EntityRenderer<? super T> getRenderer(T entity);

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/EntityRenderDispatcher;renderHitbox(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;Lnet/minecraft/entity/Entity;FFFF)V"), cancellable = true)
    public <E extends Entity> void render(E entity, double x, double y, double z, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (entity instanceof CollidableItemEntity collidableItemEntity) {
            CollidableItemEntityRenderer renderer = (CollidableItemEntityRenderer) getRenderer(collidableItemEntity);
            renderer.renderHitBox(collidableItemEntity, matrices, vertexConsumers, light);
            matrices.pop();
            ci.cancel();
        }
    }
}
