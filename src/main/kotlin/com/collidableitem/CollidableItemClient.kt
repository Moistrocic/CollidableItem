package com.collidableitem

import com.collidableitem.entity.ModEntityRenderers
import com.collidableitem.network.ModNetworking
import com.collidableitem.option.KeyBinding
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment

@Environment(EnvType.CLIENT)
object CollidableItemClient : ClientModInitializer {
    override fun onInitializeClient() {
        KeyBinding.register()
        ModNetworking.registerClient()
        ModEntityRenderers.register()
    }
}