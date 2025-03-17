package com.collidableitem.utils

import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.util.math.MatrixStack

object DrawUtil {

    fun drawSlash(
        matrices: MatrixStack,
        vertexConsumer: VertexConsumer,
        x1: Float,
        y1: Float,
        z1: Float,
        x2: Float,
        y2: Float,
        z2: Float,
        red: Float,
        green: Float,
        blue: Float,
        alpha: Float,
        unitX: Float,
        unitY: Float,
        unitZ: Float,
    ) {
        val entry = matrices.peek()
        vertexConsumer.vertex(entry, x1, y1, z1).color(red, green, blue, alpha).normal(entry, unitX, unitY, unitZ)
        vertexConsumer.vertex(entry, x2, y2, z2).color(red, green, blue, alpha).normal(entry, unitX, unitY, unitZ)
    }
}