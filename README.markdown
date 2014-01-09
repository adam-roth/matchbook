### matchbook
###### A lightweight, platform-agnostic matchmaking service, API, and SDK
=========

### About

![For real.](https://raw.github.com/adam-roth/matchbook/master/running_small.jpg)

Matchbook is a lightweight and platform-agnostic matchmaking solution, intended for use in mobile applications (think games that require near-real-time, relatively-low-latency, persistent communications between two or more client devices).  

At its core is a server component, which provides a JSON-based webservice allowing clients to find, create, and join matches.   The server also acts as a proxy/relay when necessary, allowing client devices to tunnel through any firewalls that might exist between them.  

In addition to the server component, matchbook includes prebuilt SDK's for both Java and Objective-C.   These SDK's are intended to support the development of native applications that make use of the matchbook webservice on Android and iOS devices, respectively.   

Alongside the SDK's are example Android and iOS applications that demonstrate how to use the SDK's and the backing webservice API to create a simple cross-platform (or more accurately, multi-platform) multiplayer game.  

In short, matchbook provides all the basic matchmaking/communications fundamentals for creating multiplayer games that span any number of platforms.  

And to be clear, when I say "span any number of platforms" I don't just mean you can have the same app on multiple platforms.  I mean you can have an iPhone user playing head-to-head against an Android user.  Or a 4-way match between an iPhone user, an Android user, and two Blackberry users (if you can find two Blackberry users).  Or any other combination.

Matchbook.  It's platform-agnostic.  For real.


### Getting Started - Server

First, make sure you have the prerequisites installed for building and running the server:

* Java 6+
* Maven 2+
* MySQL 5+ (or any other database that JDBC supports)

Next, grab a copy of the code under '/match-server' and edit the 'pom.xml' file in the project root directory to configure your server options.   At a minimum, you'll want to check the 'systemProperties' section, and the values of the following entries (note that if you have your own standalone Servlet container than you intend to deploy to, you can skip this step):

```xml
<au.com.suncoastpc.db.name>match_devel</au.com.suncoastpc.db.name>
<au.com.suncoastpc.server.hostname>localhost</au.com.suncoastpc.server.hostname>
<au.com.suncoastpc.server.port>8080</au.com.suncoastpc.server.port>
```

The first option specifies the **persistence unit**, as defined in 'persistence.xml' that the server should use.  More on that later.  

The second option specifies the hostname that will be used for the server.  For development purposes, 'localhost' is fine.  Though obviously for an actual deployment you'll want to use your server's public hostname here.

And the third option specifies the port the server should run on.  8080 is the default value, though of course you can specify any port you wish.  

Also check the 'properties' section that immediately follows the 'systemProperties' section, and ensure that the value of 'cargo.servlet.port' matches whatever value you have put for 'au.com.suncoastpc.server.port'.

The system properties defined in the default 'pom.xml' file are not the only options supported by matchbook.  Here is a complete overview:

<table>
<tr><th> Property Name </th><th> Description </th><th> Default Value </th></tr>
<tr><td> au.com.suncoastpc.db.name </td><td> The name of the persistence-unit that the server should use to access the database; persistence units are defined in 'persistence.xml'. </td><td> match_devel </td></tr>
<tr><td> au.com.suncoastpc.server.hostname </td><td> The hostname for this server; this is used when generating links in e-mails, etc. </td><td> localhost </td></tr>
<tr><td> au.com.suncoastpc.server.port </td><td> The port for this server; this is used when generating links in e-mails, and does not affect the port that the server actually binds to. </td><td> 8080 </td></tr>
<tr><td> au.com.suncoastpc.server.protocol </td><td> The protocol the server uses (HTTP, HTTPS, etc.); this is used when generating links in e-mails. </td><td> http </td></tr>
<tr><td> au.com.suncoastpc.server.email.local </td><td> A flag which indicates whether or not 'localhost' is a valid destination for outgoing SMTP messages (i.e. whether or not something like Postfix is running locally). </td><td> false </td></tr>
<tr><td> au.com.suncoastpc.server.email.admin </td><td> The e-mail address to use when sending outbound system e-mails. </td><td> admin@suncoastpc.com.au </td></tr>
<tr><td> au.com.suncoastpc.server.email.admin.name </td><td> The display name to use when sending outbound system e-mails. </td><td> Matchbook Admin </td></tr>
<tr><td> au.com.suncoastpc.profiling.enabled </td><td> A flag indicating whether or not performance information should be logged to the console. </td><td> true </td></tr>
<tr><td> au.com.suncoastpc.server.http.timeout </td><td> The HTTP session timeout to use, in minutes. </td><td> 120 </td></tr>
</table>

Note that if you are using your own standalone servlet container, you will need to ensure that you set the desired values for the above system properties as part of your servlet container's startup scripts.

Once you have your system properties set up, you should confirm your database connection settings.   To do this, open the 'src/webapp/WEB-INF/classes/META-INF/persistence.xml' file.  This is where the persistence units are defined.  There are two persistence units defined by default; 'match_prod' and 'match_devel'.  

In any case, whether you use one of the predefined persistence units or add your own, you'll want to ensure that the dialect, driver class, and connection URL are all correct for your setup (the default settings assume a local MySQL database).  You'll also want to update the database username and password as appropriate for your system.  

Once you're happy with the settings in persistence XML, all you need to do is ensure a blank database exists with the specified name (so for MySQL, using the default database name, you simple need to run `CREATE DATABASE matchmaking;`).  

Now you're ready to fire up the server!  To do this simply run the provided 'startServer.sh' script (or 'startServer.bat' if you are on Windows).  This will build the webapp, and deploy it to an embedded Tomcat instance using Cargo.  

Alternately, if you are using a standalone servlet container, you can run 'mvn clean install' to build the webapp and then manually deploy the WAR file that is created under '/target' to your servlet container of choice.  

Visit the running server in a web browser (by pointing your browser at http(s)://\<hostname\>:\<port\>; so http://localhost:8080 when using the default configuration options).  Because this is the first visit to matchbook, you will be prompted to provide an admin password.  Do so (and _remember_ what password you pick, since you can't recover it if you lose it), and you will be logged in to the server.  

Now all that's left to do is add a game so that you can get an API key to use in your mobile app(s).  To do this, enter your application's identifier/bundle-id (i.e. something like 'com.\<your company\>.SuperAwesomeGame') and press the 'Add Game' button.  

Your button-clicking skills will be rewarded with a private-key, which you can copy/paste into your native app(s), as discussed in the next section.    


### Getting Started - iOS

The simplest way to get started is to grab the iOS example application, located under '/Examples/Objective-C/MatchmakingExample'.  This project includes everything you need to familiarize yourself with the Matchbook iOS SDK, including a prebuilt version of the SDK framework and a simple "game" that demonstrates the basics.

The Matchbook SDK requires two pieces of configuration information from you.  The first is the URL of the API server to use (if you do not provide one, the SDK will default to 'http://localhost:8080/ap/').   So you'll need to find or set up a running Matchbook server instance.

Once you have that, there are a couple of different ways to tell the Matchbook SDK what server to use.  The first is to simply put the URL into your application's 'Info.plist' file, under the 'au.com.suncoastpc.matchbook.server' key.  This should be a String value, which contains the full server URL and context-path (so a leading 'http(s)://', and a trailing '/ap/' should be included).

The second method is to simply pass the desired server URL as a parameter when you create your 'SCMatchmaker' instance.  More on that later.

The other piece of information required by the Matchbook SDK is the private/API key to use for your application.  You can get this by registering your application id/bundle-identifier on the Matchbook server that you plan on using.  You should register your app's bundle-id exactly as it appears in Xcode/iTunes Connect/etc..  When you do so the Matchbook server will give you an API key, which you can pass to the 'SCMatchmaker' when you instantiate it.  

Once you have those things, creating/joining a match is as simple as setting up a fresh 'SCMatchmaker' instance and asking it to join the next available match:

```objc
- (void) setupMatch {
    matchmaker = [[SCMatchmaker alloc] initWithKey:MATCHMAKER_KEY andDelegate:self];    //XXX:  uses server URL contained in Info plist
    //matchmaker = [[SCMatchmaker alloc] initWithKey:MATCHMAKER_KEY andServerAddress:@"http://127.0.0.1:8080/ap/" andDelegate:self];    //XXX:  explicit server URL
    match = [[matchmaker autoJoinMatchWithMaxPlayers:NUM_PLAYERS creatingIfNecessary:YES] retain];
    
    if (match) {
        //congrats, you've got a match!
    }
    else {
        //oops, couldn't join the match; probably your server is down or you used the wrong API key, check the log for details
    }
}
```

Note that this example shows both methods for specifying the server URL.  The first call will use the URL contained in your app's 'Info.plist'.  The second will use the server URL that is passed into the constructor.  

Also note that the 'SCMatchmaker' expects to receive an object that conforms to the 'SCMatchmakerDelegate' protocol.  This protocol includes the following methods:

```objc
- (BOOL)shouldAcceptJoinFromPlayerWithId:(NSString*)playerId andDetails:(NSDictionary*)playerData;
- (void)receivedData: (NSDictionary*)data fromPlayerWithId:(NSString*)playerId;
- (void)playerJoinedWithId:(NSString*)playerId andDetails:(NSDictionary*)playerData;
- (void)playerLeft:(NSString*)playerId;
- (NSDictionary*) localPlayerDetails;
```

The most important of these delegate methods is 'receivedData:fromPlayerWithId:'.  This method is invoked whenever a packet is received from another player in the match.  When this happens, you'll most likely want to inspect the contents of the packet, take some actions, and possibly respond by sending a fresh packet from the local device.  For instance:

```objc
//handles the various messages that we can recieve from our "game"
- (void)receivedData: (NSDictionary*)data fromPlayerWithId:(NSString*)playerId {
    //handling messages from players
    if ([[data objectForKey:@"message"] isEqual:@"PING!"]) {
        //ooh look, somebody pinged us
        [self performSelectorOnMainThread:@selector(respondToPing) withObject:nil waitUntilDone:YES];
    }
    else if ([[data objectForKey:@"message"] isEqual:@"increment"]) {
        //increment our shared game counter and update the UI
        @synchronized(countLabel) {
            counter++;
            [self performSelectorOnMainThread:@selector(updateCounter) withObject:nil waitUntilDone:YES];
        }
    }
    else {
        //the player's background color must have updated (only other message that we expect)
        NSMutableDictionary* params = [NSMutableDictionary dictionary];
        [params setObject:[data objectForKey:@"color"] forKey:@"color"];
        [params setObject:playerId forKey:@"player"];
        [self performSelectorOnMainThread:@selector(updateLabel:) withObject:params waitUntilDone:NO];
    }
}
```

Obviously that's a fairly contrived example, but you get the idea.  What goes into the packets that get sent around and how your app responds to them is entirely up to you.  The packets themselves are sent between players by way of the 'SCMatch' instance that the 'SCMatchmaker' returned when you called 'autoJoinMatchWithMaxPlayers:creatingIfNecessary'.  The main API methods of interest here are:

```objc
- (void) broadcastMessage:(NSDictionary*)message;
- (void) sendMessage:(NSDictionary*)message toPlayer:(NSString*)playerId;
```

The difference between these two methods should be fairly self-explanatory.  The former broadcasts a message to all players in the game.  The latter sends a message directly to a specific player, and does not notify anyone else who is in the match.  

That covers the basics, though to get a better feel for the complete Matchbook SDK you should read through the other methods contained in the example project's 'ViewController.m' class.  And also through the SDK code/headers as well.


### Getting Started - Android

The simplest way to get started is to grab the Android example application, located under '/Examples/Java/MatchmakingExample'.  This project includes everything you need to familiarize yourself with the Matchbook Android SDK, including a prebuilt version of the SDK library and a simple "game" that demonstrates the basics.

The Matchbook SDK requires two pieces of configuration information from you.  The first is the URL of the API server to use (if you do not provide one, the SDK will default to 'http://localhost:8080/ap/').   So you'll need to find or set up a running Matchbook server instance.

Once you have that, there are a couple of different ways to tell the Matchbook SDK what server to use.  The first is to simply expose the URL as a system property, keyed under the 'au.com.suncoastpc.matchbook.server'.  This should be a String value, which contains the full server URL and context-path (so a leading 'http(s)://', and a trailing '/ap/' should be included).

The second method is to simply pass the desired server URL as a parameter when you create your 'Matchmaker' instance.  More on that later.

The other piece of information required by the Matchbook SDK is the private/API key to use for your application.  You can get this by registering your application id on the Matchbook server that you plan on using.  With Android you have a bit more flexibility, in that the SDK allows you to pass in any arbitrary application id you like and does not automatically determine the id from the runtime environment.  Note that if you have both an iOS and an Android app and you want them to be able to talk to each other, you should register your iOS application's bundle-id, and use that same identifier (and API key) for your Android app.

In any event, when you register your application identifier the Matchbook server will give you an API key, which you can pass to the 'Matchmaker' when you instantiate it. 

Once you have those things, creating/joining a match is as simple as setting up a fresh 'Matchmaker' instance and asking it to join the next available match:

```java
TelephonyManager manager = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
matchmaker = new AndroidMatchmaker(manager, APP_ID, API_KEY, this);  //XXX:  will use the server-address defined in AndroidMatchmaker.java, or the system-property value, if set
//matchmaker = new AndroidMatchmaker(manager, APP_ID, API_KEY, "http://localhost:8080/ap/" this);  //XXX:  will use the provided server URL

match = matchmaker.autoJoinMatch(NUM_PLAYERS, true);
if (match != null) {
    //congrats, you've got a match!
}
else {
    //oops, couldn't join the match; probably your server is down or you used the wrong API key, check the log for details
}
```

Note that the Android example project provides an Android-specific Matchmaker subclass (appropriately named 'AndroidMatchmaker'), which is what we use above.  Passing in the 'TelephonyManager' instance is optional, but recommended. 

Also note that the 'Matchmaker' expects to receive an object that implements the 'MatchmakerClient' interface.  This interface includes the following methods:

```java
   /**
	 * Asks the client whether or not the given player should be permitted to join the game.
	 * 
	 * @param uuid the unique-identifier of the connecting player.
	 * @param playerData any additional data that the connecting player sent as part of their connection attempt.
	 * @return true if the player should be allowed to connect
	 *         false if the player should be rejected
	 */
	public boolean shouldAcceptJoinFromPlayer(String uuid, JSONObject playerData);
	
	/**
	 * Informs the client that the given player has joined the game.  
	 * 
	 * @param uuid the unique-identifier of the player that joined.
	 * @param playerData any additional data that the connecting player sent as part of their connection attempt.
	 */
	public void playerJoined(String uuid, JSONObject playerData);
	
	/**
	 * Informs the client that the given player has left the game.
	 * 
	 * @param uuid the unique-identifier of the player that joined.
	 */
	public void playerLeft(String uuid);
	
	/**
	 * Notifies the client that data was received from the given player.
	 * 
	 * @param uuid the player that sent the data. 
	 * @param data the data that was sent.
	 */
	public void dataReceivedFromPlayer(String uuid, JSONObject data);
	
	/**
	 * Implement this method to return a custom JSONObject containing extended information about the local player.  
	 * The information returned from this method will be exchanged between clients whenever a new connection is made, 
	 * and will be sent to the shouldAcceptJoinFromPlayer() and playerJoined() callbacks when new players join the 
	 * match (if the current player is acting as the host).
	 * 
	 * If you do not wish to pass around any custom/extended information about the local player, then simply return 
	 * null from this method.
	 * 
	 * @return
	 */
	public JSONObject getPlayerDetails();
```

The most important of these methods is 'dataReceivedFromPlayer(String uuid, JSONObject data)'.  This method is invoked whenever a packet is received from another player in the match.  When this happens, you'll most likely want to inspect the contents of the packet, take some actions, and possibly respond by sending a fresh packet from the local device.  For instance:

```java
    @Override
    //handles the various messages that we can recieve from our "game"
	public void dataReceivedFromPlayer(String playerId, JSONObject data) {
		//handling messages from players
		if ("PING!".equals(data.get("message"))) {
		    //ooh look, somebody pinged us
			this.respondToPing(playerId);
		}
		else if ("increment".equals(data.get("message"))) {
		    //increment our shared game counter and update the UI
			synchronized(countView) {
				counter++;
				this.updateCounter();
			}
		}
		else {
		    //the player's background color must have updated (only other message that we expect)
			Long color = (Long)data.get("color");
			this.updateLabelForPlayer(playerId, color);
		}
	}
```

Obviously that's a fairly contrived example, but you get the idea.  What goes into the packets that get sent around and how your app responds to them is entirely up to you.  The packets themselves are sent between players by way of the 'Match' instance that the 'Matchmaker' returned when you called 'autoJoinMatch()'.  The main API methods of interest here are:

```java
    public synchronized void broadcastMessage(JSONObject message);
	public synchronized void sendMessageToPlayer(JSONObject message, String playerUuid);
```

The difference between these two methods should be fairly self-explanatory.  The former broadcasts a message to all players in the game.  The latter sends a message directly to a specific player, and does not notify anyone else who is in the match.  

That covers the basics, though to get a better feel for the complete Matchbook SDK you should read through the other methods contained in the example project's 'MatchmakingExampleActivity' class.  And also through the SDK code/javadoc as well.

### FAQ

**_Why should I use matchbook?_**<br />
Matchbook provides a starting point for creating multiplayer games (or any other type of application that benefits from connecting two or more arbitrary or non-arbitrary clients together).  It solves typical problems, such as tunneling through firewalls, and provides a communications layer and native iOS and Android SDK's.  This frees you from having to worry about these things, saves you time, and lets you focus on building the core of your application.

**_Why shouldn't I use matchbook?_**<br />
Matchbook is not:

* A social network or user database.
* An achievements platform.
* A sophisticated skills-based matchmaker.

Do not use it in place of any of those things.  Note, however, that it's entirely possible and appropriate to use matchbook _alongside_ those things.  For instance, nothing says you can't use matchbook to connect and play your game, and OpenFeint to track achievements.

**_Why is there a Java SDK, but not an Android SDK?_**<br />
Because the difference between the Java SDK and an Android SDK would be literally one class file.  It's not worthwhile setting up an completely seprate project for the sake of housing a single class.

Or to put it another way, if you take the Java SDK and add the 'AndroidMatchmaker' class from the Android example project, what you get _is_ the Android SDK.

**_Why are there two versions of the iOS SDK?_**<br />
Because while the SDK compiles into a universal iOS framework that can be dropped into any iOS project and used just like any other framework, some developers will invariably prefer just dropping in the source code instead.  And since you can't drop non-ARC source code into an ARC project (and vice-versa), it's necessary to have an ARC and a non-ARC version of the iOS SDK to properly support this use-case.

**_Why is the server interface so ugly?_**<br />
It's not ugly, it's _completely unstyled_.  The reason being that the only purpose the server-side UI really serves is the allocation of API keys.  That's literally the only important thing it does.  So giving it some pretty styling is very low priority compared to other tasks.

That said, if you'd like to create a decent looking set of initial styles and submit a pull request, feel free.

**_What does the example game do?_**<br />
A couple of things:

1. The "game" tracks and displays a shared counter across all connected devices.  Tapping the screen on any device (once the required number of players have joined) increments this counter on all devices.  The count will remain consistent on all connected devices, no matter how many people are incrementing it concurrently.

2. Every 5 seconds, the background color for each _non-server_ player will change randomly, with the new color being sent to the _server_ player.  The _server_ player's screen will list the id's of the connected players, and update the text color of each player to match their current screen color.

Note that the screen colors displayed as the app starts up provide an indication of its status.  Specifically:

* **Red** - the app has not tried to join the match yet, give it a few seconds.
* **Blue** - the app has connected to the match, and is waiting for more players to join (the default number of players the game waits for is set to 4; you can adjust this as desired).
* **Black** - something bad has happened and the app could not (and will not) join the match; you should check your log for details.
* **Green** - your match is running; you should see a list of connected player ids and the shared counter displayed on your device (_server_ player only).
* **Any other color** - your match is running; you should see the shared counter displayed on your device (tap anywhere on the screen to increment it).

The "server player" is the _first_ player to join the match.  They are the host.  If the server player disconnects, so does everyone else.

### License

I'm of the opinion that when someone takes something valuable, like source code, and knowingly and willingly puts it somewhere where literally anyone in the world can view it and grab a copy for themselves, as I have done, they are giving their implicit consent for those people to do so and to use the code however they see fit.  I think the concept of "copyleft" is, quite frankly, borderline insane.  

Information wants to be free without reservation, and good things happen when we allow it to be.  But not everyone agrees with that philosophy, and larger organizations like seeing an "official" license, so I digress.

For the sake of simplicity, you may consider all matchbook code to be licensed under the terms of the MIT license.  Or if you prefer, the Apache license.  Or CC BY.  Or any other permissive open-source license (the operative word there being "permissive").  Take your pick.  Basically use this code if you like, otherwise don't.  Though if you use matchbook to build something cool that changes the world, please remember to give credit where credit is due.  And also please tell me about it, so that I can see too.  


### Todo

- [x] Import code
- [x] About
- [x] Screenshot(s)/Photo(s)
- [x] Server overview/setup
- [x] iOS overview/setup
- [x] Android overview/setup
- [ ] Protocol discussion
- [ ] FAQ
- - Comms protocol (RLE json; link to wiki)

