package myGameEngine;

import java.util.UUID;

import ray.rage.scene.Entity;
import ray.rage.scene.SceneNode;
import ray.rml.Vector3;

public class GhostAvatar 
{
	private SceneNode node;
	private Entity entity;
	private UUID ID;
	
	public GhostAvatar(UUID id, Vector3 position)
	{ 
		this.ID = id;
	}
	// accessors and setters for id, node, entity, and position
	
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
}
