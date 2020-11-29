package myGameServer;

import ray.ai.behaviortrees.BTCompositeType;
import ray.ai.behaviortrees.BTSequence;
import ray.ai.behaviortrees.BehaviorTree;

public class NPC_Controller 
{
	private int numNPCs = 5; 
	private int amountNPCs = 0; 
	private NPC[] NPClist = new NPC[numNPCs]; // Total Amount of NPCS Allotted. 
	private long thinkStartTime, tickStartTime; 
	private long lastThinkUpdateTime, lastTickUpdateTime;
	
	private GameServerUDP localServer;
	private BehaviorTree bt = new BehaviorTree(BTCompositeType.SELECTOR);
	
	public NPC_Controller(GameServerUDP givenServer)
	{
		int i;
		localServer = givenServer; 
		
		for (i = 0; i < NPClist.length; i++) // Empty out storage. 
		{   NPClist[i] = null;   }
		
		start(); // Begins NPC BehaviorTree loop operations. 
	}
	
	public void start()
	{
		thinkStartTime = System.nanoTime();
		tickStartTime = System.nanoTime();
		lastThinkUpdateTime = thinkStartTime;
		lastTickUpdateTime = tickStartTime;
		// setupNPC(); -> client sends request. 
		setupBehaviorTree();
		npcLoop();
	}
	
	private void npcLoop() 
	{
		long currentTime, previousTime = (long) 0;
		float elapsedThinkMilliSecs;
		float elapsedTickMilliSecs;
		float elapsedMilliSecs; 
		
		while (true)
		{ 
			currentTime = System.nanoTime();
			elapsedThinkMilliSecs = (currentTime-lastThinkUpdateTime)/(1000000.0f);
			elapsedTickMilliSecs = (currentTime-lastTickUpdateTime)/(1000000.0f);
			elapsedMilliSecs = (currentTime-previousTime);
			
			if (elapsedTickMilliSecs >= 50.0f) // “TICK” -> Times= for action.
			{ 
				lastTickUpdateTime = currentTime;
				updateNPCs();   // npc.updateLocation();
				localServer.sendNPCinfo(); // Tell Server to send NPC info.
			}
			if (elapsedThinkMilliSecs >= 500.0f) // “THINK” -> Time for thinking of action choice.
			{ 
				lastThinkUpdateTime = currentTime;
				bt.update(elapsedMilliSecs);
			}
			previousTime = currentTime; // Get Average elapsed time. 
			Thread.yield();
		} 
	}

	private void setupBehaviorTree() 
	{
		bt.insertAtRoot(new BTSequence(10));
		//bt.insertAtRoot(new BTSequence(20));
		
		bt.insert(10, new TargetMoved(localServer, this, false)); // this,npc,false
		bt.insert(10, new FollowGameTarget(localServer, this));     // npc
		// bt.insert(20, new AvatarNear());   // AvatarNear(localServer,this,npc,false)
		// bt.insert(20, new GetBig());       // npc
	}

	// Adds given NPC to the end of the List. 
	public void addNPC(NPC givenNPC)
	{
		int i;
		for (i = 0; i < numNPCs; i++)
		{
			if (NPClist[i] == null)
			{
				givenNPC.setID(i); // Record index location. 
				NPClist[i] = givenNPC;
				amountNPCs += 1;
				break; 
			}
		}
	}
	
	// This basically updates their positions. I guess. 
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
	{   return amountNPCs;   }
}
