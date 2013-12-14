//
//  IAMatchmaker.h
//  MatchmakingSDK
//
//  Created by Adam Roth on 27/03/12.
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "IAMatchmakerDelegate.h"

@class IAMatch;
//@protocol IAMatchmakerDelegate;

@interface IAMatchmaker : NSObject {
    NSString* uuid;
    NSString* app;
    NSString* secret;
}

//constructors
- (id) initWithKey:(NSString*)apiKey;
- (id) initWithKey:(NSString*)apiKey andDelegate:(NSObject<IAMatchmakerDelegate>*)delegate;
- (id) initWithDeviceId:(NSString*)devId bundle:(NSString*)bundle key:(NSString*)apiKey andDelegate:(NSObject<IAMatchmakerDelegate>*)delegate;

//protected/internal API
- (NSDictionary*)player:(NSString*)playerId leftMatch:(NSString*)matchId;
//- (NSString*)getLocalIp;
//- (NSString*)getUuid;

//public API
- (BOOL)startMatch:(IAMatch*)match;
- (BOOL)cancelMatch:(IAMatch*)match;
- (IAMatch*)joinPrivateMatch:(NSString*)password;
- (IAMatch*)joinPrivateMatch:(NSString*)password withOptions:(NSString*)options;
- (IAMatch*)hostPrivateMatchWithMaxPlayers:(int)numPlayers;
- (IAMatch*)hostPrivateMatchWithMaxPlayers:(int)numPlayers andOptions:(NSString*)gameOptions;
- (IAMatch*)autoJoinMatch;
- (IAMatch*)autoJoinMatchWithMaxPlayers:(int)numPlayers;
- (IAMatch*)autoJoinMatchWithMaxPlayers:(int)numPlayers creatingIfNecessary:(BOOL)createIfNecessary;
- (IAMatch*)autoJoinMatchWithMaxPlayers:(int)numPlayers creatingIfNecessary:(BOOL)createIfNecessary withOptions:(NSString*)gameOptions;
- (NSDictionary*)player:(NSString*)playerId joinedMatch:(NSString*)matchId;


@property(strong) NSObject<IAMatchmakerDelegate>* delegate;

@end
