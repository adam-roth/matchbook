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

### Getting Started - iOS

### Getting Started - Android

### FAQ

### License

I'm of the opinion that when someone takes something valuable, like source code, and knowingly and willingly puts it somewhere where literally anyone in the world can view it and grab a copy for themselves, as I have done, they are giving their implicit consent for those people to do so and to use the code however they see fit.  I think the concept of "copyleft" is, quite frankly, borderline insane.  Information wants to be free without reservation, and good things happen when we allow it to be.  But not everyone agrees with that philosophy, and larger organizations like seeing an "official" license, so I digress.

For the sake of simplicity, you may consider all matchbook code to be licensed under the terms of the MIT license.  Or if you prefer, the Apache license.  Or CC BY.  Or any other permissive open-source license (the operative word there being "permissive").  Take your pick.  Basically use this code if you like, otherwise don't.  Though if you use matchbook to build something cool that changes the world, please remember to give credit where credit is due.  And also please tell me about it, so that I can see too.  


### Todo

- [x] Import code
- [x] About
- [x] Screenshot(s)/Photo(s)
- [ ] Server overview/setup
- [ ] iOS overview/setup
- [ ] Android overview/setup
- [ ] Protocol discussion
- [ ] FAQ
- - What matchbook is not (social network, achievements platform, skills-based matchmaker, etc.).
- - Android SDK (just use the Java SDK, plus the AndroidMatchmaker class from the example project)
- - Lack of server UI styles
- - Comms protocol (gzipped json; link to wiki)

