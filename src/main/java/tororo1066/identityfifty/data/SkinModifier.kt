package tororo1066.identityfifty.data

import me.libraryaddict.disguise.disguisetypes.PlayerDisguise
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.UUID

class SkinModifier(val uuid: UUID) {

    private var pDisguise: PlayerDisguise? = null

    fun disguise(p: Player){
        val selfP = Bukkit.getPlayer(uuid)?:return
        pDisguise = PlayerDisguise(p).setEntity(selfP)
        pDisguise?.isNameVisible = false
        pDisguise?.startDisguise()
    }

    fun unDisguise(){
        pDisguise?.stopDisguise()
    }

    fun isDisguise(): Boolean {
        return pDisguise?.isDisguiseInUse == true
    }


}