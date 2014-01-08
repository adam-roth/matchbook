//
//  NSObject+SimpleJson.h
//  MatchmakingSDK
//
//  Created by aroth on 7/01/2014.
//  Copyright (c) 2014 Suncoast Computing. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface NSObject (SimpleJson)

- (NSString *)JSONRepresentation;
- (id)JSONValue;

@end
