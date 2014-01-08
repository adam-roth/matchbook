//
//  SCMatch.h
//  MatchmakingSDK
//
//  Created by Adam Roth on 27/03/12.
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

#import <stdio.h>
#import <sys/types.h>
#import <sys/socket.h>
#import <netinet/in.h>
#import <netdb.h>
#import <Foundation/Foundation.h>

//#import "DataReaderDelegate.h"
//#import "DataWriterDelegate.h"

@protocol DataReaderDelegate;
@protocol DataWriterDelegate;

@class SCMatchmaker;

@interface SCMatch : NSObject<DataReaderDelegate, DataWriterDelegate> {
    //private fields
    NSMutableDictionary* playerDetails;
    SCMatchmaker* matchmaker;
    
    NSMutableDictionary* dataWriters;
    NSMutableArray* dataReaders;
    
    //if I'm not hosting the match, use these
    CFReadStreamRef input;
    CFWriteStreamRef output;
    
    //if I am hosting the match, use these
    BOOL stopServer;
    struct   sockaddr_in serverSocket;
    NSMutableDictionary* playerConnections;
    
    NSMutableSet* playerIds;
}

//protected API
- (NSDictionary*) matchInfo;
- (int) becomeServer;
- (void) terminateServer;
- (BOOL) connectToServer:(NSString*)host onPort:(int)port withServerId:(NSString*)serverId;
- (void) setMatchId:(NSString*)matchId;
- (void) setPassword:(NSString*)password;

//constructors (protected)
- (id) initWithPlayerId:(NSString*)myId andMatchmaker:(SCMatchmaker*)maker;
- (id) initWithPlayerId:(NSString*)myId andMatchmaker:(SCMatchmaker*)maker andPassword:(NSString*)pass;
- (id) initWithPlayerId:(NSString*)myId andMatchmaker:(SCMatchmaker*)maker andMatchId:(NSString*)match;
- (id) initWithPlayerId:(NSString*)myId andMatchmaker:(SCMatchmaker*)maker andMatchId:(NSString*)match andPassword:(NSString*)pass;


//public API
- (NSSet*) players;
- (BOOL) amITheServer;
- (void) broadcastMessage:(NSDictionary*)message;
- (void) sendMessage:(NSDictionary*)message toPlayer:(NSString*)playerId;

@property(readonly) NSString* password;
@property(readonly) NSString* myPlayerId;
@property(readonly) NSString* serverPlayerId;
@property(readonly) NSString* matchId;
@property(readonly) int port;

@end
