import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

import '../router/app_router.dart';
import '../services/prefs_service.dart';
import '../theme/app_theme.dart';

class HomeTab extends StatelessWidget {
  const HomeTab({
    super.key,
    required this.prefs,
    required this.onOpenProfile,
  });

  final PrefsService prefs;
  final VoidCallback onOpenProfile;

  @override
  Widget build(BuildContext context) {
    final name = prefs.displayName ?? prefs.phone ?? 'User';
    final prisonerNumber = prefs.sipId ?? '-';
    final jail = prefs.jailName ?? '-';

    return CustomScrollView(
      slivers: [
        SliverAppBar(
          expandedHeight: 160,
          pinned: true,
          flexibleSpace: FlexibleSpaceBar(
            background: Container(
              decoration: BoxDecoration(gradient: AppTheme.headerGradient),
              padding: const EdgeInsets.fromLTRB(20, 56, 20, 20),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                mainAxisAlignment: MainAxisAlignment.end,
                children: [
                  Text(
                    'Hello, $name',
                    style: const TextStyle(
                      color: Colors.white,
                      fontSize: 22,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                  const SizedBox(height: 4),
                  Text(
                    'Prisoner #$prisonerNumber',
                    style: const TextStyle(color: Colors.white70),
                  ),
                ],
              ),
            ),
          ),
        ),
        SliverPadding(
          padding: const EdgeInsets.all(16),
          sliver: SliverList(
            delegate: SliverChildListDelegate([
              _infoCard(
                icon: Icons.badge_outlined,
                iconColor: AppTheme.iconGreen,
                iconBg: const Color(0xFFDCEEE4),
                title: 'Prisoner Number',
                value: prisonerNumber,
              ),
              const SizedBox(height: 12),
              _infoCard(
                icon: Icons.location_on_outlined,
                iconColor: AppTheme.iconNavy,
                iconBg: const Color(0xFFE7ECF3),
                title: 'Location',
                value: jail,
              ),
              const SizedBox(height: 20),
              _actionCard(
                context,
                icon: Icons.history,
                color: AppTheme.iconGreen,
                bg: const Color(0xFFDCEEE4),
                title: 'Call History',
                subtitle: 'View past video calls',
                onTap: () => context.push(AppRouter.callHistory),
              ),
              const SizedBox(height: 12),
              _actionCard(
                context,
                icon: Icons.person_outline,
                color: AppTheme.iconNavy,
                bg: const Color(0xFFE7ECF3),
                title: 'My Profile',
                subtitle: 'Account details & logout',
                onTap: onOpenProfile,
              ),
            ]),
          ),
        ),
      ],
    );
  }

  Widget _infoCard({
    required IconData icon,
    required Color iconColor,
    required Color iconBg,
    required String title,
    required String value,
  }) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Row(
          children: [
            Container(
              padding: const EdgeInsets.all(10),
              decoration: BoxDecoration(
                color: iconBg,
                borderRadius: BorderRadius.circular(12),
              ),
              child: Icon(icon, color: iconColor),
            ),
            const SizedBox(width: 14),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(title, style: const TextStyle(color: AppTheme.textMuted, fontSize: 12)),
                  Text(value, style: const TextStyle(fontWeight: FontWeight.w600, fontSize: 16)),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _actionCard(
    BuildContext context, {
    required IconData icon,
    required Color color,
    required Color bg,
    required String title,
    required String subtitle,
    required VoidCallback onTap,
  }) {
    return Card(
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(16),
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Row(
            children: [
              Container(
                padding: const EdgeInsets.all(10),
                decoration: BoxDecoration(
                  color: bg,
                  borderRadius: BorderRadius.circular(12),
                ),
                child: Icon(icon, color: color),
              ),
              const SizedBox(width: 14),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(title, style: const TextStyle(fontWeight: FontWeight.bold)),
                    Text(subtitle, style: const TextStyle(color: AppTheme.textMuted, fontSize: 12)),
                  ],
                ),
              ),
              const Icon(Icons.chevron_right, color: AppTheme.textMuted),
            ],
          ),
        ),
      ),
    );
  }
}
