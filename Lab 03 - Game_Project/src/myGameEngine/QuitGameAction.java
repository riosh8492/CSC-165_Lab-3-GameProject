package myGameEngine;

import ray.input.action.AbstractInputAction;
import ray.rage.game.*;
import a3.MyGame;
import net.java.games.input.Event;

//An AbstractInputAction that quits the game.
//It assumes availability of a method “shutdown” in the game
//(this is always true for classes that extend BaseGame).

// Action that has been modified to quit the game. 
public class QuitGameAction extends AbstractInputAction 
{
	private MyGame localGame;
	
	public QuitGameAction(MyGame givenGame)
	{
		localGame = givenGame;
	}

	@Override
	public void performAction(float arg0, Event arg1) 
	{
		System.out.println("shutdown requested");
		localGame.setState(Game.State.STOPPING);
	}

}
