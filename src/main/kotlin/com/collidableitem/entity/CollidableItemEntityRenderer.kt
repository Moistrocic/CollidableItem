package com.collidableitem.entity

import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.render.entity.ItemEntityRenderer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.ItemEntity

class CollidableItemEntityRenderer(
    ctx: EntityRendererFactory.Context
) : ItemEntityRenderer(ctx) {
    override fun render(
        itemEntity: ItemEntity,
        f: Float,
        g: Float,
        matrixStack: MatrixStack,
        vertexConsumerProvider: VertexConsumerProvider,
        i: Int
    ) {
        super.render(itemEntity, f, g, matrixStack, vertexConsumerProvider, i)
    }
}