//
//  Base64.h
//  MatchmakingSDK
//
//  Created by Adam Roth on 27/03/12.
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//



@interface Base64 : NSObject

+ (void) initialize;
+ (NSString*) encode:(NSData*) rawBytes;
+ (NSData*) decode:(NSString*) string;

@end
