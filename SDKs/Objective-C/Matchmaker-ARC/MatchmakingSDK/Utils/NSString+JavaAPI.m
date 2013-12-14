//
//  NSString+JavaAPI.m
//  AgPro
//
//  Created by Adam Roth on 26/10/11.
//  Copyright (c) 2011 __MyCompanyName__. All rights reserved.
//

#import "NSString+JavaAPI.h"

@implementation NSString (NSString_JavaAPI)
- (int) compareTo: (NSString*) comp {
    NSComparisonResult result = [self compare:comp];
    if (result == NSOrderedSame) {
        return 0;
    }
    return result == NSOrderedAscending ? -1 : 1;
}

- (int) compareToIgnoreCase: (NSString*) comp {
    return [[self lowercaseString] compareTo:[comp lowercaseString]];
}

- (bool) contains: (NSString*) substring {
    NSRange range = [self rangeOfString:substring];
    return range.location != NSNotFound;
}

- (bool) endsWith: (NSString*) substring {
    int location = [self lastIndexOf:substring];
    return location == [self length] - [substring length];
}

- (bool) startsWith: (NSString*) substring {
    NSRange range = [self rangeOfString:substring];
    return range.location == 0;
}

- (int) indexOf: (NSString*) substring {
    NSRange range = [self rangeOfString:substring];
    return range.location == NSNotFound ? -1 : range.location;
}

- (int) indexOf:(NSString *)substring startingFrom: (int) index {
    NSString* test = [self substringFromIndex:index];
    return [test indexOf:substring];
}

- (int) lastIndexOf: (NSString*) substring {
    if (! [self contains:substring]) {
        return -1;
    }
    int matchIndex = 0;
    NSString* test = self;
    while ([test contains:substring]) {
        if (matchIndex > 0) {
            matchIndex += [substring length];
        }
        matchIndex += [test indexOf:substring];
        test = [test substringFromIndex: [test indexOf:substring] + [substring length]];
    }
    
    return matchIndex;
}

- (int) lastIndexOf:(NSString *)substring startingFrom: (int) index {
    NSString* test = [self substringFromIndex:index];
    return [test lastIndexOf:substring];
}

- (NSString*) substringFromIndex:(int)from toIndex: (int) to {
    NSRange range;
    range.location = from;
    range.length = to - from;
    return [self substringWithRange: range];
}

- (NSString*) trim {
    return [self stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]];
}

- (NSArray*) split: (NSString*) token {
    return [self split:token limit:0];
}

- (NSArray*) split: (NSString*) token limit: (int) maxResults {
    NSMutableArray* result = [NSMutableArray arrayWithCapacity: 8];
    NSString* buffer = self;
    while ([buffer contains:token]) {
        if (maxResults > 0 && [result count] == maxResults - 1) {
            break;
        }
        int matchIndex = [buffer indexOf:token];
        NSString* nextPart = [buffer substringFromIndex:0 toIndex:matchIndex];
        buffer = [buffer substringFromIndex:matchIndex + [token length]];
        if (nextPart) {
            [result addObject:nextPart];
        }
    }
    if ([buffer length] > 0) {
        [result addObject:buffer];
    }
    
    return result;
}

- (NSString*) replace: (NSString*) target withString: (NSString*) replacement {
    return [self stringByReplacingOccurrencesOfString:target withString:replacement];
}
@end