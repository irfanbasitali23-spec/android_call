import 'package:flutter/material.dart';

class AppTheme {
  AppTheme._();

  // Green / navy prison palette (matches Android app)
  static const Color primary = Color(0xFF00695C);
  static const Color primaryDark = Color(0xFF004D40);
  static const Color accent = Color(0xFF26A69A);
  static const Color navyDark = Color(0xFF152A4A);
  static const Color loginHeading = Color(0xFF12324B);
  static const Color loginTeal = Color(0xFF0E7A6B);
  static const Color loginBg = Color(0xFFF1F6F3);
  static const Color headerStart = Color(0xFF1B3A3A);
  static const Color headerEnd = Color(0xFF2E6B52);
  static const Color iconGreen = Color(0xFF2E8B57);
  static const Color iconNavy = Color(0xFF1B3A5C);
  static const Color bottomNavActive = Color(0xFF2E6B52);
  static const Color bottomNavInactive = Color(0xFF9CA3AF);
  static const Color textMuted = Color(0xFF7A8691);
  static const Color textGray = Color(0xFF6B7280);
  static const Color danger = Color(0xFFE53935);
  static const Color success = Color(0xFF43A047);
  static const Color callBg = Color(0xFF101418);
  static const Color reminderBg = Color(0xFFFDF3D6);
  static const Color reminderBorder = Color(0xFFF0DFA0);
  static const Color reminderText = Color(0xFFB4770F);

  static ThemeData light() {
    final base = ThemeData(
      useMaterial3: true,
      brightness: Brightness.light,
      colorScheme: ColorScheme.fromSeed(
        seedColor: primary,
        primary: primary,
        secondary: accent,
        surface: Colors.white,
      ),
      scaffoldBackgroundColor: loginBg,
      appBarTheme: const AppBarTheme(
        backgroundColor: headerStart,
        foregroundColor: Colors.white,
        elevation: 0,
        centerTitle: true,
      ),
      elevatedButtonTheme: ElevatedButtonThemeData(
        style: ElevatedButton.styleFrom(
          backgroundColor: iconGreen,
          foregroundColor: Colors.white,
          padding: const EdgeInsets.symmetric(vertical: 14, horizontal: 24),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(12),
          ),
        ),
      ),
      inputDecorationTheme: InputDecorationTheme(
        filled: true,
        fillColor: Colors.white,
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(12),
          borderSide: const BorderSide(color: Color(0xFFDDE6E1)),
        ),
        enabledBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(12),
          borderSide: const BorderSide(color: Color(0xFFDDE6E1)),
        ),
        focusedBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(12),
          borderSide: const BorderSide(color: loginTeal, width: 2),
        ),
        contentPadding:
            const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
      ),
      bottomNavigationBarTheme: const BottomNavigationBarThemeData(
        backgroundColor: Colors.white,
        selectedItemColor: bottomNavActive,
        unselectedItemColor: bottomNavInactive,
        type: BottomNavigationBarType.fixed,
        elevation: 8,
      ),
      cardTheme: CardThemeData(
        color: Colors.white,
        elevation: 0,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(16),
          side: const BorderSide(color: Color(0xFFE7ECF3)),
        ),
      ),
    );
    return base;
  }

  static LinearGradient headerGradient = const LinearGradient(
    colors: [headerStart, headerEnd],
    begin: Alignment.topLeft,
    end: Alignment.bottomRight,
  );

  static LinearGradient primaryButtonGradient = const LinearGradient(
    colors: [Color(0xFF1E9B63), Color(0xFF0C6B4C)],
    begin: Alignment.centerLeft,
    end: Alignment.centerRight,
  );
}
