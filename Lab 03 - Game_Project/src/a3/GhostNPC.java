package a3;

import ray.rage.scene.Entity;
import ray.rage.scene.SceneNode;
import ray.rml.Vector3;

public class GhostNPC
{ 
	private int id;
	private Vector3 position;
	private SceneNode node;
	private Entity entity;
	
	public GhostNPC(int givenID, Vector3 givenPos) // constructor
	{ 
		id = givenID; 
		position = givenPos; 
	}
	
	public void setPosition(Vector3 position)
	{
		node.setLocalPosition(position);
	}
	
	public Vector3 getPosition(Vector3 position)
	{ 
		return node.getLocalPosition();
	}
	
	public int obtainID()
	{   return id;    }
}