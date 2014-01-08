//
//  StringUtilities.h
//  MatchmakingSDK
//
//  Created by Adam Roth on 27/03/12.
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface StringUtilities : NSObject {
}

+ (NSString*) randomStringWithLengthBetween:(int)min and:(int)max;
+ (NSString*) randomStringWithLength:(int)min;
+ (BOOL) isEmpty: (NSString*)string;


@end
