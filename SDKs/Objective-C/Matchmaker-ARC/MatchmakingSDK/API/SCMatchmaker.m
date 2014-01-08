//
//  SCMatchmaker.m
//  MatchmakingSDK
//
//  Created by Adam Roth on 27/03/12.
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

#import "SCMatchmaker.h"
#import "SCMatch.h"
#import <UIKit/UIKit.h>
#import "MatchAPI.h"
#import "NSObject+SimpleJson.h"
#import "NSString+JavaAPI.h"
#import "DataUtils.h"
#import "SocketBridge.h"
#import "StringUtilities.h"
#import "SCMatchmakerDelegate.h"

#define SERVER_ROOT @"http://localhost:8080/ap/"
#define UUID_PASTEBOARD_NAME @"au.com.suncoastpc.matchbook.uuid.v4"
#define PING_INTERVAL 30.0

@interface SCMatchmaker (Private)

- (NSDictionary*)callMethod:(ApiMethod)method withParams:(NSDictionary*)params;
- (BOOL)didRequestSucceed:(NSDictionary*)response;
- (NSMutableDictionary*)defaultParams;
- (NSDictionary*)startMatchInternal:(NSString*)matchId;
- (NSDictionary*)cancelMatchInternal:(NSString*)matchId;
- (NSDictionary*)pingMatch:(NSString*)matchId;
- (NSDictionary*)player:(NSString*)playerId leftMatch:(NSString*)matchId;
- (NSDictionary*)player:(NSString*)playerId joinedMatch:(NSString*)matchId;
- (NSDictionary*)hostMatchOnPort:(int)port withMaxPlayers:(int)maxPlayers private:(BOOL)privateMatch;
- (NSDictionary*)hostMatchOnPort:(int)port withMaxPlayers:(int)maxPlayers andOptions: (NSString*)gameOptions private:(BOOL)privateMatch;
- (NSDictionary*)hostMatchOnPort:(int)port withMaxPlayers:(int)maxPlayers andOptions: (NSString*)gameOptions withNetworkIp:(NSString*)internalIp private:(BOOL)privateMatch;
- (NSDictionary*)autoMatch:(int)maxPlayers;
- (NSDictionary*)autoMatch:(int)maxPlayers withOptions:(NSString*)gameOptions;
- (NSDictionary*)joinMatchWithPassword:(NSString*)pass;
- (NSDictionary*)joinMatchWithPassword:(NSString*)pass andOptions:(NSString*)gameOptions;
- (NSDictionary*)listWaitingProxiesForMatch:(NSString*)matchId;
- (NSDictionary*)requestProxiedConnectionForMatch:(NSString*)matchId;
- (void) pingThreadEntry: (SCMatch*)match;

@end

@implementation SCMatchmaker

@synthesize delegate;

//protected/internal API
- (NSString*)getLocalIp {
    return nil;
}

- (NSString*)getUuid {
    UIPasteboard* pasteBoard = [UIPasteboard generalPasteboard];//[UIPasteboard pasteboardWithName:UUID_PASTEBOARD_NAME create:YES];
    NSIndexSet* pasteboardItems = [pasteBoard itemSetWithPasteboardTypes:[NSArray arrayWithObject:UUID_PASTEBOARD_NAME]];
    if ([pasteboardItems count] != 1) {
        if ([pasteboardItems count] > 1) {
            //wtf?!?!?!
            NSLog(@"ERROR:  Too many uuid's found!");
            abort();
        }
        
        //generate a new uuid
        CFUUIDRef uuidRef = CFUUIDCreate(kCFAllocatorDefault);
        NSString* uuidString = (NSString *)CFBridgingRelease(CFUUIDCreateString(NULL,uuidRef));
        uuidString = [[uuidString lowercaseString] replace:@" " withString:@"-"];
        [pasteBoard addItems:[NSArray arrayWithObject:[NSDictionary dictionaryWithObject:uuidString forKey:UUID_PASTEBOARD_NAME]]];
        //[pasteBoard setValue:uuidString forPasteboardType:UUID_PASTEBOARD_NAME];
        
        CFRelease(uuidRef);
        
        //refresh the index set
        pasteboardItems = [pasteBoard itemSetWithPasteboardTypes:[NSArray arrayWithObject:UUID_PASTEBOARD_NAME]];
    }
    
    //extract the uuid from the pasteboard
    //NSString* deviceId = [pasteBoard valueForPasteboardType:UUID_PASTEBOARD_NAME];
    NSString* deviceString;
    id deviceId = [[pasteBoard valuesForPasteboardType:UUID_PASTEBOARD_NAME inItemSet:pasteboardItems] objectAtIndex:0];
    if ([deviceId isKindOfClass:[NSData class]]) {
        deviceString = [[NSString alloc] initWithData:deviceId encoding:NSUTF8StringEncoding];
    }
    else {
        deviceString = deviceId;
    }
    return deviceString;
}

- (NSString*)getBundleId {
    return [[NSBundle mainBundle] bundleIdentifier];
}

- (NSString*)defaultServer {
    NSString* result = [[NSBundle mainBundle] objectForInfoDictionaryKey:@"au.com.suncoastpc.matchbook.server"];
    
    return result ? result : SERVER_ROOT;
}

//constructors
- (id) initWithKey:(NSString*)apiKey {
    return [self initWithKey:apiKey andServerAddress:[self defaultServer]];
}

- (id) initWithKey:(NSString*)apiKey andDelegate:(NSObject<SCMatchmakerDelegate>*)del {
    return [self initWithKey:apiKey andServerAddress:[self defaultServer] andDelegate:del];
}

- (id) initWithKey:(NSString*)apiKey andServerAddress:(NSString*)serverRoot {
    return [self initWithKey:apiKey andServerAddress: serverRoot andDelegate:nil];
}

- (id) initWithKey:(NSString*)apiKey andServerAddress:(NSString*)serverRoot andDelegate:(NSObject<SCMatchmakerDelegate>*)del {
    return [self initWithDeviceId:nil bundle:nil key:apiKey andServerAddress:serverRoot andDelegate:del];
}
- (id) initWithDeviceId:(NSString*)devId bundle:(NSString*)bundle key:(NSString*)apiKey andServerAddress:(NSString*)serverRoot andDelegate:(NSObject<SCMatchmakerDelegate>*)del {
    if (self = [super init]) {
        uuid = [self getUuid] ? [[self getUuid] copy] : [devId copy];
        app = [self getBundleId] ? [[self getBundleId] copy] : [bundle copy];
        secret = [apiKey copy];
        serverBaseUrl = [serverRoot copy];
        self.delegate = del;
        NSLog(@"Matchbook API initialized with uuid=%@, bundle=%@, secret=%@, server=%@", uuid, app, secret, serverBaseUrl);
    }
    
    return self;
}


//public API
//FIXME:  synchronize all
- (BOOL)startMatch:(SCMatch*)match {
    NSDictionary* reply = [self startMatchInternal:match.matchId];
    if ([self didRequestSucceed:reply]) {
        //XXX:  don't need to stop the ping thread manually, it should stop itself as soon as the server starts/purges the match
    }
    
    return [self didRequestSucceed:reply];
}

- (BOOL)cancelMatch:(SCMatch*)match {
    if ([match amITheServer]) {
        [match terminateServer];
        //XXX:  don't need to stop the ping thread manually, it should stop itself as soon as the server starts/purges the match
    }
    NSDictionary* reply = [self cancelMatchInternal:match.matchId];
    return [self didRequestSucceed:reply];
}

- (SCMatch*)joinPrivateMatch:(NSString*)password {
    return [self joinPrivateMatch:password withOptions:nil];
}

- (SCMatch*)joinPrivateMatch:(NSString*)password withOptions:(NSString*)options {
    SCMatch* result = nil;
    
    BOOL connected = NO;
    NSDictionary* serverMatch = [self joinMatchWithPassword:password andOptions:options];
    if ([self didRequestSucceed:serverMatch]) {
        result = [[SCMatch alloc] initWithPlayerId:uuid andMatchmaker:self andMatchId:[serverMatch objectForKey:@"id"] andPassword:password];
        int port = [[serverMatch objectForKey:@"port"] intValue];
        NSString* serverId = [serverMatch objectForKey:@"token"];
        if (! [StringUtilities isEmpty:[serverMatch objectForKey:@"localAddr"]]) {
            connected = [result connectToServer:[serverMatch objectForKey:@"localAddr"] onPort:port withServerId:serverId];
        }
        if (! connected) {
            connected = [result connectToServer:[serverMatch objectForKey:@"addr"] onPort:port withServerId:serverId];
        }
        if (! connected) {
            NSDictionary* proxyDetails = [self requestProxiedConnectionForMatch:result.matchId];
            if ([self didRequestSucceed:proxyDetails]) {
                NSString* serverInfo = [proxyDetails objectForKey:@"address"];
                NSArray* parts = [serverInfo split:@":"];
                connected = [result connectToServer:[parts objectAtIndex:0] onPort:[[parts objectAtIndex:1] intValue] withServerId:serverId];
            }
        }
    }
    
    return connected ? result : nil;
}

- (SCMatch*)hostPrivateMatchWithMaxPlayers:(int)numPlayers {
    return [self hostPrivateMatchWithMaxPlayers:numPlayers andOptions:nil];
}

- (SCMatch*)hostPrivateMatchWithMaxPlayers:(int)numPlayers andOptions:(NSString*)gameOptions {
    if (numPlayers < 2) {
        numPlayers = 2;
    }
    SCMatch* result = [[SCMatch alloc] initWithPlayerId:uuid andMatchmaker:self];
    int port = [result becomeServer];
    if (port > 0) {
        NSDictionary* serverReply = [self hostMatchOnPort:port withMaxPlayers:numPlayers andOptions:gameOptions withNetworkIp:[self getLocalIp] private:YES];
        if ([self didRequestSucceed:serverReply]) {
            [result setMatchId:[serverReply objectForKey:@"matchId"]];
            if (! [StringUtilities isEmpty:[serverReply objectForKey:@"password"]]) {
                [result setPassword:[serverReply objectForKey:@"password"]];
            }
            
            [NSThread detachNewThreadSelector:@selector(pingThreadEntry:) toTarget:self withObject:result];
        }
        else {
            [result terminateServer];
            result = nil;
        }
    }
    else {
        [result terminateServer];
        result = nil;
    }
    
    return result;
}

- (SCMatch*)autoJoinMatch {
    return [self autoJoinMatchWithMaxPlayers:-1 creatingIfNecessary:YES];
}

- (SCMatch*)autoJoinMatchWithMaxPlayers:(int)numPlayers {
    return [self autoJoinMatchWithMaxPlayers:numPlayers creatingIfNecessary:YES];
}

- (SCMatch*)autoJoinMatchWithMaxPlayers:(int)numPlayers creatingIfNecessary:(BOOL)createIfNecessary {
    return [self autoJoinMatchWithMaxPlayers:numPlayers creatingIfNecessary:createIfNecessary withOptions:nil];
}

- (SCMatch*)autoJoinMatchWithMaxPlayers:(int)numPlayers creatingIfNecessary:(BOOL)createIfNecessary withOptions:(NSString*)gameOptions {
    SCMatch* result = nil;
    
    NSDictionary* autoMatch = [self autoMatch:numPlayers withOptions:gameOptions];
    if ([self didRequestSucceed:autoMatch]) {
        BOOL connected = NO;
        int port = [[autoMatch objectForKey:@"port"] intValue];
        NSString* serverId = [autoMatch objectForKey:@"token"];
        result = [[SCMatch alloc] initWithPlayerId:uuid andMatchmaker:self andMatchId:[autoMatch objectForKey:@"id"]];
        if (! [StringUtilities isEmpty:[autoMatch objectForKey:@"localAddr"]]) {
            connected = [result connectToServer:[autoMatch objectForKey:@"localAddr"] onPort:port withServerId:serverId];
        }
        if (! connected) {
            connected = [result connectToServer:[autoMatch objectForKey:@"addr"] onPort:port withServerId:serverId];
        }
        if (! connected) {
            NSDictionary* proxyDetails = [self requestProxiedConnectionForMatch:result.matchId];
            if ([self didRequestSucceed:proxyDetails]) {
                NSString* serverInfo = [proxyDetails objectForKey:@"address"];
                NSArray* parts = [serverInfo split:@":"];
                connected = [result connectToServer:[parts objectAtIndex:0] onPort:[[parts objectAtIndex:1] intValue] withServerId:serverId];
            }
        }
        
        result = connected ? result : nil;
    }
    else if (createIfNecessary) {
        result = [[SCMatch alloc] initWithPlayerId:uuid andMatchmaker:self];
        int port = [result becomeServer];
        if (port > 0) {
            NSDictionary* serverReply = [self hostMatchOnPort:port withMaxPlayers:numPlayers andOptions:gameOptions withNetworkIp:[self getLocalIp] private:NO];
            if ([self didRequestSucceed:serverReply]) {
                [result setMatchId:[serverReply objectForKey:@"matchId"]];
                if (! [StringUtilities isEmpty:[serverReply objectForKey:@"password"]]) {
                    [result setPassword:[serverReply objectForKey:@"password"]];
                }
                
                [NSThread detachNewThreadSelector:@selector(pingThreadEntry:) toTarget:self withObject:result];
            }
            else {
                [result terminateServer];
                result = nil;
            }
        }
        else {
            [result terminateServer];
            result = nil;
        }
    }
    
    return result;
}

//private API
- (NSDictionary*)requestProxiedConnectionForMatch:(NSString*)matchId {
    NSMutableDictionary* params = [self defaultParams];
    [params setObject:matchId forKey:@"matchId"];
    
    return [self callMethod:REQUEST_PROXY withParams:params];
}

- (NSDictionary*)listWaitingProxiesForMatch:(NSString*)matchId {
    NSMutableDictionary* params = [self defaultParams];
    [params setObject:matchId forKey:@"matchId"];
    
    return [self callMethod:LIST_PROXIES withParams:params];
}

- (NSDictionary*)joinMatchWithPassword:(NSString*)pass {
    return [self joinMatchWithPassword:pass andOptions:nil];
}

- (NSDictionary*)joinMatchWithPassword:(NSString*)pass andOptions:(NSString*)gameOptions {
    NSMutableDictionary* params = [self defaultParams];
    [params setObject:pass forKey:@"pass"];
    if (gameOptions) {
        [params setObject:gameOptions forKey:@"gameOptions"];
    }
    
    return [self callMethod:JOIN_MATCH withParams:params];
}

- (NSDictionary*)autoMatch:(int)maxPlayers {
    return [self autoMatch:maxPlayers withOptions:nil];
}

- (NSDictionary*)autoMatch:(int)maxPlayers withOptions:(NSString*)gameOptions {
    NSMutableDictionary* params = [self defaultParams];
    if (maxPlayers > 1) {
        [params setObject:[NSString stringWithFormat:@"%d", maxPlayers] forKey:@"maxPlayers"];
    }
    if (gameOptions) {
        [params setObject:gameOptions forKey:@"gameOptions"];
    }
    
    return [self callMethod:AUTO_MATCH withParams:params];
}

- (NSDictionary*)hostMatchOnPort:(int)port withMaxPlayers:(int)maxPlayers private:(BOOL)privateMatch {
    return [self hostMatchOnPort:port withMaxPlayers:maxPlayers andOptions:nil private:privateMatch];
}

- (NSDictionary*)hostMatchOnPort:(int)port withMaxPlayers:(int)maxPlayers andOptions: (NSString*)gameOptions private:(BOOL)privateMatch {
    return [self hostMatchOnPort:port withMaxPlayers:maxPlayers andOptions:gameOptions withNetworkIp:nil private:privateMatch];
}

- (NSDictionary*)hostMatchOnPort:(int)port withMaxPlayers:(int)maxPlayers andOptions: (NSString*)gameOptions withNetworkIp:(NSString*)internalIp private:(BOOL)privateMatch {
    if (maxPlayers < 2) {
        maxPlayers = 2;
    }
    NSMutableDictionary* params = [self defaultParams];
    [params setObject:[NSString stringWithFormat:@"%d", port] forKey:@"port"];
    [params setObject:[NSString stringWithFormat:@"%d", maxPlayers] forKey:@"maxPlayers"];
    if (privateMatch) {
        [params setObject:@"true" forKey:@"privateMatch"];
    }
    if (gameOptions) {
        [params setObject:gameOptions forKey:@"gameOptions"];
    }
    if (internalIp) {
        [params setObject:internalIp forKey:@"internalIp"];
    }
    
    return [self callMethod:HOST_MATCH withParams:params];
}

- (NSDictionary*)player:(NSString*)playerId joinedMatch:(NSString*)matchId {
    NSMutableDictionary* params = [self defaultParams];
    [params setObject:playerId forKey:@"playerUuid"];
    [params setObject:matchId forKey:@"matchId"];
    
    return [self callMethod:PLAYER_JOINED withParams:params];
}

- (NSDictionary*)player:(NSString*)playerId leftMatch:(NSString*)matchId {
    NSMutableDictionary* params = [self defaultParams];
    [params setObject:playerId forKey:@"playerUuid"];
    [params setObject:matchId forKey:@"matchId"];
    
    return [self callMethod:PLAYER_LEFT withParams:params];
}

- (NSDictionary*)pingMatch:(NSString*)matchId {
    NSMutableDictionary* params = [self defaultParams];
    [params setObject:matchId forKey:@"matchId"];
    
    return [self callMethod:PING_MATCH withParams:params];
}

- (NSDictionary*)cancelMatchInternal:(NSString*)matchId {
    NSMutableDictionary* params = [self defaultParams];
    [params setObject:matchId forKey:@"matchId"];
    
    return [self callMethod:CANCEL_MATCH withParams:params];
}

- (NSDictionary*)startMatchInternal:(NSString*)matchId {
    NSMutableDictionary* params = [self defaultParams];
    [params setObject:matchId forKey:@"matchId"];
    
    return [self callMethod:START_MATCH withParams:params];
}

- (NSMutableDictionary*)defaultParams {
    NSMutableDictionary* result = [NSMutableDictionary dictionary];
    
    [result setObject:app forKey:@"app"];
    [result setObject:uuid forKey:@"uuid"];
    [result setObject:secret forKey:@"secret"];
    
    return result;
}

- (BOOL)didRequestSucceed:(NSDictionary*)response {
    return response && [[response objectForKey:@"status"] isEqual:@"success"];
}

- (NSDictionary*)callMethod:(ApiMethod)method withParams:(NSDictionary*)params {
    NSString* requestUrl = [MatchAPI urlForMethod:method onServer:serverBaseUrl withParams:params];
    NSMutableURLRequest* request = [NSMutableURLRequest requestWithURL:[NSURL URLWithString:requestUrl]];
    [request setHTTPMethod:@"POST"];
    
    NSError* error = nil;
    NSURLResponse* response = nil;
    NSData* responseText = [NSURLConnection sendSynchronousRequest:request returningResponse:&response error:&error];
    if (error) {
        NSLog(@"Error returned for request to URL:  %@, %@", requestUrl, error);
        return nil;
    }
    
    //FIXME:  JSON libs are not carried over in framework
    NSString* jsonText = [[[NSString alloc] initWithData:responseText encoding:NSASCIIStringEncoding] trim];
    NSLog(@"Call to url=%@ returned %@", request, jsonText);
    return [jsonText JSONValue];
}

- (void) pingThreadEntry: (SCMatch*)match {
    @autoreleasepool {
        NSMutableArray* bridgeArray = [[NSMutableArray alloc] init];
        while([self didRequestSucceed:[self pingMatch:match.matchId]]) {
            @autoreleasepool {
                NSDictionary* waitingProxies = [self listWaitingProxiesForMatch:match.matchId];
                if ([self didRequestSucceed:waitingProxies] && [[waitingProxies objectForKey:@"numWaiting"] intValue] > 0) {
                    int numWaiting = [[waitingProxies objectForKey:@"numWaiting"] intValue];
                    NSArray* serverInfo = [[waitingProxies objectForKey:@"address"] split:@":"];
                    for (int connection = 0; connection < numWaiting; connection++) {
                        //open a socket to the server; it will set up the relay for us
                        CFReadStreamRef input;
                        CFWriteStreamRef output;
                        [DataUtils openSocketToServer:[serverInfo objectAtIndex:0] onPort:[[serverInfo objectAtIndex:1] intValue] withReadStream:&input andWriteStream:&output];
                        
                        //send details about ourself so that the server can identify us as a valid host for this match
                        NSDictionary* myDetails = [self.delegate localPlayerDetails];
                        if (! myDetails) {
                            myDetails = [NSDictionary dictionary];
                        }
                        NSMutableDictionary* mutableDetails = [NSMutableDictionary dictionaryWithDictionary:myDetails];
                        [mutableDetails setObject:[match matchInfo] forKey:INTERNAL_DATA_KEY];
                        [DataUtils writeJson:[mutableDetails JSONRepresentation] toStream:output];
                        
                        //bridge the socket we just created back to the local server thread
                        CFReadStreamRef localInput;
                        CFWriteStreamRef localOutput;
                        [DataUtils openSocketToServer:@"localhost" onPort:match.port withReadStream:&localInput andWriteStream:&localOutput];
                        
                        //create a socket bridge (will spawn threads to manage piping the data between our sockets)
                        [bridgeArray addObject:[[SocketBridge alloc] initWithServerInput:input andServerOutput:output andClientInput:localInput andClientOutput:localOutput]];
                    }
                }
                
                
                [NSThread sleepForTimeInterval:PING_INTERVAL];
            }
        }
        
        //we don't want to discard the array and destroy the socket bridges until they are *all* closed
        BOOL atLeastOneOpen = YES;
        while (atLeastOneOpen) {
            atLeastOneOpen = NO;
            for (SocketBridge* bridge in bridgeArray) {
                if ([bridge isOpen]) {
                    NSLog(@"Found an active socket bridge, will sleep for 30 seconds and check again...");
                    atLeastOneOpen = YES;
                    break;
                }
            }
                
            [NSThread sleepForTimeInterval:PING_INTERVAL];
        }
        
        NSLog(@"All socket bridges are now inactive; releasing");
        [bridgeArray removeAllObjects];
    }
}


@end
