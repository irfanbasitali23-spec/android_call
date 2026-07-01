package com.esibil.call

/**
 * Central configuration.
 *
 * ┌──────────────────────────────────────────────────────────────────────┐
 * │  EDIT THESE VALUES to point the app at your own SIP / eSIBiL server.   │
 * └──────────────────────────────────────────────────────────────────────┘
 *
 * Everything below is a PLACEHOLDER. Replace with your real server details.
 */
object Config {

    // ---- SIP / eSIBiL calling server ------------------------------------
    // The SIP domain (realm) used in SIP addresses:  sip:<id>@<SIP_DOMAIN>
    const val SIP_DOMAIN = "110.39.151.34"

    // The SIP proxy / registrar. Usually the same host as the domain, but
    // can be an IP:port if your Asterisk / eSIBiL server is reached directly.
    // Example: "sip:sip.example.com:5060"  or  "sip:41.222.333.44:5060"
    const val SIP_PROXY = "sip:110.39.151.34:5066"

    // Transport used to reach the server: Udp, Tcp or Tls (Tls recommended
    // for production). Must match how your server is configured.
    val SIP_TRANSPORT = Transport.Udp

    // ---- Registration / provisioning backend ----------------------------
    // Your HTTP endpoint that validates a phone number and returns the SIP
    // credentials the server has assigned to that user.
    //
    // Expected request  (POST, application/json):
    //     { "phone": "+923001234567" }
    //
    // Expected response (200, application/json):
    //     { "sipId": "923001234567", "password": "s3cr3t", "domain": "sip.example.com" }
    //
    // Set to a real URL to enable live provisioning. While it still contains
    // "CHANGE_ME", the app falls back to DEV mode (see RegistrationApi) so you
    // can test calling without a backend.
    const val PROVISIONING_URL = "https://CHANGE_ME.example.com/sip/register"

    enum class Transport { Udp, Tcp, Tls }
}
