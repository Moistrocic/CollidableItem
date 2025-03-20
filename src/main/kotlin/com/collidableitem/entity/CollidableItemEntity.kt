package com.collidableitem.entity

import com.collidableitem.utils.IrregularShape
import net.minecraft.entity.EntityType
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.util.crash.CrashException
import net.minecraft.util.crash.CrashReport
import net.minecraft.util.crash.CrashReportSection
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class CollidableItemEntity : ItemEntity {
    companion object {
        private val irShapeMap: MutableMap<Item, IrregularShape> = mutableMapOf()

        @JvmStatic
        fun setIrShape(item: Item, irShape: IrregularShape) {
            irShapeMap[item] = irShape
        }

        @JvmStatic
        fun getIrShape(item: Item): IrregularShape {
            return irShapeMap[item] ?: IrregularShape.EMPTY
        }
    }

    constructor(entityType: EntityType<out ItemEntity>, world: World) : super(entityType, world)

    constructor(entity: ItemEntity) : this(ModEntityTypes.collidableItemEntityType, entity.world) {
        setPosition(entity.pos)
        setVelocity(world.random.nextDouble() * 0.2 - 0.1, 0.2, world.random.nextDouble() * 0.2 - 0.1)
        stack = entity.stack
    }

    override fun checkBlockCollision() {
        // TODO("boundingBox")
        val box = this.boundingBox
        val blockPos = BlockPos.ofFloored(box.minX + 1.0E-7, box.minY + 1.0E-7, box.minZ + 1.0E-7)
        val blockPos2 = BlockPos.ofFloored(box.maxX - 1.0E-7, box.maxY - 1.0E-7, box.maxZ - 1.0E-7)
        if (world.isRegionLoaded(blockPos, blockPos2)) {
            val mutable = BlockPos.Mutable()

            for (i in blockPos.x..blockPos2.x) {
                for (j in blockPos.y..blockPos2.y) {
                    for (k in blockPos.z..blockPos2.z) {
                        if (!this.isAlive) {
                            return
                        }

                        mutable[i, j] = k
                        val blockState = world.getBlockState(mutable)

                        try {
                            blockState.onEntityCollision(this.world, mutable, this)
                            this.onBlockCollision(blockState)
                        } catch (var12: Throwable) {
                            val crashReport = CrashReport.create(var12, "Colliding entity with block")
                            val crashReportSection = crashReport.addElement("Block being collided with")
                            CrashReportSection.addBlockInfo(crashReportSection, this.world, mutable, blockState)
                            throw CrashException(crashReport)
                        }
                    }
                }
            }
        }
    }

    override fun onPlayerCollision(player: PlayerEntity) {}
}