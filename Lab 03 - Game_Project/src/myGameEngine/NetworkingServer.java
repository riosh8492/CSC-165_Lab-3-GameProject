package myGameEngine;

import java.io.IOException;
import ray.networking.IGameConnection.ProtocolType;


public class NetworkingServer 
{
	private GameServerUDP thisUDPServer;
	//private GameServerTCP thisTCPServer;
	
	public NetworkingServer(int serverPort, String protocol)
	{ 
		try
		{ 
			if(protocol.toUpperCase().compareTo("TCP") == 0)
			{ 
				// If we prefer to use TCP.
				//thisTCPServer = new GameServerTCP(serverPort);
			}
			else
			{   thisUDPServer = new GameServerUDP(serverPort);   } 
		}
		catch (IOException e)
		{   e.printStackTrace();   } 
	} // Contructor End.
		
	public static void main(String[] args)
	{ 
		if(args.length > 1)
		{ 
			NetworkingServer app = 
					new NetworkingServer(Integer.parseInt(args[0]), args[1]);
		} 
	} // Main End.
}
