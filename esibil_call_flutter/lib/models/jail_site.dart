class JailSite {
  const JailSite({
    required this.ip,
    required this.jailName,
    required this.status,
  });

  final String ip;
  final String jailName;
  final String status;

  factory JailSite.fromJson(Map<String, dynamic> json) {
    return JailSite(
      ip: json['ip'] as String,
      jailName: json['jail_name'] as String,
      status: json['status'] as String? ?? 'up',
    );
  }
}
