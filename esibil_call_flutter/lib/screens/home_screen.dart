import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

import '../router/app_router.dart';
import '../services/prefs_service.dart';
import '../services/sip_service.dart';
import '../theme/app_theme.dart';
import 'home_tab.dart';
import 'profile_tab.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key, required this.prefs});

  final PrefsService prefs;

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  int _index = 0;

  Future<void> _logout() async {
    try {
      await SipService.instance.unregister();
    } catch (_) {}
    await widget.prefs.clear();
    if (!mounted) return;
    context.go(AppRouter.welcome);
  }

  void _switchToProfile() => setState(() => _index = 1);

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: IndexedStack(
        index: _index,
        children: [
          HomeTab(
            prefs: widget.prefs,
            onOpenProfile: _switchToProfile,
          ),
          ProfileTab(prefs: widget.prefs, onLogout: _logout),
        ],
      ),
      bottomNavigationBar: BottomNavigationBar(
        currentIndex: _index,
        onTap: (i) => setState(() => _index = i),
        items: const [
          BottomNavigationBarItem(
            icon: Icon(Icons.home),
            label: 'Home',
          ),
          BottomNavigationBarItem(
            icon: Icon(Icons.person),
            label: 'Profile',
          ),
        ],
      ),
    );
  }
}
