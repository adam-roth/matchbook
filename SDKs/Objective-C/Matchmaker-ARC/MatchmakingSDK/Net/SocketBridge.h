//
//  SocketBridge.h
//  MatchmakingSDK
//
//  Created by Adam Roth on 28/03/12.
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface SocketBridge : NSObject {
    BOOL closed;
    
    CFReadStreamRef serverInput;
    CFWriteStreamRef serverOutput;
    
    CFReadStreamRef clientInput;
    CFWriteStreamRef clientOutput;
}

- (BOOL) isOpen;
- (id) initWithServerInput:(CFReadStreamRef)serverIn andServerOutput:(CFWriteStreamRef)serverOut andClientInput:(CFReadStreamRef)clientIn andClientOutput:(CFWriteStreamRef)clientOut;

@end
