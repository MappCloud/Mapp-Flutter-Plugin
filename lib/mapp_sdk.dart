import 'dart:async';

import 'package:flutter/services.dart';
import 'helper_classes.dart';

class MappSdk {
  //static const MethodChannel _channel = MethodChannel('mapp_sdk');

  static MethodChannel _privateChannel = const MethodChannel('mapp_sdk');

  static MethodChannel get _channel {
    if (_privateChannel == null) {
      _privateChannel = const MethodChannel('mapp_sdk');
    }
    _privateChannel!.setMethodCallHandler(_platformCallHandler);
    return _privateChannel!;
  }

  //inapp handlers
  static late void Function(dynamic) didReceiveDeepLinkWithIdentifier;
  static late void Function(dynamic) didReceiveInappMessageWithIdentifier;
  static late void Function(dynamic) didReceiveCustomLinkWithIdentifier;
  static late void Function(dynamic) didReceiveInBoxMessages;
  static late void Function(dynamic) inAppCallFailedWithResponse;
  static late void Function(dynamic) didReceiveInBoxMessage;

  //push notification handlers
  static late void Function(dynamic) handledRemoteNotification;
  static late void Function(dynamic) handledRichContent;

  static Future<void> _platformCallHandler(MethodCall call) {
    try {
      switch (call.method) {
        case 'didReceiveDeepLinkWithIdentifier':
          didReceiveDeepLinkWithIdentifier(call.arguments);
          break;
        case 'didReceiveInappMessageWithIdentifier':
          didReceiveInappMessageWithIdentifier(call.arguments);
          break;
        case 'didReceiveCustomLinkWithIdentifier':
          didReceiveCustomLinkWithIdentifier(call.arguments);
          break;
        case 'didReceiveInBoxMessages':
          didReceiveInBoxMessages(call.arguments);
          break;
        case 'inAppCallFailedWithResponse':
          inAppCallFailedWithResponse(call.arguments);
          break;
        case 'didReceiveInBoxMessage':
          didReceiveInBoxMessage(call.arguments);
          break;
        case 'handledRemoteNotification':
          handledRemoteNotification(call.arguments);
          break;
        case 'handledRichContent':
          handledRichContent(call.arguments);
          break;
        default:
          print('Unknowm method ${call.method} ');
      }
    } catch (e) {
      print(e);
    }
    return Future.value();
  }

  static Future<String?> get platformVersion async {
    final String? version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<String?> engage(String sdkKey, String googleProjectId,
      SERVER server, String appID, String tenantID) async {
    final String? version = await _channel.invokeMethod(
        'engage', [sdkKey, googleProjectId, server.index, appID, tenantID]);
    return 'successfull $version engage method invoked';
  }

  static Future<String?> postponeNotificationRequest(bool postpone) async {
    final String? version =
        await _channel.invokeMethod('postponeNotificationRequest', [postpone]);
    return 'successfull $version postponeNotificationRequest method invoked';
  }

  static Future<String?> showNotificationsOnForeground(
      bool showNotification) async {
    final String? version = await _channel
        .invokeMethod('showNotificationsOnForeground', [showNotification]);
    return 'successfull $version postponeNotificationRequest method invoked';
  }

  static Future<bool> isReady() async {
    final bool version = await _channel.invokeMethod('isReady');
    return version;
  }

  static Future<String?> setPushEnabled(bool optIn) async {
    final String? version = await _channel.invokeMethod('optIn', [optIn]);
    return 'successfull $version opted in device with value $optIn';
  }

  static Future<bool> isPushEnabled() async {
    final bool version = await _channel.invokeMethod('isPushEnabled');
    return version;
  }

  static Future<String?> logOut(bool pushEnabled) async {
    final String? version =
        await _channel.invokeMethod('logoutWithOptin', [pushEnabled]);
    return 'successfull $version logged out with status $pushEnabled';
  }

  static Future<String?> setAlias(String alias) async {
    final String? version =
        await _channel.invokeMethod('setDeviceAlias', [alias]);
    return 'successfull $version set new alias $alias';
  }

  static Future<String> getAlias() async {
    final String alias = await _channel.invokeMethod('getDeviceAlias');
    return alias;
  }

  static Future<List<String>?> getTags() async {
    final List<String> tags = await _channel.invokeMethod('fetchDeviceTags');
    return tags;
  }

  static Future<List<String>?> fetchApplicationTags() async {
    final List<String> tags =
        await _channel.invokeMethod('fetchApplicationTags');
    return tags;
  }

  static Future<String?> addTags(List<String> tags) async {
    final String version =
        await _channel.invokeMethod('addTagsToDevice', [tags]);
    return "Tags are added successfully! $version";
  }

  static Future<String?> removeTags(List<String> tags) async {
    final String version =
        await _channel.invokeMethod('removeTagsFromDevice', [tags]);
    return "Tags are removed successfully! $version";
  }

  static Future<String?> setDateValueWithKey(DateTime date, String key) async {
    final String version =
        await _channel.invokeMethod('setDateValueWithKey', [date, key]);
    return "Date $date added successfully! $version";
  }

  static Future<String?> setAttributeInt(String key, int value) async {
    final String version =
        await _channel.invokeMethod('setNumberValueWithKey', [value, key]);
    return "Number $value added successfully! $version";
  }

  static Future<String?> incrementNumericKey(String key, int value) async {
    final String version =
        await _channel.invokeMethod('incrementNumericKey', [value, key]);
    return "Number $value incremented successfully! $version";
  }

  static Future<String?> setAttributeString(String key, String value) async {
    final String version =
        await _channel.invokeMethod('setStringValueWithKey', [value, key]);
    return "String $value added successfully! $version";
  }

  static Future<String?> fetchCustomFieldByKey(String key) async {
    final String fieldValue =
        await _channel.invokeMethod('fetchCustomFieldByKey', [key]);
    return fieldValue;
  }

  static Future<String> getDeviceInfo() async {
    final String deviceInfo = await _channel.invokeMethod('getDeviceInfo');
    return deviceInfo;
  }

  static Future<String?> removeBadgeNumber() async {
    final String deviceInfo = await _channel.invokeMethod('removeBadgeNumber');
    return deviceInfo;
  }

  //InApp methods
  static Future<String?> triggerInApp(String value) async {
    final String returnValue =
        await _channel.invokeMethod('triggerInApp', [value]);
    return returnValue;
  }

  static Future<dynamic> fetchInboxMessage() async {
    final String returnValue = await _channel.invokeMethod('fetchInboxMessage');
    return returnValue;
  }

  static Future<String?> fetchInBoxMessageWithMessageId(int value) async {
    final String returnValue =
        await _channel.invokeMethod('fetchInBoxMessageWithMessageId', [value]);
    return returnValue;
  }
}
