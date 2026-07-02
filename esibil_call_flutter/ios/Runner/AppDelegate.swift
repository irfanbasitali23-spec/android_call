import Flutter
import UIKit

@main
@objc class AppDelegate: FlutterAppDelegate {
  private let sipChannel = "com.esibil.call/sip"
  private let sipEventChannel = "com.esibil.call/sip_events"

  override func application(
    _ application: UIApplication,
    didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
  ) -> Bool {
    GeneratedPluginRegistrant.register(with: self)

    if let controller = window?.rootViewController as? FlutterViewController {
      let methodChannel = FlutterMethodChannel(
        name: sipChannel,
        binaryMessenger: controller.binaryMessenger
      )
      methodChannel.setMethodCallHandler { call, result in
        result(FlutterMethodNotImplemented)
      }

      let eventChannel = FlutterEventChannel(
        name: sipEventChannel,
        binaryMessenger: controller.binaryMessenger
      )
      eventChannel.setStreamHandler(SipEventStreamHandler())
    }

    return super.application(application, didFinishLaunchingWithOptions: launchOptions)
  }
}

private class SipEventStreamHandler: NSObject, FlutterStreamHandler {
  func onListen(withArguments arguments: Any?, eventSink events: @escaping FlutterEventSink) -> FlutterError? {
    return nil
  }

  func onCancel(withArguments arguments: Any?) -> FlutterError? {
    return nil
  }
}
