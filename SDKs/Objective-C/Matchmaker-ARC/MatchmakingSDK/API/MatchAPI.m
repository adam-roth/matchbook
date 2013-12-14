//
//  MatchAPI.m
//  MatchmakingSDK
//
//  Created by Adam Roth on 27/03/12.
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

#import "MatchAPI.h"
#import "NSString+JavaAPI.h"
#import "StringUtilities.h"

@implementation MatchAPI

static NSString* METHOD_NAMES[] = {@"autoMatch", @"hostMatch", @"joinMatch", @"pingMatch", @"startMatch", @"cancelMatch", 
                                   @"playerJoined", @"playerLeft", @"requestProxiedConnection", @"listWaitingProxies"};

+ (NSString*)methodNameFor:(ApiMethod)method {
    return METHOD_NAMES[method];
}

+ (NSString*)urlForMethod:(ApiMethod)method onServer:(NSString*)serverBase {
    NSString* methodName = [self methodNameFor:method];
    if ([serverBase endsWith:@"/"]) {
        return [serverBase stringByAppendingFormat:@"%@?format=json", methodName];
    }
    return [serverBase stringByAppendingFormat:@"/%@?format=json", methodName];
}

+ (NSString*)urlForMethod:(ApiMethod)method onServer:(NSString*)serverBase withQueryString:(NSString*)queryString {
    NSString* methodName = [self methodNameFor:method];
    if ([StringUtilities isEmpty:queryString]) {
        return [self urlForMethod:method onServer:serverBase];
    }
    
    if ([serverBase endsWith:@"/"]) {
        return [serverBase stringByAppendingFormat:@"%@?format=json&%@", methodName, queryString];
    }
    return [serverBase stringByAppendingFormat:@"/%@?format=json&%@", methodName, queryString];
}

+ (NSString*)urlForMethod:(ApiMethod)method onServer:(NSString*)serverBase withParams:(NSDictionary*)queryParams {
    NSString* queryString = @"";
    for (NSString* key in [queryParams keyEnumerator]) {
        if (! [StringUtilities isEmpty:[queryParams objectForKey:key]]) {
            if ([StringUtilities isEmpty:queryString]) {
                queryString = [NSString stringWithFormat:@"%@=%@", key, [queryParams objectForKey:key]];
            }
            else {
                queryString = [queryString stringByAppendingFormat:@"&%@=%@", key, [queryParams objectForKey:key]];
            }
        }
    }
    
    return [self urlForMethod:method onServer:serverBase withQueryString:queryString];
}

@end
