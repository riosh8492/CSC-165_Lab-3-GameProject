package myGameServer;

/** Class NPC. Meant to hold all information regarding NPC in both
 *  client's world. Server will be main place for NPC control/Management.*/

public class NPC 
{
	float locX, locY, locZ; // other state info goes here (FSM)
	
	public NPC(float x, float y, float z)
	{
		locX = x;
		locY = y; 
		locZ = z;
	}
	
	public double getX() { return locX; }
	public double getY() { return locY; }
	public double getZ() { return locZ; }

	public void updateLocation() 
	{
		// Return an update? 
	}

}
