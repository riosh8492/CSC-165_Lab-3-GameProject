package myGameServer;

import ray.rml.Vector3f;

/** Class NPC. Meant to hold all information regarding NPC in All
 *  client's world. Server will be main place for NPC control/Management.*/

public class NPC 
{
	float locX, locY, locZ; // other state info goes here (FSM)
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
	public double getX() { return locX; }
	public double getY() { return locY; }
	public double getZ() { return locZ; }

	public void updateLocation() 
	{
		// Return an update? 
	}
	
	public void setID(int givenID)
	{   indexID = givenID;   }
	
	public int getID()
	{   return indexID;      }

}
