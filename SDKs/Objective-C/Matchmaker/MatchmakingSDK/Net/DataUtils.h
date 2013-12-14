//
//  DataUtils.h
//  MatchmakingSDK
//
//  Created by Adam Roth on 27/03/12.
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>

#define INTERNAL_DATA_KEY @"__MatchData"

#define BROADCAST @"broadcast"
#define RELAY @"relay"
#define INTERNAL @"internal"
#define ACTION @"action"
#define UUID @"uuid"

#define ACTION_PLAYER_JOIN @"join"

@interface DataUtils : NSObject

+ (NSData*) serializeInt32:(int)num;
+ (int) reconstructInt32:(NSData*)data;

+ (NSData*) serializeJson:(NSString*)jsonData;
+ (void) writeJson: (NSString*)jsonData toStream: (CFWriteStreamRef)stream;
+ (NSDictionary*) readJsonFromStream: (CFReadStreamRef)stream;
+ (int) reconstructInt32FromStream:(CFReadStreamRef)stream;
+ (int) fillBuffer: (NSMutableData*)buffer fromStream: (CFReadStreamRef)stream;

+ (void)openSocketToServer:(NSString*)server onPort:(int)port withReadStream:(CFReadStreamRef*)readStream andWriteStream:(CFWriteStreamRef*)writeStream;

@end
