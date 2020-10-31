package a3;

import java.util.UUID;

import ray.rage.scene.Entity;
import ray.rage.scene.SceneNode;
import ray.rml.Vector3;
import ray.rml.Vector3f;

public class GhostAvatar 
{
	private Entity entity;
	private SceneNode node;
	private UUID ID;
	private Vector3f localPos; 
	
	public GhostAvatar(UUID id, Vector3 position)
	{ 
		this.ID = id;
		this.localPos = (Vector3f) position; 
	}
	
	// ID Accessor for this Ghost Avatar
	public UUID obtainGhostID()
	{   return ID;   }
	
	// ID Setter for this Ghost Avatar
	public void setGhostID(UUID givenID)
	{   ID = givenID;   }
	
	// Node Accessor for this Ghost Avatar
	public SceneNode obtainGhostNode()
	{   return node;   }
	
	// Node Setter for this Ghost Avatar
	public void setGhostNode(SceneNode givenNode)
	{   node = givenNode;   }
	
	// Node Accessor for this Ghost Avatar
	public Entity obtainGhostEntity()
	{   return entity;   }
	
	// Node Setter for this Ghost Avatar
	public void setGhostEntity(Entity givenEntity)
	{   entity = givenEntity;   }
	
	// Position Getter for this Ghost Avatar
	public Vector3f obtainGhostPosition()
	{   return localPos;   }
	
	// Position Setter for this Ghost Avatar
	public void setGhostPosition(Vector3f givenPos)
	{   localPos = givenPos;   }
}
