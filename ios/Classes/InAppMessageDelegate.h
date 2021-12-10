//
//  InAppMessageDelegate.h
//  testXcFrameworkObjc
//
//  Created by Mihajlo Jezdic on 9.12.21..
//

#import <Foundation/Foundation.h>
#import <Flutter/Flutter.h>
#import "AppoxeeInappSDK.h"

NS_ASSUME_NONNULL_BEGIN

@interface InAppMessageDelegate : NSObject

-(instancetype)initWith:(FlutterMethodChannel*) channel;
-(void) addNotificationListeners;

@end

NS_ASSUME_NONNULL_END