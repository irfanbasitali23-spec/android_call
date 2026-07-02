import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import 'config/app_config.dart';
import 'router/app_router.dart';
import 'services/prefs_service.dart';
import 'services/sip_service.dart';
import 'theme/app_theme.dart';

class EsibilCallApp extends StatefulWidget {
  const EsibilCallApp({super.key, required this.prefs});

  final PrefsService prefs;

  @override
  State<EsibilCallApp> createState() => _EsibilCallAppState();
}

class _EsibilCallAppState extends State<EsibilCallApp> {
  late final AppRouter _appRouter;

  @override
  void initState() {
    super.initState();
    AppConfig.restoreFromSavedDomain(widget.prefs.domain);
    _appRouter = AppRouter(widget.prefs);
    _listenForIncomingCalls();
    _restoreSipIfLoggedIn();
  }

  Future<void> _restoreSipIfLoggedIn() async {
    if (!widget.prefs.isLoggedIn) return;
    try {
      await SipService.instance.register(
        sipId: widget.prefs.sipId!,
        password: widget.prefs.password,
        domain: widget.prefs.domain,
      );
    } catch (_) {
      // Native SIP may be unavailable on iOS stub.
    }
  }

  void _listenForIncomingCalls() {
    SipService.instance.events.listen((event) {
      if (!event.isIncomingCall) return;
      _appRouter.router.push(
        AppRouter.call,
        extra: {
          'incoming': true,
          'video': event.video,
          'remote': event.remote,
        },
      );
    });
  }

  @override
  Widget build(BuildContext context) {
    return MultiProvider(
      providers: [
        Provider<PrefsService>.value(value: widget.prefs),
      ],
      child: MaterialApp.router(
        title: 'eSIBiL Call',
        debugShowCheckedModeBanner: false,
        theme: AppTheme.light(),
        routerConfig: _appRouter.router,
      ),
    );
  }
}
