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
    serverBase = [self checkServerBase:serverBase];
    
    NSString* methodName = [self methodNameFor:method];
    if ([serverBase endsWith:@"/"]) {
        return [serverBase stringByAppendingFormat:@"%@?format=json", methodName];
    }
    return [serverBase stringByAppendingFormat:@"/%@?format=json", methodName];
}

+ (NSString*)urlForMethod:(ApiMethod)method onServer:(NSString*)serverBase withQueryString:(NSString*)queryString {
    serverBase = [self checkServerBase:serverBase];
    
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
    serverBase = [self checkServerBase:serverBase];
    
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

+ (NSString*) checkServerBase:(NSString*)serverBase {
    //make sure we have a sane server URL; we assume that the webservice API methods have not been moved from their default path of '/ap'
    if (! [serverBase endsWith:@"/ap/"]) {
        NSLog(@"WARN:  The provided Matchbook server URL does not appear valid; url=%@", serverBase);
        if ([serverBase endsWith:@"/ap"]) {
            serverBase = [serverBase stringByAppendingString:@"/"];
        }
        else {
            serverBase = [serverBase stringByAppendingString:@"/ap/"];
        }
        NSLog(@"WARN:  Matchbook will attempt to use the following server URL:  %@", serverBase);
    }
    
    return serverBase;
}

@end
