package myGameEngine;

import a3.MyGame;
import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;
import ray.rage.scene.SceneNode;
import ray.rml.Angle;
import ray.rml.Degreef;
import ray.rml.Matrix3;
import ray.rml.Matrix3f;
import ray.rml.Vector3;
import ray.rml.Vector3f;

/** MoveYawAction meant to handle the changes the occur to the 
 *  Camera and Dolphin Node during mode switches for the controller.
 *  
 * */

public class MoveYawAction extends AbstractInputAction
{	
	SceneNode localDolphinNode;
	MyGame localGame;

	public MoveYawAction(SceneNode givenDolphinN, MyGame givenGame)
	{
		//Angle angle = Degreef.createFrom(23.0f);
				
		localDolphinNode = givenDolphinN;
		//localDolphinNode.yaw(angle); // Check if I can change dolphin?
		localGame = givenGame;
	}
	
	private void nodeModeAction(float time2, Event e) 
	{		
		String command = e.getComponent().getName();
		char charCommand = command.charAt(0); 

		float stickValue = e.getValue();
		float time1 = localGame.obtainTime1();
		
		float deltaRate = time2 - time1; // Used to manage consistent elapsed time movement.
		float deltaAng = deltaRate + 1.0f;  // Movement change
		
		if ((charCommand == 'L') || (charCommand == 'R'))
		{
			if (charCommand == 'L') // Left blank due to possible mouse controller.
			{  
				globalNodeYaw(true, deltaAng);  
			}
			else
			{  
				globalNodeYaw(false, deltaAng);  
			}
		}
		else
		{
			if (stickValue > 0.4f)
			{  
				globalNodeYaw(true, deltaAng);  
			}
			else if (stickValue < -0.4)
			{  
				globalNodeYaw(false, deltaAng);  
			}
		}
	}
	
	public void globalNodeYaw(boolean direction, float deltaAmount)
	{
		Angle myPosAngle = Degreef.createFrom(deltaAmount),  // Pos 10 degrees.
		      myNegAngle = Degreef.createFrom(-deltaAmount); // Pos 10 degrees.
		
		Vector3 globalY = Vector3f.createFrom(0.0f, 1.0f, 0.0f); // Get world origin axis. 
		Matrix3 matRot; 
	
		//System.out.println("globalNodeYaw Info: ");
		//System.out.println("direction: " + direction + " | deltaAmount: " + deltaAmount);
		
		if (direction)
		{
			matRot = Matrix3f.createRotationFrom(Degreef.createFrom(myPosAngle), globalY);
			localDolphinNode.setLocalRotation(matRot.mult(localDolphinNode.getWorldRotation()));
		}
		else 
		{
			matRot = Matrix3f.createRotationFrom(Degreef.createFrom(myNegAngle), globalY);
			localDolphinNode.setLocalRotation(matRot.mult(localDolphinNode.getWorldRotation()));
		}
	}
	
	@Override
	public void performAction(float time, Event e) 
	{
		
		if (localDolphinNode != null) // If there is a passed dolphin node, move dolphin front/back
		{
			nodeModeAction(time/1000.0f, e); 
		}
		
	}

}
