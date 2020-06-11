package info.danielmartinez.fcontacts

import android.content.ContentResolver
import android.content.Context
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar
import android.app.Activity
import android.provider.ContactsContract
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodChannel.Result

class FContactsPlugin (
    private val contentResolver: ContentResolver,
    private val context: Context
) : MethodCallHandler {
  companion object {
    @JvmStatic
    fun registerWith(registrar: Registrar) {
      val channel = MethodChannel(registrar.messenger(), "fcontacts")
      channel.setMethodCallHandler(FContactsPlugin( registrar.context().contentResolver, registrar.context() ))
    }
   
  }

  override fun onMethodCall(call: MethodCall, result: Result) {
    when {
      call.method == "getPlatformVersion" -> result.success("Android ${android.os.Build.VERSION.RELEASE}")
      call.method == "list" -> list( result, call.argument("query") )
      else -> result.notImplemented()
    }
  }

  private fun list( result: Result, query: String? = null ) {
    FContactsHandler( this.contentResolver, this.context ).list (query) { items ->
      result.success( items )
    }
  }
    
}
