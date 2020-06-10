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
      const val PICK_PHONE = 2015
      const val PICK_EMAIL = 2020
      const val FLUTTER_CONTACT_PICKER = "info.danielmartinez.fcontacts"
  }
    private var channel1: MethodChannel? = null
    private var activity: ActivityPluginBinding? = null
    private val context1: PickContext = V2Context()


  override fun onMethodCall(call: MethodCall, result: Result) {
    when {
      call.method == "getPlatformVersion" -> result.success("Android ${android.os.Build.VERSION.RELEASE}")
      call.method == "list" -> list( result, call.argument("query") )
      call.method == "myfunc" -> myfunc(result)
      call.method == "pickPhoneContact" -> ContactPicker.requestPicker(PICK_PHONE, ContactsContract.CommonDataKinds.Phone.CONTENT_URI, result, context1)
      else -> result.notImplemented()
    }
  }

  private fun list( result: Result, query: String? = null ) {
    FContactsHandler( this.contentResolver, this.context ).list (query) { items ->
      result.success( items )
    }
  }
    
    private fun myfunc( result: Result) {
      result.success( "this is my string, i called it" )
  }
    
  override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel1 = MethodChannel(binding.binaryMessenger, FLUTTER_CONTACT_PICKER)
        channel1?.setMethodCallHandler(this)
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel1?.setMethodCallHandler(null)
        channel1 = null
    }

    override fun onDetachedFromActivity() {
        activity?.removeActivityResultListener(context1)
        activity = null
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) = onAttachedToActivity(binding)

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        binding.addActivityResultListener(context1)
        activity = binding
    }

    override fun onDetachedFromActivityForConfigChanges() = onDetachedFromActivity()

    private inner class V2Context : AbstractPickContext() {
        override val activity: Activity
            get() = this@FlutterContactPickerPlugin.activity?.activity ?: error("No Activity")

    }
}
