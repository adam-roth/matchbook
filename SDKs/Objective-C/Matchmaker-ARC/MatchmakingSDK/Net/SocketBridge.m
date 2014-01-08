//
//  SocketBridge.m
//  MatchmakingSDK
//
//  Created by Adam Roth on 28/03/12.
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

#import "SocketBridge.h"
#import "DataUtils.h"
#import "NSObject+SimpleJson.h"

@implementation SocketBridge

- (void) tearDown {
    @synchronized(self) {
        if (! closed) {
            CFReadStreamClose(serverInput);
            CFWriteStreamClose(clientOutput);
            CFReadStreamClose(clientInput);
            CFWriteStreamClose(serverOutput);
            CFRelease(serverInput);
            CFRelease(clientOutput);
            CFRelease(clientInput);
            CFRelease(serverOutput);
            closed = YES;
        }
    }
}

- (void) pipeServerToClient {
    while (! closed) {
        @autoreleasepool {
            NSDictionary* packet = [DataUtils readJsonFromStream:serverInput];
            if (! packet) {
                NSLog(@"SocketBridge failed to read packet from server, closing ReadStream and WriteStream");
                break;
            }
            [DataUtils writeJson:[packet JSONRepresentation] toStream:clientOutput];
        }
    }
    [self tearDown];
}

- (void) pipeClientToServer {
    while (! closed) {
        @autoreleasepool {
            NSDictionary* packet = [DataUtils readJsonFromStream:clientInput];
            if (! packet) {
                NSLog(@"SocketBridge failed to read packet from client, closing ReadStream and WriteStream");
                break;
            }
            [DataUtils writeJson:[packet JSONRepresentation] toStream:serverOutput];
        }
    }
    [self tearDown];
}

- (id) initWithServerInput:(CFReadStreamRef)serverIn andServerOutput:(CFWriteStreamRef)serverOut andClientInput:(CFReadStreamRef)clientIn andClientOutput:(CFWriteStreamRef)clientOut {
    if (self = [super init]) {
        serverInput = serverIn;
        serverOutput = serverOut;
        
        clientInput = clientIn;
        clientOutput = clientOut;
        
        closed = NO;
        
        //start the threads
        [NSThread detachNewThreadSelector:@selector(pipeClientToServer) toTarget:self withObject:nil];
        [NSThread detachNewThreadSelector:@selector(pipeServerToClient) toTarget:self withObject:nil];
    }
    
    return self;
}

- (BOOL) isOpen {
    return ! closed;
}

@end
