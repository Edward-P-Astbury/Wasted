package android.system.time.trigger.application

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

import android.system.time.Trigger
import android.system.time.Utils

class ApplicationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils(this).fire(Trigger.APPLICATION)
        finishAndRemoveTask()
    }
}