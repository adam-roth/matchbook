//
//  DataReader.m
//  MatchmakingSDK
//
//  Created by Adam Roth on 28/03/12.
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

#import "DataReader.h"
#import "DataUtils.h"
#import "SCMatchmaker.h"
#import "DataWriter.h"

@implementation DataReader 

@synthesize delegate;

- (void) handleInternalPacket:(NSDictionary*)packet {
    NSDictionary* internalData = [packet objectForKey:INTERNAL_DATA_KEY];
    NSString* action = [internalData objectForKey:ACTION];
    if ([action isEqual:ACTION_PLAYER_JOIN]) {
        //notify interested parties that a new player has joined
        NSString* playerId = [internalData objectForKey:UUID];
        [delegate playerJoinedWithDetails:packet andId:playerId];
        [matchmaker.delegate playerJoinedWithId:playerId andDetails:packet];
    }
}

- (void) threadEntryPoint {
    while(true) {
        @autoreleasepool {
            NSDictionary* packet = [DataUtils readJsonFromStream:input];
            if (packet && [delegate isPacketValid:packet]) {
                NSSet* players = [delegate players];
                NSDictionary* senderDetails = [packet objectForKey:INTERNAL_DATA_KEY];
                NSString* senderId = [senderDetails objectForKey:UUID];
                if ((senderId && [players containsObject:senderId]) || [delegate isPacketInternal:packet]) {
                    if ([delegate amITheServer]) {
                        BOOL ignorePacket = NO;
                        if ([delegate isPacketBroadcast:packet]) {
                            for (NSString* playerId in players) {
                                if (! [playerId isEqual:senderId]) {
                                    DataWriter* theWriter = [delegate dataWriterForPlayer:playerId];
                                    [theWriter sendData:packet];
                                }
                            }
                        }
                        else if ([delegate isPacketRelay:packet]) {
                            NSString* recipientId = [senderDetails objectForKey:RELAY];
                            if (! [recipientId isEqual:[delegate myPlayerId]]) {
                                ignorePacket = YES;
                                DataWriter* theWriter = [delegate dataWriterForPlayer:recipientId];
                                [theWriter sendData:packet];
                            }
                        }
                        
                        if (! ignorePacket) {
                            if (! [delegate isPacketInternal:packet]) {
                                //pass on the packet to thre delegate
                                [matchmaker.delegate receivedData:packet fromPlayerWithId:senderId];
                            }
                            else {
                                [self handleInternalPacket:packet];
                            }
                        }
                    }
                    else {
                        //just pass the packet on if it's not internal
                        if ([delegate isPacketInternal:packet]) {
                            [self handleInternalPacket:packet];
                        }
                        else {
                            [matchmaker.delegate receivedData:packet fromPlayerWithId:senderId];
                        }
                    }
                }
                else {
                    //valid packet but invalid sender/contents, abandon connection
                    //NSLog(@"DataReader got a packet with invalid metadata, closing ReadStream");
                    CFReadStreamClose(input);
                    CFRelease(input);
                    break;
                }
            }
            else {
                //invalid packet, abandon connection
                //NSLog(@"DataReader could not read packet, closing ReadStream");
                CFReadStreamClose(input);
                CFRelease(input);
                break;
            }
        }
    }
    [writer close];
    [delegate dataReaderDidDisconnect:self];
}

- (id) initWithMatchmaker:(SCMatchmaker*)maker andInput:(CFReadStreamRef)dataIn andDelegate:(NSObject<DataReaderDelegate>*)del andPairedWriter:(DataWriter*)theWriter {
    if (self = [super init]) {
        matchmaker = maker;
        input = dataIn;
        writer = theWriter;
        self.delegate = del;
        
        //start the thread
        [NSThread detachNewThreadSelector:@selector(threadEntryPoint) toTarget:self withObject:nil];
    }
    
    return self;
}


@end
