package info.danielmartinez.fcontacts

import android.app.Activity
import io.flutter.plugin.common.PluginRegistry

interface PickContext : PluginRegistry.ActivityResultListener {

    val activity: Activity

    fun removeActivityResultListener(listener: PluginRegistry.ActivityResultListener)

    fun addActivityResultListener(listener: PluginRegistry.ActivityResultListener)

}
