/// Central configuration. SIP domain/proxy are updated at runtime when the
/// user picks a jail from the sites API ([applyJailServer]).
class AppConfig {
  AppConfig._();

  static const String defaultIp = '110.39.151.34';
  static const int sipPort = 5066;

  /// SIP realm: sip:<id>@<sipDomain>
  static String sipDomain = defaultIp;

  /// Registrar / proxy address passed to Linphone.
  static String sipProxy = 'sip:$defaultIp:$sipPort';

  /// Fixed SIP password for all prisoner registrations.
  static const String sipPassword = 'ppf@gcs123';

  /// Jails / sites listing API.
  static const String jailsApiUrl =
      'https://saddlebrown-chinchilla-896456.hostingersite.com/index.php';
  static const String jailsApiToken = r'GCS$PPF@9!';

  static const SipTransport sipTransport = SipTransport.udp;

  static const String provisioningUrl =
      'https://CHANGE_ME.example.com/sip/register';

  static void applyJailServer(String ip) {
    sipDomain = ip;
    sipProxy = 'sip:$ip:$sipPort';
  }

  /// Restore runtime SIP settings from saved prefs on cold start.
  static void restoreFromSavedDomain(String? domain) {
    if (domain != null && domain.isNotEmpty) {
      applyJailServer(domain);
    }
  }

  // Legacy aliases
  static String get sipDomainLegacy => sipDomain;
  static String get sipProxyLegacy => sipProxy;
}

enum SipTransport { udp, tcp, tls }
