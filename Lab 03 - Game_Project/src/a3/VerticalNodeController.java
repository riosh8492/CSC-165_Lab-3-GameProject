package a3;

import ray.rage.scene.Node;
import ray.rage.scene.controllers.AbstractController;
import ray.rml.Angle;
import ray.rml.Degreef;
import ray.rml.Vector3;
import ray.rml.Vector3f;

// Random use controller.
// Goal to make a planet revolve around another one. Moon example.
public class VerticalNodeController extends AbstractController
{
	boolean path = true; // false -> move down
	boolean path1 = true, path2 = true;
	boolean path3 = true; 

	@Override
	protected void updateImpl(float elapsedTimeMillis) 
	{
		Vector3f localPosition, newPos;
		String name;
		float temp = 0.0f;
		
		for (Node n : super.controlledNodesList)
		{	
			name = n.getName(); 
			if (name.contains("Planet"))
			{
				localPosition = (Vector3f) n.getLocalPosition(); // Get the Position.
				temp = localPosition.y(); 
				
				if (temp <= 0.0f)
				{ 
					path = (name.contains("earth")) ? true : path; 
					path1 = (name.contains("mars")) ? true : path; 
					path2 = (name.contains("venus")) ? true : path; 
					path3 = (name.contains("jupiter")) ? true : path; 
				}
				else if (temp >= 2.0f)
				{ 
					path = (name.contains("earth")) ? false : path; 
					path1 = (name.contains("mars")) ? false : path; 
					path2 = (name.contains("venus")) ? false : path; 
					path3 = (name.contains("jupiter")) ? false : path;
				}
			
				if (path || path1 || path2 || path3)
				{
					n.moveUp(0.01f);
				}
				else if (!path || !path1 || !path2 || !path3)
				{
					n.moveDown(0.01f);
				}
			}
		}

	}

}
