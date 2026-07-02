import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

import '../router/app_router.dart';
import '../services/prefs_service.dart';
import '../theme/app_theme.dart';

class WelcomeScreen extends StatelessWidget {
  const WelcomeScreen({super.key, required this.prefs});

  final PrefsService prefs;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.all(24),
          child: Column(
            children: [
              const Spacer(flex: 2),
              Container(
                width: 88,
                height: 88,
                decoration: BoxDecoration(
                  gradient: AppTheme.primaryButtonGradient,
                  borderRadius: BorderRadius.circular(20),
                ),
                child: const Icon(Icons.videocam, color: Colors.white, size: 44),
              ),
              const SizedBox(height: 24),
              Text(
                'Welcome to Video Call App',
                textAlign: TextAlign.center,
                style: Theme.of(context).textTheme.headlineSmall?.copyWith(
                      color: AppTheme.loginHeading,
                      fontWeight: FontWeight.bold,
                    ),
              ),
              const SizedBox(height: 8),
              Text(
                'محکمہ داخلہ، حکومت پنجاب',
                style: Theme.of(context).textTheme.titleMedium?.copyWith(
                      color: AppTheme.textGray,
                    ),
              ),
              const SizedBox(height: 4),
              Text(
                'خوش آمدید',
                style: Theme.of(context).textTheme.titleLarge?.copyWith(
                      color: AppTheme.loginTeal,
                      fontWeight: FontWeight.w600,
                    ),
              ),
              const SizedBox(height: 32),
              Text(
                'To start a video call, please add prisoner details first.',
                textAlign: TextAlign.center,
                style: Theme.of(context).textTheme.bodyLarge?.copyWith(
                      color: AppTheme.textGray,
                    ),
              ),
              const SizedBox(height: 8),
              Text(
                'ویڈیو کال شروع کرنے کے لیے، پہلے قیدی کی تفصیلات شامل کریں',
                textAlign: TextAlign.center,
                style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                      color: AppTheme.textMuted,
                    ),
              ),
              const Spacer(flex: 3),
              SizedBox(
                width: double.infinity,
                child: DecoratedBox(
                  decoration: BoxDecoration(
                    gradient: AppTheme.primaryButtonGradient,
                    borderRadius: BorderRadius.circular(14),
                  ),
                  child: ElevatedButton(
                    onPressed: () => context.push(AppRouter.beforeContinue),
                    style: ElevatedButton.styleFrom(
                      backgroundColor: Colors.transparent,
                      shadowColor: Colors.transparent,
                      padding: const EdgeInsets.symmetric(vertical: 16),
                    ),
                    child: const Column(
                      children: [
                        Text(
                          'Add Prisoner Information',
                          style: TextStyle(
                            fontSize: 16,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                        SizedBox(height: 4),
                        Text('قیدی کی معلومات شامل کریں'),
                      ],
                    ),
                  ),
                ),
              ),
              const SizedBox(height: 24),
            ],
          ),
        ),
      ),
    );
  }
}
