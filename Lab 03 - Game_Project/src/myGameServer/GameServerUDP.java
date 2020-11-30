package myGameServer;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.UUID;
import ray.networking.server.GameConnectionServer;
import ray.networking.server.IClientInfo;
import ray.rml.Vector3f;


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
	private NPC_Controller npcCtrl;
	private Vector3f gameBallPosition = null; 
	private boolean oneTime = true; 
	
	public GameServerUDP(int localPort) throws IOException
	{ 
		super(localPort, ProtocolType.UDP); 
		clientAddressList = new String[10][2]; // Ten possible Clients. 
		gameBallPosition = null;// (Vector3f) Vector3f.createFrom(0.0f, 0.0f, 0.0f); // Set dummy ball loc. Will be updated later. 
		
		System.out.println("Game Server Address: " + this.getLocalInetAddress().toString());
	}
	
	@Override
	public void processPacket(Object o, InetAddress senderIP, int sndPort)
	{
		String message = (String) o;
		String[] msgTokens = message.split(",");
		
		if(msgTokens.length > 0)
		{
			System.out.println("\nGame Server UDP - MSG: " + message);
			
			if (oneTime) // Tempt disable.
			{
				//npcCtrl.start(); // Starts NPC loop. 
				oneTime = false;
			}
			
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
						addClient(ci, clientID);           // Add Client
						sendJoinedMessage(clientID, true); // Send Response that connection true.
						if (gameBallPosition == null)
						{
							sendBallPositionRequest(clientID);      // If no record of ball location, send request for it. 
						}
													
						recordJoinedClient(clientID.toString(), senderIP.toString());  // Record newly joined Client.
					}
					else
					{   System.out.println("Client IP Address is already in Use.");   }
				}
				catch (IOException e)
				{   e.printStackTrace();   } 
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
				// Server now needs to relay the message to clients. 
				// msg format: move,forward/yaw/pitch,clientID,newX,newY,newZ.
				
				UUID updateID = UUID.fromString(msgTokens[2]);
				String msgCommand = msgTokens[1]; // forward/horizontal/pitch/yaw. 
				
				Vector3f updatePos = null;
				
				if (msgCommand.contains("forward") || msgCommand.contains("horizontal"))
				{
					updatePos = (Vector3f) Vector3f.createFrom(
								Float.parseFloat(msgTokens[3]),
								Float.parseFloat(msgTokens[4]),
								Float.parseFloat(msgTokens[5]));
					sendMoveMessages(updateID, msgTokens[1], updatePos);
				}
				else // Command is either Yaw/Pitch
				{
					sendMoveMessages(updateID, msgTokens[1], updatePos);
				}
				
			}
			// Ball Position handling.
			if (msgTokens[0].compareTo("detailsForBall") == 0)
			{
				Vector3f ballPos = (Vector3f) Vector3f.createFrom(
						 Float.parseFloat(msgTokens[1]),
						 Float.parseFloat(msgTokens[2]),
						 Float.parseFloat(msgTokens[3]));
				gameBallPosition = ballPos; // Record Ball Position instance. 
			}
			// Additional cases for receiving messages about NPCs, such as:
			if(msgTokens[0].compareTo("needNPC") == 0)
			{
				String clientID = msgTokens[1];
				Vector3f givenPos = (Vector3f) Vector3f.createFrom(
									 Float.parseFloat(msgTokens[2]),
									 Float.parseFloat(msgTokens[3]),
									 Float.parseFloat(msgTokens[4]));

				NPC newNPC = new NPC(givenPos);
				npcCtrl.addNPC(newNPC);
				
				// Call function to send NPC Creation msgs to clients. 
				sendMessageToAll_CreateNPC(newNPC);
				//System.out.println("NeedNPC block end.");
			}
			if(msgTokens[0].compareTo("collide") == 0)
			{
				// If needed. NPC collides with ball? 
			}
		}
	} // Function end. 
	
	// Meant to be called when first client is created. 
	private void sendBallPositionRequest(UUID clientID) 
	{
		String message;
		try
		{   if (gameBallPosition == null) // If no ball position on record, use first one. 
			{
				message = "requestBallPosition";//"detailsForBall"; 
				sendPacket(message, clientID);
			}
		}
		catch (IOException e) 
		{   e.printStackTrace();   }
	}

	// Sends Message to all Clients to create NPC at given location & client. 
	private void sendMessageToAll_CreateNPC(NPC newNPC) 
	{
		Vector3f givenPos = newNPC.getVectorPos();
		System.out.println("GameServer - sendMessageToAll_CreateNPC.");

		try
		{
			String msg = new String("c_npc," + newNPC.getID());
			msg += "," + givenPos.x();
			msg += "," + givenPos.y();
			msg += "," + givenPos.z();
			sendPacketToAll(msg);
		}
		catch (IOException e) 
		{   e.printStackTrace();   }
	}

	private boolean validateClient(InetAddress senderIP) 
	{
		// This goes through the client record and checks to see if IP address is not already there. 
		int i, recordLength = clientAddressList.length;
		
		for (i=0; i < recordLength; i++)
		{
			if (clientAddressList[i][1] == senderIP.toString())
			{   return false;   }
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
	public void sendMoveMessages(UUID clientID, String updateType, Vector3f updatePos)
	{ 
		String message;
		System.out.println("GameServer - sendMoveMessages.");
		// Format: move,forward,clientID,x,y,z;
		try
		{ 
			if (updateType.contains("forward") || updateType.contains("horizontal"))
			{
				message = new String("move," + updateType + "," + clientID.toString());
				message += "," + updatePos.x()+"," + updatePos.y() + "," + updatePos.z();
				forwardPacketToAll(message, clientID);
			}
			else // Its a pitch/yaw with no change in position. 
			{
				message = new String("move," + updateType + "," + clientID.toString());
				//message += "," + updatePos.x()+"," + updatePos.y() + "," + updatePos.z();
				forwardPacketToAll(message, clientID);
			}
		}
		catch (IOException e) 
		{   e.printStackTrace();   } 
	}
	
	// NPC Server Methods
	// Used by NPC Controller. Which has BT tree and AI etc. 
	public void sendNPCinfo() // informs clients of current/new NPC positions
	{ 
		String msg; 
		NPC npc;
		for (int i=0; i < npcCtrl.obtainNpcAmount(); i++)
		{ 
			npc = npcCtrl.getNPC(i); 
			if (npc != null)
			{
				try
				{ 
					msg = new String("m_npc," + Integer.toString(i));
					msg += "," + npc.getX();
					msg += "," + npc.getY();
					msg += "," + npc.getZ();
					sendPacketToAll(msg);
				}
				catch (IOException e)
				{
					System.out.println("ERROR in func: sendNPCinfo().");
				}
			}
		}
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

	// Function to give Game Server a reference to NPC Controller. 
	public void obtainNPCReference(NPC_Controller givenController) 
	{   
		npcCtrl = givenController;  
	}
	
	public Vector3f obtainServerBallPosition()
	{   
		if (gameBallPosition == null)
		{   return (Vector3f) Vector3f.createFrom(0.0f, 0.0f, 0.0f);   }
		else
		{   return gameBallPosition;   }
	}
}
