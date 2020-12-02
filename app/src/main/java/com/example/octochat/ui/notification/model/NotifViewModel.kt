package com.example.octochat.ui.notification.model

import android.app.AlarmManager
import android.app.Application
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.CountDownTimer
import android.os.SystemClock
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.AlarmManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.octochat.ui.notification.receiver.AlarmReceiver
import com.example.octochat.ui.notification.cancelNotifications
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext



class NotifViewModel(private val app: Application) : AndroidViewModel(app) {

    //private val timerLengthOptions: IntArray
    private val notifyPendingIntent: PendingIntent

    private val alarmManager = app.getSystemService(Context.ALARM_SERVICE) as
            AlarmManager
    private var prefs =
        app.getSharedPreferences("com.example.octochat",
            Context.MODE_PRIVATE)
    private val notifyIntent = Intent(app, AlarmReceiver::class.java)

    /*private val _timeSelection = MutableLiveData<Int>()
    val timeSelection: LiveData<Int>
        get() = _timeSelection*/

    /*private val _elapsedTime = MutableLiveData<Long>()
    val elapsedTime: LiveData<Long>
        get() = _elapsedTime*/

    private var _alarmOn = MutableLiveData<Boolean>()
    val isAlarmOn: LiveData<Boolean>
        get() = _alarmOn


    //private lateinit var timer: CountDownTimer

    init {
        _alarmOn.value = PendingIntent.getBroadcast(
            getApplication(),
            REQUEST_CODE,
            notifyIntent,
            PendingIntent.FLAG_NO_CREATE
        ) != null

        notifyPendingIntent = PendingIntent.getBroadcast(
            getApplication(),
            REQUEST_CODE,
            notifyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        //timerLengthOptions = app.resources.getIntArray(R.array.minutes_array)

        //If alarm is not null, resume the timer back for this alarm
        /*if (_alarmOn.value!!) {
            createTimer()
        }*/

    }

    /**
     * Turns on or off the alarm
     *
     * @param isChecked, alarm status to be set.
     */
   /* fun setAlarm(isChecked: Boolean) {
        when (isChecked) {
            true -> timeSelection.value?.let { startTimer(it) }
            false -> cancelNotification()
        }
    }*/

    /**
     * Sets the desired interval for the alarm
     *
     * @param timerLengthSelection, interval timerLengthSelection value.
     */
    /*fun setTimeSelected(timerLengthSelection: Int) {
        _timeSelection.value = timerLengthSelection
    }
*/
    /**
     * Creates a new alarm, notification and timer
     */
    private fun startTimer(timerLengthSelection: Int) {
        _alarmOn.value?.let {
            if (!it) {
                _alarmOn.value = true
                /*val selectedInterval = when (timerLengthSelection) {
                    0 -> SECOND * 10 //For testing only
                    else ->timerLengthOptions[timerLengthSelection] * MINUTE
                }
                val triggerTime = SystemClock.elapsedRealtime() +
                        selectedInterval*/

                // TODO STEP 1.5 - Get an instance of NotificationManager and
                // call sendNotification
                // TODO STEP 1.10 - Remove the notification code
//                val notificationManager = ContextCompat.getSystemService(app,
//                        NotificationManager::class.java
//                ) as NotificationManager
//
//                notificationManager.sendNotification(
//                        app.getString(R.string.timer_running), app)
                // TODO END STEP 1.10
                // TODO END STEP 1.5

                // TODO STEP 1.15 - Call cancel notification
                val notificationManager =
                    ContextCompat.getSystemService(
                        app,
                        NotificationManager::class.java
                    ) as NotificationManager
                notificationManager.cancelNotifications()


                // TODO END STEP 1.15

                /*AlarmManagerCompat.setExactAndAllowWhileIdle(
                    alarmManager,
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    triggerTime,
                    notifyPendingIntent
                )

                viewModelScope.launch {
                    saveTime(triggerTime)
                }*/
            }
        }
        //createTimer()
    }

    /**
     * Creates a new timer
     */
    /*private fun createTimer() {
        viewModelScope.launch {
            val triggerTime = loadTime()
            timer = object : CountDownTimer(triggerTime, SECOND) {
                override fun onTick(millisUntilFinished: Long) {
                    _elapsedTime.value = triggerTime -
                            SystemClock.elapsedRealtime()
                    if (_elapsedTime.value!! <= 0) {
                        resetTimer()
                    }
                }

                override fun onFinish() {
                    resetTimer()
                }
            }
            timer.start()
        }
    }*/

    /**
     * Cancels the alarm, notification and resets the timer
     */
    private fun cancelNotification() {
        //resetTimer()
        alarmManager.cancel(notifyPendingIntent)
    }

    /**
     * Resets the timer on screen and sets alarm value false
     */
    /*private fun resetTimer() {
        timer.cancel()
        _elapsedTime.value = 0
        _alarmOn.value = false
    }

    private suspend fun saveTime(triggerTime: Long) =
        withContext(Dispatchers.IO) {
            prefs.edit().putLong(TRIGGER_TIME, triggerTime).apply()
        }

    private suspend fun loadTime(): Long =
        withContext(Dispatchers.IO) {
            prefs.getLong(TRIGGER_TIME, 0)
        }*/

    private companion object {
        private const val MINUTE: Long = 60_000L
        private const val SECOND: Long = 1_000L
        private const val REQUEST_CODE = 0
        private const val TRIGGER_TIME = "TRIGGER_AT"
    }
}