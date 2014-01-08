//
//  ViewController.h
//  MAtchmakingExample
//
//  Created by Adam Roth on 29/03/12.
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <MatchmakingSDK/SCMatchmakerDelegate.h>

@class SCMatch;
@class SCMatchmaker;

@interface ViewController : UIViewController<SCMatchmakerDelegate> {
    SCMatch* match;
    SCMatchmaker* matchmaker;
    
    UILabel* countLabel;
    int counter;
    BOOL matchRunning;
}

@end
