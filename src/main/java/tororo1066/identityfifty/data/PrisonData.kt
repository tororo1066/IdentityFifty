package tororo1066.identityfifty.data

import org.bukkit.Location
import java.util.UUID

class PrisonData {

    lateinit var spawnLoc: Location
    lateinit var plateLoc: Location
    val inPlayer = ArrayList<UUID>()
    lateinit var lastPressUUID: UUID

}