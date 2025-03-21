package com.collidableitem.utils

import com.bulletphysics.collision.dispatch.CollisionObject
import com.bulletphysics.collision.shapes.BvhTriangleMeshShape
import com.bulletphysics.collision.shapes.ByteBufferVertexData
import com.bulletphysics.collision.shapes.TriangleIndexVertexArray
import com.collidableitem.entity.getNormalData
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.util.math.MatrixStack
import javax.vecmath.Vector3f

object DrawUtil {

    fun drawFaceBorder(
        matrices: MatrixStack,
        vertexConsumer: VertexConsumer,
        collisionObject: CollisionObject,
        red: Float,
        green: Float,
        blue: Float,
        alpha: Float
    ) {
        val collisionShape = collisionObject.collisionShape as BvhTriangleMeshShape
        val meshData = (collisionShape.meshInterface as TriangleIndexVertexArray)
            .getLockedVertexIndexBase(0) as ByteBufferVertexData
        val triangleCount = meshData.indexCount / 3
        for (i in 0 until triangleCount) {
            val triangleIndex = meshData.getTriangleIndex(i)
            triangleIndex.add(triangleIndex.first())
            val triangleVertices = triangleIndex.stream().map{
                val vertex = Vector3f()
                meshData.getVertex(it, vertex)
                vertex
            }.toList()
            val vector = meshData.getNormalData(i)
            triangleVertices.zipWithNext{ vertex1, vertex2 ->
                drawSlash(matrices, vertexConsumer,
                    vertex1.x, vertex1.y, vertex1.z, vertex2.x, vertex2.y, vertex2.z,
                    red, green, blue, alpha,
                    vector.x, vector.y, vector.z
                )
            }
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

fun ByteBufferVertexData.getTriangleIndex(idx: Int): MutableList<Int> {
    val index1 = this.getIndex(idx * 3)
    val index2 = this.getIndex(idx * 3 + 1)
    val index3 = this.getIndex(idx * 3 + 2)
    return mutableListOf(index1, index2, index3)
}