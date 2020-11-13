package myGameEngine;

import ray.input.action.AbstractInputAction;
import ray.rage.scene.*;
import ray.rml.*;
import a3.MyGame;
import a3.ProtocolClient;
import net.java.games.input.Event;

public class MoveFrontBackAction extends AbstractInputAction
{
	private MyGame localGame = null;
	private SceneNode localDolphinNode = null;
	private float givenTime = 0.0f;
	private float [] givenPlaneLoc; 
	private ProtocolClient protClient;
	
	// Network Constructor
	public MoveFrontBackAction(SceneNode givenNode, MyGame givenGame, ProtocolClient p)
	{
		localDolphinNode = givenNode;
		localGame = givenGame; // Purpose: to obtain last elapsed time float via func in MyGame
		givenPlaneLoc = localGame.obtainPlaneLoc(); 
		protClient = p;
	}
	
	// Handles event where controller is acting in node mode. 
	public void nodeModeAction(float time, Event e)
	{
		String command = e.getComponent().getName();
		char charCommand = command.charAt(0);
		float stickValue = e.getValue();
		
		float deltaRate = time - givenTime; // Used to manage consistent elapsed time movement.
		float deltaPos = deltaRate * 0.5f;  // Movement change
	
		if ((charCommand == 'W') || (charCommand == 'S')) // determine if command came from keyboard.
		{
			if (charCommand == 'W')
			{  
				localDolphinNode.moveForward(deltaPos);
				if (isWithinBounds((Vector3f) localDolphinNode.getLocalPosition()) == false)
				{
					localDolphinNode.moveBackward(deltaPos);
				}
			
			}
			else
			{  	
				localDolphinNode.moveBackward(deltaPos);
				if (isWithinBounds((Vector3f) localDolphinNode.getLocalPosition()) == false)
				{
					localDolphinNode.moveForward(deltaPos);
				}
				
			}
		}
		else // If no command from keyboard found, it came from GP.
		{
			if (stickValue > 0.0f)
			{  
				localDolphinNode.moveBackward(deltaPos);  
				if (isWithinBounds((Vector3f) localDolphinNode.getLocalPosition()) == false)
				{
					localDolphinNode.moveForward(deltaPos);
				}
			}
			else if (stickValue < 0.0f)
			{  
				localDolphinNode.moveForward(deltaPos); 
				if (isWithinBounds((Vector3f) localDolphinNode.getLocalPosition()) == false)
				{
					localDolphinNode.moveBackward(deltaPos);
				}
			}
			else 
			{
				System.out.println("Stick: N/A");
			}
		}
		
	}
	
	public boolean isWithinBounds(Vector3f givenNewPos)
	{
		float getX = givenNewPos.x(), 
		      getY = givenNewPos.y(), 
		      getZ = givenNewPos.z();

		float boundX = 5.0f,
			  lowerBoundY = 0.45f, upperBoundY = 1.6f,
		      boundZ = 15.0f;
				
		if ((getY < lowerBoundY) || (getY > upperBoundY)) // First check if the position is out of Y bounds
		{
			return false;
		}
		else if ((getX < (-boundX)) || (getX > boundX)) // If not, check x Bounds
		{
			return false;
		}
		else if ((getZ < (-boundZ)) || (getZ > boundZ)) // Check z bounds finally. 
		{
			return false;
		}
		
		return true; // Checking all bounds; its new position is valid. 
	}
	
	@Override
	public void performAction(float time, Event e) // This moves it along its own UVN axises
	{ 
		
		if (localDolphinNode != null) // If there is a passed dolphin node, move dolphin front/back
		{
			givenTime = localGame.obtainTime1();
			nodeModeAction(time/1000.0f, e); 
			
			if (protClient != null)
			{
				protClient.sendMoveMessage("forward", localDolphinNode.getLocalPosition()); // Network
			}
		}
		
	}


}