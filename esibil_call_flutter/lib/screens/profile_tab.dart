import 'package:flutter/material.dart';

import '../services/prefs_service.dart';
import '../theme/app_theme.dart';

class ProfileTab extends StatelessWidget {
  const ProfileTab({
    super.key,
    required this.prefs,
    required this.onLogout,
  });

  final PrefsService prefs;
  final VoidCallback onLogout;

  @override
  Widget build(BuildContext context) {
    final displayName = prefs.displayName ?? prefs.phone ?? 'User';
    final phone = prefs.phone ?? '-';
    final jail = prefs.jailName ?? '-';
    final sipId = prefs.sipId ?? '-';

    return CustomScrollView(
      slivers: [
        SliverAppBar(
          expandedHeight: 140,
          pinned: true,
          title: const Text('Profile / پروفائل'),
          flexibleSpace: FlexibleSpaceBar(
            background: Container(
              decoration: BoxDecoration(gradient: AppTheme.headerGradient),
            ),
          ),
        ),
        SliverPadding(
          padding: const EdgeInsets.all(16),
          sliver: SliverList(
            delegate: SliverChildListDelegate([
              Center(
                child: CircleAvatar(
                  radius: 44,
                  backgroundColor: const Color(0xFFDCEEE4),
                  child: Text(
                    displayName.isNotEmpty ? displayName[0].toUpperCase() : '?',
                    style: const TextStyle(
                      fontSize: 32,
                      fontWeight: FontWeight.bold,
                      color: AppTheme.iconGreen,
                    ),
                  ),
                ),
              ),
              const SizedBox(height: 12),
              Center(
                child: Text(
                  displayName,
                  style: Theme.of(context).textTheme.titleLarge?.copyWith(
                        fontWeight: FontWeight.bold,
                        color: AppTheme.loginHeading,
                      ),
                ),
              ),
              const SizedBox(height: 4),
              Center(
                child: Text(
                  'Spouse to Inmate',
                  style: TextStyle(color: AppTheme.textGray.withOpacity(0.9)),
                ),
              ),
              const SizedBox(height: 8),
              Center(
                child: Container(
                  padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 4),
                  decoration: BoxDecoration(
                    color: const Color(0xFFDCEEE4),
                    borderRadius: BorderRadius.circular(20),
                  ),
                  child: const Text(
                    'Approved',
                    style: TextStyle(color: AppTheme.iconGreen, fontWeight: FontWeight.w600),
                  ),
                ),
              ),
              const SizedBox(height: 24),
              _sectionTitle('Connected Inmate'),
              _detailRow('Name', displayName),
              _detailRow('Inmate ID', sipId),
              _detailRow('Jail', jail),
              const SizedBox(height: 16),
              _sectionTitle('Account Details'),
              _detailRow('Phone', phone),
              _detailRow('Account Type', 'Family Member'),
              _detailRow('Verification Status', 'Verified'),
              const SizedBox(height: 32),
              OutlinedButton.icon(
                onPressed: onLogout,
                icon: const Icon(Icons.logout, color: AppTheme.danger),
                label: const Text('Logout', style: TextStyle(color: AppTheme.danger)),
                style: OutlinedButton.styleFrom(
                  side: const BorderSide(color: AppTheme.danger),
                  padding: const EdgeInsets.symmetric(vertical: 14),
                ),
              ),
              const SizedBox(height: 24),
              Center(
                child: Text(
                  'Punjab Prison Foundation Video Call App v1.0.0',
                  style: TextStyle(fontSize: 11, color: AppTheme.textMuted.withOpacity(0.8)),
                  textAlign: TextAlign.center,
                ),
              ),
            ]),
          ),
        ),
      ],
    );
  }

  Widget _sectionTitle(String title) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 8, top: 4),
      child: Text(
        title,
        style: const TextStyle(
          fontWeight: FontWeight.bold,
          color: AppTheme.loginHeading,
          fontSize: 15,
        ),
      ),
    );
  }

  Widget _detailRow(String label, String value) {
    return Card(
      margin: const EdgeInsets.only(bottom: 8),
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
        child: Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Text(label, style: const TextStyle(color: AppTheme.textMuted)),
            Flexible(
              child: Text(
                value,
                textAlign: TextAlign.end,
                style: const TextStyle(fontWeight: FontWeight.w500),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
