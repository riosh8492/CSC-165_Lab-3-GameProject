package myGameServer;

import java.io.IOException;

import ray.networking.IGameConnection.ProtocolType;

/** Class NetworkingServer
 *  Basically this class initializes the GameServer to begin to await for messages. 
 *  Run this class main first before running the client in order to begin.
 *  
 *  @author Hector_Rios. 
 * */

public class NetworkingServer 
{
	private GameServerUDP thisUDPServer;
	private NPC_Controller npcCtrl;
	private long startTime, lastUpdateTime; 
	
	public NetworkingServer(int serverPort, String protocol)
	{ 
		System.out.println("Server Initializated ...");
		
		startTime = System.nanoTime();
		lastUpdateTime = startTime;
		
		try
		{ 
			if(protocol.toUpperCase().compareTo("TCP") == 0)
			{ 
				// If we prefer to use TCP.
				//thisTCPServer = new GameServerTCP(serverPort);
			}
			else
			{   
				thisUDPServer = new GameServerUDP(serverPort); 
			} 
		}
		catch (IOException e)
		{   
			System.out.println("Error in creating Game Server. ");
			e.printStackTrace();   
		} 
		
		// start NPC control loop
		
		npcCtrl = new NPC_Controller(thisUDPServer); // npcCtrl.setupNPCs();
		thisUDPServer.obtainNPCReference(npcCtrl);
		npcCtrl.start(); 
	}

	public static void main(String[] args)
	{ 
		System.out.println("NetworkingServer main print.");
		if(args.length > 1)
		{ 
			NetworkingServer app = 
					new NetworkingServer(Integer.parseInt(args[0]), args[1]);
		} 
		else 
		{
			NetworkingServer app = 
					new NetworkingServer(6001, "UDP");
		}
	} // Main End.
}
