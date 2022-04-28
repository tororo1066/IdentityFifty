package tororo1066.identityfifty.data

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import tororo1066.identityfifty.IdentityFifty

class MapData {

    companion object{
        fun loadFromYml(name: String){
            val config = IdentityFifty.sConfig.getConfig("map/${name}")?:throw NullPointerException()
            val data = MapData()
            data.name = config.getString("name")?:"name"
            data.world = Bukkit.getWorld(config.getString("world")!!)!!
            data.survivorLimit = config.getInt("survivorLimit")
            data.hunterLimit = config.getInt("hunterLimit")
            data.generatorLimit = config.getInt("generatorLimit")
            data.escapeGeneratorLimit = config.getInt("escapeGeneratorLimit")
            config.getStringList("survivorSpawnLocations").forEach {
                val split = it.split(",")
                data.survivorSpawnLocations.add(Location(data.world,split[0].toDouble(),split[1].toDouble(),split[2].toDouble()))
            }
            config.getStringList("hunterSpawnLocations").forEach {
                val split = it.split(",")
                data.hunterSpawnLocations.add(Location(data.world,split[0].toDouble(),split[1].toDouble(),split[2].toDouble()))
            }
            config.getStringList("generatorLocations").forEach {
                val split = it.split(",")
                val generatorData = GeneratorData()
                generatorData.location = Location(data.world,split[0].toDouble(),split[1].toDouble(),split[2].toDouble())
                generatorData.health = split[3].toInt()
                data.generators.add(generatorData)
            }
            config.getStringList("escapeGeneratorLocations").forEach {
                val split = it.split(",")
                val generatorData = GeneratorData()
                generatorData.location = Location(data.world,split[0].toDouble(),split[1].toDouble(),split[2].toDouble())
                generatorData.health = split[3].toInt()
                data.escapeGenerators.add(generatorData)
            }
            data.goalRegions.addAll(config.getStringList("goalRegionId"))

        }
    }

    var name = ""
    lateinit var world: World
    var survivorLimit = 0
    var hunterLimit = 0
    var generatorLimit = 0
    var escapeGeneratorLimit = 0
    val survivorSpawnLocations = ArrayList<Location>()
    val hunterSpawnLocations = ArrayList<Location>()
    val generators = ArrayList<GeneratorData>()
    val escapeGenerators = ArrayList<GeneratorData>()
    val goalRegions = ArrayList<String>()
}