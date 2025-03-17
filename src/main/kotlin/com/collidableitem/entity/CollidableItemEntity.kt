package com.collidableitem.entity

import net.minecraft.entity.EntityType
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.world.World

class CollidableItemEntity : ItemEntity {

    constructor(entityType: EntityType<out ItemEntity>, world: World) : super(entityType, world)

    constructor(entity: ItemEntity) : this(ModEntityTypes.collidableItemEntityType, entity.world) {
        setPosition(entity.pos)
        setVelocity(world.random.nextDouble() * 0.2 - 0.1, 0.2, world.random.nextDouble() * 0.2 - 0.1)
        stack = entity.stack
    }

    override fun onPlayerCollision(player: PlayerEntity) {}
}