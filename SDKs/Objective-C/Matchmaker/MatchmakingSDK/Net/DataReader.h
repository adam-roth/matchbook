//
//  DataReader.h
//  MatchmakingSDK
//
//  Created by Adam Roth on 28/03/12.
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>

@class IAMatchmaker;
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

@class DataWriter;

@interface DataReader : NSObject {
    //private
    IAMatchmaker* matchmaker;
    CFReadStreamRef input;
    DataWriter* writer;
}

- (id) initWithMatchmaker:(IAMatchmaker*)maker andInput:(CFReadStreamRef)dataIn andDelegate:(NSObject<DataReaderDelegate>*)del andPairedWriter:(DataWriter*)writer;

@property(retain) NSObject<DataReaderDelegate>* delegate;

@end
