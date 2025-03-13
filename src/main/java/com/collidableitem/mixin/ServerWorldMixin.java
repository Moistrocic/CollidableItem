package com.collidableitem.mixin;

import com.collidableitem.entity.CollidableItemEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.server.world.ServerEntityManager;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerWorld.class)
public class ServerWorldMixin {
    @Shadow @Final private ServerEntityManager<Entity> entityManager;

    @Inject(method = "addEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerEntityManager;addEntity(Lnet/minecraft/world/entity/EntityLike;)Z"), cancellable = true)
    private void addEntityMixin(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof ItemEntity itemEntity) {
            cir.setReturnValue(this.entityManager.addEntity(new CollidableItemEntity(itemEntity)));
        }
    }
}
