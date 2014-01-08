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
    //converts ourself to a JSON string
    NSError* error = nil;
    NSData* tempData = [NSJSONSerialization dataWithJSONObject:self options:kNilOptions error:&error];
    if (error) {
        return nil;
    }
    
    return [[[NSString alloc] initWithData:tempData encoding:NSUTF8StringEncoding] autorelease];
}


- (id) JSONValue {
    //converts from a string back into a proper object
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
