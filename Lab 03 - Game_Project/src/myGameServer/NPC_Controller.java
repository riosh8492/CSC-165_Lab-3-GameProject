package myGameServer;

public class NPC_Controller 
{
	private int numNPCs = 5; 
	private int currentNPCs = 0; 
	private NPC[] NPClist = new NPC[numNPCs]; // Total Amount of NPCS Allotted. 
	
	public NPC_Controller()
	{
		
	}
	
	// Adds given NPC to the end of the List. 
	public void addNPC(NPC givenNPC)
	{
		int i;
		for (i = 0; i < numNPCs; i++)
		{
			if (NPClist[i] == null)
			{
				NPClist[i] = givenNPC;
				break; 
			}
		}
	}
	
	public void updateNPCs()
	{ 
		for (int i=0; i<numNPCs; i++)
		{ 
				NPClist[i].updateLocation();
		} 
	}

	public void setupNPCs() 
	{
		System.out.println("NPC controller -> Set Up NPCs");
		
	}

	// Returns NPC at index given.
	public NPC getNPC(int i) 
	{   return NPClist[i];   }
	
	public int obtainNpcAmount()
	{   return NPClist.length;   }
}
