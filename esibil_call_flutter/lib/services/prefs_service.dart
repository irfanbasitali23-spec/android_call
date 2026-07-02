import 'package:shared_preferences/shared_preferences.dart';

/// Persistent store for SIP credentials, profile info and onboarding state.
class PrefsService {
  PrefsService(this._sp);

  final SharedPreferences _sp;

  static const _keyPhone = 'phone';
  static const _keySipId = 'sip_id';
  static const _keyPassword = 'password';
  static const _keyDomain = 'domain';
  static const _keyDisplayName = 'display_name';
  static const _keyJailName = 'jail_name';
  static const _keyJailIp = 'jail_ip';
  static const _keySetupComplete = 'setup_complete';
  static const _keyPhotoPrefix = 'photo_path_';

  String? get phone => _sp.getString(_keyPhone);
  set phone(String? v) => _setString(_keyPhone, v);

  String? get sipId => _sp.getString(_keySipId);
  set sipId(String? v) => _setString(_keySipId, v);

  String? get password => _sp.getString(_keyPassword);
  set password(String? v) => _setString(_keyPassword, v);

  String? get domain => _sp.getString(_keyDomain);
  set domain(String? v) => _setString(_keyDomain, v);

  String? get displayName => _sp.getString(_keyDisplayName);
  set displayName(String? v) => _setString(_keyDisplayName, v);

  String? get jailName => _sp.getString(_keyJailName);
  set jailName(String? v) => _setString(_keyJailName, v);

  String? get jailIp => _sp.getString(_keyJailIp);
  set jailIp(String? v) => _setString(_keyJailIp, v);

  bool get isSetupComplete => _sp.getBool(_keySetupComplete) ?? false;
  set isSetupComplete(bool v) => _sp.setBool(_keySetupComplete, v);

  bool get isRegistered =>
      (sipId?.isNotEmpty ?? false) && (password?.isNotEmpty ?? false);

  bool get isLoggedIn => isSetupComplete && isRegistered;

  String? photoPath(int index) => _sp.getString('$_keyPhotoPrefix$index');

  Future<void> setPhotoPath(int index, String? path) async {
    if (path == null) {
      await _sp.remove('$_keyPhotoPrefix$index');
    } else {
      await _sp.setString('$_keyPhotoPrefix$index', path);
    }
  }

  Future<void> saveProfile({
    required String phone,
    required String sipId,
    required String password,
    required String domain,
    required String displayName,
    required String jailName,
    required String jailIp,
  }) async {
    await _sp.setString(_keyPhone, phone);
    await _sp.setString(_keySipId, sipId);
    await _sp.setString(_keyPassword, password);
    await _sp.setString(_keyDomain, domain);
    await _sp.setString(_keyDisplayName, displayName);
    await _sp.setString(_keyJailName, jailName);
    await _sp.setString(_keyJailIp, jailIp);
    await _sp.setBool(_keySetupComplete, true);
  }

  Future<void> clear() => _sp.clear();

  void _setString(String key, String? value) {
    if (value == null) {
      _sp.remove(key);
    } else {
      _sp.setString(key, value);
    }
  }

  static Future<PrefsService> create() async {
    final sp = await SharedPreferences.getInstance();
    return PrefsService(sp);
  }
}
