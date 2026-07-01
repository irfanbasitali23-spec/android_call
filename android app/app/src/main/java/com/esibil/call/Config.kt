package com.esibil.call

/**
 * Central configuration. SIP domain/proxy are updated at runtime when the
 * user picks a jail from the sites API ([applyJailServer]).
 */
object Config {

    private const val DEFAULT_IP = "110.39.151.34"
    private const val SIP_PORT = 5066

    /** SIP realm: sip:&lt;id&gt;@&lt;sipDomain&gt; */
    var sipDomain: String = DEFAULT_IP
        private set

    /** Registrar / proxy address passed to Linphone. */
    var sipProxy: String = "sip:$DEFAULT_IP:$SIP_PORT"
        private set

    /** Fixed SIP password for all prisoner registrations. */
    const val SIP_PASSWORD = "ppf@gcs123"

    /** Jails / sites listing API. */
    const val JAILS_API_URL =
        "https://saddlebrown-chinchilla-896456.hostingersite.com/index.php"
    const val JAILS_API_TOKEN = "GCS\$PPF@9!"

    val SIP_TRANSPORT = Transport.Udp

    const val PROVISIONING_URL = "https://CHANGE_ME.example.com/sip/register"

    fun applyJailServer(ip: String) {
        sipDomain = ip
        sipProxy = "sip:$ip:$SIP_PORT"
    }

    /** Restore runtime SIP settings from saved prefs on cold start. */
    fun restoreFromSavedDomain(domain: String?) {
        if (!domain.isNullOrBlank()) applyJailServer(domain)
    }

    // Legacy aliases used by older call sites
    val SIP_DOMAIN: String get() = sipDomain
    val SIP_PROXY: String get() = sipProxy

    enum class Transport { Udp, Tcp, Tls }
}
