//
//  ViewController.h
//  MAtchmakingExample
//
//  Created by Adam Roth on 29/03/12.
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <MatchmakingSDK/API/IAMatchmakerDelegate.h>

@class IAMatch;
@class IAMatchmaker;

@interface ViewController : UIViewController<IAMatchmakerDelegate> {
    IAMatch* match;
    IAMatchmaker* matchmaker;
    
    UILabel* countLabel;
    int counter;
    BOOL matchRunning;
}

@end
