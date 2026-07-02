import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

import '../screens/add_prisoner_screen.dart';
import '../screens/before_continue_screen.dart';
import '../screens/call_history_screen.dart';
import '../screens/call_screen.dart';
import '../screens/home_screen.dart';
import '../screens/welcome_screen.dart';
import '../services/prefs_service.dart';

class AppRouter {
  AppRouter(this.prefs);

  final PrefsService prefs;

  static const welcome = '/';
  static const beforeContinue = '/before-continue';
  static const addPrisoner = '/add-prisoner';
  static const home = '/home';
  static const callHistory = '/call-history';
  static const call = '/call';

  late final GoRouter router = GoRouter(
    initialLocation: welcome,
    redirect: (context, state) {
      final loggedIn = prefs.isLoggedIn;
      final loc = state.matchedLocation;
      final isOnboarding = loc == welcome ||
          loc == beforeContinue ||
          loc == addPrisoner;

      if (loggedIn && isOnboarding) return home;
      if (!loggedIn && (loc == home || loc == callHistory)) return welcome;
      return null;
    },
    routes: [
      GoRoute(
        path: welcome,
        builder: (context, state) => WelcomeScreen(prefs: prefs),
      ),
      GoRoute(
        path: beforeContinue,
        builder: (context, state) => const BeforeContinueScreen(),
      ),
      GoRoute(
        path: addPrisoner,
        builder: (context, state) => AddPrisonerScreen(prefs: prefs),
      ),
      GoRoute(
        path: home,
        builder: (context, state) => HomeScreen(prefs: prefs),
      ),
      GoRoute(
        path: callHistory,
        builder: (context, state) => CallHistoryScreen(prefs: prefs),
      ),
      GoRoute(
        path: call,
        builder: (context, state) {
          final extra = state.extra as Map<String, dynamic>? ?? {};
          return CallScreen(
            incoming: extra['incoming'] as bool? ?? false,
            video: extra['video'] as bool? ?? false,
            remote: extra['remote'] as String?,
          );
        },
      ),
    ],
    errorBuilder: (context, state) => Scaffold(
      body: Center(child: Text('Route not found: ${state.uri}')),
    ),
  );
}
