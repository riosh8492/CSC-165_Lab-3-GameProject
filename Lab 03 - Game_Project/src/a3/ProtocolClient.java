package a3;

import java.io.IOException;
import java.net.InetAddress;
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
	private Vector<GhostAvatar> ghostAvatars;
	private GhostAvatar ghostHolder; 
	private int gAvatarID;
	private Vector3f gAvatarPosition; 
	
	public ProtocolClient(InetAddress remAddr, int remPort, ProtocolType pType, MyGame givenGame) throws IOException
	{
		super(remAddr, remPort, pType);
		this.game = givenGame;
		this.id = UUID.randomUUID();
		this.ghostAvatars = new Vector<GhostAvatar>();
		System.out.println("Client ID: " + id);
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
			
			// Informs this client to destroy given ghost ID.
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
				if (msgTokens[1] != id.toString())
				{
					// Send a details for message based on ID. If ID is same, then ignore, else send.
					sendDetailsForMessage(msgTokens[1], game.getPlayerPosition());
				}
			}
			
			// Provides client with updated status of a remote avatar
			// Update Ghost avatar for sender
			if (msgTokens[0].compareTo("detailsFor") == 0 )// receive “dsfr”)
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
	public void sendMoveMessage(Vector3 pos) // Pos = Vector3D
	{ 
		// etc….. 
	}
	
	private void createGhostAvatar(UUID ghostID, Vector3f ghostPos) 
	{
		System.out.println("Client. CREATE Msg received. Now calling -> CreateGhostAvatar function");
		// Basically record ghost creation and then call game to create ghost based on given Entity & Node.
		
		ghostHolder = new GhostAvatar(ghostID, ghostPos); // Create ghost avatar instance. 
		ghostAvatars.add(ghostHolder);					  // Record ghost avatar. 
		
		try 
		{   game.addGhostAvatarToGameWorld(ghostHolder);   } 
		catch (IOException e) 
		{   e.printStackTrace();   }		
	}
	
	private void updateGhostAvatar(UUID givenID, Vector3f updatePos) {
		// Update specific ghost avatar.
		System.out.println("Client. Update certain local ghost avatar. " + givenID); 
		int i =0, recordLength = ghostAvatars.size();
		GhostAvatar tempGhost;
		
		for (i = 0; i < recordLength; i++)
		{
			tempGhost = ghostAvatars.get(i);
			if (tempGhost.obtainGhostID() == givenID)
			{
				// Update Ghost object's position.
				tempGhost.setGhostPosition(updatePos);
				
				// Update's Ghost Node's game world pos.
				game.updateGhostAvatar(tempGhost.obtainGhostID(), updatePos);
				
				break; // End loop. 
			}
		}
		
		// If function still going, then this client does not have an instance of the ghost avatar. Thus we have to make one.
		System.out.println("----> Creating ghost avatar in details for as the ghost the details are for isn't existant");
		createGhostAvatar(givenID, updatePos);
		
	}

	private void removeGhostAvatar(UUID ghostID) {
		// Empty by Intention
		System.out.println("RemoveGhostAvatar function called ... ");
		
	}
}
