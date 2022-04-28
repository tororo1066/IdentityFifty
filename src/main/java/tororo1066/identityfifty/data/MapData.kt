package tororo1066.identityfifty.data

import org.bukkit.Location
import org.bukkit.World

class MapData {
    var name = ""
    lateinit var world: World
    var survivorLimit = 0
    var hunterLimit = 0
    var generatorLimit = 0
    val survivorSpawnLocations = ArrayList<Location>()
    val hunterSpawnLocations = ArrayList<Location>()
    val generators = ArrayList<GeneratorData>()
    val goalRegions = ArrayList<String>()
}