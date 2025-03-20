package com.collidableitem.entity

import com.collidableitem.utils.DrawUtil
import com.collidableitem.utils.IrregularShape
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.*
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.render.entity.ItemEntityRenderer
import net.minecraft.client.render.item.ItemRenderer
import net.minecraft.client.render.model.json.ModelTransformationMode
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.ItemEntity
import org.joml.Vector3f
import java.util.*

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
        if (CollidableItemEntity.getIrShape(item) == IrregularShape.EMPTY) {
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

            vertexConsumerCapturer.getVertexesCapturer()?.getIrShape()?.let {
                CollidableItemEntity.setIrShape(item, it)
            }
        }
        CollidableItemEntity.getIrShape(item).getFaces().forEach {
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

        private val irShape: IrregularShape = IrregularShape()
        private val pointStack: Stack<Vector3f> = Stack()

        fun getIrShape(): IrregularShape {
            return irShape
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
                irShape.addFace(pointList, vector)
            }
            return bufferBuilder.normal(x, y, z)
        }
    }
}