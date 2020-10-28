package a3;

import java.io.IOException;
import java.net.InetAddress;
import java.util.UUID;
import java.util.Vector;

import myGameEngine.GhostAvatar;
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
	private Vector<GhostAvatar> ghostAvatars;
	private int ghostID;
	private Vector3f ghostPosition; 
	
	public ProtocolClient(InetAddress remAddr, int remPort, ProtocolType pType, MyGame givenGame) throws IOException
	{
		super(remAddr, remPort, pType);
		this.game = givenGame;
		this.id = UUID.randomUUID();
		this.ghostAvatars = new Vector<GhostAvatar>();
	}
	
	// Messages Received from the Server.
	@Override
	protected void processPacket(Object msg)
	{ 
		String strMessage = (String) msg;
		String[] msgTokens = strMessage.split(",");
		
		System.out.println("ProtocolClient - message received: " + strMessage);
		
		if(msgTokens.length > 0)
		{	
			if(msgTokens[0].compareTo("join") == 0) // receive “join”
			{ // format: join, success or join, failure
				if(msgTokens[1].compareTo("success") == 0)
				{ 
					game.setIsConnected(true);
					sendCreateMessage(game.getPlayerPosition());
				}
				if(msgTokens[1].compareTo("failure") == 0)
				{   game.setIsConnected(false);   }
			}
			
			// Informs all clients with the name of a client who quit
			if(msgTokens[0].compareTo("bye") == 0) // receive “bye”
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
			if(msgTokens[0].compareTo("wantDetails") == 0) // rec. “wants…”
			{ 
				// Server sent request to send own info to newly made client for ghost creation.
				System.out.println("Remote Client wants this clients Details. Msg: " + strMessage);
				if (msgTokens[1] != id.toString())
				{
					// Send a details for message based on ID. If ID is same, then ignore, else send.
					// System.out.println("Client - sendDetailsForMessage: " + strMessage);
					sendDetailsForMessage(msgTokens[1], game.getPlayerPosition());
				}
			}
			
			// Provides client with updated status of a remote avatar
			// Update Ghost avatar for sender
			if (msgTokens[0].compareTo("detailsFor") == 0 )// receive “dsfr”)
			{ 
				// Shouldn't send anything else, this is where client finally recieves info from 
				// server about other clients. 
				String[] pos = {msgTokens[2], msgTokens[3], msgTokens[4]}; 

				updateGhostAvatar(msgTokens[1], pos);
				System.out.println("Client - details for requested -> call sendDetailsForMessage");
			}

			if(msgTokens[0].compareTo("move") == 0) // rec. “move...”
			{ 
				// etc….. 
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
	
	public void sendByeMessage()
	{ 
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
		try
		{ 
			String message = new String("detailsFor," + targetID.toString());
			message += ", " + id.toString();
			message += "," + pos.x()+"," + pos.y() + "," + pos.z();
			sendPacket(message);
		}
		catch (IOException e) 
		{   e.printStackTrace();   }
	}
	public void sendMoveMessage(Vector3 pos) // Pos = Vector3D
	{ 
		// etc….. 
	}
	
	private void createGhostAvatar(UUID ghostID, Vector3f ghostPosition2) {
		// Empty by Intention
		System.out.println("CreateGhostAvatar function called ... ");
	}
	
	private void updateGhostAvatar(String string, String[] pos) {
		// Update specific ghost avatar.
		System.out.println("Client must update specific ghost avatars. "); 
		System.out.println("Ghost to Update: " + string);
	}

	private void removeGhostAvatar(UUID ghostID) {
		// Empty by Intention
		System.out.println("RemoveGhostAvatar function called ... ");
		
	}
}
