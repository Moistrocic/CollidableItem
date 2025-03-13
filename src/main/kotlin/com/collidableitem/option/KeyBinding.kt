package com.collidableitem.option

import com.collidableitem.CollidableItem.MOD_ID
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import org.lwjgl.glfw.GLFW

object KeyBinding {
    private val dropItems = KeyBindingHelper.registerKeyBinding(
        KeyBinding(
            "key.${MOD_ID}",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_Q,
            "category.${MOD_ID}"
        )
    )

    fun register() {
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            while (dropItems.wasPressed()) {
                client.player?.dropCollidableItem()
            }
        }
    }
}

fun ClientPlayerEntity.dropCollidableItem() {

}