package com.collidableitem.mixin;

import com.collidableitem.entity.CollidableItemEntity;
import com.collidableitem.utils.IrregularShape;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemEntity.class)
public class ItemEntityMixin {
    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;isSpaceEmpty(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Box;)Z"))
    private boolean isSpaceEmpty(World world, Entity entity, Box box) {
        if (entity instanceof CollidableItemEntity collidableItemEntity) {
            Vec3d pos = entity.getPos();
            Item item = collidableItemEntity.getStack().getItem();
            IrregularShape irShape = CollidableItemEntity.getIrShape(item);
            BlockBox blockBox = irShape.getBlockBox(pos);
            return BlockPos.stream(blockBox)
                    .map(blockPos -> {
                        BlockState state = world.getBlockState(blockPos);
                        VoxelShape shape = state.getCollisionShape(world, blockPos);
                        return shape.getBoundingBoxes();
                    }).noneMatch(boxes -> {
                        for (Box box1 : boxes) {
                            if (irShape.isIntersect(box1)) {
                                return true;
                            }
                        }
                        return false;
                    });
        }
        return world.isSpaceEmpty(entity, entity.getBoundingBox().contract(1.0E-7));
    }

    @Inject(method = "canMerge(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
    private static void canMerge(ItemStack stack1, ItemStack stack2, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }

    @Inject(method = "canMerge()Z", at = @At("HEAD"), cancellable = true)
    private void canMerge(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }
}
