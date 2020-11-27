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
	//private GameServerTCP thisTCPServer;
	private GameServerUDP thisUDPServer;
	private NPC_Controller npcCtrl;
	private long startTime, lastUpdateTime; 
	
	/*
	GameAIServerTCP tcpServer;
	public NetworkingServer(int id) // constructor
	{ startTime = System.nanoTime();
	lastUpdateTime = startTime;
	npcCtrl = new NPCcontroller();
	. . .
	// start networking TCP server (as before)
	. . .
	// start NPC control loop
	npcCtrl.setupNPCs();
	npcLoop();
 * */
	
	public NetworkingServer(int serverPort, String protocol)
	{ 
		System.out.println("Server Initializated ...");
		
		startTime = System.nanoTime();
		lastUpdateTime = startTime;
		npcCtrl = new NPC_Controller();
		
		try
		{ 
			System.out.println("Server Idle: Try Statement ...");
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
		{   e.printStackTrace();   } 
		
		// start NPC control loop
		npcCtrl.setupNPCs();
		thisUDPServer.initializeNPCRecord(npcCtrl);
		npcLoop(); 
	}
		
	// Loop that constantly updates all the created NPCs. 
	public void npcLoop() // NPC control loop
	{ 
		long frameStartTime;
		float elapMilSecs;
		
		while (true)
		{ 
			frameStartTime = System.nanoTime();
			elapMilSecs = (frameStartTime-lastUpdateTime)/(1000000.0f);
			
			if (elapMilSecs >= 50.0f)
			{ 
				lastUpdateTime = frameStartTime;
				npcCtrl.updateNPCs();
				thisUDPServer.sendNPCinfo();
			}
			Thread.yield(); // ???
		} 
	}
	// main

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
