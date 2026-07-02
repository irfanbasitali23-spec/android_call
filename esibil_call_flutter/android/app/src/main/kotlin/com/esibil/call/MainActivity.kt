package com.esibil.call

import android.os.Bundle
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity() {

    private var linphoneBridge: LinphoneBridge? = null

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        val bridge = LinphoneBridge(applicationContext)
        linphoneBridge = bridge

        MethodChannel(
            flutterEngine.dartExecutor.binaryMessenger,
            CHANNEL
        ).setMethodCallHandler(bridge)

        EventChannel(
            flutterEngine.dartExecutor.binaryMessenger,
            EVENT_CHANNEL
        ).setStreamHandler(bridge)
    }

    override fun onDestroy() {
        linphoneBridge?.dispose()
        linphoneBridge = null
        super.onDestroy()
    }

    companion object {
        private const val CHANNEL = "com.esibil.call/sip"
        private const val EVENT_CHANNEL = "com.esibil.call/sip_events"
    }
}
