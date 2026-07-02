/// Immutable representation of a single call history entry.
class CallRecord {
  const CallRecord({
    required this.name,
    required this.prisonerId,
    required this.location,
    required this.date,
    required this.time,
    required this.duration,
    required this.status,
  });

  final String name;
  final String prisonerId;
  final String location;
  final String date;
  final String time;
  final String? duration;
  final CallStatus status;

  factory CallRecord.fromJson(Map<String, dynamic> json) {
    return CallRecord(
      name: json['name'] as String? ?? 'Unknown',
      prisonerId: json['prisonerId'] as String? ?? '-',
      location: json['location'] as String? ?? '-',
      date: json['date'] as String? ?? '',
      time: json['time'] as String? ?? '',
      duration: json['duration'] as String?,
      status: CallStatus.fromString(json['status'] as String? ?? 'missed'),
    );
  }
}

enum CallStatus {
  completed,
  missed,
  rejected;

  static CallStatus fromString(String value) {
    switch (value.toLowerCase()) {
      case 'completed':
        return CallStatus.completed;
      case 'rejected':
        return CallStatus.rejected;
      default:
        return CallStatus.missed;
    }
  }

  String get label {
    switch (this) {
      case CallStatus.completed:
        return 'Completed';
      case CallStatus.missed:
        return 'Missed';
      case CallStatus.rejected:
        return 'Rejected';
    }
  }
}
