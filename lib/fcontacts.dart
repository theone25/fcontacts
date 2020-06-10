import 'dart:async';
import 'dart:typed_data';

import 'package:flutter/services.dart';

class FContacts {
  static const MethodChannel _channel = const MethodChannel('fcontacts');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<List<FContact>> all() async {
    Iterable all = await _channel.invokeMethod("list");
    var mapped = all.map((item) => FContact.fromMap(item));
    return mapped.toList();
  }

  static Future<List<FContact>> list({query: String}) async {
    Iterable filtered =
        await _channel.invokeMethod("list", <String, dynamic>{'query': query});
    var mapped = filtered.map((item) => FContact.fromMap(item));
    return mapped.toList();
  }
}

class FContact {
  FContact({this.identifier, this.displayName});

  String identifier;
  String displayName;
  String contactType;
  Uint8List thumbnail;
  List<FContactValueLabeled> emails = List<FContactValueLabeled>();
  List<FContactValueLabeled> phoneNumbers = List<FContactValueLabeled>();

  FContact.fromMap(Map map) {
    identifier = map["identifier"];
    displayName = map["displayName"];
    contactType = map["contactType"];
    thumbnail = map["thumbnailData"] ?? null;
    emails = (map["emails"] as Iterable)
        ?.map((item) => FContactValueLabeled.fromMap(item))
        ?.toList();
    phoneNumbers = (map["phoneNumbers"] as Iterable)
        ?.map((item) => FContactValueLabeled.fromMap(item))
        ?.toList();
}

class FContactValueLabeled {
  String label;
  String value;
  FContactValueLabeled({this.label, this.value});
  static FContactValueLabeled fromMap(Map map) {
    return FContactValueLabeled(
        label: map.keys.first as String, value: map.values.first as String);
  }
}


