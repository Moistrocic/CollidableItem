package com.collidableitem.entity

import com.collidableitem.entity.ModEntityTypes.collidableItemEntityType
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry

object ModEntityRenderers {
    fun register() {
        EntityRendererRegistry.register(collidableItemEntityType) { ctx -> CollidableItemEntityRenderer(ctx) }
    }
}