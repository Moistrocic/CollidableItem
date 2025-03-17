package com.collidableitem.entity

import com.collidableitem.utils.DrawUtil
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.*
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.render.entity.ItemEntityRenderer
import net.minecraft.client.render.item.ItemRenderer
import net.minecraft.client.render.model.json.ModelTransformationMode
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.ItemEntity
import net.minecraft.item.Item
import net.minecraft.util.math.random.Random
import org.joml.Vector3f
import java.util.*
import kotlin.collections.ArrayList

class CollidableItemEntityRenderer(
    ctx: EntityRendererFactory.Context
) : ItemEntityRenderer(ctx) {
    private var itemRenderer: ItemRenderer? = null
    private val random: Random = Random.create()
    private var facesMap: MutableMap<Item, List<VertexesCapturer.Face>> = mutableMapOf()

    init {
        itemRenderer = ctx.itemRenderer
    }

    fun getItemRenderer(): ItemRenderer {
        return itemRenderer!!
    }

    fun getRandom(): Random {
        return random
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
        if (!facesMap.containsKey(item)) {
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

            vertexConsumerCapturer.getVertexesCapturer()?.getFaces()?.let { facesMap[item] = it }
        }
        val faces = facesMap[item] ?: return
        faces.forEach {
            DrawUtil.drawFaceBorder(matrixStack, vertexConsumerProvider.getBuffer(RenderLayer.getLines()),
                it, 1.0f, 1.0f, 1.0f, 1.0f)
        }
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

    class VertexesCapturer(
        private val bufferBuilder: VertexConsumer
    ) : VertexConsumer by bufferBuilder {

        private var vertexCache: Vector3f = Vector3f()

        private val faces: MutableList<Face> = ArrayList()
        private val pointStack: Stack<Vector3f> = Stack()

        fun getFaces(): List<Face> {
            return faces
        }

        override fun vertex(x: Float, y: Float, z: Float): VertexConsumer {
            vertexCache = Vector3f(x, y, z)
            return bufferBuilder.vertex(x, y, z)
        }

        override fun normal(x: Float, y: Float, z: Float): VertexConsumer {
            val vector = Vector3f(x, y, z)
            pointStack.push(vertexCache)
            if (pointStack.size >= 4) {
                val pointList = listOf(
                    pointStack.pop(),
                    pointStack.pop(),
                    pointStack.pop(),
                    pointStack.pop()
                )
                val face = Face(pointList, vector)
                faces.add(face)
            }
            return bufferBuilder.normal(x, y, z)
        }

        data class Face(
            val pointList: List<Vector3f>,
            val direction: Vector3f
        )
    }
}