import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

import '../services/sip_service.dart';
import '../theme/app_theme.dart';

class CallScreen extends StatefulWidget {
  const CallScreen({
    super.key,
    this.incoming = false,
    this.video = false,
    this.remote,
  });

  final bool incoming;
  final bool video;
  final String? remote;

  @override
  State<CallScreen> createState() => _CallScreenState();
}

class _CallScreenState extends State<CallScreen> {
  bool _incoming = false;
  bool _video = false;
  bool _speakerOn = false;
  bool _micMuted = false;
  String _status = 'Connecting…';
  String _remoteName = '-';

  @override
  void initState() {
    super.initState();
    _incoming = widget.incoming;
    _video = widget.video;
    _remoteName = widget.remote ?? '-';
    _speakerOn = _video;
    _status = _incoming ? 'Incoming call…' : 'Calling…';

    SipService.instance.setSpeaker(_speakerOn);
    _listenEvents();
  }

  void _listenEvents() {
    SipService.instance.events.listen((event) {
      if (!event.isCall || !mounted) return;
      final state = event.state ?? '';
      if (event.remote != null && event.remote!.isNotEmpty) {
        setState(() => _remoteName = event.remote!);
      }
      switch (state) {
        case 'OutgoingProgress':
        case 'OutgoingInit':
          setState(() => _status = 'Calling…');
        case 'OutgoingRinging':
          setState(() => _status = 'Ringing…');
        case 'Connected':
        case 'StreamsRunning':
          setState(() {
            _status = 'Connected';
            _incoming = false;
            _video = event.video || _video;
          });
        case 'End':
        case 'Released':
        case 'Error':
          setState(() => _status = 'Call ended');
          Future.delayed(const Duration(milliseconds: 600), () {
            if (mounted) context.pop();
          });
      }
    });
  }

  Future<void> _answer() async {
    await SipService.instance.answer(video: _video);
    setState(() => _incoming = false);
  }

  Future<void> _decline() async {
    await SipService.instance.decline();
    if (mounted) context.pop();
  }

  Future<void> _hangUp() async {
    await SipService.instance.hangUp();
    if (mounted) context.pop();
  }

  Future<void> _toggleMute() async {
    _micMuted = !_micMuted;
    await SipService.instance.setMicMuted(_micMuted);
    setState(() {});
  }

  Future<void> _toggleSpeaker() async {
    _speakerOn = !_speakerOn;
    await SipService.instance.setSpeaker(_speakerOn);
    setState(() {});
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppTheme.callBg,
      body: SafeArea(
        child: Column(
          children: [
            const SizedBox(height: 32),
            CircleAvatar(
              radius: 48,
              backgroundColor: AppTheme.iconGreen.withOpacity(0.3),
              child: Icon(
                _video ? Icons.videocam : Icons.call,
                size: 40,
                color: Colors.white,
              ),
            ),
            const SizedBox(height: 16),
            Text(
              _remoteName,
              style: const TextStyle(
                color: Colors.white,
                fontSize: 24,
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 8),
            Text(
              _status,
              style: const TextStyle(color: Colors.white70, fontSize: 16),
            ),
            if (_video) ...[
              const SizedBox(height: 24),
              Expanded(
                child: Container(
                  margin: const EdgeInsets.symmetric(horizontal: 16),
                  decoration: BoxDecoration(
                    color: Colors.black26,
                    borderRadius: BorderRadius.circular(16),
                    border: Border.all(color: Colors.white24),
                  ),
                  child: const Center(
                    child: Text(
                      'Video (native surface)',
                      style: TextStyle(color: Colors.white54),
                    ),
                  ),
                ),
              ),
            ] else
              const Spacer(),
            const SizedBox(height: 24),
            if (_incoming) _incomingControls() else _inCallControls(),
            const SizedBox(height: 32),
          ],
        ),
      ),
    );
  }

  Widget _incomingControls() {
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceEvenly,
      children: [
        _roundButton(
          icon: Icons.call_end,
          color: AppTheme.danger,
          label: 'Decline',
          onTap: _decline,
        ),
        _roundButton(
          icon: Icons.call,
          color: AppTheme.success,
          label: 'Answer',
          onTap: _answer,
        ),
      ],
    );
  }

  Widget _inCallControls() {
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceEvenly,
      children: [
        _roundButton(
          icon: _micMuted ? Icons.mic_off : Icons.mic,
          color: Colors.white24,
          label: 'Mute',
          onTap: _toggleMute,
        ),
        _roundButton(
          icon: Icons.call_end,
          color: AppTheme.danger,
          label: 'Hang up',
          onTap: _hangUp,
          size: 64,
        ),
        _roundButton(
          icon: _speakerOn ? Icons.volume_up : Icons.volume_off,
          color: Colors.white24,
          label: 'Speaker',
          onTap: _toggleSpeaker,
        ),
      ],
    );
  }

  Widget _roundButton({
    required IconData icon,
    required Color color,
    required String label,
    required VoidCallback onTap,
    double size = 56,
  }) {
    return Column(
      children: [
        Material(
          color: color,
          shape: const CircleBorder(),
          child: InkWell(
            onTap: onTap,
            customBorder: const CircleBorder(),
            child: SizedBox(
              width: size,
              height: size,
              child: Icon(icon, color: Colors.white),
            ),
          ),
        ),
        const SizedBox(height: 6),
        Text(label, style: const TextStyle(color: Colors.white70, fontSize: 12)),
      ],
    );
  }
}
