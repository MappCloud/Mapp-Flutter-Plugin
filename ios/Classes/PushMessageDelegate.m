//  Created by Stefan Stevanovic on 9.12.21..
//

#import "PushMessageDelegate.h"

@interface PushMessageDelegate() <AppoxeeNotificationDelegate>

@property FlutterMethodChannel* channel;

@end

@implementation PushMessageDelegate

- (instancetype)initWith:(FlutterMethodChannel *)channel {
    if (!self) {
        self = [super init];
    }
    self.channel = channel;
    return self;
}

- (void)addNotificationListeners {
    [[NSNotificationCenter defaultCenter] removeObserver:self name:@"handledRemoteNotification" object:nil];
    [NSNotificationCenter.defaultCenter addObserver:self selector:@selector(remoteNotificationHandler:) name:@"handledRemoteNotification" object:nil];

    [[NSNotificationCenter defaultCenter] removeObserver:self name:@"handledRichContent" object:nil];
    [NSNotificationCenter.defaultCenter addObserver:self selector:@selector(richContentHandler:) name:@"handledRichContent" object:nil];

}

//selectors to forward data to flutter level
- (void)remoteNotificationHandler:(NSNotification *)notification {
    NSLog(@"notification reveived in remote notification with identifier %@!", notification);
    [self.channel invokeMethod:@"handledRemoteNotification" arguments:notification.userInfo];
}

- (void)richContentHandler:(NSNotification *)notification {
    NSLog(@"notification reveived with rich content %@!", notification);
    [self.channel invokeMethod:@"handledRichContent" arguments:notification.userInfo];
}

//delegate method
- (void)appoxee:(Appoxee *)appoxee handledRemoteNotification:(APXPushNotification *)pushNotification andIdentifer:(NSString *)actionIdentifier {
    if ([self getPushMessage:pushNotification]) {
        [[NSNotificationCenter defaultCenter] postNotificationName:
             @"handledRemoteNotification" object:nil userInfo:[self getPushMessage:pushNotification]];
    }
    NSString* deepLink = pushNotification.extraFields[@"apx_dpl"];
    if (deepLink && ![deepLink isEqualToString:@""] && actionIdentifier) {
        [[NSNotificationCenter defaultCenter] postNotificationName:
            @"handledRemoteNotification" object:nil userInfo: @{@"action":actionIdentifier, @"url": deepLink, @"event_trigger": @"" }];
    }
}

- (void)appoxee:(Appoxee *)appoxee handledRichContent:(APXRichMessage *)richMessage didLaunchApp:(BOOL)didLaunch {
    if ([self getRichMessage:richMessage]) {
        [[NSNotificationCenter defaultCenter] postNotificationName:
             @"handledRichContent" object:nil userInfo:[self getRichMessage:richMessage]];
    }
}

-(NSDictionary *) getPushMessage: (APXPushNotification *) pushMessage {
    NSMutableDictionary *dict = [[NSMutableDictionary alloc] init];
    if (pushMessage.title)
        [dict setObject:pushMessage.title forKey:@"title"];
    if (pushMessage.alert)
        [dict setObject:pushMessage.alert forKey:@"alert"];
    if (pushMessage.body)
        [dict setObject:pushMessage.body forKey:@"body"];
    if (pushMessage.uniqueID)
        [dict setObject:[NSNumber numberWithInteger: pushMessage.uniqueID] forKey: @"id"];
    if (pushMessage.badge)
        [dict setObject:[NSNumber numberWithInteger: pushMessage.badge] forKey: @"badge"];
    if (pushMessage.subtitle)
        [dict setObject:pushMessage.subtitle forKey:@"subtitle"];
    if (pushMessage.pushAction.categoryName)
        [dict setObject:pushMessage.pushAction.categoryName forKey:@"category" ];
    if (pushMessage.extraFields)
        [dict setObject:pushMessage.extraFields forKey:@"extraFields"];
    if (pushMessage.isRich)
        [dict setObject:pushMessage.isRich ? @"true": @"false" forKey:@"isRich"];
    if (pushMessage.isSilent)
        [dict setObject:pushMessage.isSilent ? @"true": @"false" forKey:@"isSilent"];
    if (pushMessage.isTriggerUpdate)
        [dict setObject:pushMessage.isTriggerUpdate ? @"true": @"false" forKey:@"isTriggerUpdate"];
    return dict;
}

-(NSDictionary *) getRichMessage: (APXRichMessage *) message {
    NSMutableDictionary *dict = [[NSMutableDictionary alloc] init];
    [dict setObject:[[NSNumber numberWithInteger:message.uniqueID] stringValue] forKey:@"id"];
    [dict setObject:message.title forKey:@"title"];
    [dict setObject:message.content forKey:@"content"];
    [dict setObject:message.title forKey:@"title"];
    [dict setObject:message.messageLink forKey:@"messageLink"];
    [dict setObject:[self stringFromDate: message.postDate inUTC:false] forKey:@"postDate"];
    [dict setObject:[self stringFromDate: message.postDate inUTC:true] forKey:@"postDateUTC"];
    return dict;

}

- (NSString *)stringFromDate:(NSDate *)date inUTC: (BOOL) isUTC{
    NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
    if (isUTC) {
        [dateFormatter setTimeZone:[NSTimeZone timeZoneWithName:@"UTC"]];
    }
    [dateFormatter setDateFormat: @"yyyy-MM-dd'T'HH:mm:ss"];
    
    return [dateFormatter stringFromDate:date];
}

@end
