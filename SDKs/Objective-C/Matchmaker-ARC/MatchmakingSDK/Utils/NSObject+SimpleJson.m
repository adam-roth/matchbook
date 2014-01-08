//
//  NSObject+SimpleJson.m
//  MatchmakingSDK
//
//  Created by aroth on 7/01/2014.
//  Copyright (c) 2014 Suncoast Computing. All rights reserved.
//

#import "NSObject+SimpleJson.h"

@implementation NSObject (SimpleJson)

- (NSString*)JSONRepresentation {
    //write ourself to a JSON string; only works if we're a type that 'NSJSONSerialization' supports
    NSError* error = nil;
    NSData* tempData = [NSJSONSerialization dataWithJSONObject:self options:kNilOptions error:&error];
    if (error) {
        return nil;
    }
    
    return [[NSString alloc] initWithData:tempData encoding:NSUTF8StringEncoding];
}


- (id) JSONValue {
    //converts from a string back into a proper object; only works if we're an NSString or NSData instance
    if (! [self isKindOfClass:[NSString class]] && ! [self isKindOfClass:[NSData class]]) {
        return nil;
    }
    
    NSData* jsonData = nil;
    if ([self isKindOfClass:[NSData class]]) {
        jsonData = (NSData*)self;
    }
    else {
        //we must be an NSString
        jsonData = [((NSString*)self) dataUsingEncoding:NSUTF8StringEncoding];
    }
    
    return [NSJSONSerialization JSONObjectWithData:jsonData options:kNilOptions error:nil];
}

@end
