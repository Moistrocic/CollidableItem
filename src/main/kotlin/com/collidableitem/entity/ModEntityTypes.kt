package com.collidableitem.entity

import com.collidableitem.CollidableItem
import net.minecraft.entity.EntityType
import net.minecraft.entity.SpawnGroup
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier

object ModEntityTypes {
    val collidableItemEntityType: EntityType<CollidableItemEntity> = Registry.register(
        Registries.ENTITY_TYPE,
        Identifier.of(CollidableItem.MOD_ID, "collidable_item"),
        EntityType.Builder.create(::CollidableItemEntity, SpawnGroup.MISC)
            .dimensions(0.25F, 0.25F)
            .eyeHeight(0.0F)
            .maxTrackingRange(6)
            .trackingTickInterval(20)
            .build()
    )
    fun register() {}
}