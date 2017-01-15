package hackathon.morton.jonathan.uncommonhacks2017

import android.animation.ObjectAnimator
import android.app.ActivityManager
import android.app.Service
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.res.Resources
import android.graphics.PixelFormat
import android.graphics.Point
import android.media.AudioAttributes
import android.media.SoundPool
import android.net.Uri
import android.os.*
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import org.jetbrains.anko.find
import java.util.*


class ClippyService : Service() {

    private var windowManager: WindowManager? = null
    private var clippyView: LinearLayout? = null
    private var clippyImageView: ImageView? = null
    private var dialogView: LinearLayout? = null

    private var questionTextView: TextView? = null
    private var option1TextView: TextView? = null
    private var noThanksTextView: TextView? = null
    private var windowSize: Point? = Point()

    private var xInitial: Int = 0
    private var yInitial: Int = 0
    private var xInitialMargin: Int = 0
    private var yInitialMargin: Int = 0


    override fun onBind(intent: Intent): IBinder? {
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (startId == Service.START_STICKY) {
            startClippy()
            return super.onStartCommand(intent, flags, startId)
        } else {
            return Service.START_NOT_STICKY
        }
    }

    fun startClippy() {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        windowSize!!.set(
                Resources.getSystem().displayMetrics.widthPixels,
                Resources.getSystem().displayMetrics.heightPixels)


        dialogView = inflater.inflate(R.layout.dialog, null) as LinearLayout
        val dialogParams = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT)
        dialogParams.gravity = Gravity.BOTTOM or Gravity.RIGHT
        dialogView!!.visibility = View.GONE
        windowManager!!.addView(dialogView, dialogParams)

        clippyView = inflater.inflate(R.layout.clippy, null) as LinearLayout
        clippyImageView = clippyView!!.find(R.id.clippyImageView)
        val clippyParams = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT)
        clippyParams.gravity = Gravity.BOTTOM or Gravity.RIGHT
        clippyParams.x = 0
        clippyParams.y = 0
        windowManager!!.addView(clippyView, clippyParams)

        var clickStartTime: Long = 0
        var clickEndTime: Long = 0
        clippyView!!.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(view: View?, event: MotionEvent?): Boolean {
                val clippyLayoutParams = clippyView!!.layoutParams as WindowManager.LayoutParams

                var startX = event!!.rawX.toInt()
                var startY = event!!.rawY.toInt()
                val destinationX: Int
                val destinationY: Int


                when (event!!.getAction()) {
                    MotionEvent.ACTION_DOWN -> {
                        clickStartTime = System.currentTimeMillis()
                        xInitial = startX
                        yInitial = startY

                        xInitialMargin = clippyLayoutParams.x
                        yInitialMargin = clippyLayoutParams.y

                        //todo hide tip
                    }

                    MotionEvent.ACTION_MOVE -> {
                        val xDiffMove = xInitial - startX
                        val yDiffMove = yInitial - startY

                        destinationX = xInitialMargin + xDiffMove
                        destinationY = yInitialMargin + yDiffMove

                        clippyLayoutParams.x = destinationX
                        clippyLayoutParams.y = destinationY
                        dialogView!!.visibility = View.GONE
                        windowManager!!.updateViewLayout(clippyView, clippyLayoutParams)
                    }

                    MotionEvent.ACTION_UP -> {
                        clickEndTime = System.currentTimeMillis()
                        val clickTime = clickEndTime - clickStartTime
                        if (clickTime < 300) {
                            handleClick()
                        } else {
                            L.d("Long click of " + clickTime)
                        }
                    }
                    else -> return false
                }
                return true
            }
        })

    }

    override fun onDestroy() {
        if (clippyView != null) {
            windowManager!!.removeView(clippyView)
        }
        if (clippyImageView != null) {
            windowManager!!.removeView(clippyImageView)
        }

        if (dialogView != null) {
            windowManager!!.removeView(dialogView)
        }
        super.onDestroy()
    }

    private fun handleClick() {
        var dialogParams = dialogView!!.layoutParams as WindowManager.LayoutParams
        var clippyParms = clippyView!!.layoutParams as WindowManager.LayoutParams
        dialogParams.x = clippyParms.x
        dialogParams.y = clippyParms.y + clippyImageView!!.height + 10

        windowManager!!.updateViewLayout(dialogView, dialogParams)
        dialogView!!.visibility = View.VISIBLE

        questionTextView = dialogView!!.find(R.id.questionTextView)
        option1TextView = dialogView!!.find(R.id.option1TextView)
        noThanksTextView = dialogView!!.find(R.id.noThanksTextView)

        val currentApp = getTopAppName(this)
        L.d(currentApp)

        when (currentApp) {
            "com.android.providers.media", "com.jrtstudio.AnotherMusicPlayer" -> {
                questionTextView!!.text = getString(R.string.music_app_intro)
                option1TextView!!.text = getString(R.string.music_app_party)
                option1TextView!!.setOnClickListener {
                    dialogView!!.visibility = View.GONE
                    playHorns()
                }
                noThanksTextView!!.setOnClickListener {
                    dialogView!!.visibility = View.GONE
                }
            }
            "com.sonyericsson.conversations" -> {
                questionTextView!!.text = getString(R.string.messaging_app_intro)
                option1TextView!!.text = getString(R.string.messaging_app_text_boo)
                option1TextView!!.setOnClickListener {
                    dialogView!!.visibility = View.GONE

                    val smsIntent = Intent(Intent.ACTION_VIEW)
                    smsIntent.setData(Uri.parse("sms:"))
                    smsIntent.putExtra("address", "12125551212")
                    smsIntent.putExtra("sms_body", "u up?")
                    smsIntent.addFlags(FLAG_ACTIVITY_NEW_TASK)
                    startActivity(smsIntent)

                }
                noThanksTextView!!.setOnClickListener {
                    dialogView!!.visibility = View.GONE
                }
            }
            else -> {
                dialogView!!.visibility = View.GONE
                val objectAnimator: ObjectAnimator = ObjectAnimator.ofFloat(clippyImageView, "rotation", 0.0f, 360f)
                objectAnimator.duration = 500
                objectAnimator.start()

            }
        }



    }


    /*From: http://stackoverflow.com/a/28066580/1994921 */
    fun getTopAppName(context: Context): String {
        val mActivityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        var strName = ""
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                strName = getLollipopFGAppPackageName(context)
            } else {
                strName = mActivityManager.getRunningTasks(1)[0].topActivity.className
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return strName
    }


    private fun getLollipopFGAppPackageName(context: Context): String {

        val uselessPackages = arrayOf(
                "com.nuance.swype.dtc",
                "com.android.providers.userdictionary",
                "com.google.android.gsf",
                "googlequicksearchbox",
                "com.android.providers.calendar",
                "com.android.captiveportallogin",
                "com.android.providers.contacts",
                "com.urbandroid.lux")
        try {
            val usageStatsManager = context.getSystemService("usagestats") as UsageStatsManager
            val milliSecs = (60 * 1000).toLong()
            val date = Date()
            val queryUsageStats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, date.getTime() - milliSecs, date.getTime())
            if (queryUsageStats.size > 0) {
                Log.i("LPU", "queryUsageStats size: " + queryUsageStats.size)
            }

            val statsList = queryUsageStats.toMutableList()
                    .filter { !uselessPackages.contains(it.packageName) }
            var recentTime: Long = 0
            var recentPkg = ""
            for (status in statsList) {
                if (status.lastTimeStamp > recentTime) {
                    recentTime = status.lastTimeStamp
                    recentPkg = status.packageName
                }
            }
            return recentPkg
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return ""
    }

    fun playHorns() {
        PlayHorns().execute()
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val vibrationPattern = longArrayOf(0, 250, 200, 250, 200, 500, 200, 500, 200)
        vibrator.vibrate(vibrationPattern, -1)
    }

    private inner class PlayHorns : AsyncTask<Void, Void, Void>() {
        var soundPool: SoundPool? = null

        override fun doInBackground(vararg params: Void): Void? {
            val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            if (soundPool == null) {
                L.d("created a new SoundPool")
                soundPool = SoundPool.Builder()
                        .setMaxStreams(7)
                        .setAudioAttributes(audioAttributes)
                        .build()
            }
            val hornSound = soundPool!!.load(applicationContext, R.raw.air_horn, 1)
            playDelayed(soundPool, hornSound, 0)
            playDelayed(soundPool, hornSound, 400)
            playDelayed(soundPool, hornSound, 600)
            playDelayed(soundPool, hornSound, 800)
            playDelayed(soundPool, hornSound, 1000)
            playDelayed(soundPool, hornSound, 1600)
            playDelayed(soundPool, hornSound, 1900)
            playDelayed(soundPool, hornSound, 2000)

            return null
        }

        override fun onPostExecute(aVoid: Void?) {
            super.onPostExecute(aVoid)
            releaseDelayed(soundPool, 5000)
        }

        protected fun playDelayed(soundPool: SoundPool?, sound: Int, delay: Int) {
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed({ soundPool!!.play(sound, 0.9f, 0.9f, 1, 0, 1f) }, delay.toLong())
        }

        protected fun releaseDelayed(soundPool: SoundPool?, delay: Int) {
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed({ soundPool!!.release() }, delay.toLong())
        }
    }
}
