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

class CollidableItemEntityRenderer(
    ctx: EntityRendererFactory.Context
) : ItemEntityRenderer(ctx) {
    private var itemRenderer: ItemRenderer? = null
    private val random: Random = Random.create()
    private var linesMap: MutableMap<Item, List<VertexesCapturer.Line>> = mutableMapOf()

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
        val item = itemEntity.stack.item
        if (!linesMap.containsKey(item)) {
            val vertexConsumerCapturer = VertexConsumerCapturer(vertexConsumerProvider)

            matrixStack.push()
            val camera = MinecraftClient.getInstance().gameRenderer.camera
            matrixStack.translate(camera.pos.x, camera.pos.y, camera.pos.z)
            matrixStack.translate(-itemEntity.x, -itemEntity.y, -itemEntity.z)
            val itemStack = itemEntity.stack
            val bakedModel = itemRenderer!!.getModel(itemStack, itemEntity.world, null, 0)
            itemRenderer!!.renderItem(itemStack, ModelTransformationMode.FIXED, false, matrixStack,
                vertexConsumerCapturer, i, OverlayTexture.DEFAULT_UV, bakedModel)
            matrixStack.pop()

            vertexConsumerCapturer.getVertexesCapturer()?.getLines()?.let { linesMap[item] = it }
        }
        val lines = linesMap[item] ?: return
        lines.forEach { (startPoint, endPoint) ->
            val startVertex = startPoint.first
            val endVertex = endPoint.first
            val vector1 = startPoint.second
            DrawUtil.drawSlash(matrixStack, vertexConsumerProvider.getBuffer(RenderLayer.getLines()),
                startVertex.x, startVertex.y, startVertex.z,
                endVertex.x, endVertex.y, endVertex.z,
                1.0f, 1.0f, 1.0f, 1.0f, vector1.x, vector1.y, vector1.z)
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

        private val lines: MutableList<Line> = ArrayList()
        private var vertexCache: Vector3f = Vector3f()
        private var vertexVectorCache: Pair<Vector3f, Vector3f>? = null
        private var startVertexVector: Pair<Vector3f, Vector3f>? = null

        fun getLines(): List<Line> {
            if (vertexVectorCache != null &&
                startVertexVector != null) {
                val endClosedLine = Line(vertexVectorCache!!, startVertexVector!!)
                lines.add(endClosedLine)
            }
            return lines
        }

        override fun vertex(x: Float, y: Float, z: Float): VertexConsumer {
            vertexCache = Vector3f(x, y, z)
            return bufferBuilder.vertex(x, y, z)
        }

        override fun normal(x: Float, y: Float, z: Float): VertexConsumer {
            val vector = Vector3f(x, y, z)
            val vertexVector = Pair(vertexCache, vector)
            if (startVertexVector?.second != vertexVector.second) {
                startVertexVector?.let {
                    val closedLine = Line(vertexVectorCache!!, it)
                    lines.add(closedLine)
                }
                startVertexVector = vertexVector
            }
            vertexVectorCache?.let {
                if (it.second == vertexVector.second) {
                    val line = Line(it, vertexVector)
                    lines.add(line)
                }
            }
            vertexVectorCache = vertexVector
            return bufferBuilder.normal(x, y, z)
        }

        data class Line(
            val startPoint: Pair<Vector3f, Vector3f>,
            val endPoint: Pair<Vector3f, Vector3f>
        )
    }
}