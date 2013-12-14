//
//  DataWriter.h
//  MatchmakingSDK
//
//  Created by Adam Roth on 28/03/12.
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>

@class IAMatchmaker;
@class DataWriter;

@protocol DataWriterDelegate <NSObject>
- (void) dataWriterDidDisconnect:(DataWriter*)writer forPlayerId:(NSString*)playerId;
- (BOOL) amITheServer;
- (NSString*) myPlayerId;
- (NSString*) matchId;
- (NSString*) matchInfo;
- (void) playerLeft:(NSString*)playerId;
@end

@interface DataWriter : NSObject {
    //private
    IAMatchmaker* matchmaker;
    CFWriteStreamRef output;
    NSMutableArray* sendBuffer;
    NSString* playerId;
    BOOL open;
}

- (id) initWithMatchmaker:(IAMatchmaker*)maker andOutput:(CFWriteStreamRef)outData andDelegate:(NSObject<DataWriterDelegate>*)del forPlayer:(NSString*)player;

- (void) close;
- (void) sendData:(NSDictionary*)jsonData;

@property(retain) NSObject<DataWriterDelegate>* delegate;

@end
