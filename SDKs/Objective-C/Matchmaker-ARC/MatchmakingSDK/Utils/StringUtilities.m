//
//  StringUtilities.m
//  MatchmakingSDK
//
//  Created by Adam Roth on 27/03/12.
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

#import "StringUtilities.h"

@implementation StringUtilities

+ (NSString*) uuidString {
    // Create universally unique identifier (object)
    CFUUIDRef uuidObject = CFUUIDCreate(kCFAllocatorDefault);
    
    // Get the string representation of CFUUID object.
    NSString *uuidStr = (NSString *)CFBridgingRelease(CFUUIDCreateString(kCFAllocatorDefault, uuidObject));
    uuidStr = [[uuidStr lowercaseString] stringByReplacingOccurrencesOfString:@"-" withString:@""];
    
    CFRelease(uuidObject);
    
    return uuidStr;
}

+ (NSString*) randomStringWithLengthBetween:(int)min and:(int)max {
    int spread = max - min;
    int length = min + arc4random() % spread;
    
    return [self randomStringWithLength:length];
}

+ (NSString*) randomStringWithLength:(int)min {
    NSString* buffer = @"";
    while ([buffer length] < min) {
        buffer = [buffer stringByAppendingString:[self uuidString]];
    }
    
    return [buffer substringToIndex:min];
}


+ (BOOL) isEmpty: (NSString*)string {
    return ! (string && [string length] > 0);
}

@end
