import 'package:flutter/material.dart';

import '../models/call_record.dart';
import '../services/prefs_service.dart';
import '../services/sip_service.dart';
import '../theme/app_theme.dart';

class CallHistoryScreen extends StatefulWidget {
  const CallHistoryScreen({super.key, required this.prefs});

  final PrefsService prefs;

  @override
  State<CallHistoryScreen> createState() => _CallHistoryScreenState();
}

class _CallHistoryScreenState extends State<CallHistoryScreen> {
  List<CallRecord> _records = [];
  bool _loading = true;
  String? _error;

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    setState(() {
      _loading = true;
      _error = null;
    });
    try {
      final logs = await SipService.instance.getCallLogs(
        prisonerId: widget.prefs.sipId,
        location: widget.prefs.jailName,
      );
      if (!mounted) return;
      setState(() {
        _records = logs;
        _loading = false;
      });
    } catch (e) {
      if (!mounted) return;
      setState(() {
        _error = e.toString();
        _loading = false;
      });
    }
  }

  (int total, int completed, int minutes) _stats() {
    var completed = 0;
    var totalSeconds = 0;
    for (final r in _records) {
      if (r.status == CallStatus.completed) {
        completed++;
        totalSeconds += _parseDuration(r.duration);
      }
    }
    return (_records.length, completed, totalSeconds ~/ 60);
  }

  int _parseDuration(String? duration) {
    if (duration == null || !duration.contains(':')) return 0;
    final parts = duration.split(':');
    if (parts.length != 2) return 0;
    return (int.tryParse(parts[0]) ?? 0) * 60 + (int.tryParse(parts[1]) ?? 0);
  }

  @override
  Widget build(BuildContext context) {
    final stats = _stats();

    return Scaffold(
      appBar: AppBar(title: const Text('Call History')),
      body: _loading
          ? const Center(child: CircularProgressIndicator())
          : _error != null
              ? Center(
                  child: Column(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Text(_error!, textAlign: TextAlign.center),
                      TextButton(onPressed: _load, child: const Text('Retry')),
                    ],
                  ),
                )
              : RefreshIndicator(
                  onRefresh: _load,
                  child: _records.isEmpty
                      ? ListView(
                          children: const [
                            SizedBox(height: 120),
                            Center(child: Text('No call history yet')),
                          ],
                        )
                      : ListView(
                          padding: const EdgeInsets.all(16),
                          children: [
                            Row(
                              children: [
                                _statCard('Total', '${stats.$1}'),
                                const SizedBox(width: 8),
                                _statCard('Completed', '${stats.$2}'),
                                const SizedBox(width: 8),
                                _statCard('Minutes', '${stats.$3}'),
                              ],
                            ),
                            const SizedBox(height: 16),
                            ..._records.map(_recordTile),
                          ],
                        ),
                ),
    );
  }

  Widget _statCard(String label, String value) {
    return Expanded(
      child: Card(
        child: Padding(
          padding: const EdgeInsets.symmetric(vertical: 14),
          child: Column(
            children: [
              Text(
                value,
                style: const TextStyle(
                  fontSize: 20,
                  fontWeight: FontWeight.bold,
                  color: AppTheme.iconGreen,
                ),
              ),
              Text(label, style: const TextStyle(fontSize: 12, color: AppTheme.textMuted)),
            ],
          ),
        ),
      ),
    );
  }

  Widget _recordTile(CallRecord record) {
    final (bg, fg) = switch (record.status) {
      CallStatus.completed => (const Color(0xFFDCEEE4), AppTheme.iconGreen),
      CallStatus.missed => (const Color(0xFFFBE6D2), const Color(0xFFC97A1D)),
      CallStatus.rejected => (const Color(0xFFFADBDB), const Color(0xFFD64545)),
    };

    return Card(
      margin: const EdgeInsets.only(bottom: 10),
      child: Padding(
        padding: const EdgeInsets.all(14),
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            CircleAvatar(
              backgroundColor: bg,
              child: Icon(
                record.status == CallStatus.completed
                    ? Icons.call
                    : Icons.call_missed,
                color: fg,
                size: 20,
              ),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(record.name, style: const TextStyle(fontWeight: FontWeight.bold)),
                  Text('${record.date} · ${record.time}',
                      style: const TextStyle(fontSize: 12, color: AppTheme.textMuted)),
                  if (record.duration != null)
                    Text('Duration: ${record.duration}',
                        style: const TextStyle(fontSize: 12, color: AppTheme.textGray)),
                ],
              ),
            ),
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
              decoration: BoxDecoration(
                color: bg,
                borderRadius: BorderRadius.circular(8),
              ),
              child: Text(
                record.status.label,
                style: TextStyle(fontSize: 11, color: fg, fontWeight: FontWeight.w600),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
