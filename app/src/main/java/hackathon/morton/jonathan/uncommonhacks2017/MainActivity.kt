package hackathon.morton.jonathan.uncommonhacks2017

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    companion object {
        val REQUEST_OVERLAY = 15
        val REQUEST_APPS = 16
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startButton.setOnClickListener {
            L.d("Button pressed")
            if (!Utils.canDrawOverlays(this)) {
                requestOverlayPermission()
            } else if (!Utils.canAccessApps(this)) {
                requestAppsPermission()
            } else {
                startService(Intent(this, ClippyService::class.java))
            }
        }
    }

    fun requestOverlayPermission() {
        if (!Utils.canDrawOverlays(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            intent.data = Uri.parse("package:" + packageName)
            startActivityForResult(intent, REQUEST_OVERLAY)
        } else {

        }
    }

    fun requestAppsPermission() {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        startActivityForResult(intent, REQUEST_APPS)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_OVERLAY -> {
                if (Utils.canDrawOverlays(this)) {
                    L.d("Got permission")
                } else {
                    //todo: don't deny the permission
                }
                super.onActivityResult(requestCode, resultCode, data)
            }

            REQUEST_APPS -> {
                if (Utils.canDrawOverlays(this)) {
                    L.d("Got permission")
                    startService(Intent(this, ClippyService::class.java))
                } else {
                    //todo: don't deny the permission
                }
                super.onActivityResult(requestCode, resultCode, data)
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }
}
