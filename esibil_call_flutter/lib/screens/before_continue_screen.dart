import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

import '../router/app_router.dart';
import '../theme/app_theme.dart';

class BeforeContinueScreen extends StatelessWidget {
  const BeforeContinueScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Column(
          children: [
            Text('Before You Continue', style: TextStyle(fontSize: 16)),
            Text('جاری رکھنے سے پہلے', style: TextStyle(fontSize: 12)),
          ],
        ),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(20),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Container(
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                color: AppTheme.reminderBg,
                border: Border.all(color: AppTheme.reminderBorder),
                borderRadius: BorderRadius.circular(12),
              ),
              child: Row(
                children: [
                  Container(
                    padding: const EdgeInsets.all(10),
                    decoration: BoxDecoration(
                      color: AppTheme.reminderBorder,
                      borderRadius: BorderRadius.circular(10),
                    ),
                    child: const Icon(
                      Icons.warning_amber_rounded,
                      color: AppTheme.reminderText,
                    ),
                  ),
                  const SizedBox(width: 12),
                  const Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          'Important Notice',
                          style: TextStyle(
                            fontWeight: FontWeight.bold,
                            color: AppTheme.reminderText,
                          ),
                        ),
                        Text(
                          'اہم اطلاع',
                          style: TextStyle(color: AppTheme.reminderText),
                        ),
                      ],
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 24),
            Text(
              'Instructions / ہدایات',
              style: Theme.of(context).textTheme.titleMedium?.copyWith(
                    fontWeight: FontWeight.bold,
                    color: AppTheme.loginHeading,
                  ),
            ),
            const SizedBox(height: 12),
            Text(
              'Video calls will only be allowed if your registered mobile number '
              'already exists in the prisoner\'s profile. If your number is not '
              'registered in the prisoner\'s profile, the call will not be permitted.',
              style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                    color: AppTheme.textGray,
                    height: 1.5,
                  ),
            ),
            const SizedBox(height: 12),
            Text(
              'ویڈیو کالز صرف اس صورت میں کی اجازت دی جائے گی جب آپ کا رجسٹرڈ '
              'موبائل نمبر قیدی کے پروفائل میں پہلے سے موجود ہو۔ اگر آپ کا نمبر '
              'قیدی کے پروفائل میں رجسٹرڈ نہیں ہے تو کال کی اجازت نہیں دی جائے گی۔',
              textDirection: TextDirection.rtl,
              style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                    color: AppTheme.textMuted,
                    height: 1.5,
                  ),
            ),
            const SizedBox(height: 16),
            Text(
              'Please read carefully before proceeding',
              style: Theme.of(context).textTheme.bodySmall?.copyWith(
                    color: AppTheme.textMuted,
                    fontStyle: FontStyle.italic,
                  ),
            ),
            const SizedBox(height: 32),
            SizedBox(
              width: double.infinity,
              child: ElevatedButton.icon(
                onPressed: () => context.push(AppRouter.addPrisoner),
                icon: const Icon(Icons.arrow_forward),
                label: const Text('Continue / جاری رکھیں'),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
