package android.system.time.trigger.lock

import android.app.job.JobParameters
import android.app.job.JobService

import android.system.time.Trigger
import android.system.time.Utils

class LockJobService : JobService() {
    override fun onStartJob(params: JobParameters?): Boolean {
        Utils(this).fire(Trigger.LOCK)
        return false
    }

    override fun onStopJob(params: JobParameters?): Boolean { return true }
}