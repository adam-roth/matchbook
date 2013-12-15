//
//  SCMatchmaker.h
//  MatchmakingSDK
//
//  Created by Adam Roth on 27/03/12.
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "SCMatchmakerDelegate.h"

@class SCMatch;

@interface SCMatchmaker : NSObject {
    NSString* uuid;
    NSString* app;
    NSString* secret;
    NSString* serverBaseUrl;
}

//constructors
- (id) initWithKey:(NSString*)apiKey;
- (id) initWithKey:(NSString*)apiKey andServerAddress:(NSString*)serverRoot;
- (id) initWithKey:(NSString*)apiKey andDelegate:(NSObject<SCMatchmakerDelegate>*)del;
- (id) initWithKey:(NSString*)apiKey andServerAddress:(NSString*)serverRoot andDelegate:(NSObject<SCMatchmakerDelegate>*)del;
- (id) initWithDeviceId:(NSString*)devId bundle:(NSString*)bundle key:(NSString*)apiKey andServerAddress:(NSString*)serverRoot andDelegate:(NSObject<SCMatchmakerDelegate>*)del;

//protected/internal API
- (NSDictionary*)player:(NSString*)playerId leftMatch:(NSString*)matchId;
//- (NSString*)getLocalIp;
//- (NSString*)getUuid;

//public API
- (BOOL)startMatch:(SCMatch*)match;
- (BOOL)cancelMatch:(SCMatch*)match;
- (SCMatch*)joinPrivateMatch:(NSString*)password;
- (SCMatch*)joinPrivateMatch:(NSString*)password withOptions:(NSString*)options;
- (SCMatch*)hostPrivateMatchWithMaxPlayers:(int)numPlayers;
- (SCMatch*)hostPrivateMatchWithMaxPlayers:(int)numPlayers andOptions:(NSString*)gameOptions;
- (SCMatch*)autoJoinMatch;
- (SCMatch*)autoJoinMatchWithMaxPlayers:(int)numPlayers;
- (SCMatch*)autoJoinMatchWithMaxPlayers:(int)numPlayers creatingIfNecessary:(BOOL)createIfNecessary;
- (SCMatch*)autoJoinMatchWithMaxPlayers:(int)numPlayers creatingIfNecessary:(BOOL)createIfNecessary withOptions:(NSString*)gameOptions;
- (NSDictionary*)player:(NSString*)playerId joinedMatch:(NSString*)matchId;


@property(strong) NSObject<SCMatchmakerDelegate>* delegate;

@end
