//
//  DataReaderDelegate.h
//  MatchmakingSDK
//
//  Created by aroth on 14/12/2013.
//  Copyright (c) 2013 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>

@class DataReader;
@class DataWriter;

@protocol DataReaderDelegate <NSObject>
- (void) dataReaderDidDisconnect:(DataReader*)reader;
- (BOOL) amITheServer;
- (BOOL) isPacketValid:(NSDictionary*)packet;
- (BOOL) isPacketBroadcast:(NSDictionary*)packet;
- (BOOL) isPacketRelay:(NSDictionary*)packet;
- (BOOL) isPacketInternal:(NSDictionary*)packet;
- (NSSet*) players;
- (NSString*) myPlayerId;
- (void) playerJoinedWithDetails:(NSDictionary*)details andId:(NSString*)playerId;
- (DataWriter*)dataWriterForPlayer:(NSString*)playerId;
@end