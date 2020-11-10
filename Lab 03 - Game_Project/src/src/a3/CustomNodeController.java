package a3;

import ray.rage.scene.Node;
import ray.rage.scene.controllers.AbstractController;
import ray.rml.Angle;
import ray.rml.Degreef;
import ray.rml.Vector3;
import ray.rml.Vector3f;

// Random use controller.
// Goal to make a planet revolve around another one. Moon example.
public class CustomNodeController extends AbstractController
{
	@Override
	protected void updateImpl(float elapsedTimeMillis) 
	{
		Vector3f coreForwardAxis;
		Angle deltaAngle;
		
		for (Node n : super.controlledNodesList)
		{	
			if (n.getName().contains("PrismGroup"))
			{
				// Got core prism node to rotate. 
				coreForwardAxis = (Vector3f) n.getLocalForwardAxis();
				
				if (n.getName().contains("PrismGroup3"))
				{
					deltaAngle = Degreef.createFrom(-0.5f);
				}
				else
				{
					deltaAngle = Degreef.createFrom(0.5f);
				}

				n.rotate(deltaAngle, coreForwardAxis); // Rotates the core prism.
			}
		}

	}

}
