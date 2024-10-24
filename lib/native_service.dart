import 'dart:convert';

import 'package:flutter/services.dart';

const nativeChannel = MethodChannel('com.laochinabank.mb/hms_location');

Future<String> getLocation() async {
  final result = await nativeChannel.invokeMethod("getLocation");
  print('11111111111: $result');
  Map data = jsonDecode(result);
  print('aaaaaaaaaaaa: $data');
  return result;
}
