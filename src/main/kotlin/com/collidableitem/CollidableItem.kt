package com.collidableitem

import com.collidableitem.entity.ModEntityTypes
import com.collidableitem.network.ModNetworking
import net.fabricmc.api.ModInitializer
import org.slf4j.LoggerFactory

object CollidableItem : ModInitializer {
	const val MOD_ID = "collidableitem"
    val LOGGER = LoggerFactory.getLogger(MOD_ID)

	override fun onInitialize() {
		ModNetworking.register()
		ModEntityTypes.register()
	}
}