package a3;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.UUID;
import java.util.Vector;

import ray.networking.client.GameConnectionClient;
import ray.rml.Vector3;
import ray.rml.Vector3f;

/** Class ProtocolClient extends GameConnectionClient
 *  Basically this class gives the GameWorld the ability to send info to 
 *  the GameServer. In the Game class, it constantly calls this class's method
 *  ProcessPackets which manages all the packets sent from client-to-server and vice versa.  
 *  This class also handles Ghost Avatar management.
 *  
 *  @author Hector_Rios. 
 * */

public class ProtocolClient extends GameConnectionClient
{
	private MyGame game;
	private UUID id;
	private ArrayList<GhostAvatar> ghostAvatars;
	private GhostAvatar ghostHolder; 
	private int gAvatarID;
	private Vector3f gAvatarPosition; 
	
	private ArrayList<GhostNPC> ghostNPCs = new ArrayList<GhostNPC>(10); // NPC/AI Variables. 
	
	public ProtocolClient(InetAddress remAddr, int remPort, ProtocolType pType, MyGame givenGame) throws IOException
	{
		super(remAddr, remPort, pType);
		this.game = givenGame;
		this.id = UUID.randomUUID();
		this.ghostAvatars = new ArrayList<GhostAvatar>();
		System.out.println("Client ID: " + id);
	}
	
	// Messages Received from the Server.
	@Override
	protected void processPacket(Object msg)
	{ 
		String strMessage = (String) msg;
		String[] msgTokens = strMessage.split(",");
		
		System.out.println("ProtocolClient - message received: " + strMessage);
		System.out.println("ProtocolClient - GhostAvatars: " + ghostAvatars);
		
		if(msgTokens.length > 0)
		{	
			if(msgTokens[0].compareTo("join") == 0) // receive �join�
			{ // format: join, success or join, failure
				if(msgTokens[1].compareTo("success") == 0)
				{ 
					game.setIsConnected(true);
					game.updateClientPosition(Integer.parseInt(msgTokens[2]));
					sendCreateMessage(game.getPlayerPosition());
					sendCreateMessage_NPC(game.getGhostPosition_NPC()); 
				}
				if(msgTokens[1].compareTo("failure") == 0)
				{   game.setIsConnected(false);   }
			}
			
			// Informs this client to destroy given ghost ID.
			if(msgTokens[0].compareTo("bye") == 0) // receive �bye�
			{ // format: bye, remoteId
				UUID ghostID = UUID.fromString(msgTokens[1]);
				removeGhostAvatar(ghostID);
			}
						
			if(msgTokens[0].compareTo("create") == 0) // Create ghost or avatar. 
			{ 
				// If given our ID back with a msg of creation.
				// Then we do that. else it is a ghost avatar creation of another player.
				if (msgTokens[1] == id.toString())
				{
					System.out.println("Create Self avatar here?");
				}
				else
				{
					// When sent a create, commits ghost creation.
					UUID ghostID = UUID.fromString(msgTokens[1]);
					Vector3f ghostPosition = (Vector3f) Vector3f.createFrom(
						Float.parseFloat(msgTokens[2]),
						Float.parseFloat(msgTokens[3]),
						Float.parseFloat(msgTokens[4]));
				
					createGhostAvatar(ghostID, ghostPosition);
				}
			}
			// Informs client that a remote client wants a local status update
			if(msgTokens[0].compareTo("wantDetails") == 0) // rec. �wants��
			{ 
				// Server sent request to send own info to newly made client for ghost creation.
				if (msgTokens[1] != id.toString())
				{
					// Send a details for message based on ID. If ID is same, then ignore, else send.
					sendDetailsForMessage(msgTokens[1], game.getPlayerPosition());
				}
			}
			
			// Provides client with updated status of a remote avatar
			// Update Ghost avatar for sender
			if (msgTokens[0].compareTo("detailsFor") == 0 )// receive �dsfr�)
			{ 
				// Getting details to either create ghost or update current ghost 
				UUID ghostID = UUID.fromString(msgTokens[1]);
				Vector3f pos = (Vector3f) Vector3f.createFrom(
						Float.parseFloat(msgTokens[2]),
						Float.parseFloat(msgTokens[3]),
						Float.parseFloat(msgTokens[4]));

				updateGhostAvatar(ghostID, pos);
				System.out.println("Client - details for requested -> call sendDetailsForMessage");
			}
			// Request for Ball location. 
			if(msgTokens[0].compareTo("requestBallPosition") == 0)
			{
				sendBallPositionToServer(); 
			}

			// Got a move message. This means that one of the ghost avatars needs to be updated. 
			if(msgTokens[0].compareTo("move") == 0) // rec. �move...�
			{ 
				// * * * Format: move,forward,clientID,x,y,z * * *
				UUID ghostID = UUID.fromString(msgTokens[2]); // Construct Ghost ID.
				String command = msgTokens[1];				  // Get specific string move command. 
				Vector3f pos = null;						  // Set position to null if rotation. 
				
				// Give: updateMove, ID, new position. 
				if (command.contains("yaw") || command.contains("pitch"))
				{
					moveUpdateGhostAvatar(command, ghostID.toString(), pos); // Pass no position if its a rotation
				}
				else // Its a positional move
				{
					pos = (Vector3f) Vector3f.createFrom(
							Float.parseFloat(msgTokens[3]),
							Float.parseFloat(msgTokens[4]),
							Float.parseFloat(msgTokens[5]));
					moveUpdateGhostAvatar(msgTokens[1], ghostID.toString(), pos);
				}
			}
			// GHOST NPC MSGS
			// handle updates to NPC positions
			// format: (msg-command,npcID,x,y,z)
			if(msgTokens[0].compareTo("c_npc") == 0)
			{ 
				int npcGhostID = Integer.parseInt(msgTokens[1]);
				Vector3 ghostPosition = Vector3f.createFrom(
						Float.parseFloat(msgTokens[2]),
						Float.parseFloat(msgTokens[3]),
						Float.parseFloat(msgTokens[4]));
				createGhostNPC(npcGhostID, ghostPosition);
			}
			// Meant for Updating GhostNPC movement. 
			if(msgTokens[0].compareTo("m_npc") == 0)
			{ 
				int npcGhostID = Integer.parseInt(msgTokens[1]);
				Vector3 updateGhostPos = Vector3f.createFrom(
						Float.parseFloat(msgTokens[2]),
						Float.parseFloat(msgTokens[3]),
						Float.parseFloat(msgTokens[4]));
				updateGhostNPC(npcGhostID, updateGhostPos);
			}
		}
	}

	// Client Responses to the Messages from the Server. 
	public void sendJoinMessage() // format: join, localId
	{ 
		try
		{   sendPacket(new String("join," + id.toString()));   }
		catch (IOException e) 
		{   e.printStackTrace();   } 
	} 
	
	public void sendCreateMessage(Vector3f pos)
	{ // format: (create, localId, x,y,z)
		try
		{ 
			String message = new String("create," + id.toString());
			message += "," + pos.x()+"," + pos.y() + "," + pos.z();
			sendPacket(message);
		}
		catch (IOException e) 
		{   e.printStackTrace();   } 
	}
	
	// Client sends NPC creation request
	private void sendCreateMessage_NPC(Vector3f pos) 
	{
		try
		{ 
			String message = new String("needNPC," + id.toString());
			message += "," + pos.x()+"," + pos.y() + "," + pos.z();
			sendPacket(message);
		}
		catch (IOException e) 
		{   e.printStackTrace();   } 
		
	}
	
	public void sendByeMessage()
	{ 
		System.out.println("Sending BYE msg to server.");
		try
		{ 
			String message = new String("bye," + id.toString());
			sendPacket(message);
		}
		catch (IOException e) 
		{   e.printStackTrace();   } 
	}
	
	// Returns a details-for request to the server for another client.
	public void sendDetailsForMessage(String targetID, Vector3f pos) // Pos = Vector3D
	{ 
		System.out.println("Client. Received WANTS-DEATILS Msg. Sending DETAILS-FOR Msg.");
		try
		{ 
			String message = new String("detailsFor," + targetID.toString());
			message += "," + id.toString();
			message += "," + pos.x()+"," + pos.y() + "," + pos.z();
			sendPacket(message);
		}
		catch (IOException e) 
		{   e.printStackTrace();   }
	}
	
	// info to sent out: which client needs update, update pos, type of movement. 
	public void sendMoveMessage(String moveType, Vector3 updatePos) // Pos = Vector3D
	{ 
		System.out.println("Client. Sending Update Move Message to Server. moveType: " + moveType);
		try
		{ 
			String message = new String("move," + moveType); // move,forward
			message += "," + id.toString();
			message += "," + updatePos.x()+"," + updatePos.y() + "," + updatePos.z();
			sendPacket(message);
		}
		catch (IOException e) 
		{   e.printStackTrace();   }
	}
	
	private void createGhostAvatar(UUID ghostID, Vector3f ghostPos) 
	{
		System.out.println("Client. CREATE Msg/Command received. Now calling -> CreateGhostAvatar function");
		// Basically record ghost creation and then call game to create ghost based on given Entity & Node.
		
		if (isGhostCreationValid(ghostID) == false) // if false: theres no ghost existing w that ID; create a ghost.
		{
			ghostHolder = new GhostAvatar(ghostID, ghostPos); // Create ghost avatar instance. 
			ghostAvatars.add(ghostHolder);					  // Record ghost avatar. 
			
			try 
			{   game.addGhostAvatarToGameWorld(ghostHolder);   } 
			catch (IOException e) 
			{   e.printStackTrace();   }
		}		
	}
	
	
	// Moving / Rotations. 
	// Meant to update the movements of client avatars for other clients. 
	public void moveUpdateGhostAvatar(String updateMove, String ghostID, Vector3f updatePos)
	{
		// Update ghost instance that client holds, then call game world to update position. 
		UUID ghostUUID = UUID.fromString(ghostID);
		GhostAvatar tempGhost = obtainGhostInstance(ghostID);
		
		if (tempGhost != null)
		{
			if (updateMove.contains("pitch") || updateMove.contains("yaw"))
			{
				game.updateRotateGhostAvatar(ghostUUID, updateMove); // For Rotations
			}
			else // Else its a positional matter.
			{
				tempGhost.setGhostPosition(updatePos);            // For Positional Updates. 
				game.updateGhostAvatar(ghostUUID, updatePos);
			}
		}
		else 
		{   
			System.out.println("Move Update recieved. Ghost to update no found ** Creating ghost.'");  
			createGhostAvatar(ghostUUID, updatePos);
		}
	}
	
	
	
	public GhostAvatar obtainGhostInstance(String givenID)
	{
		int i = 0, recordLength = ghostAvatars.size();
		GhostAvatar tempGhost;
		UUID searchID = UUID.fromString(givenID);

		for (i = 0; i < recordLength; i++)
		{
			tempGhost = ghostAvatars.get(i);
			if (tempGhost.obtainGhostID().equals(searchID))
			{
				return tempGhost; 
			}
		}
		
		return null; 
	}
	
	private void updateGhostAvatar(UUID givenID, Vector3f updatePos) {
		// Update specific ghost avatar.
		System.out.println("Client. Update certain local ghost avatar. " + givenID); 
		int i =0, recordLength = ghostAvatars.size();
		GhostAvatar tempGhost;
		boolean ghostNotFound = true;
		
		for (i = 0; i < recordLength; i++)
		{
			tempGhost = ghostAvatars.get(i);
			if (tempGhost.obtainGhostID().equals(givenID))
			{
				// Update Ghost object's position.
				tempGhost.setGhostPosition(updatePos);
				
				// Update's Ghost Node's game world pos.
				game.updateGhostAvatar(tempGhost.obtainGhostID(), updatePos);
				ghostNotFound = false;
				break; // End loop. 
			}
		}
		
		if (ghostNotFound) // If ghost not found, then it wasn't made -> create one. 
		{
			System.out.println("----> Creating ghost avatar in details for as the ghost the details are for isn't existant");
			createGhostAvatar(givenID, updatePos);
		}
	}
	
	// Checks the ProtocolClient Record to see if given ghost ID already exists. 
	private boolean isGhostCreationValid(UUID ghostID)
	{
		GhostAvatar clientGhost = obtainGhostInstance(ghostID.toString());
		boolean ghostCreated = (clientGhost == null) ? false : true; // If a ghost exists -> not valid call to create ghost avatar. Vice versa.  
		System.out.println("isGhostCreationValid: False-Nonexistant/True-Existant: " + ghostCreated);
		return ghostCreated;
	}

	private void removeGhostAvatar(UUID ghostID) {
		System.out.println("RemoveGhostAvatar function called for Ghost: " + ghostID.toString());
		
		GhostAvatar deadGhost = obtainGhostInstance(ghostID.toString());
		GhostAvatar tempGhost; 
		int i, rlength = ghostAvatars.size();

		// Rid of the Ghost Existence in the local game world.
		game.removeGhostAvatarFromGameWorld(deadGhost);
		
		// Rid of the Ghost instance in this ProtocolClient.
		for (i = 0; i < rlength; i++)
		{
			tempGhost = ghostAvatars.get(i);
			if (tempGhost.obtainGhostID().equals(ghostID))
			{
				ghostAvatars.remove(i); // Remove ghost found at that instance.
				break; // End loop. 
			}
		} // End loop. 
	}
	
	// Create GHOST NPC in Client. 
	private void createGhostNPC(int id, Vector3 position)
	{ 
		GhostNPC newNPC = new GhostNPC(id, position);
		//ghostNPCs.add(newNPC);
		ghostNPCs.add(id, newNPC); // Id means index
		game.addGhostNPCtoGameWorld(newNPC);
	}
	
	// GHOST NPC Update. 
	private void updateGhostNPC(int id, Vector3 position)
	{ 
		GhostNPC ghostNPC;
		
		if (ghostNPCs.size() != 0)
		{		
			ghostNPC = ghostNPCs.get(id);
			if (ghostNPC != null)
			{
				ghostNPC.setPosition(position);
				game.updateNPCGhostAvatar(id, position);
			}
			
		}
		else // If no NPC to update, then make one.  
		{
			createGhostNPC(id, position); 
		}
	}
	
	// Outward Request by Client to the SERVER. 
	public void askForNPCinfo()
	{ 
		try
		{ 
			sendPacket(new String("needNPC," + id.toString()));
		}
		catch (IOException e)
		{ 
			e.printStackTrace();
		} 
	}
	
	// Send ball location to the server. 
	public void sendBallPositionToServer() 
	{
		String msg;
		Vector3f ballPos = game.obtainBallLocation(); 
		try
		{ 
			msg = new String("detailsForBall");
			msg += "," + ballPos.x()+"," + ballPos.y() + "," + ballPos.z();
			sendPacket(msg);
		}
		catch (IOException e) 
		{   e.printStackTrace();   } 
	}
	
	public UUID obtainClientID()
	{   return id;   }
}
