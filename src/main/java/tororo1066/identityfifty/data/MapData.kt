package tororo1066.identityfifty.data

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.map.MapView.Scale
import tororo1066.identityfifty.enumClass.AllowAction
import kotlin.math.max
import kotlin.math.min

class MapData: Cloneable {

    companion object{
        fun loadFromYml(config: YamlConfiguration): MapData {
            val data = MapData()
            data.name = config.getString("name")?:"name"
            data.world = Bukkit.getWorld(config.getString("world","world")!!)!!
            data.survivorLimit = config.getInt("survivorLimit")
            data.hunterLimit = config.getInt("hunterLimit")
            data.generatorLimit = config.getInt("generatorLimit")
            data.generatorGoal = config.getInt("generatorGoal")
            data.escapeGeneratorLimit = config.getInt("escapeGeneratorLimit")
            data.needSummonHatchGenerator = config.getInt("needSummonHatchGenerator")
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
                generatorData.doorLocation = Location(data.world,split[3].toDouble(),split[4].toDouble(),split[5].toDouble())
                generatorData.health = split[6].toInt()
                data.escapeGenerators.add(generatorData)
            }
            data.goalRegions.addAll(config.getStringList("goalRegionId"))

            config.getStringList("prisonLocations").forEach {
                val split = it.split(",")
                val prisonData = PrisonData()
                prisonData.spawnLoc = Location(data.world,split[0].toDouble(),split[1].toDouble(),split[2].toDouble())
                prisonData.escapeLoc = Location(data.world,split[3].toDouble(),split[4].toDouble(),split[5].toDouble())
                prisonData.doorLoc = Location(data.world,split[6].toDouble(),split[7].toDouble(),split[8].toDouble())
                data.prisons[prisonData.escapeLoc] = prisonData
            }

            config.getStringList("woodPlates").forEach {
                val split = it.split(",")
                val woodPlateData = WoodPlateData()
                woodPlateData.loc = Location(data.world,split[0].toDouble(),split[1].toDouble(),split[2].toDouble())
                woodPlateData.length = split[3].toInt()
                woodPlateData.face = BlockFace.valueOf(split[4])

                data.woodPlates[woodPlateData.loc] = woodPlateData
            }

            config.getStringList("hatches").forEach {
                val split = it.split(",")
                val hatchData = HatchData()
                hatchData.location = Location(data.world,split[0].toDouble(),split[1].toDouble(),split[2].toDouble())
                data.hatches[hatchData.location] = hatchData
            }

            data.lobbyLocation = config.getLocation("lobbyLocation")

            val mapId = config.getInt("mapId",-1)
            if (mapId != -1){
                data.mapId = mapId
            }

            data.windowBlock = Material.valueOf(config.getString("windowBlock","SOUL_SAND")!!)

            return data
        }
    }

    var name = ""
    lateinit var world: World
    var survivorLimit = 0
    var hunterLimit = 0
    var generatorLimit = 0
    var escapeGeneratorLimit = 0
    var generatorGoal = 0
    var needSummonHatchGenerator = 0
    val survivorSpawnLocations = ArrayList<Location>()
    val hunterSpawnLocations = ArrayList<Location>()
    val generators = ArrayList<GeneratorData>()
    val escapeGenerators = ArrayList<GeneratorData>()
    val goalRegions = ArrayList<String>()
    val prisons = HashMap<Location,PrisonData>()
    var woodPlates = HashMap<Location,WoodPlateData>()
    var hatches = HashMap<Location,HatchData>()
    lateinit var windowBlock: Material

    var lobbyLocation: Location? = null

    var mapId: Int? = null

    public override fun clone(): MapData {
        return super.clone() as MapData
    }
}