package edu.ucsc.eis.spaceinvaders;

import java.awt.event.KeyEvent;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderError;
import org.drools.builder.KnowledgeBuilderErrors;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.ResourceFactory;
import org.drools.logger.KnowledgeRuntimeLogger;
import org.drools.logger.KnowledgeRuntimeLoggerFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.StatelessKnowledgeSession;

import org.newdawn.spaceinvaders.Entity;
import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.GameWindowCallback;
import org.newdawn.spaceinvaders.SystemTimer;

/**
 * An extension of the New Dawn Space Invaders Game class, that overrides
 * methods when necessary, in order to inject knowledge to the rules
 * engine.
 * 
 * Extending has a nice property of allowing this object to be used in
 * all the other classes that the Space Invaders game comes with, but
 * did require a hacking of the original Game class to remove the constructor,
 * as Java forces that constructor to be called <i>first</i>, for whatever
 * reason. I wanted to call it last, but no dice, so that one had to take
 * a hack.
 * 
 * Ahh, research.
 * 
 * @author cflewis
 *
 */
public class SpaceInvadersRules extends Game implements GameWindowCallback {
	private static final long serialVersionUID = 1L;
	KnowledgeRuntimeLogger knowledgeLogger = null;
	StatelessKnowledgeSession ksession = null;
	
	public SpaceInvadersRules() {
		super(org.newdawn.spaceinvaders.ResourceFactory.JAVA2D);
		
		try {
			// load up the knowledge base
			KnowledgeBase kbase = readKnowledgeBase();
			ksession = kbase.newStatelessKnowledgeSession();
			//knowledgeLogger = KnowledgeRuntimeLoggerFactory.newFileLogger(ksession, "test");
		} catch (Throwable t) {
			t.printStackTrace();
			System.exit(1);
		}
		
		//ksession.insert(this);
		ksession.execute(this);
		//ksession.fireAllRules();
		
		org.newdawn.spaceinvaders.ResourceFactory.get().setRenderingType(org.newdawn.spaceinvaders.ResourceFactory.JAVA2D);
		window = org.newdawn.spaceinvaders.ResourceFactory.get().getGameWindow();
		
		windowTitle = "Space Invaders Enterprise Edition";
		
		window.setResolution(800,600);
		window.setGameWindowCallback(this);
		window.setTitle(windowTitle);
		
		window.startRendering();
	}
	
	@Override
	protected void finalize() throws Throwable {
		try {
			//knowledgeLogger.close();
		}
		finally {
			super.finalize();
		}
	}
	
	@Override
	protected void initEntities() {
		super.initEntities();
	}
	
	/**
	 * Notification that a frame is being rendered. Responsible for
	 * running game logic and rendering the scene.
	 */
	@Override
	public void frameRendering() {
		SystemTimer.sleep(lastLoopTime+10-SystemTimer.getTime());
		
		// work out how long its been since the last update, this
		// will be used to calculate how far the entities should
		// move this loop
		long delta = SystemTimer.getTime() - lastLoopTime;
		ksession.setGlobal("delta", delta);
		lastLoopTime = SystemTimer.getTime();
		lastFpsTime += delta;
		fps++;
				
		// update our FPS counter if a second has passed
		if (lastFpsTime >= 1000) {
			window.setTitle(windowTitle+" (FPS: "+fps+")");
			lastFpsTime = 0;
			fps = 0;
		}
		
		//ksession.fireAllRules();
		if (!waitingForKeyPress) { ksession.execute(entities); }
		
		// cycle round asking each entity to move itself
//		if (!waitingForKeyPress) {
//			for (int i=0;i<entities.size();i++) {
//				Entity entity = (Entity) entities.get(i);
//				
//				entity.move(delta);
//			}
//		}
		
		// cycle round drawing all the entities we have in the game
//		for (int i=0;i<entities.size();i++) {
//			Entity entity = (Entity) entities.get(i);
//			
//			entity.draw();
//		}
				
		// brute force collisions, compare every entity against
		// every other entity. If any of them collide notify 
		// both entities that the collision has occured
//		for (int p=0;p<entities.size();p++) {
//			for (int s=p+1;s<entities.size();s++) {
//				Entity me = (Entity) entities.get(p);
//				Entity him = (Entity) entities.get(s);
//				
//				if (me.collidesWith(him)) {
//					me.collidedWith(him);
//					him.collidedWith(me);
//				}
//			}
//		}
				
		// remove any entity that has been marked for clear up
		entities.removeAll(removeList);
		removeList.clear();

		// if a game event has indicated that game logic should
		// be resolved, cycle round every entity requesting that
		// their personal logic should be considered.
		if (logicRequiredThisLoop) {
			for (int i=0;i<entities.size();i++) {
				Entity entity = (Entity) entities.get(i);
				entity.doLogic();
			}
			
			logicRequiredThisLoop = false;
		}
		
		// if we're waiting for an "any key" press then draw the 
		// current message 
		if (waitingForKeyPress) {
			message.draw(325,250);
		}
		
		// resolve the movemfent of the ship. First assume the ship 
		// isn't moving. If either cursor key is pressed then
		// update the movement appropraitely
		ship.setHorizontalMovement(0);
		
		boolean leftPressed = window.isKeyPressed(KeyEvent.VK_LEFT);
		boolean rightPressed = window.isKeyPressed(KeyEvent.VK_RIGHT);
		boolean firePressed = window.isKeyPressed(KeyEvent.VK_SPACE);
		
		if (!waitingForKeyPress) {
			if ((leftPressed) && (!rightPressed)) {
				ship.setHorizontalMovement(-moveSpeed);
			} else if ((rightPressed) && (!leftPressed)) {
				ship.setHorizontalMovement(moveSpeed);
			}
			
			// if we're pressing fire, attempt to fire
			if (firePressed) {
				tryToFire();
			}
		} else {
			if (!firePressed) {
				fireHasBeenReleased = true;
			}
			if ((firePressed) && (fireHasBeenReleased)) {
				waitingForKeyPress = false;
				fireHasBeenReleased = false;
				startGame();
			}
		}
		
		// if escape has been pressed, stop the game
		if (window.isKeyPressed(KeyEvent.VK_ESCAPE)) {
			System.exit(0);
		}
	}

	private static KnowledgeBase readKnowledgeBase() throws Exception {
		KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
		kbuilder.add(ResourceFactory.newClassPathResource("SpaceInvaders.drl"), ResourceType.DRL);
		KnowledgeBuilderErrors errors = kbuilder.getErrors();
		
		if (errors.size() > 0) {
			for (KnowledgeBuilderError error: errors) {
				System.err.println(error);
			}
			throw new IllegalArgumentException("Could not parse knowledge.");
		}
		
		KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
		kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
		return kbase;
	}
	
	public long getLastLoopTime() {
		return lastLoopTime;
	}
	
	public void setLastLoopTime(long lastLoopTime) {
		this.lastLoopTime = lastLoopTime;
	}
	
	public static void main(String argv[]) {
		new SpaceInvadersRules();
	}
}