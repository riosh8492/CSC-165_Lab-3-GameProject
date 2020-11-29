package myGameServer;

import ray.ai.behaviortrees.BTBehavior;
import ray.ai.behaviortrees.BTCondition;
import ray.rml.Vector3f;

public class TargetMoved extends BTCondition
{
	private boolean givenNegate;
	private GameServerUDP localServer; 
	private NPC_Controller npc_ctrl; 
	
	public TargetMoved(GameServerUDP givenServer, NPC_Controller npc_Controller, boolean toNegate)
	{
		super(toNegate);
		givenNegate = toNegate;
		localServer = givenServer; 
		npc_ctrl = npc_Controller; 
	}
	
	@Override
	protected boolean check() // Called when BTree is 'updated'
	{
		// Get Ball Position 
		NPC npc; 
		Vector3f targetRef = localServer.obtainServerBallPosition();
		Vector3f npcPos;
		int i;
		
		for (i=0; i<npc_ctrl.obtainNpcAmount(); i++)
		{
			npc = npc_ctrl.getNPC(i);
			if (npc != null)
			{
				npcPos = npc.getVectorPos();
				if (targetRef.x() != npcPos.x())
				{     
					return true;     
				}
			}
		}
		
		return false;
	}

}
