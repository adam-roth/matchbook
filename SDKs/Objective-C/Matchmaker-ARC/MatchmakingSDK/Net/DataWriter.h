//
//  DataWriter.h
//  MatchmakingSDK
//
//  Created by Adam Roth on 28/03/12.
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "DataWriterDelegate.h"

@class SCMatchmaker;

@interface DataWriter : NSObject {
    //private
    SCMatchmaker* matchmaker;
    CFWriteStreamRef output;
    NSMutableArray* sendBuffer;
    NSString* playerId;
    BOOL open;
}

- (id) initWithMatchmaker:(SCMatchmaker*)maker andOutput:(CFWriteStreamRef)outData andDelegate:(NSObject<DataWriterDelegate>*)del forPlayer:(NSString*)player;

- (void) close;
- (void) sendData:(NSDictionary*)jsonData;

@property(strong) NSObject<DataWriterDelegate>* delegate;

@end
