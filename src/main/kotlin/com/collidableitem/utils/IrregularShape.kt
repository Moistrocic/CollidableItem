package com.collidableitem.utils

import com.bulletphysics.collision.dispatch.CollisionObject
import net.minecraft.util.math.*
import org.joml.Vector3f

class IrregularShape {
    companion object {
        @JvmStatic
        val EMPTY = IrregularShape()
    }

    private val faces: MutableList<Face> = mutableListOf()
    private var minPoint: Vector3f? = null
    private var maxPoint: Vector3f? = null

    fun getBlockBox(centerPoint: Position): BlockBox {
        val blockPos1 = minPoint?.let { centerPoint.translate(it).toBlockPos() }
        val blockPos2 = maxPoint?.let { centerPoint.translate(it).toBlockPos() }
        if (blockPos1 != null && blockPos2 != null) {
            return BlockBox(blockPos1.x, blockPos1.y, blockPos1.z, blockPos2.x, blockPos2.y, blockPos2.z)
        }
        return BlockBox(0, 0, 0, 0, 0, 0)
    }

    fun isIntersect(box: Box): Boolean {
        // TODO

        return true
    }

    fun addFace(pointList: List<Vector3f>, direction: Vector3f) {
        val face = Face(pointList, direction)
        faces.add(face)
        pointList.forEach {
            if (minPoint == null) {
                minPoint = it
            }
            if (maxPoint == null) {
                maxPoint = it
            }
            if (it.x <= minPoint!!.x &&
                it.y <= minPoint!!.y &&
                it.z <= minPoint!!.z) {
                minPoint = it
            } else if (it.x >= maxPoint!!.x &&
                it.y >= maxPoint!!.y &&
                it.z >= minPoint!!.z) {
                maxPoint = it
            }
        }
    }

    fun getFaces(): List<Face> {
        return faces
    }

    data class Face(
        val pointList: List<Vector3f>,
        val direction: Vector3f
    )
}

fun Position.translate(offset: Vector3f): Position {
    return Vec3d(x + offset.x, y + offset.y, z + offset.z)
}

fun Position.toBlockPos(): BlockPos {
    return BlockPos(x.toInt(), y.toInt(), z.toInt())
}