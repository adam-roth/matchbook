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

Please see the [Server Steup](https://github.com/adam-roth/matchbook/wiki/Server-Setup) instructions on the wiki.    


### Getting Started - iOS

Please see the [iOS Walkthrough](https://github.com/adam-roth/matchbook/wiki/iOS-Walkthrough) for information on using the matchbook SDK in an iOS application.


### Getting Started - Android

Please see the [Android Walkthrough](https://github.com/adam-roth/matchbook/wiki/Android-Walkthrough) for information on using the matchbook SDK in an Android application.


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
Because the difference between the Java SDK and an Android SDK would be literally one class file.  It's not worthwhile setting up a completely seprate project for the sake of housing a single class.

Or to put it another way, if you take the Java SDK and add the 'AndroidMatchmaker' class from the Android example project, what you get _is_ the Android SDK.

**_Why are there two versions of the iOS SDK?_**<br />
Because while the SDK compiles into a universal iOS framework that can be dropped into any iOS project and used just like any other framework, some developers will invariably prefer just dropping in the source code instead.  And since you can't drop non-ARC source code into an ARC project (and vice-versa), it's necessary to have an ARC and a non-ARC version of the iOS SDK to properly support this use-case.

**_What communications protocol is used behind the scenes?_**<br />
Behind the scenes clients communicate using a very simple "run-length encoded JSON" protocol.  Basially they pass JSON-formatted "packets" to each other, with each packet preceded by a 32-bit integer that specifies the length of the incoming JSON data.  It's not strictly necessary to send the size of the JSON data like that (streaming JSON parsers, etc., etc.), but doing so makes a number of things both easier, and more secure.

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

**_Why is the server interface so ugly?_**<br />
It's not ugly, it's _completely unstyled_.  The reason being that the only purpose the server-side UI really serves is the allocation of API keys.  That's literally the only important thing it does.  So giving it some pretty styling is very low priority compared to other tasks.

That said, if you'd like to create a decent looking set of initial styles and submit a pull request, feel free.



### License

I'm of the opinion that when someone takes something valuable, like source code, and knowingly and willingly puts it somewhere where literally anyone in the world can view it and grab a copy for themselves, as I have done, they are giving their implicit consent for those people to do so and to use the code however they see fit.  I think the concept of "copyleft" is, quite frankly, borderline insane.  

Information wants to be free without reservation, and good things happen when we allow it to be.  But not everyone agrees with that philosophy, and larger organizations like seeing an "official" license, so I digress.

For the sake of simplicity, you may consider all matchbook code to be licensed under the terms of the MIT license.  Or if you prefer, the Apache license.  Or CC BY.  Or any other permissive open-source license (the operative word there being "permissive").  Take your pick.  Basically use this code if you like, otherwise don't.  Though if you use matchbook to build something cool that changes the world, please remember to give credit where credit is due.  And also please tell me about it, so that I can see too.  



