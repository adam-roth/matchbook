//
//  DataReader.h
//  MatchmakingSDK
//
//  Created by Adam Roth on 28/03/12.
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "DataReaderDelegate.h"

@class IAMatchmaker;
@class DataWriter;

@interface DataReader : NSObject {
    //private
    IAMatchmaker* matchmaker;
    CFReadStreamRef input;
    DataWriter* writer;
}

- (id) initWithMatchmaker:(IAMatchmaker*)maker andInput:(CFReadStreamRef)dataIn andDelegate:(NSObject<DataReaderDelegate>*)del andPairedWriter:(DataWriter*)writer;

@property(retain) NSObject<DataReaderDelegate>* delegate;

@end
