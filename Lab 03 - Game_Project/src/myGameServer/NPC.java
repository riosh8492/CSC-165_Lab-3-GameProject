package myGameServer;

import ray.rml.Vector3f;

/** Class NPC. Meant to hold all information regarding NPC in All
 *  client's world. Server will be main place for NPC control/Management.*/

public class NPC 
{
	private float locX, locY, locZ; // other state info goes here (FSM)
	private Vector3f position; 
	private int indexID; 
	
	public NPC(float x, float y, float z)
	{
		locX = x;
		locY = y; 
		locZ = z;
		position = (Vector3f) Vector3f.createFrom(locX, locY, locZ);
		indexID = 0;
	}
	
	public NPC(Vector3f givenPos) 
	{
		locX = givenPos.x();
		locY = givenPos.y();
		locZ = givenPos.z();
		position = givenPos; 
		indexID = 0;
	}

	public Vector3f getVectorPos() { return position; }
	public float getX() { return locX; }
	public float getY() { return locY; }
	public float getZ() { return locZ; }

	// Updates this NPC's position. 
	public void updateLocation(float f, float g, float h) 
	{
		locX = f; locY = g; locZ = h;
	}
	
	public void setID(int givenID)
	{   indexID = givenID;   }
	
	public int getID()
	{   return indexID;      }

}
