//
//  ViewController.m
//  MAtchmakingExample
//
//  Created by Adam Roth on 29/03/12.
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

#import "ViewController.h"
#import <MatchmakingSDK/SCMatch.h>
#import <MatchmakingSDK/SCMatchmaker.h>

//au.com.suncoastpc.MatchmakingExample
#define MATCHMAKER_KEY @"ef20a2a065e345d0"

#define NUM_PLAYERS 4
#define LABEL_SIZE 44

@implementation ViewController

//demonstrates a simple periodic message broadcast to all players
- (void) pingLoop {
    @autoreleasepool {
        while ([[match players] count] > 0) {
            @autoreleasepool {
                [NSThread sleepForTimeInterval:5.0];
                [match broadcastMessage:[NSDictionary dictionaryWithObject:@"PING!" forKey:@"message"]];
            }
        }
    }
}

- (void) runPingLoopWhenReady {
    while ([[match players] count] < NUM_PLAYERS - 1) {
        [NSThread sleepForTimeInterval:0.1];
    }
    [NSThread detachNewThreadSelector:@selector(pingLoop) toTarget:self withObject:nil];
}

- (void) respondToPing {
    int red = (arc4random() % 255);
    int green = (arc4random() % 255);
    int blue = (arc4random() % 255);
    
    self.view.backgroundColor = [UIColor colorWithRed:red / 255.0 green:green / 255.0 blue:blue / 255.0 alpha:1.0];
    [self performSelectorOnMainThread:@selector(updateCounter) withObject:nil waitUntilDone:NO];
    [match sendMessage:[NSDictionary dictionaryWithObject:[NSNumber numberWithInt:(0x00FFFFFF & (red << 16 | green << 8 | blue))] forKey:@"color"] toPlayer:[match serverPlayerId]];
}

- (void) addLabelForPlayer:(NSString*)playerId atIndex:(int)index {
    UILabel* playerLabel = [[[UILabel alloc] initWithFrame:CGRectMake(0.0, index * LABEL_SIZE, self.view.frame.size.width, LABEL_SIZE)] autorelease];
    playerLabel.opaque = YES;
    playerLabel.backgroundColor = [UIColor blackColor];
    playerLabel.textColor = [UIColor blueColor];
    playerLabel.tag = [playerId hash];
    playerLabel.font = [UIFont boldSystemFontOfSize:12.0];
    playerLabel.text = [NSString stringWithFormat:@"  Player %d:  %@", index, playerId];
    
    [self.view addSubview:playerLabel];
    countLabel.frame = CGRectMake(0.0, index * LABEL_SIZE, self.view.frame.size.width, self.view.frame.size.height - (index * LABEL_SIZE));
}

- (void) addLabelForPlayer:(NSString*)playerId {
    [self addLabelForPlayer:playerId atIndex:[[match players] count]];
}

- (void) removeLabelForPlayer:(NSString*)playerId {
    UILabel* playerLabel = (UILabel*)[self.view viewWithTag:[playerId hash]];
    [playerLabel removeFromSuperview];
    
    int index = 0;
    for (NSString* player in [match players]) {
        if (! [player isEqual:playerId]) {
            playerLabel = (UILabel*)[self.view viewWithTag:[player hash]];
            playerLabel.text = [NSString stringWithFormat:@"  Player %d:  %@", index, player];
            playerLabel.frame = CGRectMake(0.0, index * LABEL_SIZE, self.view.frame.size.width, LABEL_SIZE);
            
            index++;
        }
    }
    
    countLabel.frame = CGRectMake(0.0, index * LABEL_SIZE, self.view.frame.size.width, self.view.frame.size.height - (index * LABEL_SIZE));
}

- (void) updateLabel:(NSDictionary*)params{
    NSString* playerId = [params objectForKey:@"player"];
    int newColor = [[params objectForKey:@"color"] intValue];
    
    int red = newColor >> 16 & 0x000000FF;
    int green = newColor >> 8 & 0x000000FF;
    int blue = newColor & 0x000000FF;
    UILabel* playerLabel = (UILabel*)[self.view viewWithTag:[playerId hash]];
    
    playerLabel.textColor = [UIColor colorWithRed:red / 255.0 green:green / 255.0 blue:blue / 255.0 alpha:1.0];
    
    
    NSLog(@"The color for player %@ is now 0x%06x", playerId, newColor);
}

- (void)updateCounter {
    countLabel.hidden = NO;
    countLabel.text = [NSString stringWithFormat:@"%d", counter];
}

//matchmaking delegate methods
- (BOOL)shouldAcceptJoinFromPlayerWithId:(NSString*)playerId andDetails:(NSDictionary*)playerData {
    //whether or not a new player should be allowed to join
    return [[match players] count] < NUM_PLAYERS - 1;  //players will never contain our own id, and it will not yet contain the joining player id
}

//handles the various messages that we can recieve from our "game"
- (void)receivedData: (NSDictionary*)data fromPlayerWithId:(NSString*)playerId {
    //handling messages from players
    if ([[data objectForKey:@"message"] isEqual:@"PING!"]) {
        matchRunning = YES;
        [self performSelectorOnMainThread:@selector(respondToPing) withObject:nil waitUntilDone:YES];
    }
    else if ([[data objectForKey:@"message"] isEqual:@"increment"]) {
        @synchronized(countLabel) {
            counter++;
            [self performSelectorOnMainThread:@selector(updateCounter) withObject:nil waitUntilDone:YES];
        }
    }
    else {
        matchRunning = YES;
        NSMutableDictionary* params = [NSMutableDictionary dictionary];
        [params setObject:[data objectForKey:@"color"] forKey:@"color"];
        [params setObject:playerId forKey:@"player"];
        [self performSelectorOnMainThread:@selector(updateLabel:) withObject:params waitUntilDone:NO];
        [self performSelectorOnMainThread:@selector(updateCounter) withObject:nil waitUntilDone:NO];
    }
}

//handle player join events; note that for our pusposes we only care about these events when we are the acting host
- (void)playerJoinedWithId:(NSString*)playerId andDetails:(NSDictionary*)playerData {
    //handling joins from players
    if (match && [match amITheServer]) {
        //set up a status label for the player
        [self performSelectorOnMainThread:@selector(addLabelForPlayer:) withObject:playerId waitUntilDone:YES];
        
        if ([[match players] count] == NUM_PLAYERS - 2) {  //players will never contain our own id, and it will not yet contain the joining player id
            //start the match
            [matchmaker startMatch:match];
            [self.view performSelectorOnMainThread:@selector(setBackgroundColor:) withObject:[UIColor greenColor] waitUntilDone:NO];
            [self performSelectorInBackground:@selector(runPingLoopWhenReady) withObject:nil];
        }
    }
    
}

//handle player disconnect events; again we only care about these when acting as the host
- (void)playerLeft:(NSString*)playerId {
    //handling disconnects from players
    //[matchmaker cancelMatch:match];
    //[match release];
    NSLog(@"Player left...");
    if (match && [match amITheServer]) {
        [self performSelectorOnMainThread:@selector(removeLabelForPlayer:) withObject:playerId waitUntilDone:NO];
    }
}

- (NSDictionary*) localPlayerDetails {
    //information about the local player; optional
    return nil;
}
//end matchmaking delegate methods


- (void) setupMatch {
    matchmaker = [[SCMatchmaker alloc] initWithKey:MATCHMAKER_KEY andDelegate:self];                                                        //XXX:  uses server URL contained in Info plist
    //matchmaker = [[SCMatchmaker alloc] initWithKey:MATCHMAKER_KEY andServerAddress:@"http://127.0.0.1:8080/ap/" andDelegate:self];        //XXX:  explicit server URL
    match = [[matchmaker autoJoinMatchWithMaxPlayers:NUM_PLAYERS creatingIfNecessary:YES] retain];
    if (match) {
        self.view.backgroundColor = [UIColor blueColor];
    }
    else {
        self.view.backgroundColor = [UIColor blackColor];
    }
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Release any cached data, images, etc that aren't in use.
}

#pragma mark - View lifecycle

- (void)counterTapped {
    if (match && matchRunning) {
        @synchronized(countLabel) {
            counter++;
            [self performSelectorOnMainThread:@selector(updateCounter) withObject:nil waitUntilDone:YES];
        }
        [match broadcastMessage:[NSDictionary dictionaryWithObject:@"increment" forKey:@"message"]];
    }
}

- (void)viewDidLoad {
    match = nil;
    matchmaker = nil;
    counter = 0;
    matchRunning = NO;
    
    [super viewDidLoad];
    
	// Do any additional setup after loading the view, typically from a nib.
    countLabel = [[UILabel alloc] initWithFrame:CGRectMake(0.0, 0.0, self.view.frame.size.width, self.view.frame.size.height)];
    countLabel.textAlignment = UITextAlignmentCenter;
    countLabel.textColor = [UIColor whiteColor];
    countLabel.text = [NSString stringWithFormat:@"%d", counter];
    countLabel.backgroundColor = [UIColor clearColor];
    countLabel.adjustsFontSizeToFitWidth = YES;
    countLabel.font = [UIFont boldSystemFontOfSize:64.0];
    countLabel.hidden = YES;
    countLabel.userInteractionEnabled = YES;
    
    [self.view addSubview:countLabel];
    
    // Create gesture recognizer
    UITapGestureRecognizer *tapHandler = [[[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(counterTapped)] autorelease];
    
    // Set required taps and number of touches
    [tapHandler setNumberOfTapsRequired:1];
    [tapHandler setNumberOfTouchesRequired:1];
    
    // Add the gesture to the view
    [countLabel addGestureRecognizer:tapHandler];
}

- (void)viewDidUnload {
    [match release];
    [matchmaker release];
    [super viewDidUnload];
    
    [countLabel removeFromSuperview];
    [countLabel release];
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
}

- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
    self.view.backgroundColor = [UIColor redColor];
    
    [self performSelector:@selector(setupMatch) withObject:nil afterDelay:3.0];
}

- (void)viewWillDisappear:(BOOL)animated {
	[super viewWillDisappear:animated];
}

- (void)viewDidDisappear:(BOOL)animated {
	[super viewDidDisappear:animated];
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Return YES for supported orientations
    return (interfaceOrientation != UIInterfaceOrientationPortraitUpsideDown);
}

@end
