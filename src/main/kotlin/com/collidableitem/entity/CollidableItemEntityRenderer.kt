package com.collidableitem.entity

import com.bulletphysics.collision.dispatch.CollisionObject
import com.bulletphysics.collision.shapes.BvhTriangleMeshShape
import com.bulletphysics.collision.shapes.ByteBufferVertexData
import com.bulletphysics.collision.shapes.TriangleIndexVertexArray
import com.collidableitem.utils.DrawUtil
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.*
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.render.entity.ItemEntityRenderer
import net.minecraft.client.render.item.ItemRenderer
import net.minecraft.client.render.model.json.ModelTransformationMode
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.ItemEntity
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.vecmath.Vector3f
import kotlin.collections.ArrayList

class CollidableItemEntityRenderer(
    ctx: EntityRendererFactory.Context
) : ItemEntityRenderer(ctx) {
    private var itemRenderer: ItemRenderer? = null

    init {
        itemRenderer = ctx.itemRenderer
    }

    override fun render(
        itemEntity: ItemEntity,
        f: Float,
        g: Float,
        matrixStack: MatrixStack,
        vertexConsumerProvider: VertexConsumerProvider,
        i: Int
    ) {

    }

    fun renderHitBox(
        itemEntity: ItemEntity,
        matrixStack: MatrixStack,
        vertexConsumerProvider: VertexConsumerProvider,
        i: Int
    ) {
        val item = itemEntity.stack.item
        if (CollidableItemEntity.getBaseCollisionObject(item) == null) {
            val vertexConsumerCapturer = VertexConsumerCapturer(vertexConsumerProvider)

            matrixStack.push()
            val camera = MinecraftClient.getInstance().gameRenderer.camera
            matrixStack.translate(camera.pos.x, camera.pos.y, camera.pos.z)
            matrixStack.translate(-itemEntity.x, -itemEntity.y, -itemEntity.z)
            matrixStack.translate(0.0, 0.25, 0.0)
            val itemStack = itemEntity.stack
            val bakedModel = itemRenderer!!.getModel(itemStack, itemEntity.world, null, 0)
            itemRenderer!!.renderItem(itemStack, ModelTransformationMode.FIXED, false, matrixStack,
                vertexConsumerCapturer, i, OverlayTexture.DEFAULT_UV, bakedModel)
            matrixStack.pop()

            vertexConsumerCapturer.getVertexesCapturer()?.let {
                val triangleVertexIndexes = it.getTriangleVertexIndexArray()
                val triangleIndexesBuffer = createByteBuffer(
                    triangleVertexIndexes.toList(),
                    triangleVertexIndexes.size * Int.SIZE_BYTES,
                    ByteBuffer::putInt
                )
                val vertexCoordinates = it.getVerticesCoordinateArray()
                val vertexCoordinatesBuffer = createByteBuffer(
                    vertexCoordinates.toList(),
                    vertexCoordinates.size * Float.SIZE_BYTES,
                    ByteBuffer::putFloat
                )
                val normalArray = it.getNormalArray()
                val normalArrayBuffer = createByteBuffer(
                    normalArray.toList(),
                    normalArray.size * Float.SIZE_BYTES,
                    ByteBuffer::putFloat
                )
                val mesh = TriangleIndexVertexArray(
                    triangleVertexIndexes.size / 3,
                    triangleIndexesBuffer,
                    Int.SIZE_BYTES * 3,
                    vertexCoordinates.size / 3,
                    vertexCoordinatesBuffer,
                    Float.SIZE_BYTES * 3
                )
                (mesh.getLockedVertexIndexBase(0) as ByteBufferVertexData).setNormalData(
                    normalArray.size / 3,
                    normalArrayBuffer,
                    Float.SIZE_BYTES * 3
                )
                val collisionShape = BvhTriangleMeshShape(mesh, true)
                val collisionObject = CollisionObject()
                collisionObject.collisionShape = collisionShape
                CollidableItemEntity.setBaseCollisionObject(item, collisionObject)
            }
        }
        if (itemEntity is CollidableItemEntity) {
            val collisionObject = itemEntity.getCollisionObject()
            DrawUtil.drawFaceBorder(matrixStack, vertexConsumerProvider.getBuffer(RenderLayer.getLines()),
                collisionObject, 1.0f, 1.0f, 1.0f, 1.0f)
        }
    }

    private fun <T> createByteBuffer(array: List<T>, bytes: Int, putOperation: ByteBuffer.(T) -> ByteBuffer): ByteBuffer {
        val bytebuffer = ByteBuffer
            .allocateDirect(bytes)
            .order(ByteOrder.nativeOrder())
        array.forEach { bytebuffer.putOperation(it) }
        return bytebuffer
    }

    class VertexConsumerCapturer(
        private val vertexConsumerProvider: VertexConsumerProvider
    ) : VertexConsumerProvider by vertexConsumerProvider {
        private var vertexesCapturer: VertexConsumer? = null

        fun getVertexesCapturer(): VertexesCapturer? {
            return vertexesCapturer as VertexesCapturer?
        }

        override fun getBuffer(renderLayer: RenderLayer): VertexConsumer {
            vertexesCapturer = VertexesCapturer(vertexConsumerProvider.getBuffer(renderLayer))
            return vertexesCapturer!!
        }
    }

    class VertexesCapturer(private val bufferBuilder: VertexConsumer) : VertexConsumer by bufferBuilder {

        private val vertices = mutableSetOf<Vector3f>()
        private val triangleVertexIndexArray = ArrayList<Int>()
        private val normals = ArrayList<Vector3f>()
        private val quadIndex = ArrayList<Int>()
        private var vertexCache = Vector3f()

        fun getNormalArray(): FloatArray {
            return createFloatArray(normals.size * 3, normals.toTypedArray())
        }

        fun getVerticesCoordinateArray(): FloatArray {
            return createFloatArray(vertices.size * 3, vertices.toTypedArray())
        }

        private fun createFloatArray(size: Int, array: Array<Vector3f>): FloatArray {
            val floatArray = FloatArray(size)
            var index = 0
            array.forEach {
                floatArray[index++] = it.x
                floatArray[index++] = it.y
                floatArray[index++] = it.z
            }
            return floatArray
        }

        fun getTriangleVertexIndexArray(): IntArray {
            return triangleVertexIndexArray.toIntArray()
        }

        override fun vertex(x: Float, y: Float, z: Float): VertexConsumer {
            vertexCache = Vector3f(x, y, z)
            return bufferBuilder.vertex(x, y, z)
        }

        override fun normal(x: Float, y: Float, z: Float): VertexConsumer {
            val normal = Vector3f(x, y, z)
            vertices.add(vertexCache)
            quadIndex.add(vertices.indexOf(vertexCache))
            if (quadIndex.size == 4) {
                triangleVertexIndexArray.addAll(listOf(quadIndex[0], quadIndex[1], quadIndex[2]))
                triangleVertexIndexArray.addAll(listOf(quadIndex[0], quadIndex[2], quadIndex[3]))
                normals.addAll(listOf(normal, normal))
                quadIndex.clear()
            }
            return bufferBuilder.normal(x, y, z)
        }
    }
}

private var ByteBufferVertexData.normalData: ByteBuffer?
    get() = CollidableItemEntity.normalDataMap[this]
    set(value) {
        CollidableItemEntity.normalDataMap[this] = value!!
    }

private var ByteBufferVertexData.normalCount: Int
    get() = CollidableItemEntity.normalCountMap[this] ?: 0
    set(value) {
        CollidableItemEntity.normalCountMap[this] = value
    }

private var ByteBufferVertexData.normalStride: Int
    get() = CollidableItemEntity.normalStrideMap[this] ?: 0
    set(value) {
        CollidableItemEntity.normalStrideMap[this] = value
    }

fun ByteBufferVertexData.setNormalData(normalCount: Int, normalData: ByteBuffer, normalStride: Int) {
    this.normalCount = normalCount
    this.normalData = normalData
    this.normalStride = normalStride
}

fun ByteBufferVertexData.getNormalData(idx: Int): Vector3f {
    val off = idx * normalStride
    return this.normalData?.let {
        Vector3f(
        it.getFloat(off + Float.SIZE_BYTES * 0),
        it.getFloat(off + Float.SIZE_BYTES * 1),
        it.getFloat(off + Float.SIZE_BYTES * 2)
        )
    } ?: Vector3f()
}