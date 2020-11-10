package myGameEngine;

import a3.MyGame;
import a3.ProtocolClient;
import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;
import ray.rage.scene.Camera;
import ray.rage.scene.SceneNode;
import ray.rml.Angle;
import ray.rml.Degreef;
import ray.rml.Vector3;
import ray.rml.Vector3f;


public class MovePitchAction extends AbstractInputAction{
	
	SceneNode localDolphinN;
	MyGame localGame;
	ProtocolClient protClient;
	boolean messageDir = false; 
	
	public MovePitchAction(SceneNode givenDolphin, MyGame givenGame, ProtocolClient p) 
	{
		localDolphinN = givenDolphin;
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
		float deltaAng = deltaRate * 0.5f;  // Rotate movement change
		
		Angle myPosAngle = Degreef.createFrom(deltaAng), // Pos 10 degrees.
			  myNegAngle = Degreef.createFrom(-deltaAng); // Pos 10 degrees.
		
		System.out.println("Pitch Command: " + command);
		System.out.println("command char length: " + command.length());
		System.out.println("Char command[0]: " + charCommand);
		
		if ((charCommand == 'U') || (charCommand == 'D')) // Move forward
		{
			//System.out.println("Pitch Command: " + command);

			if (charCommand == 'U')
			{   localDolphinN.pitch(myPosAngle); messageDir = true;   }
			else
			{   localDolphinN.pitch(myNegAngle); messageDir = false;  }
		}
		else 
		{
			if (stickValue > 0.4f)
			{  localDolphinN.pitch(myPosAngle); messageDir = true;  }
			else if (stickValue < -0.4f)
			{  localDolphinN.pitch(myNegAngle); messageDir = false; }
			
		}
	}

	@Override
	public void performAction(float time2, Event e) 
	{
		String messageDetail = "pitch";
		String command = e.getComponent().getName();
		char charCommand = command.charAt(0); 

		float stickValue = e.getValue();
		float time1 = localGame.obtainTime1();
		
		float deltaRate = (time2/1000.0f) - time1; // Used to manage consistent elapsed time movement.
		float deltaAng = deltaRate + 1.0f;  // Movement change
		
		Angle angle = Degreef.createFrom(1.0f);
		Angle myPosAngle = Degreef.createFrom(deltaAng), // Pos 10 degrees.
			  myNegAngle = Degreef.createFrom(-deltaAng); // Pos 10 degrees.

		if ((charCommand == 'U') || (charCommand == 'D')) // Move forward
		{
			if (charCommand == 'U')
			{   angle = myPosAngle; messageDir = true;    }
			else
			{   angle = myNegAngle; messageDir = false;   }
		}
		else 
		{
			if (stickValue > 0.4f)
			{  angle = myPosAngle; messageDir = true;   }
			else if (stickValue < -0.4f)
			{  angle = myNegAngle; messageDir = false;   }
			localDolphinN.pitch(angle);
		}//*/
		
		if (localDolphinN != null) // If a game ref wasn't entered.
		{
			//this.nodeModeAction(time2/1000.0f, e);
			
			if (protClient != null)
			{
				// Send MSG
				messageDetail += (messageDir) ? "u" : "d"; // Means of converying yaw direction. 
				protClient.sendMoveMessage(messageDetail, localDolphinN.getLocalPosition()); // Network
			}
		}
		
	}


}