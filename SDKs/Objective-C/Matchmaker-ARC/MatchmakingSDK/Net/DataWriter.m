//
//  DataWriter.m
//  MatchmakingSDK
//
//  Created by Adam Roth on 28/03/12.
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

#import "DataWriter.h"
#import "SCMatchmaker.h"
#import "NSObject+SimpleJson.h"
#import "DataUtils.h"
#import "SCMatchmakerDelegate.h"

@implementation DataWriter

@synthesize delegate;

- (void) threadEntryPoint {
    while (open) {
        @autoreleasepool {
            NSMutableArray* packetsToSend = [NSMutableArray array];
            @synchronized(sendBuffer) {
                [packetsToSend addObjectsFromArray:sendBuffer];
                [sendBuffer removeAllObjects];
            }
            
            for (__strong NSDictionary* packet in packetsToSend) {
                if (! [packet objectForKey:INTERNAL_DATA_KEY]) {
                    NSMutableDictionary* newPacket = [NSMutableDictionary dictionaryWithDictionary:packet];
                    [newPacket setObject:[delegate matchInfo] forKey:INTERNAL_DATA_KEY];
                    packet = newPacket;
                }
                [DataUtils writeJson:[packet JSONRepresentation] toStream:output];
            }
            
            [NSThread sleepForTimeInterval:([sendBuffer count] > 0 ? 0.01 : 0.1)];
        }
    }
    //NSLog(@"DataWriter is closing WriteStream");
    CFWriteStreamClose(output);
    CFRelease(output);
    
    if ([delegate amITheServer] && ! [[delegate myPlayerId] isEqual:playerId]) {
        [matchmaker.delegate playerLeft:playerId];
        [matchmaker player:playerId leftMatch:[delegate matchId]];
        
        [delegate playerLeft:playerId];
    }
    [delegate dataWriterDidDisconnect:self forPlayerId:playerId];
}

- (id) initWithMatchmaker:(SCMatchmaker*)maker andOutput:(CFWriteStreamRef)outData andDelegate:(NSObject<DataWriterDelegate>*)del forPlayer:(NSString*)player{
    if (self = [super init]) {
        output = outData;
        self.delegate = del;
        matchmaker = maker;
        sendBuffer = [[NSMutableArray alloc] init];
        playerId = [player copy];
        open = YES;
        
        //start the thread
        [NSThread detachNewThreadSelector:@selector(threadEntryPoint) toTarget:self withObject:nil];
    }
    
    return self;
}


- (void) close {
    open = NO;
}

- (void) sendData:(NSDictionary*)jsonData {
    if (! jsonData) {
        return;
    }
    @synchronized(sendBuffer) {
        [sendBuffer addObject:jsonData];
    }
}

@end
