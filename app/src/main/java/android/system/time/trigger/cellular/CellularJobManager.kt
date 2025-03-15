package android.system.time.trigger.cellular

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.system.time.Preferences
import java.util.concurrent.TimeUnit

class CellularJobManager(private val ctx: Context) {
    companion object {
        private const val JOB_ID = 2000
    }
    private val prefs by lazy { Preferences.new(ctx) }
    private val scheduler = ctx.getSystemService(JobScheduler::class.java)

    fun schedule(): Int {
        return scheduler?.schedule(
            JobInfo.Builder(JOB_ID, ComponentName(ctx, CellularJobService::class.java))
                .setMinimumLatency(TimeUnit.MINUTES.toMillis(prefs.triggerCellularCount.toLong()))
                .setBackoffCriteria(0, JobInfo.BACKOFF_POLICY_LINEAR)
                .setPersisted(true)
                .build()
        ) ?: JobScheduler.RESULT_FAILURE
    }

    fun cancel() = scheduler?.cancel(JOB_ID)
}