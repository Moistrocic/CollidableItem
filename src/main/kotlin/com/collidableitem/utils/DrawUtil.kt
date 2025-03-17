package com.collidableitem.utils

import com.collidableitem.entity.CollidableItemEntityRenderer
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.util.math.MatrixStack

object DrawUtil {

    fun drawFaceBorder(
        matrices: MatrixStack,
        vertexConsumer: VertexConsumer,
        face: CollidableItemEntityRenderer.VertexesCapturer.Face,
        red: Float,
        green: Float,
        blue: Float,
        alpha: Float
    ) {
        val pointList = face.pointList.toMutableList()
        val vector = face.direction
        pointList.add(face.pointList.first())
        pointList.zipWithNext().forEach { (point1, point2) ->
            drawSlash(matrices, vertexConsumer,
                point1.x, point1.y, point1.z, point2.x, point2.y, point2.z,
                red, green, blue, alpha,
                vector.x, vector.y, vector.z
            )
        }
    }

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