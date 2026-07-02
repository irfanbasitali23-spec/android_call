import 'dart:async';

import 'package:flutter/services.dart';

import '../config/app_config.dart';
import '../models/call_record.dart';

/// Flutter bridge to native Linphone SIP via MethodChannel / EventChannel.
class SipService {
  SipService._();

  static final SipService instance = SipService._();

  static const MethodChannel _channel = MethodChannel('com.esibil.call/sip');
  static const EventChannel _events = EventChannel('com.esibil.call/sip_events');

  Stream<SipEvent>? _eventStream;

  Stream<SipEvent> get events {
    _eventStream ??= _events.receiveBroadcastStream().map((dynamic raw) {
      final map = Map<String, dynamic>.from(raw as Map);
      return SipEvent.fromMap(map);
    });
    return _eventStream!;
  }

  Future<void> register({
    required String sipId,
    String? password,
    String? domain,
    String? proxy,
  }) async {
    await _channel.invokeMethod<void>('register', {
      'sipId': sipId,
      'password': password ?? AppConfig.sipPassword,
      'domain': domain ?? AppConfig.sipDomain,
      'proxy': proxy ?? AppConfig.sipProxy,
    });
  }

  Future<void> unregister() async {
    await _channel.invokeMethod<void>('unregister');
  }

  Future<void> answer({bool video = false}) async {
    await _channel.invokeMethod<void>('answer', {'video': video});
  }

  Future<void> decline() async {
    await _channel.invokeMethod<void>('decline');
  }

  Future<void> hangUp() async {
    await _channel.invokeMethod<void>('hangUp');
  }

  Future<void> setSpeaker(bool on) async {
    await _channel.invokeMethod<void>('setSpeaker', {'on': on});
  }

  Future<void> setMicMuted(bool muted) async {
    await _channel.invokeMethod<void>('setMicMuted', {'muted': muted});
  }

  Future<void> startCall(String remote, {bool video = false}) async {
    await _channel.invokeMethod<void>('startCall', {
      'remote': remote,
      'video': video,
    });
  }

  Future<List<CallRecord>> getCallLogs({
    String? prisonerId,
    String? location,
  }) async {
    final result = await _channel.invokeMethod<List<dynamic>>('getCallLogs');
    if (result == null) return [];

    return result.map((e) {
      final map = Map<String, dynamic>.from(e as Map);
      if (prisonerId != null) map['prisonerId'] = prisonerId;
      if (location != null) map['location'] = location;
      return CallRecord.fromJson(map);
    }).toList();
  }
}

class SipEvent {
  const SipEvent({
    required this.type,
    this.state,
    this.message,
    this.remote,
    this.video = false,
  });

  final String type;
  final String? state;
  final String? message;
  final String? remote;
  final bool video;

  factory SipEvent.fromMap(Map<String, dynamic> map) {
    return SipEvent(
      type: map['type'] as String? ?? '',
      state: map['state'] as String?,
      message: map['message'] as String?,
      remote: map['remote'] as String?,
      video: map['video'] as bool? ?? false,
    );
  }

  bool get isRegistration => type == 'registration';
  bool get isCall => type == 'call';
  bool get isIncomingCall =>
      isCall && state == 'IncomingReceived';
}
