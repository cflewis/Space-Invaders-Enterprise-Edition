Space Invaders Enterprise Edition
=================================
Space Invaders Enterprise Edition is a version of [Coke and Code](http://www.cokeandcode.com/spaceinvaderstutorial)'s Space Invaders implementation, but all the game logic is represented as a [Drools](http://www.jboss.org/drools) rule file, rather than written in Java.

This code is released under a [BSD license](http://creativecommons.org/licenses/BSD/) (as was the original code from Kevin! Open-source is awesome!), so you can do with it as you wish, I just take no responsibility for your crimes :)

You can build this by using Maven:
	mvn compile 
	
and run it using Maven too:
	mvn exec:java

Packaging is a little hard right now due to the timer.jar library. I haven't worked out how to get Maven to bundle that up with the assembly plugin, so `mvn package` isn't going to produce a JAR with dependencies that will actually execute.