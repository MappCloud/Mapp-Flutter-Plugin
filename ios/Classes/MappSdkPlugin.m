#import "MappSdkPlugin.h"
#import "PushMessageDelegate.h"
#import "InAppMessageDelegate.h"

static FlutterMethodChannel *channel;

@interface MappSdkPlugin () <AppoxeeInappDelegate>

@end

@implementation MappSdkPlugin


+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  channel = [FlutterMethodChannel
      methodChannelWithName:@"mapp_sdk"
            binaryMessenger:[registrar messenger]];
  MappSdkPlugin* instance = [[MappSdkPlugin alloc] init];
  [registrar addMethodCallDelegate:instance channel:channel];
}

- (void)handleMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result {
  if ([@"getPlatformVersion" isEqualToString:call.method]) {
    result([@"iOS " stringByAppendingString:[[UIDevice currentDevice] systemVersion]]);
  } else if ([@"engage" isEqualToString:call.method]){
    NSNumber* severNumber = call.arguments[2];
    PushMessageDelegate* pushMessageDelegate = [[PushMessageDelegate alloc] initWith:channel];
      InAppMessageDelegate* inAppMessageDelegate = [[InAppMessageDelegate alloc] initWith:channel];
    [pushMessageDelegate addNotificationListeners];
    [[Appoxee shared] engageAndAutoIntegrateWithLaunchOptions:NULL andDelegate:pushMessageDelegate with:TEST];
    [inAppMessageDelegate addNotificationListeners];
    [[Appoxee shared] engageAndAutoIntegrateWithLaunchOptions:NULL andDelegate:inAppMessageDelegate with:TEST];
    [[AppoxeeInapp shared] engageWithDelegate:inAppMessageDelegate with:tEST];


  } else if ([@"postponeNotificationRequest" isEqualToString:call.method]){
    BOOL value = call.arguments[0];
    [[Appoxee shared] setPostponeNotificationRequest:value];
  } else if ([@"showNotificationsOnForeground" isEqualToString:call.method]){
    BOOL value = call.arguments[0];
    [[Appoxee shared] setShowNotificationsOnForeground:value];
  } else if ([@"isReady" isEqualToString:call.method]) {
    result([[Appoxee shared] isReady] ? @YES : @NO);
  } else if ([@"optIn" isEqualToString:call.method]){
    BOOL value = call.arguments[0];
    [[Appoxee shared] disablePushNotifications:!value withCompletionHandler:NULL];
  } else if ([@"isPushEnabled" isEqualToString:call.method]){
    [[Appoxee shared] isPushEnabled:^(NSError * _Nullable appoxeeError, id  _Nullable data) {
        if (!appoxeeError) {
            BOOL state = [(NSNumber *)data boolValue];
            result((NSNumber *)data);
        } else {
          result(@NO);
        }
    }];
  } else if ([@"logoutWithOptin" isEqualToString:call.method]){
    BOOL value = call.arguments[0];
    [[Appoxee shared] logoutWithOptin:value];
  } else if ([@"setDeviceAlias" isEqualToString:call.method]){
    NSString* alias = call.arguments[0];
    [[Appoxee shared] setDeviceAlias:alias withCompletionHandler:NULL];
  } else if ([@"getDeviceAlias" isEqualToString:call.method]){
    [[Appoxee shared] getDeviceAliasWithCompletionHandler:^(NSError * _Nullable appoxeeError, id  _Nullable data) {
        if (!appoxeeError && [data isKindOfClass:[NSString class]])
        {
            NSString *deviceAlias = (NSString *)data;
            result(deviceAlias);
        } else {
          result(@"Error while getting alias!");
        }
    }];
  } else if ([@"fetchDeviceTags" isEqualToString:call.method]){
    [[Appoxee shared] fetchDeviceTags:^(NSError * _Nullable appoxeeError, id  _Nullable data) {
        if (!appoxeeError && [data isKindOfClass:[NSArray class]]) {
            NSArray *deviceTags = (NSArray *)data;
            result(deviceTags);
        }
    }];
  } else if ([@"fetchApplicationTags" isEqualToString:call.method]){
    [[Appoxee shared] fetchApplicationTags:^(NSError * _Nullable appoxeeError, id  _Nullable data) {
        if (!appoxeeError && [data isKindOfClass:[NSArray class]]) {
            NSArray *applicationTags = (NSArray *)data;
            result(applicationTags);
        }
    }];
  } else if ([@"addTagsToDevice" isEqualToString:call.method]){
    NSArray<NSString *> * tags = call.arguments[0];
    [[Appoxee shared] addTagsToDevice:tags withCompletionHandler:NULL];
  } else if ([@"removeTagsFromDevice" isEqualToString:call.method]){
    NSArray<NSString *> * tags = call.arguments[0];
    [[Appoxee shared] removeTagsFromDevice:tags withCompletionHandler:NULL];
  } else if ([@"setDateValueWithKey" isEqualToString:call.method]){
    NSDate* date = call.arguments[0];
    NSString* key = call.arguments[1];
    [[Appoxee shared] setDateValue:date forKey:key withCompletionHandler:NULL];
  } else if ([@"setNumberValueWithKey" isEqualToString:call.method]){
    NSNumber* number = call.arguments[1];
    NSString* key = call.arguments[0];
    [[Appoxee shared] setNumberValue:number forKey:key withCompletionHandler:NULL];
  } else if ([@"incrementNumericKey" isEqualToString:call.method]){
    NSNumber* number = call.arguments[1];
    NSString* key = call.arguments[0];
    [[Appoxee shared] incrementNumericKey:key byNumericValue:number withCompletionHandler:NULL];
  } else if ([@"setStringValueWithKey" isEqualToString:call.method]){
    NSString* stringValue = call.arguments[1];
    NSString* key = call.arguments[0];
    [[Appoxee shared] setStringValue:stringValue forKey:key withCompletionHandler:NULL];
  } else if ([@"fetchCustomFieldByKey" isEqualToString:call.method]){
    NSString* key = call.arguments[0];
    [[Appoxee shared] fetchCustomFieldByKey:key withCompletionHandler:^(NSError * _Nullable appoxeeError, id  _Nullable data) {
        if (!appoxeeError && [data isKindOfClass:[NSDictionary class]]) {
        NSDictionary *dictionary = (NSDictionary *)data; NSString *key = [[dictionary allKeys] firstObject];
            id value = dictionary[key]; // can be of the following types: NSString || NSNumber || NSDate }
        // If there wan't an error, and data object is nil, then value doesn't exist on the device.
        }
    }];
  } else if ([@"getDeviceInfo" isEqualToString:call.method]){
    APXClientDevice* device = [[Appoxee shared] deviceInfo];
    NSDictionary *deviceData = [self deviceInfo:device];
    result([deviceData description]);
  } else if ([@"removeBadgeNumber" isEqualToString:call.method]){
    [UIApplication sharedApplication].applicationIconBadgeNumber = 0;
  } else if ([@"addTagsToDevice" isEqualToString:call.method]){
    NSArray<NSString *> * array = call.arguments[0];
    [[Appoxee shared] addTagsToDevice:array withCompletionHandler:NULL];
  } else if ([@"removeTagsFromDevice" isEqualToString:call.method]){
    NSArray<NSString *> * array = call.arguments[0];
    [[Appoxee shared] removeTagsFromDevice:array withCompletionHandler:NULL];
  } else if ([@"triggerInApp" isEqualToString:call.method]){
    NSString * eventName = call.arguments[0];
    [[AppoxeeInapp shared]reportInteractionEventWithName: eventName andAttributes:NULL];
  } else if ([@"fetchInboxMessage" isEqualToString:call.method]){
    [[AppoxeeInapp shared] fetchAPXInBoxMessages];
    NSLog(@"This is fetchAPXInBox");
  } else if ([@"fetchInBoxMessageWithMessageId" isEqualToString:call.method]){
    NSNumber * messageId = call.arguments[0];
    [[AppoxeeInapp shared] fetchInBoxMessageWithMessageId:[messageId stringValue]];
    NSLog(@"This is fetchInBoxMessageWithMessageId");
  } else {
    result(FlutterMethodNotImplemented);
  }
}

- (NSDictionary *) deviceInfo: (APXClientDevice *) device {
    NSMutableDictionary *dict = [[NSMutableDictionary alloc] init];
    [dict setObject: device.udid forKey:@"udid"];
    [dict setObject: device.sdkVersion forKey:@"sdkVersion"];
    [dict setObject:device.locale forKey:@"locale"];
    [dict setObject:device.timeZone forKey:@"timezone"];
    [dict setObject:device.hardwearType forKey:@"deviceModel"];
    [dict setObject:device.osVersion forKey:@"osVersion"];
    [dict setObject:device.osName forKey:@"osName"];
    return dict;
}

-(UIViewController* ) topViewController: (UIViewController* ) base {
    if ([base isKindOfClass:UINavigationController.class]) {
        UINavigationController* nav  = (UINavigationController*) base;
        return [self topViewController:[nav visibleViewController]];
    }
    if ([base isKindOfClass:UITabBarController.class]) {
        UITabBarController* tab = (UITabBarController*) base;
        UIViewController* selected = [tab selectedViewController];
        if (selected) {
            return [self topViewController:selected];
        }
    }
    UIViewController* presented = [base presentedViewController];
    if (presented) {
        return [self topViewController:presented];
    }
    return base;
}

@end
