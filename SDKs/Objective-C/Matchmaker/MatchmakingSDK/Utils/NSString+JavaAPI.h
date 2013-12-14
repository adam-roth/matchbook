//
//  NSString+JavaAPI.h
//  AgPro
//
//  Created by Adam Roth on 26/10/11.
//  Copyright (c) 2011 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface NSString (JavaAPI)

- (int) compareTo: (NSString*) comp;
- (int) compareToIgnoreCase: (NSString*) comp;
- (bool) contains: (NSString*) substring;
- (bool) endsWith: (NSString*) substring;
- (bool) startsWith: (NSString*) substring;
- (int) indexOf: (NSString*) substring;
- (int) indexOf:(NSString *)substring startingFrom: (int) index;
- (int) lastIndexOf: (NSString*) substring;
- (int) lastIndexOf:(NSString *)substring startingFrom: (int) index;
- (NSString*) substringFromIndex:(int)from toIndex: (int) to;
- (NSString*) trim;
- (NSArray*) split: (NSString*) token;
- (NSString*) replace: (NSString*) target withString: (NSString*) replacement;
- (NSArray*) split: (NSString*) token limit: (int) maxResults;

@end
