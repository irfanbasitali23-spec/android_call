import 'package:flutter_test/flutter_test.dart';
import 'package:esibil_call_flutter/config/app_config.dart';

void main() {
  test('AppConfig has expected SIP defaults', () {
    expect(AppConfig.sipPassword, 'ppf@gcs123');
    expect(AppConfig.sipPort, 5066);
    expect(AppConfig.jailsApiToken, r'GCS$PPF@9!');
  });
}
