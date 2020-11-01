package myGameEngine;

import a3.MyGame;
import a3.ProtocolClient;
import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;
import ray.rage.scene.Camera;
import ray.rage.scene.SceneNode;
import ray.rml.Angle;
import ray.rml.Degreef;
import ray.rml.Matrix3;
import ray.rml.Matrix3f;
import ray.rml.Vector3;
import ray.rml.Vector3f;

public class MoveLeftRightAction extends AbstractInputAction 
{
	private SceneNode localDolphin; 
	private MyGame localGame;
	ProtocolClient protClient;

	public MoveLeftRightAction(SceneNode givenDolphinN, MyGame givenGame)
	{
		localDolphin = givenDolphinN;
		localGame = givenGame;
	}

	public MoveLeftRightAction(SceneNode givenDolphinN, MyGame givenGame, ProtocolClient p) {
		// TODO Auto-generated constructor stub
		localDolphin = givenDolphinN;
		localGame = givenGame;
		protClient = p;
	}

	// Handles event where controller is acting in node mode. 
	public void nodeModeAction(float time2, Event e)
	{
		String command = e.getComponent().getName();
		char charCommand = command.charAt(0);
		
		float stickValue = e.getValue();
		float time1 = localGame.obtainTime1(); 
		
		float deltaRate = time2 - time1; // Used to manage consistent elapsed time movement.
		float deltaPos = deltaRate * 0.5f;  // Movement change
		
		float item = 1.0f;
		
		if ((charCommand == 'A') || (charCommand == 'D')) // Move Left/Right via KB.
		{
			if (charCommand == 'A')
			{  localDolphin.moveLeft(-deltaPos);  }
			else 
			{  localDolphin.moveRight(-deltaPos);  }
		}
		else // If no command from KB then check stick values.
		{
			if (stickValue > 0.0f)
			{  localDolphin.moveRight(-deltaPos);  }
			else if (stickValue < 0.0f)
			{  localDolphin.moveLeft(-deltaPos); }
			else 
			{
				System.out.println("Left/Right -> Stick: N/A");
			}
		}
	}
	
	@Override
	public void performAction(float time, Event e) // This moves it along its own UVN axises
	{ 
		if (localDolphin != null)
		{
			nodeModeAction(time/1000.0f, e);
			protClient.sendMoveMessage("horizontal", localDolphin.getLocalPosition());
		}
		else
		{   System.out.println("MIA Dolphin ...");   }
	}
}
