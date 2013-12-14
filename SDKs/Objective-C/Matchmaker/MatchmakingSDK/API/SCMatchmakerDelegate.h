//
//  MatchmakerDelegate.h
//  MatchmakingSDK
//
//  Created by Adam Roth on 27/03/12.
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>

@protocol SCMatchmakerDelegate <NSObject>

@required
- (BOOL)shouldAcceptJoinFromPlayerWithId:(NSString*)playerId andDetails:(NSDictionary*)playerData;
- (void)receivedData: (NSDictionary*)data fromPlayerWithId:(NSString*)playerId;

//@optional
- (void)playerJoinedWithId:(NSString*)playerId andDetails:(NSDictionary*)playerData;
- (void)playerLeft:(NSString*)playerId;
- (NSDictionary*) localPlayerDetails;

@end
