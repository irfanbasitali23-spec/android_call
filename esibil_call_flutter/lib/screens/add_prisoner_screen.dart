import 'dart:io';

import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:image_picker/image_picker.dart';
import 'package:permission_handler/permission_handler.dart';

import '../config/app_config.dart';
import '../models/jail_site.dart';
import '../router/app_router.dart';
import '../services/jails_api.dart';
import '../services/prefs_service.dart';
import '../services/sip_service.dart';
import '../theme/app_theme.dart';

class AddPrisonerScreen extends StatefulWidget {
  const AddPrisonerScreen({super.key, required this.prefs});

  final PrefsService prefs;

  @override
  State<AddPrisonerScreen> createState() => _AddPrisonerScreenState();
}

class _AddPrisonerScreenState extends State<AddPrisonerScreen> {
  final _usernameController = TextEditingController();
  final _displayNameController = TextEditingController();
  final _passwordController =
      TextEditingController(text: AppConfig.sipPassword);

  final _jailsApi = JailsApi();
  final _picker = ImagePicker();

  List<JailSite> _jails = [];
  JailSite? _selectedJail;
  bool _loadingJails = true;
  bool _busy = false;
  String? _jailError;

  final List<XFile?> _photos = [null, null, null];

  JailSite? _pendingJail;
  String _pendingUsername = '';
  String _pendingDisplayName = '';

  @override
  void initState() {
    super.initState();
    _loadJails();
    _listenSipEvents();
  }

  void _listenSipEvents() {
    SipService.instance.events.listen((event) {
      if (!event.isRegistration || !mounted) return;
      final state = event.state ?? '';
      if (state == 'Progress') {
        setState(() => _busy = true);
      } else if (state == 'Ok') {
        _onRegistrationSuccess();
      } else if (state == 'Failed') {
        setState(() => _busy = false);
        _showSnack('Registration Failed: ${event.message ?? ''}');
      } else {
        setState(() => _busy = false);
      }
    });
  }

  Future<void> _loadJails() async {
    setState(() {
      _loadingJails = true;
      _jailError = null;
    });
    try {
      final sites = await _jailsApi.fetchSites();
      if (!mounted) return;
      setState(() {
        _jails = sites;
        _loadingJails = false;
      });
    } catch (e) {
      if (!mounted) return;
      setState(() {
        _loadingJails = false;
        _jailError = e.toString();
      });
    }
  }

  Future<void> _capturePhoto(int index) async {
    final cam = await Permission.camera.request();
    if (!cam.isGranted) {
      _showSnack('Camera permission is required');
      return;
    }
    final file = await _picker.pickImage(
      source: ImageSource.camera,
      preferredCameraDevice: CameraDevice.front,
      imageQuality: 85,
    );
    if (file != null && mounted) {
      setState(() => _photos[index] = file);
    }
  }

  Future<void> _performLogin() async {
    final jail = _selectedJail;
    if (jail == null) {
      _showSnack('Please select a jail');
      return;
    }

    final username = _usernameController.text.trim();
    if (username.isEmpty) {
      _showSnack('Please enter your phone number');
      return;
    }

    if (_photos.any((p) => p == null)) {
      _showSnack('Please take all 3 photos');
      return;
    }

    final displayName = _displayNameController.text.trim().isEmpty
        ? username
        : _displayNameController.text.trim();

    await Permission.microphone.request();
    await Permission.notification.request();

    AppConfig.applyJailServer(jail.ip);
    setState(() => _busy = true);

    _pendingUsername = username;
    _pendingDisplayName = displayName;
    _pendingJail = jail;

    try {
      await SipService.instance.register(sipId: username);
    } catch (e) {
      setState(() => _busy = false);
      _showSnack('Registration error: $e');
    }
  }

  Future<void> _onRegistrationSuccess() async {
    final jail = _pendingJail;
    if (jail == null) return;

    await widget.prefs.saveProfile(
      phone: _pendingUsername,
      sipId: _pendingUsername,
      password: AppConfig.sipPassword,
      domain: AppConfig.sipDomain,
      displayName: _pendingDisplayName,
      jailName: jail.jailName,
      jailIp: jail.ip,
    );

    for (var i = 0; i < _photos.length; i++) {
      await widget.prefs.setPhotoPath(i, _photos[i]?.path);
    }

    if (!mounted) return;
    setState(() => _busy = false);
    _showSnack('Login Successful! / کامیابی سے لاگ ان ہو گیا!');
    context.go(AppRouter.home);
  }

  void _showSnack(String msg) {
    if (!mounted) return;
    ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(msg)));
  }

  @override
  void dispose() {
    _usernameController.dispose();
    _displayNameController.dispose();
    _passwordController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Column(
          children: [
            Text('Add Prisoner Information', style: TextStyle(fontSize: 15)),
            Text('قیدی کی معلومات شامل کریں', style: TextStyle(fontSize: 11)),
          ],
        ),
      ),
      body: AbsorbPointer(
        absorbing: _busy,
        child: Opacity(
          opacity: _busy ? 0.7 : 1,
          child: SingleChildScrollView(
            padding: const EdgeInsets.all(20),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                _label('Select Jail / Prison', 'جیل منتخب کریں'),
                if (_loadingJails)
                  const Padding(
                    padding: EdgeInsets.symmetric(vertical: 12),
                    child: LinearProgressIndicator(),
                  )
                else if (_jailError != null)
                  Column(
                    crossAxisAlignment: CrossAxisAlignment.stretch,
                    children: [
                      Text(_jailError!, style: const TextStyle(color: AppTheme.danger)),
                      TextButton(onPressed: _loadJails, child: const Text('Retry')),
                    ],
                  )
                else
                  DropdownButtonFormField<JailSite>(
                    value: _selectedJail,
                    decoration: const InputDecoration(
                      hintText: '---- Select Jail ----',
                    ),
                    items: _jails
                        .map(
                          (j) => DropdownMenuItem(
                            value: j,
                            child: Text(j.jailName),
                          ),
                        )
                        .toList(),
                    onChanged: (j) {
                      setState(() => _selectedJail = j);
                      if (j != null) AppConfig.applyJailServer(j.ip);
                    },
                  ),
                const SizedBox(height: 16),
                _label('Username (Phone Number)', 'صارف نام (فون نمبر)'),
                TextField(
                  controller: _usernameController,
                  keyboardType: TextInputType.phone,
                  decoration: const InputDecoration(
                    hintText: 'Enter phone number',
                  ),
                ),
                const SizedBox(height: 16),
                _label('Password', 'پاس ورڈ'),
                TextField(
                  controller: _passwordController,
                  readOnly: true,
                  obscureText: true,
                  decoration: const InputDecoration(
                    hintText: 'Fixed password',
                  ),
                ),
                const SizedBox(height: 16),
                _label('Display Name (optional)', 'ڈسپلے نام (اختیاری)'),
                TextField(
                  controller: _displayNameController,
                  decoration: const InputDecoration(
                    hintText: 'Enter your name',
                  ),
                ),
                const SizedBox(height: 16),
                _label('Take 3 photos', 'تین تصاویر لیں'),
                const SizedBox(height: 8),
                Row(
                  children: List.generate(3, (i) => Expanded(child: _photoSlot(i))),
                ),
                const SizedBox(height: 32),
                SizedBox(
                  width: double.infinity,
                  child: ElevatedButton(
                    onPressed: _busy ? null : _performLogin,
                    child: _busy
                        ? const SizedBox(
                            height: 22,
                            width: 22,
                            child: CircularProgressIndicator(strokeWidth: 2),
                          )
                        : const Column(
                            children: [
                              Text('Login', style: TextStyle(fontWeight: FontWeight.bold)),
                              Text('لاگ اِن', style: TextStyle(fontSize: 12)),
                            ],
                          ),
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }

  Widget _label(String en, String ur) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 6),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            en,
            style: const TextStyle(
              fontWeight: FontWeight.w600,
              color: AppTheme.loginHeading,
            ),
          ),
          Text(ur, style: const TextStyle(fontSize: 12, color: AppTheme.textMuted)),
        ],
      ),
    );
  }

  Widget _photoSlot(int index) {
    final photo = _photos[index];
    return Padding(
      padding: EdgeInsets.only(left: index == 0 ? 0 : 4, right: index == 2 ? 0 : 4),
      child: InkWell(
        onTap: () => _capturePhoto(index),
        borderRadius: BorderRadius.circular(12),
        child: AspectRatio(
          aspectRatio: 1,
          child: Container(
            decoration: BoxDecoration(
              border: Border.all(color: AppTheme.iconGreen, width: 2),
              borderRadius: BorderRadius.circular(12),
              color: const Color(0xFFDCEEE4),
            ),
            child: photo != null
                ? ClipRRect(
                    borderRadius: BorderRadius.circular(10),
                    child: Image.file(File(photo.path), fit: BoxFit.cover),
                  )
                : Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      Icon(Icons.camera_alt, color: AppTheme.iconGreen.withOpacity(0.8)),
                      const SizedBox(height: 4),
                      Text('Photo ${index + 1}', style: const TextStyle(fontSize: 11)),
                    ],
                  ),
          ),
        ),
      ),
    );
  }
}
