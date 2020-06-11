package com.example.flutterimagecompress

import android.os.Build
import com.example.flutterimagecompress.core.CompressFileHandler
import com.example.flutterimagecompress.core.CompressListHandler
import com.example.flutterimagecompress.format.FormatRegister
import com.example.flutterimagecompress.handle.common.CommonHandler
import com.example.flutterimagecompress.handle.heif.HeifHandler
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

class FlutterImageCompressPlugin : FlutterPlugin, MethodCallHandler {

    companion object {
        var showLog = false
        lateinit var registrar: FlutterPlugin.FlutterPluginBinding
    }

    init {
        FormatRegister.registerFormat(CommonHandler(0)) // jpeg
        FormatRegister.registerFormat(CommonHandler(1)) // png
        FormatRegister.registerFormat(HeifHandler()) // heic / heif
        FormatRegister.registerFormat(CommonHandler(3)) // webp
    }

    override fun onMethodCall(call: MethodCall, result: Result): Unit {
        when (call.method) {
            "showLog" -> result.success(handleLog(call))
            "compressWithList" -> registrar.let { CompressListHandler(call, result).handle(it) }
            "compressWithFile" -> CompressFileHandler(call, result).handle(registrar)
            "compressWithFileAndGetFile" -> CompressFileHandler(call, result).handleGetFile(registrar)
            "getSystemVersion" -> result.success(Build.VERSION.SDK_INT)
            else -> result.notImplemented()
        }
    }

    private fun handleLog(call: MethodCall): Int {
        val arg = call.arguments<Boolean>()
        showLog = (arg == true)
        return 1
    }

    override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        val channel = MethodChannel(binding.binaryMessenger, "flutter_image_compress")
        channel.setMethodCallHandler(this)
        registrar = binding
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    }

}
