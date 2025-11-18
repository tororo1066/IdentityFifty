package tororo1066.identityfifty.map

import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.map.*
import tororo1066.identityfifty.data.MapData
import kotlin.math.roundToInt

class IdentityFiftyMapRenderer(val map: MapData): MapRenderer() {

    private var firstRenderer = true
    private var previousCursor: MapCursor? = null

    override fun render(view: MapView, canvas: MapCanvas, p: Player) {
        val scale = when(view.scale){
            MapView.Scale.CLOSEST->0.5
            MapView.Scale.CLOSE->1.0
            MapView.Scale.NORMAL->2.0
            MapView.Scale.FAR->4.0
            MapView.Scale.FARTHEST->8.0
        }

        if (firstRenderer){
            firstRenderer = false

            view.isTrackingPosition = false
            view.isUnlimitedTracking = false
            while (canvas.cursors.size() > 0){
                val cursor = canvas.cursors.getCursor(0)
                canvas.cursors.removeCursor(cursor)
            }

            canvas.cursors = MapCursorCollection()

            val x = view.centerX
            val z = view.centerZ
            map.generators.forEach {
                val cursorX = ((it.location.blockX - x) / scale).toInt().toByte()
                val cursorZ = ((it.location.blockZ - z) / scale).toInt().toByte()
                canvas.cursors.addCursor(
                    MapCursor(cursorX, cursorZ, 0,
                        MapCursor.Type.BANNER_WHITE, true, Component.text("羊型発電機"))
                )
            }

            map.escapeGenerators.forEach {
                val cursorX = ((it.location.blockX - x) / scale).toInt().toByte()
                val cursorZ = ((it.location.blockZ - z) / scale).toInt().toByte()
                canvas.cursors.addCursor(
                    MapCursor(cursorX, cursorZ, 0,
                        MapCursor.Type.BANNER_YELLOW, true, Component.text("牛型発電機"))
                )
            }

            map.prisons.forEach {
                val cursorX = ((it.key.blockX - x) / scale).toInt().toByte()
                val cursorZ = ((it.key.blockZ - z) / scale).toInt().toByte()
                canvas.cursors.addCursor(
                    MapCursor(cursorX, cursorZ, 0,
                        MapCursor.Type.BANNER_BROWN, true, Component.text("牢屋"))
                )
            }

            map.hatches.forEach {
                val cursorX = ((it.key.blockX - x) / scale).toInt().toByte()
                val cursorZ = ((it.key.blockZ - z) / scale).toInt().toByte()
                canvas.cursors.addCursor(
                    MapCursor(cursorX, cursorZ, 0,
                        MapCursor.Type.BANNER_BLUE, true, Component.text("ハッチ"))
                )
            }
        }

        //Display player's cursor
        previousCursor?.let {
            canvas.cursors.removeCursor(it)
        }
        val cursorX = ((p.location.blockX - view.centerX) / scale).toInt().toByte()
        val cursorZ = ((p.location.blockZ - view.centerZ) / scale).toInt().toByte()
        previousCursor = canvas.cursors.addCursor(
            MapCursor(cursorX, cursorZ, yawToDirectionIndex(p.location.yaw),
                MapCursor.Type.PLAYER, true, null as Component?)
        )
    }

    private fun yawToDirectionIndex(yaw: Float): Byte {
        val normalizedYaw = (yaw + 360) % 360
        return ((normalizedYaw / 22.5).roundToInt() % 16).toByte()
    }
}