package myGameServer;

import ray.ai.behaviortrees.BTAction;
import ray.ai.behaviortrees.BTStatus;

public class FollowGameTarget extends BTAction 
{
	private GameServerUDP localServer;
	private NPC_Controller npc_ctrl; 
	
	public FollowGameTarget(GameServerUDP givenServer, NPC_Controller npc_Controller)
	{
		localServer = givenServer;
		npc_ctrl = npc_Controller;
	}
	
	@Override
	protected BTStatus update(float timeRate) 
	{
		// Basically when this is called, it moves the NPC toward the ball bit by bit.
		// CALL ON NPC Controller to update all npcs then call game server to send
		// NPC move updates. 
		return BTStatus.BH_SUCCESS;
	}
}
