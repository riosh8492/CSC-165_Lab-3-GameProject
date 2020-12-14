package myGameServer;

import ray.ai.behaviortrees.BTCompositeType;
import ray.ai.behaviortrees.BTSequence;
import ray.ai.behaviortrees.BehaviorTree;
import ray.rml.Vector3f;
import java.lang.Thread;

public class NPC_Controller extends Thread
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
		System.out.println("Start () ENDING"); 
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
			previousTime = currentTime; // Get Average elapsed time. 

			if (elapsedTickMilliSecs >= 50.0f) // “TICK” -> Times= for action.
			{ 
				System.out.println("-- TICK");
				lastTickUpdateTime = currentTime;
				//updateNPCs();   // npc.updateLocation();
				localServer.sendNPCinfo(); // Tell Server to send NPC info to all clients for update.
			}
			if (elapsedThinkMilliSecs >= 500.0f) // “THINK” -> Time for thinking of action choice.
			{ 
				System.out.println("-- THINK");
				lastThinkUpdateTime = currentTime;
				bt.update(elapsedMilliSecs);
			}
			//Thread.yield();
			NPC_Controller.yield();
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
	public void updateNPCs() {}

	public void setupNPCs() 
	{
		//System.out.println("NPC controller -> Set Up NPCs");
	}

	// Returns NPC at index given.
	public NPC getNPC(int i) 
	{   return NPClist[i];   }
	
	public int obtainNpcAmount()
	{   return amountNPCs;   }

	// Go through all NPCs, move them and send out npc update to clients. 
	public void followTargetAction(Vector3f ballPos) 
	{
		int i;
		float delta = 0.2f, moveDelta = 0.0f; 
		NPC npc;
		
		for (i = 0; i < amountNPCs; i++)
		{
			npc = NPClist[i];
			if (npc != null)
			{
				// Update position if not equal x axis position.
				if (npc.getX() != ballPos.x())
				{
					moveDelta = (npc.getX() > ballPos.x()) ? -delta : delta;
					moveDelta = (ballPos.x() == 0.0f) ? 0.0f : moveDelta; 
					
					npc.updateLocation(npc.getX() + moveDelta, npc.getY(), npc.getZ());
				}
			}
		}
	 	
	}
}
