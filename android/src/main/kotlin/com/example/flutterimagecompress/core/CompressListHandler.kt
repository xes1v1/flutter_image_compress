package com.example.flutterimagecompress.core

import com.example.flutterimagecompress.FlutterImageCompressPlugin
import com.example.flutterimagecompress.exception.CompressError
import com.example.flutterimagecompress.exif.Exif
import com.example.flutterimagecompress.format.FormatRegister
import com.example.flutterimagecompress.logger.log
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import java.io.ByteArrayOutputStream
import java.util.concurrent.Executors

class CompressListHandler(private val call: MethodCall, result: MethodChannel.Result) : ResultHandler(result) {

  companion object {
    @JvmStatic
    private val executor = Executors.newFixedThreadPool(5)
  }

  fun handle(registrar: FlutterPlugin.FlutterPluginBinding) {
    executor.execute {
      @Suppress("UNCHECKED_CAST") val args: List<Any> = call.arguments as List<Any>
      val arr = args[0] as ByteArray
      var minWidth = args[1] as Int
      var minHeight = args[2] as Int
      val quality = args[3] as Int
      val rotate = args[4] as Int
      val autoCorrectionAngle = args[5] as Boolean
      val format = args[6] as Int
      val keepExif = args[7] as Boolean
      val inSampleSize = args[8] as Int

      val exifRotate = if (autoCorrectionAngle) Exif.getRotationDegrees(arr) else 0

      if (exifRotate == 270 || exifRotate == 90) {
        val tmp = minWidth
        minWidth = minHeight
        minHeight = tmp
      }

      val formatHandler = FormatRegister.findFormat(format)

      if (formatHandler == null) {
        log("No support format.")
        reply(null)
        return@execute
      }

      val targetRotate = rotate + exifRotate

      try {
        val outputStream = ByteArrayOutputStream()
        formatHandler.handleByteArray(registrar.applicationContext, arr, outputStream, minWidth, minHeight, quality, targetRotate, keepExif, inSampleSize)
        reply(outputStream.toByteArray())
      } catch (e: CompressError) {
        log(e.message)
        if (FlutterImageCompressPlugin.showLog) e.printStackTrace()
        reply(null)
      } catch (e: Exception) {
        if (FlutterImageCompressPlugin.showLog) e.printStackTrace()
        reply(null)
      }
    }
  }


}