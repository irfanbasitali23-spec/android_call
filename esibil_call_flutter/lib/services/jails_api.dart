import 'dart:convert';

import 'package:http/http.dart' as http;

import '../config/app_config.dart';
import '../models/jail_site.dart';

/// Fetches jail/site list from the PPF sites API.
class JailsApi {
  JailsApi({http.Client? client}) : _client = client ?? http.Client();

  final http.Client _client;

  Future<List<JailSite>> fetchSites() async {
    final response = await _client
        .get(
          Uri.parse(AppConfig.jailsApiUrl),
          headers: {
            'Authorization': 'Bearer ${AppConfig.jailsApiToken}',
          },
        )
        .timeout(const Duration(seconds: 20));

    if (response.statusCode != 200) {
      throw StateError('Jails API error: HTTP ${response.statusCode}');
    }

    final body = response.body;
    if (body.isEmpty) {
      throw StateError('Empty jails API response');
    }

    final json = jsonDecode(body) as Map<String, dynamic>;
    if (json['status'] != 'success') {
      throw StateError('Jails API status: ${json['status']}');
    }

    final sites = json['sites'] as List<dynamic>;
    final list = sites
        .map((e) => JailSite.fromJson(e as Map<String, dynamic>))
        .toList()
      ..sort((a, b) => a.jailName.compareTo(b.jailName));
    return list;
  }
}
