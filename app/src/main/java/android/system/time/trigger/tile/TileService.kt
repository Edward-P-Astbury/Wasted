package android.system.time.trigger.tile

import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import java.util.*
import kotlin.concurrent.timerTask

import android.system.time.admin.DeviceAdminManager
import android.system.time.Preferences
import android.system.time.Trigger
import android.system.time.Utils

@RequiresApi(Build.VERSION_CODES.N)
class TileService : TileService() {
    private lateinit var prefs: Preferences
    private lateinit var admin: DeviceAdminManager
    private lateinit var utils: Utils
    private var counter = 0
    private var timer: Timer? = null

    override fun onCreate() {
        super.onCreate()
        init()
    }

    private fun init() {
        prefs = Preferences.new(this)
        admin = DeviceAdminManager(this)
        utils = Utils(this)
    }

    override fun onStartListening() {
        super.onStartListening()
        update(
            if (prefs.isEnabled && admin.isActive()) Tile.STATE_INACTIVE
            else Tile.STATE_UNAVAILABLE
        )
    }

    override fun onClick() {
        super.onClick()
        if (!prefs.isWipeData) {
            utils.fire(Trigger.TILE, false)
            return
        }
        val v = counter
        counter++
        when (v) {
            0 -> {
                update(Tile.STATE_ACTIVE)
                timer?.cancel()
                timer = Timer()
                timer?.schedule(timerTask {
                    utils.fire(Trigger.TILE)
                }, prefs.triggerTileDelay)
            }
            else -> {
                timer?.cancel()
                update(Tile.STATE_INACTIVE)
                counter = 0
            }
        }
    }

    private fun update(tileState: Int) {
        qsTile.state = tileState
        qsTile.updateTile()
    }
}