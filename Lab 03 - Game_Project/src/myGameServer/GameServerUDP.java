package myGameServer;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.UUID;
import ray.networking.server.GameConnectionServer;
import ray.networking.server.IClientInfo;


/** Class GameServerUDP extends GameConnectionServer<UUID>
 *  Basically this class carries out the Server type management of type UDP.
 *  This is where one can adjust how the server sends messages and reacts to 
 *  messages that are sent to it. 
 *  
 *  @author Hector_Rios. 
 * */

public class GameServerUDP extends GameConnectionServer<UUID>
{
	private String[][] clientAddressList; 
	
	public GameServerUDP(int localPort) throws IOException
	{ 
		super(localPort, ProtocolType.UDP); 
		clientAddressList = new String[10][2]; // Ten possible Clients. 
	}
	
	@Override
	public void processPacket(Object o, InetAddress senderIP, int sndPort)
	{
		String message = (String) o;
		String[] msgTokens = message.split(",");
		
		if(msgTokens.length > 0)
		{
			System.out.println("Game Server UDP - MSG: " + message);
			
			// case where server receives a JOIN messagec
			if(msgTokens[0].compareTo("join") == 0)
			{   
				try
				{ 
					IClientInfo ci;	
					ci = getServerSocket().createClientInfo(senderIP, sndPort);
					UUID clientID = UUID.fromString(msgTokens[1]);
					
					if (validateClient(senderIP))
					{
						addClient(ci, clientID);
						sendJoinedMessage(clientID, true);
						
						recordJoinedClient(clientID.toString(), senderIP.toString()); 
					}
					else
					{
						System.out.println("Client IP Address is already in Use.");
					}
				}
				catch (IOException e)
				{ 
					e.printStackTrace();
				} 
			}	
			
			// case where server receives a CREATE message
			if(msgTokens[0].compareTo("create") == 0)
			{  
				UUID clientID = UUID.fromString(msgTokens[1]);
			
				String[] pos = {msgTokens[2], msgTokens[3], msgTokens[4]};
				sendCreateMessages(clientID, pos);
				sendWantsDetailsMessages(clientID);
			}
			
			// case where server receives a BYE message
			if(msgTokens[0].compareTo("bye") == 0)
			{ 
				UUID clientID = UUID.fromString(msgTokens[1]);
				sendByeMessages(clientID);
				removeClient(clientID);
			}
			// case where server receives a DETAILS-FOR message
			if(msgTokens[0].compareTo("detailsFor") == 0)
			{ 
				//Look up sender name / Send DETAILSFOR(sender,pos,orient) to <addr,port>
				System.out.println("Details For message to be sent to client area.: "+ message);
				
				UUID targetID = UUID.fromString(msgTokens[1]);
				UUID remoteID = UUID.fromString(msgTokens[2]);
				
				String[] pos = {msgTokens[3], msgTokens[4], msgTokens[5]}; 
				
				sndDetailsMsg(targetID, remoteID, pos);
			}
			// case where server receives a MOVE message
			if(msgTokens[0].compareTo("move") == 0)
			{ 
				// etc.
			}
		}
	} // Function end. 
	
	private boolean validateClient(InetAddress senderIP) 
	{
		// This goes through the client record and checks to see if IP address is not already there. 
		int i, recordLength = clientAddressList.length;
		
		for (i=0; i < recordLength; i++)
		{
			if (clientAddressList[i][1] == senderIP.toString())
			{
				return false;
			}
		}
		return true;
	}

	// Record given client information in records.
	private void recordJoinedClient(String ID, String IP_Address) 
	{
		int i;
		int recordLength = clientAddressList.length;
		
		for (i=0; i < recordLength; i++)
		{
			if (clientAddressList[i][0] == null)
			{
				clientAddressList[i][0] = ID;
				clientAddressList[i][1] = IP_Address;
				//System.out.println("Client UUID & IP_Address Saved ... Max 10 ppl...");
				//System.out.println(recordLength - i + " Spots Left.");
				break; 
			}
		}
		
	}

	// Sending confirmation to client that they have JOINED. 
	public void sendJoinedMessage(UUID clientID, boolean success)
	{   // format: join, success or join, failure
		try
		{ 
			String message = new String("join,");
		
			if (success) 
			{  message += "success";  }
			else 
			{  message += "failure";  }
			
			sendPacket(message, clientID);
		}
		catch (IOException e) 
		{   e.printStackTrace();   }
	}
	
	// This sends a CREATE msg to all other clients to create a ghost of new joined player. 
	public void sendCreateMessages(UUID clientID, String[] position)
	{   // format: create, remoteId, x, y, z
		System.out.println("GameServer - sendCreateMessages.");
		
		try
		{ 
			String message = new String("create," + clientID.toString());
			
			message += "," + position[0];
			message += "," + position[1];
			message += "," + position[2];
			forwardPacketToAll(message, clientID);
		}
		catch (IOException e) 
		{   e.printStackTrace();   } 
	}
	
	// Getting a details for message and then sending message info to target client. 
	public void sndDetailsMsg(UUID targetID, UUID remoteID, String[] position)
	{ 
		String clientID = targetID.toString(); 
		String senderID = remoteID.toString(); 
			
		System.out.println("GameServer - sendDetailsMsg to client: " + clientID + ". From senderID: " + senderID);

		try
		{ 
			String message = new String("detailsFor," + senderID.toString());
			message += "," + position[0];
			message += "," + position[1];
			message += "," + position[2];
			
			sendPacket(message, targetID);
		}
		catch (IOException e) 
		{   e.printStackTrace();   } 
	}
	
	// Send a WantDetails msg to all clients to send their own details to new client for ghost avatar pos. 
	public void sendWantsDetailsMessages(UUID clientID)
	{ 
		System.out.println("GameServer - sendAllWantsDetailMessages.");

		try
		{ 
			String message = new String("wantDetails," + clientID.toString());
			forwardPacketToAll(message, clientID);
		}
		catch (IOException e) 
		{   e.printStackTrace();   } 

	}
	public void sendMoveMessages(UUID clientID, String[] position)
	{ 
		// etc….. 
	}
	
	// Relay given Client BYE message to other clients for ghost termination. 
	public void sendByeMessages(UUID clientID)
	{ 
		try
		{ 
			String message = new String("bye," + clientID.toString());
			forwardPacketToAll(message, clientID);
		}
		catch (IOException e) 
		{   e.printStackTrace();   }
	}
}
