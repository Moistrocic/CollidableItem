package com.collidableitem.utils

import com.bulletphysics.collision.dispatch.CollisionObject
import com.bulletphysics.collision.shapes.BoxShape
import com.bulletphysics.linearmath.Transform
import net.minecraft.util.math.*
import javax.vecmath.Vector3f

class IrregularShape {
    companion object {
        @JvmStatic
        val EMPTY = IrregularShape()
        private val transformCache = Transform()
        private val collisionObjectCache = CollisionObject()

//        @JvmStatic
//        fun toCollisionObject(box: Box): CollisionObject {
//            val pos = box.center
//            val xLength = box.lengthX.toFloat()
//            val yLength = box.lengthY.toFloat()
//            val zLength = box.lengthZ.toFloat()
//            val boxShape = BoxShape(Vector3f(xLength / 2, yLength/ 2, zLength/ 2))
//            transformCache.setIdentity()
//            transformCache.origin.set(pos.x.toFloat(), pos.y.toFloat(), pos.z.toFloat())
//            collisionObjectCache.collisionShape = boxShape
//            collisionObjectCache.setWorldTransform(transformCache)
//            return collisionObjectCache
//        }
    }

    private val faces: MutableList<Face> = mutableListOf()
    private val pointList: MutableSet<Vector3f> = mutableSetOf()
    private var minPoint: Vector3f? = null
    private var maxPoint: Vector3f? = null
    private var posCache: Vector3f = Vector3f()
    private val transform = Transform()
    private var collisionObject: CollisionObject? = null

//    fun toCollisionObjects(irShape: IrregularShape): CollisionObject {
//        if (collisionObject == null) {
//            collisionObject = CollisionObject()
//
//        }
//        transform.setIdentity()
//        transform.origin.set(posCache.x, posCache.y, posCache.z)
//        collisionObject.setWorldTransform(transform)
//        return collisionObject
//    }

//    fun setPos(pos: Vector3f) {
//        posCache = pos
//    }
//
//    fun setPos(pos: Vec3d) {
//        val pos1 = Vector3f(
//            pos.x.toFloat(),
//            pos.y.toFloat(),
//            pos.z.toFloat()
//        )
//        setPos(pos1)
//    }

//    fun getBlockBox(centerPoint: Position): BlockBox {
//        val blockPos1 = minPoint?.let { centerPoint.translate(it).toBlockPos() }
//        val blockPos2 = maxPoint?.let { centerPoint.translate(it).toBlockPos() }
//        if (blockPos1 != null && blockPos2 != null) {
//            return BlockBox(blockPos1.x, blockPos1.y, blockPos1.z, blockPos2.x, blockPos2.y, blockPos2.z)
//        }
//        return BlockBox(0, 0, 0, 0, 0, 0)
//    }

//    fun isIntersect(irShape1: IrregularShape): Boolean {
//        // TODO
//        return true
//    }
//
//    fun isIntersect(collisionObject: CollisionObject): Boolean {
//
//    }

    fun addFace(pointList: List<Vector3f>, direction: Vector3f) {
        this.pointList.addAll(pointList)
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