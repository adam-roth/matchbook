//
//  DataReader.h
//  MatchmakingSDK
//
//  Created by Adam Roth on 28/03/12.
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "DataReaderDelegate.h"

@class SCMatchmaker;
@class DataWriter;

@interface DataReader : NSObject {
    //private
    SCMatchmaker* matchmaker;
    CFReadStreamRef input;
    DataWriter* writer;
}

- (id) initWithMatchmaker:(SCMatchmaker*)maker andInput:(CFReadStreamRef)dataIn andDelegate:(NSObject<DataReaderDelegate>*)del andPairedWriter:(DataWriter*)writer;

@property(strong) NSObject<DataReaderDelegate>* delegate;

@end
