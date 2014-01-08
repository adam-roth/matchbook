//
//  SCMatch.m
//  MatchmakingSDK
//
//  Created by Adam Roth on 27/03/12.
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

#import "SCMatch.h"
#import "SCMatchmaker.h"
#import "DataUtils.h"
#import "NSObject+SimpleJson.h"
#import "DataReader.h"
#import "DataWriter.h"
#import "SCMatchmakerDelegate.h"

@implementation SCMatch 
 
@synthesize matchId, port, password, myPlayerId, serverPlayerId;

//private API
- (int) randomPort {
    return 1025 + arc4random() % 60000;
}

- (BOOL) isPacketValid:(NSDictionary*)packet {
    if (! packet || ! [packet objectForKey:INTERNAL_DATA_KEY]) {
        return NO;
    }
    
    NSDictionary* internalData = [packet objectForKey:INTERNAL_DATA_KEY];
    if (! [self.matchId isEqual:[internalData objectForKey:@"matchId"]]) {
        return NO;
    }
    
    if (self.password && ! [self.password isEqual:[internalData objectForKey:@"password"]]) {
        return NO;
    }
    
    return [internalData objectForKey:UUID] != nil;
}

- (NSDictionary*) getInternalData:(NSDictionary*)packet {
    if (! [self isPacketValid:packet]) {
        return nil;
    }
    return [packet objectForKey:INTERNAL_DATA_KEY];
}

- (BOOL) isPacketBroadcast:(NSDictionary*)packet {
    return [self isPacketValid:packet] && [[self getInternalData:packet] objectForKey:BROADCAST] != nil;
}

- (NSDictionary*)configurePacketForBroadcast:(NSDictionary*)packet {
    NSMutableDictionary* newPacket = packet ? [NSMutableDictionary dictionaryWithDictionary:packet] : [NSMutableDictionary dictionary];
    
    if (! [newPacket objectForKey:INTERNAL_DATA_KEY]) {
        [newPacket setObject:[self matchInfo] forKey:INTERNAL_DATA_KEY];
    }
    
    NSMutableDictionary* internalData = [NSMutableDictionary dictionaryWithDictionary:[newPacket objectForKey:INTERNAL_DATA_KEY]];
    [internalData removeObjectForKey:RELAY];
    [internalData setObject:@"true" forKey:BROADCAST];
    [newPacket setObject:internalData forKey:INTERNAL_DATA_KEY];
    
    return newPacket;
}

- (BOOL) isPacketRelay:(NSDictionary*)packet {
    return [self isPacketValid:packet] && [[self getInternalData:packet] objectForKey:RELAY] != nil;
}

- (NSDictionary*)configurePacketForRelay:(NSDictionary*)packet toRecipient:(NSString*)recipient {
    NSMutableDictionary* newPacket = packet ? [NSMutableDictionary dictionaryWithDictionary:packet] : [NSMutableDictionary dictionary];
    
    if (! [newPacket objectForKey:INTERNAL_DATA_KEY]) {
        [newPacket setObject:[self matchInfo] forKey:INTERNAL_DATA_KEY];
    }
    
    NSMutableDictionary* internalData = [NSMutableDictionary dictionaryWithDictionary:[newPacket objectForKey:INTERNAL_DATA_KEY]];
    [internalData removeObjectForKey:BROADCAST];
    [internalData setObject:recipient forKey:RELAY];
    [newPacket setObject:internalData forKey:INTERNAL_DATA_KEY];
    
    return newPacket;
}

- (BOOL) isPacketInternal:(NSDictionary*)packet {
    return [self isPacketValid:packet] && [[self getInternalData:packet] objectForKey:INTERNAL] != nil;
}

- (NSDictionary*)configurePacketForInternalUse:(NSDictionary*)packet {
    NSMutableDictionary* newPacket = packet ? [NSMutableDictionary dictionaryWithDictionary:packet] : [NSMutableDictionary dictionary];
    
    if (! [newPacket objectForKey:INTERNAL_DATA_KEY]) {
        [newPacket setObject:[self matchInfo] forKey:INTERNAL_DATA_KEY];
    }
    
    NSMutableDictionary* internalData = [NSMutableDictionary dictionaryWithDictionary:[newPacket objectForKey:INTERNAL_DATA_KEY]];
    [internalData setObject:@"true" forKey:INTERNAL];
    [newPacket setObject:internalData forKey:INTERNAL_DATA_KEY];
    
    return newPacket;
}

//protected API
- (void) initFieldsWithMatch:(NSString*)match andMatchmaker:(SCMatchmaker*)maker andPlayerId:(NSString*)myId andPassword:(NSString*)pass {
    matchId = [match copy];
    password = [pass copy];
    myPlayerId = [myId copy];
    matchmaker = maker;
    port = -1;
    playerIds = [[NSMutableSet alloc] init];
    playerDetails = [[NSMutableDictionary alloc] init];
    dataReaders = [[NSMutableArray alloc] init];
    dataWriters = [[NSMutableDictionary alloc] init];
}

- (id) initWithPlayerId:(NSString*)myId andMatchmaker:(SCMatchmaker*)maker {
    if (self = [super init]) {
        [self initFieldsWithMatch:nil andMatchmaker:maker andPlayerId:myId andPassword:nil];
    }
    
    return self;
}

- (id) initWithPlayerId:(NSString*)myId andMatchmaker:(SCMatchmaker*)maker andPassword:(NSString*)pass {
    if (self = [super init]) {
        [self initFieldsWithMatch:nil andMatchmaker:maker andPlayerId:myId andPassword:pass];
    }
    
    return self;
}

- (id) initWithPlayerId:(NSString*)myId andMatchmaker:(SCMatchmaker*)maker andMatchId:(NSString*)match {
    if (self = [super init]) {
        [self initFieldsWithMatch:match andMatchmaker:maker andPlayerId:myId andPassword:nil];
    }
    
    return self;
}

- (id) initWithPlayerId:(NSString*)myId andMatchmaker:(SCMatchmaker*)maker andMatchId:(NSString*)match andPassword:(NSString*)pass {
    if (self = [super init]) {
        [self initFieldsWithMatch:match andMatchmaker:maker andPlayerId:myId andPassword:pass];
    }
    
    return self;
}

- (void) dealloc {
    self.password = nil;
    self.matchId = nil;
}

- (NSDictionary*) matchInfo {
    NSMutableDictionary* result = [NSMutableDictionary dictionary];
    
    [result setObject:self.myPlayerId forKey:UUID];
    [result setObject:self.password ? self.password : @"" forKey:@"password"];
    [result setObject:self.matchId forKey:@"matchId"];
    
    return result;
}

- (void)serverThreadEntryPoint:(NSNumber*)sockId {
    int socketId = [sockId intValue];
    while (! stopServer) {
        //FIXME:  need to debug; socket is created but appears to disconnect clients instantly
        //basic server loop, accept connections, create CFReadStreams and CFWriteStreams, and spin up threads to handle them
        NSLog(@"Matchmaker is waiting for players to join...");
        int connectedSocketId = -1;
        struct sockaddr_in clientSocket;
        unsigned int addrlen = sizeof(clientSocket);
        connectedSocketId = accept(socketId, (struct sockaddr *)&clientSocket, &addrlen);
        if (connectedSocketId != -1) {
            //successful connection
            NSLog(@"Matchmaker accepted connection; socketId=%d", connectedSocketId);
            CFReadStreamRef clientInput;
            CFWriteStreamRef clientOutput;
            CFStreamCreatePairWithSocket(kCFAllocatorDefault, connectedSocketId, &clientInput, &clientOutput);
            CFReadStreamSetProperty(clientInput, kCFStreamPropertyShouldCloseNativeSocket, kCFBooleanTrue);
            CFWriteStreamSetProperty(clientOutput, kCFStreamPropertyShouldCloseNativeSocket, kCFBooleanTrue);
            if (! CFReadStreamOpen(clientInput) || ! CFWriteStreamOpen(clientOutput)) {
                NSLog(@"Matchmaker could not initialize streams!");
            }
            
            //now handshake with the client
            NSDictionary* clientDetails = [DataUtils readJsonFromStream:clientInput];
            NSDictionary* internalData = [self getInternalData:clientDetails];
            if (! [self isPacketValid:clientDetails]) {
                NSLog(@"Matchmaker detected an invalid handshake, disconnecting client; socketId=%d", connectedSocketId);
                CFReadStreamClose(clientInput);
                CFWriteStreamClose(clientOutput);
                CFRelease(clientInput);
                CFRelease(clientOutput);
                close(connectedSocketId);
                
                continue;
            }
            
            NSString* playerId = [internalData objectForKey:UUID];
            if (! playerId || [playerId isEqual:self.myPlayerId]) {
                //invalid uuid
                NSLog(@"Matchmaker detected an invalid uuid, disconnecting client; socketId=%d", connectedSocketId);
                CFReadStreamClose(clientInput);
                CFWriteStreamClose(clientOutput);
                CFRelease(clientInput);
                CFRelease(clientOutput);
                close(connectedSocketId);
                
                continue;
            }
            
            NSLog(@"Matchmaker completed handshake with client; socketId=%d", connectedSocketId);
            
            //successful handshake, confirm with the delegate
            if (! [matchmaker.delegate shouldAcceptJoinFromPlayerWithId:playerId andDetails:clientDetails]) {
                NSLog(@"Matchmaking delegate vetoed the connection, disconnecting client; socketId=%d", connectedSocketId);
                CFReadStreamClose(clientInput);
                CFWriteStreamClose(clientOutput);
                CFRelease(clientInput);
                CFRelease(clientOutput);
                close(connectedSocketId);
                
                continue;
            }
            
            //now send our details back to the client
            NSDictionary* localDetails = [matchmaker.delegate localPlayerDetails];
            NSMutableDictionary* serverDetails = localDetails ? [NSMutableDictionary dictionaryWithDictionary:localDetails] : [NSMutableDictionary dictionary];
            [serverDetails setObject:[self matchInfo] forKey:INTERNAL_DATA_KEY];
            [DataUtils writeJson:[serverDetails JSONRepresentation] toStream:clientOutput];
            
            //notify the matchmaker
            [matchmaker player:playerId joinedMatch:self.matchId];
            [matchmaker.delegate playerJoinedWithId:playerId andDetails:clientDetails];
            
            NSLog(@"Matchmaker is spinning up data-handler threads for player; socketId=%d", socketId);
            
            @synchronized(playerIds) {
                //FIXME:  test the ordering
                DataWriter* playerWriter = [[DataWriter alloc] initWithMatchmaker:matchmaker andOutput:clientOutput andDelegate:self forPlayer:playerId];
                DataReader* playerReader = [[DataReader alloc] initWithMatchmaker:matchmaker andInput:clientInput andDelegate:self andPairedWriter:playerWriter];
                [dataWriters setObject:playerWriter forKey:playerId];
                [dataReaders addObject:playerReader];
                
                //tell the new player about everyone else
                for (NSString* player in playerIds) {
                    if (! [player isEqual:playerId]) {
                        NSDictionary* packet = [playerDetails objectForKey:player];
                        packet = [self configurePacketForInternalUse:packet];
                        
                        NSMutableDictionary* mutablePacket = [NSMutableDictionary dictionaryWithDictionary:packet];
                        NSMutableDictionary* mutableInternalData = [NSMutableDictionary dictionaryWithDictionary:[self getInternalData:mutablePacket]];
                        [mutableInternalData setObject:ACTION_PLAYER_JOIN forKey:ACTION];
                        [mutablePacket setObject:mutableInternalData forKey:INTERNAL_DATA_KEY];
                        
                        [DataUtils writeJson:[packet JSONRepresentation] toStream:clientOutput];
                    }
                }
                
                //tell everyone else about the new player
                NSDictionary* packet = [self configurePacketForInternalUse:clientDetails];
                NSMutableDictionary* mutablePacket = [NSMutableDictionary dictionaryWithDictionary:packet];
                NSMutableDictionary* mutableInternalData = [NSMutableDictionary dictionaryWithDictionary:[self getInternalData:mutablePacket]];
                [mutableInternalData setObject:ACTION_PLAYER_JOIN forKey:ACTION];
                [mutablePacket setObject:mutableInternalData forKey:INTERNAL_DATA_KEY];
                
                [self broadcastMessage:packet];
                
                [playerIds addObject:playerId];
                [playerDetails setObject:clientDetails forKey:playerId];

            }
            
            NSLog(@"Matchmaker completed join for player; socketId=%d", socketId);
        }
    }
    
    NSLog(@"Shutting down game server and disconnecting all clients...");
    
    close(socketId);
    @synchronized(playerIds) {
        for (NSString* playerId in [playerIds copy]) {
            [[dataWriters objectForKey:playerId] close];
        }
        [dataReaders makeObjectsPerformSelector:@selector(close)];
        [dataReaders removeAllObjects];
        [dataWriters removeAllObjects];
    }
    
}

- (int) becomeServer {
    int socketId = -1;
    port = [self randomPort];
    
    socketId = socket(AF_INET, SOCK_STREAM, 0);
    if (socketId == -1) {
        //failed to set up socket
        port = -1;
        return port;
    }
    
    memset(&serverSocket, 0, sizeof(serverSocket));
    serverSocket.sin_family = AF_INET;
    serverSocket.sin_addr.s_addr = INADDR_ANY;
    serverSocket.sin_port = htons(port);
    
    if (bind(socketId, (struct sockaddr *)&serverSocket, sizeof(serverSocket)) == -1) {
		port = -1;
        return port;
	}
    
    if (listen(socketId, 8) == -1) {
        port = -1;
        return port;
    }
    
    //at this point the socket should be bound; spawn the server thread
    stopServer = NO;
    serverPlayerId = [self.myPlayerId copy];
    [NSThread detachNewThreadSelector:@selector(serverThreadEntryPoint:) toTarget:self withObject:[NSNumber numberWithInt:socketId]];
    
    return port;
}

- (void) terminateServer {
    //FIXME:  anything more that needs to be done here?
    stopServer = YES;
}

- (BOOL) connectToServer:(NSString*)host onPort:(int)prt withServerId:(NSString*)server {
    serverPlayerId = [server copy];
    [DataUtils openSocketToServer:host onPort:prt withReadStream:&input andWriteStream:&output];
    
    //handshake synchronously
    NSDictionary* localDetails = [matchmaker.delegate localPlayerDetails];
    NSMutableDictionary* myDetails = localDetails ? [NSMutableDictionary dictionaryWithDictionary:localDetails] : [NSMutableDictionary dictionary];
    [myDetails setObject:[self matchInfo] forKey:INTERNAL_DATA_KEY];
    
    //send our info to the server
    [DataUtils writeJson:[myDetails JSONRepresentation] toStream:output];
    
    //server should send back its details
    NSDictionary* serverInfo = [DataUtils readJsonFromStream:input];
    NSDictionary* internalData = [self getInternalData:serverInfo];
    if (! [self isPacketValid:serverInfo]) {
        //invalid handshake, reject
        CFReadStreamClose(input);
        CFWriteStreamClose(output);
        CFRelease(input);
        CFRelease(output);
        return NO;
    }
    
    NSString* handshakeId = [internalData objectForKey:UUID];
    if (! handshakeId || ! [handshakeId isEqual:self.serverPlayerId]) {
        //invalid handshake, reject
        CFReadStreamClose(input);
        CFWriteStreamClose(output);
        CFRelease(input);
        CFRelease(output);
        return NO;
    }
    
    //handshake okay, notify the delegate
    [matchmaker.delegate playerJoinedWithId:serverPlayerId andDetails:serverInfo];
    
    //spawn threads to manage the socket
    DataWriter* writer = [[DataWriter alloc] initWithMatchmaker:matchmaker andOutput:output andDelegate:self forPlayer:serverPlayerId];
    DataReader* reader = [[DataReader alloc] initWithMatchmaker:matchmaker andInput:input andDelegate:self andPairedWriter:writer];
    
    @synchronized(playerIds) {
        [playerIds addObject:serverPlayerId];
        [playerDetails setObject:serverInfo forKey:serverPlayerId];
        [dataWriters setObject:writer forKey:serverPlayerId];
        [dataReaders addObject:reader];
    }
    
    return YES;
    
}

- (void) playerJoinedWithDetails:(NSDictionary*)details andId:(NSString*)playerId {
    @synchronized(playerIds) {
        [playerIds addObject:playerId];
        [playerDetails setObject:details forKey:playerId];
    }
}

- (void) playerLeft:(NSString*)playerId {
    @synchronized(playerIds) {
        //FIXME:  purge dataReader/dataWriter?
        [playerIds removeObject:playerId];
        [playerDetails removeObjectForKey:playerIds];
    }
}

- (void) setMatchId:(NSString*)mtchId {
    matchId = [mtchId copy];
}

- (void) setPassword:(NSString*)pass {
    password = [pass copy];
}


//public API
- (NSSet*) players {
    return [NSSet setWithArray:[playerIds allObjects]];
}

- (BOOL) amITheServer {
    return [self.serverPlayerId isEqual:self.myPlayerId];
}

- (void) broadcastMessage:(NSDictionary*)message {
    @synchronized(playerIds) {
        if ([self amITheServer]) {
            //if we're the server, we can broadcast to all other players directly
            for (NSString* playerId in playerIds) {
                [[dataWriters objectForKey:playerId] sendData:message];
            }
        }
        else {
            //configure the packet for broadcast, and send it off to the server
            message = [self configurePacketForBroadcast:message];
            [[dataWriters objectForKey:self.serverPlayerId] sendData:message];
        }
    }
}

- (void) sendMessage:(NSDictionary*)message toPlayer:(NSString*)playerId {
    @synchronized(playerIds) {
        if (! [playerIds containsObject:playerId]) {
            return;
        }
        if ([self amITheServer]) {
            //can send directly to the recipient
            [[dataWriters objectForKey:playerId] sendData:message];
        }
        else {
            //relay the message through the server
            message = [self configurePacketForRelay:message toRecipient:playerId];
            [[dataWriters objectForKey:self.serverPlayerId] sendData:message];
        }
    }
}

//delegate protocols
- (void) dataReaderDidDisconnect:(DataReader*)reader {
    @synchronized(playerIds) {
        [dataReaders removeObject:reader];
    }
}

- (void) dataWriterDidDisconnect:(DataWriter*)writer forPlayerId:(NSString*)playerId {
    [writer close];
    @synchronized(playerIds) {
        [playerIds removeObject:playerId];
        [playerDetails removeObjectForKey:playerId];
        [dataWriters removeObjectForKey:playerId];
    }
}

- (DataWriter*)dataWriterForPlayer:(NSString*)playerId {
    return [dataWriters objectForKey:playerId];
}

@end
