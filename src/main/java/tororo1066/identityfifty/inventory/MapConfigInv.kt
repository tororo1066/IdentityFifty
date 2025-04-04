package tororo1066.identityfifty.inventory

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import tororo1066.identityfifty.IdentityFifty
import tororo1066.identityfifty.data.*
import tororo1066.tororopluginapi.SInput
import tororo1066.tororopluginapi.config.SConfig
import tororo1066.tororopluginapi.defaultMenus.LargeSInventory
import tororo1066.tororopluginapi.otherClass.PlusInt
import tororo1066.tororopluginapi.otherUtils.UsefulUtility
import tororo1066.tororopluginapi.sInventory.SInventoryItem
import tororo1066.tororopluginapi.sItem.SItem
import tororo1066.tororopluginapi.utils.LocType
import tororo1066.tororopluginapi.utils.toLocString

class MapConfigInv(private val mapName: String, private val mapData: MapData) : LargeSInventory(IdentityFifty.plugin,"§e§l${mapData.name}の設定") {

    private val sInput = SInput(IdentityFifty.plugin)

    private val survivorSpawnLocationsInv = LargeSInventory(IdentityFifty.plugin,"§a§lサバイバーのスポーン地点")
    private val hunterSpawnLocationsInv = LargeSInventory(IdentityFifty.plugin,"§a§lハンターのスポーン地点")
    private val generatorLocationsInv = LargeSInventory(IdentityFifty.plugin,"§a§l発電機の設定")
    private val escapeGeneratorLocationsInv = LargeSInventory(IdentityFifty.plugin,"§a§lゲート付近の発電機の設定")
    private val goalRegionsInv = LargeSInventory(IdentityFifty.plugin,"§a§lゴールエリアの設定")
    private val prisonsInv = LargeSInventory(IdentityFifty.plugin,"§a§l牢屋の設定")
    private val woodPlatesInv = LargeSInventory(IdentityFifty.plugin,"§a§l板の設定")
    private val hatchesInv = LargeSInventory(IdentityFifty.plugin,"§a§lハッチの設定")

    companion object{
        val editNow = ArrayList<String>()
    }


    override fun renderMenu(p: Player): Boolean {

        setOnClick {
            p.playSound(p.location,Sound.UI_BUTTON_CLICK,1f,1f)
        }

        editNow.add(mapName)

        val items = ArrayList<SInventoryItem>()

        val setName = createInputItem(SItem(Material.OAK_SIGN).setDisplayName("§a名前設定").addLore("§a現在の値：§e${mapData.name}"),
            String::class.java) { string, _ ->

            mapData.name = string
            p.sendMessage("§a名前を§d${string}§aにしました")
        }

        items.add(setName)

        val setWorld = createInputItem(SItem(Material.GRASS_BLOCK).setDisplayName("§aワールド設定").addLore("§a現在の値：§e${mapData.world.name}"),
            String::class.java) { string, _ ->
            val world = Bukkit.getWorld(string)
            if (world == null){
                p.sendMessage("§4ワールドが存在しません")
                return@createInputItem
            }
            mapData.world = world
            p.sendMessage("§aワールドを§d${string}§aにしました")
        }

        items.add(setWorld)

        val setSurvivorLimit = createInputItem(SItem(Material.DIAMOND_SWORD).setDisplayName("§aサバイバーの最大値設定").addLore("§a現在の値：§e${mapData.survivorLimit}"),
            PlusInt::class.java) { int, _ ->
            mapData.survivorLimit = int.get()
            p.sendMessage("§aサバイバーの最大値を§d${int.get()}§aにしました")
        }

        items.add(setSurvivorLimit)

        val setHunterLimit = createInputItem(SItem(Material.NETHERITE_SWORD).setDisplayName("§aハンターの最大値設定").addLore("§a現在の値：§e${mapData.hunterLimit}"),
            PlusInt::class.java) { int, _ ->
            mapData.hunterLimit = int.get()
            p.sendMessage("§aハンターの最大値を§d${int.get()}§aにしました")
        }

        items.add(setHunterLimit)

        val setGeneratorGoal = createInputItem(SItem(Material.LIME_WOOL).setDisplayName("§aゲートを出現させるために壊す発電機の数設定").addLore("§a現在の値：§e${mapData.generatorGoal}"),
            PlusInt::class.java) { int, _ ->
            mapData.generatorGoal = int.get()
            p.sendMessage("§aゲートを出現させるために壊す発電機の数を§d${int.get()}§aにしました")
        }

        items.add(setGeneratorGoal)

        val setGeneratorLimit = createInputItem(SItem(Material.WHITE_WOOL).setDisplayName("§a発電機の出る量設定").addLore("§a現在の値：§e${mapData.generatorLimit}"),
            PlusInt::class.java) { int, _ ->
            mapData.generatorLimit = int.get()
            p.sendMessage("§a発電機の出る量を§d${int.get()}§aにしました")
        }

        items.add(setGeneratorLimit)

        val setEscapeGeneratorLimit = createInputItem(SItem(Material.RED_WOOL).setDisplayName("§aゲート発電機の出る量設定").addLore("§a現在の値：§e${mapData.escapeGeneratorLimit}"),
            PlusInt::class.java) { int, _ ->
            mapData.escapeGeneratorLimit = int.get()
            p.sendMessage("§aゲート発電機の出る量を§d${int.get()}§aにしました")
        }

        items.add(setEscapeGeneratorLimit)

        val spawnHatchGenerator = createInputItem(SItem(Material.OAK_TRAPDOOR).setDisplayName("§aハッチを出現させるために壊す発電機の数設定").addLore("§a現在の値：§e${mapData.needSummonHatchGenerator}"),
            PlusInt::class.java) { int, _ ->
            mapData.needSummonHatchGenerator = int.get()
            p.sendMessage("§aハッチを出現させるために壊す発電機の数を§d${int.get()}§aにしました")
        }

        items.add(spawnHatchGenerator)

        val setSurvivorSpawnLocations = SItem(Material.DIAMOND_BLOCK).setDisplayName("§aサバイバーのスポーン地点").toSInventoryItem().setCanClick(false).setClickEvent { e ->
            loadSurvivorSpawnLocations()
            moveChildInventory(survivorSpawnLocationsInv, e.whoClicked as Player)
        }

        items.add(setSurvivorSpawnLocations)

        val setHunterSpawnLocations = SItem(Material.REDSTONE_BLOCK).setDisplayName("§aハンターのスポーン地点").toSInventoryItem().setCanClick(false).setClickEvent { e ->
            loadHunterSpawnLocations()
            moveChildInventory(hunterSpawnLocationsInv, e.whoClicked as Player)
        }

        items.add(setHunterSpawnLocations)

        val setGeneratorLocations = SItem(Material.IRON_BLOCK).setDisplayName("§a発電機の設定").toSInventoryItem().setCanClick(false).setClickEvent { e ->
            loadGeneratorLocations()
            moveChildInventory(generatorLocationsInv, e.whoClicked as Player)
        }

        items.add(setGeneratorLocations)

        val setEscapeGeneratorLocations = SItem(Material.COPPER_BLOCK).setDisplayName("§aゲート付近の発電機の設定").toSInventoryItem().setCanClick(false).setClickEvent { e ->
            loadEscapeGeneratorLocations()
            moveChildInventory(escapeGeneratorLocationsInv, e.whoClicked as Player)
        }

        items.add(setEscapeGeneratorLocations)

        val setGoalRegions = SItem(Material.EMERALD_BLOCK).setDisplayName("§aゴールエリアの設定").toSInventoryItem().setCanClick(false).setClickEvent { e ->
            loadGoalRegions()
            moveChildInventory(goalRegionsInv, e.whoClicked as Player)
        }

        items.add(setGoalRegions)

        val setPrisons = SItem(Material.IRON_BARS).setDisplayName("§a牢屋の設定").toSInventoryItem().setCanClick(false).setClickEvent { e ->
            loadPrisonLocations()
            moveChildInventory(prisonsInv, e.whoClicked as Player)
        }

        items.add(setPrisons)

        val setWoodPlates = SItem(Material.OAK_PLANKS).setDisplayName("§a板の設定").toSInventoryItem().setCanClick(false).setClickEvent { e ->
            loadWoodPlates()
            moveChildInventory(woodPlatesInv, e.whoClicked as Player)
        }

        items.add(setWoodPlates)

        val setHatches = SItem(Material.IRON_TRAPDOOR).setDisplayName("§aハッチの設定").toSInventoryItem().setCanClick(false).setClickEvent { e ->
            loadHatches()
            moveChildInventory(hatchesInv, e.whoClicked as Player)
        }

        items.add(setHatches)

        val lobbyLocation = createInputItem(SItem(Material.QUARTZ).setDisplayName("§aゲームが終わったときのテレポート先設定")
            .addLore("§a現在の値：§e${mapData.lobbyLocation?.toLocString(LocType.WORLD_BLOCK_SPACE)}"),
            Location::class.java) { loc, _ ->
            mapData.lobbyLocation = loc
            p.sendMessage("§aゲームが終わったときのテレポート先を§d${loc.toLocString(LocType.WORLD_BLOCK_SPACE)}§aにしました")
        }

        items.add(lobbyLocation)

        val mapId = createInputItem(SItem(Material.MAP).setDisplayName("§aマップid設定").addLore("§a現在の値：§e${mapData.mapId}"),
            PlusInt::class.java) { int, _ ->
            mapData.mapId = int.get()
            p.sendMessage("§aマップidを§d${int.get()}§aにしました")
        }

        items.add(mapId)

        val windowBlock = createInputItem(SItem(Material.SOUL_SAND).setDisplayName("§a窓のブロック設定").addLore("§a現在の値：§e${mapData.windowBlock}"),
            String::class.java) { str, _ ->
            val material = UsefulUtility.sTry({ Material.valueOf(str) }, { null })
            if (material == null){
                p.sendMessage("§4ブロックが存在しません")
                return@createInputItem
            }
            mapData.windowBlock = material
            p.sendMessage("§a窓のブロックを§d${material}§aにしました")
        }

        items.add(windowBlock)

        val save = SItem(Material.LIME_STAINED_GLASS).setDisplayName("§a保存").toSInventoryItem().setCanClick(false).setClickEvent { e ->
            e.whoClicked.closeInventory()
            editNow.remove(mapName)
            if (!save()){
                e.whoClicked.sendMessage("§4保存に失敗しました")
                return@setClickEvent
            }
            e.whoClicked.sendMessage("§a保存に成功しました！")
        }

        items.add(save)

        setResourceItems(items)

        setOnClose {
            editNow.remove(mapName)
        }

        return true
    }

    private fun loadSurvivorSpawnLocations(){

        val items = ArrayList<SInventoryItem>()

        mapData.survivorSpawnLocations.forEach { loc ->
            val item = SItem(Material.REDSTONE_BLOCK).setDisplayName("${loc.blockX} ${loc.blockY} ${loc.blockZ}").addLore("§cシフト左クリックで削除").toSInventoryItem().setCanClick(false).setClickEvent shift@{
                if (it.click != ClickType.SHIFT_LEFT)return@shift
                mapData.survivorSpawnLocations.remove(loc)
                it.whoClicked.sendMessage("§a削除しました")
                loadSurvivorSpawnLocations()
            }
            items.add(item)
        }
        val item = SItem(Material.EMERALD_BLOCK).setDisplayName("§a§l+追加する")
        val inputItem = createInputItem(item,Location::class.java) { loc, p ->
            mapData.survivorSpawnLocations.add(Location(mapData.world,loc.blockX.toDouble(),loc.blockY.toDouble(),loc.blockZ.toDouble()))
            p.sendMessage("§a${loc.blockX} ${loc.blockY} ${loc.blockZ}を追加しました")
            loadSurvivorSpawnLocations()
        }
        items.add(inputItem)
        survivorSpawnLocationsInv.setResourceItems(items)
        survivorSpawnLocationsInv.renderInventory(survivorSpawnLocationsInv.nowPage)
    }

    private fun loadHunterSpawnLocations(){

        val items = ArrayList<SInventoryItem>()

        mapData.hunterSpawnLocations.forEach { loc ->
            val item = SItem(Material.REDSTONE_BLOCK).setDisplayName("${loc.blockX} ${loc.blockY} ${loc.blockZ}").addLore("§cシフト左クリックで削除").toSInventoryItem().setCanClick(false).setClickEvent shift@{
                if (it.click != ClickType.SHIFT_LEFT)return@shift
                mapData.hunterSpawnLocations.remove(loc)
                it.whoClicked.sendMessage("§a削除しました")
                loadHunterSpawnLocations()
            }
            items.add(item)
        }
        val item = SItem(Material.EMERALD_BLOCK).setDisplayName("§a§l+追加する")
        val inputItem = createInputItem(item,Location::class.java) { loc, p ->
            mapData.hunterSpawnLocations.add(Location(mapData.world,loc.blockX.toDouble(),loc.blockY.toDouble(),loc.blockZ.toDouble()))
            p.sendMessage("§a${loc.blockX} ${loc.blockY} ${loc.blockZ}を追加しました")
            loadHunterSpawnLocations()
        }
        items.add(inputItem)
        hunterSpawnLocationsInv.setResourceItems(items)
        hunterSpawnLocationsInv.renderInventory(hunterSpawnLocationsInv.nowPage)
    }

    private fun loadGeneratorLocations(){

        val items = ArrayList<SInventoryItem>()

        mapData.generators.forEach { data ->
            val item = SItem(Material.REDSTONE_BLOCK).setDisplayName("${data.location.blockX} ${data.location.blockY} ${data.location.blockZ}")
                .addLore("§aHP：${data.health}")
                .addLore("§cシフト左クリックで削除").toSInventoryItem().setCanClick(false).setClickEvent shift@{
                if (it.click != ClickType.SHIFT_LEFT)return@shift
                mapData.generators.remove(data)
                it.whoClicked.sendMessage("§a削除しました")
                loadGeneratorLocations()
            }
            items.add(item)
        }
        val item = SItem(Material.EMERALD_BLOCK).setDisplayName("§a§l+追加する")
        val inputItem = createInputItem(item,Location::class.java,"§b発電機の位置を入力してください", invOpenCancel = true) { loc, p ->
            val data = GeneratorData()
            data.location = Location(mapData.world,loc.blockX.toDouble(),loc.blockY.toDouble(),loc.blockZ.toDouble())
            p.sendMessage("§a${loc.blockX} ${loc.blockY} ${loc.blockZ}を追加しました")
            sInput.sendInputCUI(p,PlusInt::class.java,"§b発電機のHPを入力してください", action = { int ->
                data.health = int.get()
                mapData.generators.add(data)
                loadGeneratorLocations()
                generatorLocationsInv.open(p)
                p.sendMessage("§aHPを${int}にしました")
            })

        }
        items.add(inputItem)
        generatorLocationsInv.setResourceItems(items)
        generatorLocationsInv.renderInventory(generatorLocationsInv.nowPage)
    }

    private fun loadEscapeGeneratorLocations(){

        val items = ArrayList<SInventoryItem>()

        mapData.escapeGenerators.forEach { data ->
            val item = SItem(Material.REDSTONE_BLOCK).setDisplayName("${data.location.blockX} ${data.location.blockY} ${data.location.blockZ}")
                .addLore("§aHP：${data.health}")
                .addLore("§cシフト左クリックで削除").toSInventoryItem().setCanClick(false).setClickEvent shift@{
                if (it.click != ClickType.SHIFT_LEFT)return@shift
                mapData.escapeGenerators.remove(data)
                it.whoClicked.sendMessage("§a削除しました")
                loadEscapeGeneratorLocations()
            }
            items.add(item)
        }
        val item = SItem(Material.EMERALD_BLOCK).setDisplayName("§a§l+追加する")
        val inputItem = createInputItem(item,Location::class.java,"§b発電機の位置を入力してください", invOpenCancel = true) { loc, p ->
            val data = GeneratorData()
            data.location = Location(mapData.world,loc.blockX.toDouble(),loc.blockY.toDouble(),loc.blockZ.toDouble())
            p.sendMessage("§a${loc.blockX} ${loc.blockY} ${loc.blockZ}を追加しました")
            sInput.sendInputCUI(p,Location::class.java,"§b鉄のドアの位置を入力してください", action = { doorLoc ->
                data.doorLocation = Location(mapData.world,doorLoc.blockX.toDouble(),doorLoc.blockY.toDouble(),doorLoc.blockZ.toDouble())
                p.sendMessage("§a${doorLoc.blockX} ${doorLoc.blockY} ${doorLoc.blockZ}を追加しました")
                sInput.sendInputCUI(p,PlusInt::class.java,"§b発電機のHPを入力してください", action = { int ->
                    data.health = int.get()
                    mapData.escapeGenerators.add(data)
                    loadEscapeGeneratorLocations()
                    escapeGeneratorLocationsInv.open(p)
                    p.sendMessage("§aHPを${int}にしました")
                })
            })
        }
        items.add(inputItem)
        escapeGeneratorLocationsInv.setResourceItems(items)
        escapeGeneratorLocationsInv.renderInventory(escapeGeneratorLocationsInv.nowPage)
    }

    private fun loadGoalRegions(){

        val items = ArrayList<SInventoryItem>()

        mapData.goalRegions.forEach { id ->
            val item = SItem(Material.REDSTONE_BLOCK).setDisplayName(id).addLore("§cシフト左クリックで削除").toSInventoryItem().setCanClick(false).setClickEvent shift@{
                if (it.click != ClickType.SHIFT_LEFT)return@shift
                mapData.goalRegions.remove(id)
                it.whoClicked.sendMessage("§a削除しました")
                loadGoalRegions()
            }
            items.add(item)
        }
        val item = SItem(Material.EMERALD_BLOCK).setDisplayName("§a§l+追加する")
        val inputItem = createInputItem(item,String::class.java) { str, p ->
            mapData.goalRegions.add(str)
            loadGoalRegions()
            p.sendMessage("§a${str}を追加しました")
        }
        items.add(inputItem)
        goalRegionsInv.setResourceItems(items)
        goalRegionsInv.renderInventory(goalRegionsInv.nowPage)
    }

    private fun loadPrisonLocations(){

        val items = ArrayList<SInventoryItem>()

        mapData.prisons.forEach { data ->
            val item = SItem(Material.REDSTONE_BLOCK).setDisplayName("§a情報")
                .addLore("§b脱出判定のlocation：${locToString(data.value.escapeLoc)}")
                .addLore("§d鉄のドアのlocation：${locToString(data.value.doorLoc)}")
                .addLore("§e出現位置のlocation：${locToString(data.value.spawnLoc)}")
                .addLore("§cシフト左クリックで削除").toSInventoryItem().setCanClick(false).setClickEvent shift@{
                if (it.click != ClickType.SHIFT_LEFT)return@shift
                mapData.prisons.remove(data.key)
                it.whoClicked.sendMessage("§a削除しました")
                loadPrisonLocations()
            }
            items.add(item)
        }
        val item = SItem(Material.EMERALD_BLOCK).setDisplayName("§a§l+追加する")
        val inputItem = createInputItem(item,Location::class.java,"§b脱出判定の位置を入力してください", invOpenCancel = true) { loc, p ->
            val data = PrisonData()
            data.escapeLoc = Location(mapData.world,loc.blockX.toDouble(),loc.blockY.toDouble(),loc.blockZ.toDouble())
            p.sendMessage("§a${locToString(loc)}を追加しました")
            sInput.sendInputCUI(p,Location::class.java,"§b鉄のドアの位置を入力してください", action = { doorLoc ->
                data.doorLoc = Location(mapData.world,doorLoc.blockX.toDouble(),doorLoc.blockY.toDouble(),doorLoc.blockZ.toDouble())
                p.sendMessage("§a${doorLoc.blockX} ${doorLoc.blockY} ${doorLoc.blockZ}を追加しました")
                sInput.sendInputCUI(p,Location::class.java,"§b出現位置を入力してください", action = { loc3 ->
                    data.spawnLoc = loc3
                    mapData.prisons[data.escapeLoc] = data
                    loadPrisonLocations()
                    prisonsInv.open(p)
                    p.sendMessage("§a${locToString(loc3)}を追加しました")
                })
            })
        }
        items.add(inputItem)
        prisonsInv.setResourceItems(items)
        prisonsInv.renderInventory(prisonsInv.nowPage)
    }

    private fun loadWoodPlates(){

        val items = ArrayList<SInventoryItem>()

        mapData.woodPlates.forEach { data ->
            val item = SItem(Material.REDSTONE_BLOCK).setDisplayName("§a情報")
                .addLore("§b位置：${locToString(data.key)}")
                .addLore("§d長さ：${data.value.length}")
                .addLore("§e向き：${data.value.face.name}")
                .addLore("§cシフト左クリックで削除").toSInventoryItem().setCanClick(false).setClickEvent shift@{
                if (it.click != ClickType.SHIFT_LEFT)return@shift
                mapData.woodPlates.remove(data.key)
                it.whoClicked.sendMessage("§a削除しました")
                loadWoodPlates()
            }
            items.add(item)
        }
        val item = SItem(Material.EMERALD_BLOCK).setDisplayName("§a§l+追加する")
        val inputItem = createInputItem(item,Location::class.java,"§b板の根本位置を入力してください", invOpenCancel = true) { loc, p ->
            val data = WoodPlateData()
            data.loc = Location(mapData.world,loc.blockX.toDouble(),loc.blockY.toDouble(),loc.blockZ.toDouble())
            p.sendMessage("§a${loc.blockX} ${loc.blockY} ${loc.blockZ}を追加しました")
            sInput.sendInputCUI(p,PlusInt::class.java,"§b板の長さを入力してください", action = { int ->
                data.length = int.get()
                p.sendMessage("§a板の長さを${int.get()}にしました")
                sInput.sendInputCUI(p,BlockFace::class.java,"§b板の向きを入力してください", action = { face ->
                    data.face = face
                    mapData.woodPlates[data.loc] = data
                    loadWoodPlates()
                    woodPlatesInv.open(p)
                    p.sendMessage("§a向きを${face.name}にしました")
                })
            })


        }
        items.add(inputItem)
        woodPlatesInv.setResourceItems(items)
        woodPlatesInv.renderInventory(woodPlatesInv.nowPage)
    }

    private fun loadHatches(){

        val items = ArrayList<SInventoryItem>()

        mapData.hatches.forEach { data ->
            val item = SItem(Material.REDSTONE_BLOCK).setDisplayName("§a情報")
                .addLore("§b位置：${locToString(data.key)}")
                .addLore("§cシフト左クリックで削除").toSInventoryItem().setCanClick(false).setClickEvent shift@{
                if (it.click != ClickType.SHIFT_LEFT)return@shift
                mapData.hatches.remove(data.key)
                it.whoClicked.sendMessage("§a削除しました")
                loadHatches()
            }
            items.add(item)
        }
        val item = SItem(Material.EMERALD_BLOCK).setDisplayName("§a§l+追加する")
        val inputItem = createInputItem(item,Location::class.java,"§bハッチの位置を入力してください") { loc, p ->
            val data = HatchData()
            data.location = Location(mapData.world,loc.blockX.toDouble(),loc.blockY.toDouble(),loc.blockZ.toDouble())
            p.sendMessage("§a${loc.blockX} ${loc.blockY} ${loc.blockZ}を追加しました")
            mapData.hatches[data.location] = data
            loadHatches()
        }
        items.add(inputItem)
        hatchesInv.setResourceItems(items)
        hatchesInv.renderInventory(hatchesInv.nowPage)
    }

    private fun save(): Boolean{
        val sConfig = SConfig(IdentityFifty.plugin)
        val config = sConfig.getConfig("map/${mapName}")?:return false
        config.set("name",mapData.name)
        config.set("world",mapData.world.name)
        config.set("survivorLimit",mapData.survivorLimit)
        config.set("hunterLimit",mapData.hunterLimit)
        config.set("generatorGoal",mapData.generatorGoal)
        config.set("generatorLimit",mapData.generatorLimit)
        config.set("escapeGeneratorLimit",mapData.escapeGeneratorLimit)
        config.set("needSummonHatchGenerator",mapData.needSummonHatchGenerator)
        config.set("survivorSpawnLocations",mapData.survivorSpawnLocations.stream().map { "${it.blockX},${it.blockY},${it.blockZ}" }.toList())
        config.set("hunterSpawnLocations",mapData.hunterSpawnLocations.stream().map { "${it.blockX},${it.blockY},${it.blockZ}" }.toList())
        config.set("generatorLocations",mapData.generators.stream().map { "${it.location.blockX},${it.location.blockY},${it.location.blockZ},${it.health}" }.toList())
        config.set("escapeGeneratorLocations",mapData.escapeGenerators.stream().map { "${it.location.blockX},${it.location.blockY},${it.location.blockZ},${it.doorLocation.blockX},${it.doorLocation.blockY},${it.doorLocation.blockZ},${it.health}" }.toList())
        config.set("goalRegionId",mapData.goalRegions)
        config.set("prisonLocations",mapData.prisons.values.stream().map { "${it.spawnLoc.blockX},${it.spawnLoc.blockY},${it.spawnLoc.blockZ},${it.escapeLoc.blockX},${it.escapeLoc.blockY},${it.escapeLoc.blockZ},${it.doorLoc.blockX},${it.doorLoc.blockY},${it.doorLoc.blockZ}" }.toList())
        config.set("woodPlates",mapData.woodPlates.values.stream().map { "${it.loc.blockX},${it.loc.blockY},${it.loc.blockZ},${it.length},${it.face.name}" }.toList())
        config.set("hatches",mapData.hatches.values.stream().map { "${it.location.blockX},${it.location.blockY},${it.location.blockZ}" }.toList())
        config.set("lobbyLocation",mapData.lobbyLocation)
        config.set("mapId",mapData.mapId)
        config.set("windowBlock",mapData.windowBlock.name)
        sConfig.saveConfig(config,"map/${mapName}")
        IdentityFifty.maps[mapName] = mapData
        return true
    }

    private fun locToString(location: Location): String {
        return "${location.blockX} ${location.blockY} ${location.blockZ}"
    }
}