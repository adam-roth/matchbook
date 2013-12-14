//
//  MatchAPI.h
//  MatchmakingSDK
//
//  Created by Adam Roth on 27/03/12.
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>

typedef enum {
    AUTO_MATCH      = 0,
    HOST_MATCH      = 1,
	JOIN_MATCH      = 2,
	PING_MATCH      = 3,
	START_MATCH     = 4,
	CANCEL_MATCH    = 5,
	PLAYER_JOINED   = 6,
	PLAYER_LEFT     = 7,
	REQUEST_PROXY   = 8,
	LIST_PROXIES    = 9
} ApiMethod;

@interface MatchAPI : NSObject

+ (NSString*)methodNameFor:(ApiMethod)method;
+ (NSString*)urlForMethod:(ApiMethod)method onServer:(NSString*)serverBase;
+ (NSString*)urlForMethod:(ApiMethod)method onServer:(NSString*)serverBase withQueryString:(NSString*)queryString;
+ (NSString*)urlForMethod:(ApiMethod)method onServer:(NSString*)serverBase withParams:(NSDictionary*)queryParams;
@end
