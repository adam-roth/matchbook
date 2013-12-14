//
//  DataWriterDelegate.h
//  MatchmakingSDK
//
//  Created by aroth on 14/12/2013.
//  Copyright (c) 2013 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>

@class DataWriter;

@protocol DataWriterDelegate <NSObject>
- (void) dataWriterDidDisconnect:(DataWriter*)writer forPlayerId:(NSString*)playerId;
- (BOOL) amITheServer;
- (NSString*) myPlayerId;
- (NSString*) matchId;
- (NSString*) matchInfo;
- (void) playerLeft:(NSString*)playerId;
@end