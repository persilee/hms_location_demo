import 'package:flutter/services.dart';

const nativeChannel = MethodChannel('com.laochinabank.mb/hms_location');

Future<String> getLocation() async {
  final result = await nativeChannel.invokeMethod("getLocation");
  return result;
}
