// Author: Hector Rios. ID: 220205545
package a3;


import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Iterator;

import ray.networking.IGameConnection.ProtocolType;
import java.util.UUID;

import myGameEngine.*;
import ray.input.GenericInputManager;
import ray.input.InputManager;
import ray.input.action.Action;
import ray.rage.*;
import ray.rage.asset.material.Material;
import ray.rage.asset.texture.Texture;
import ray.rage.asset.texture.TextureManager;
import ray.rage.game.*;
import ray.rage.rendersystem.*;
import ray.rage.rendersystem.shader.GpuShaderProgram;
import ray.rage.rendersystem.Renderable.*;
import ray.rage.scene.*;
import ray.rage.scene.Camera.Frustum.*;
import net.java.games.input.Controller;
import ray.rage.scene.controllers.*;
import ray.rage.util.BufferUtil;
import ray.rml.*;
import ray.rage.rendersystem.gl4.GL4RenderSystem;
import ray.rage.rendersystem.states.TextureState;
import ray.rage.rendersystem.states.*;

public class MyGame extends VariableFrameRateGame //implements MouseListener, MouseMotionListener
{
	// to minimize variable allocation in update()
	GL4RenderSystem rs; //
	float elapsTime = 0.0f;
	String elapsTimeStr, counterStr, dispStr;
	int elapsTimeSec, counter = 0, playerScore = 0, tempPlayerScore = 0;
	float time1 = 0.0f, time2 = 0.0f;
	boolean timeCount = false;
	float elapsedTimeHolder = 0.0f;
	Camera myCamera, myCamera2;
	SceneNode myCameraN, myCameraN2;
	
	private int p1Score = 0, p2Score = 0; // Scores to be displayed

	private RenderWindow renderWindow;
	private RenderSystem renderSystem;

	ArrayList<String> planetGameRecord = new ArrayList<String>(10);
	ArrayList<String> prismGameRecord1 = new ArrayList<String>(6); // main prism group.
	ArrayList<String> prismGameRecord2 = new ArrayList<String>(6); // main prism group.
	
	//RotationController rotateController; 
	//CustomNodeController customController;
	
	private float[] planeLoc = new float [] {0.0f, 0.0f, 0.0f}; // Predetermined dolphin positions
	
	// Input-Action Management
	private InputManager im; // input manager of device input
	private Action quitGameAction, switchModeAction; // Action objs to be tied to certain button components.
	private Action moveFrontBackAction, moveFrontBackActionGP;  
	private Action moveLeftRightAction, moveLeftRightActionGP; // Camera C Mode movement.
	private Action yawNodeAction, pitchNodeAction;       // Camera C move Pitch/Yaw
	
	private Camera3Pcontroller orbitController1, orbitController2;
	private Action yawNodeActionGP;
	private Action pitchNodeActionGP;
	
	private String serverAddress;    // Needed Variables for Network Operation
	private int serverPort;
	private ProtocolType serverProtocol;
	private ProtocolClient protClient;
	private boolean isClientConnected;
	private ArrayList<UUID> gameObjectsToRemove;
	
    public MyGame(String serverAddr, int sPort, String placeHolder)
    {
        super();
        
        System.out.println("MyGame Initilization");
        System.out.println("IP adress: " + serverAddr);
        System.out.println("sPort: " + sPort);
        System.out.println("UDP/TCP?: " + placeHolder);
        serverAddress = serverAddr;
        serverPort = sPort;
        serverProtocol = ProtocolType.UDP; // Change to UDP
        
		setupNetworking(elapsTime/1000.0f); // Send inital JOIN message to Server. 
    }

    public static void main(String[] args) 
    {
    	Game game;
    	if (args.length == 0)
    	{   // a3.MyGame 10.0.0.246 6001 UDP. IP address, Port number, UDP/TCP
    		game = new MyGame("10.0.0.246", Integer.parseInt("6002"), "UDP");  
    	}
    	else
    	{
            game = new MyGame(args[0], Integer.parseInt(args[1]), args[2]);
    	}
        try {
            game.startup();
            game.run();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        } finally {
            game.shutdown();
            game.exit();
        }
    }
	
    // Auto-called by Rage
	@Override
	protected void setupWindow(RenderSystem rs, GraphicsEnvironment ge) {
		rs.createRenderWindow(new DisplayMode(1000, 700, 24, 60), false);
	}
	
	// now we add setting up viewports in the window
	@Override
	protected void setupWindowViewports(RenderWindow rw)
	{ 	
		rw.addKeyListener(this);
		Viewport topViewport = rw.getViewport(0);
		topViewport.setDimensions(.51f, .01f, .99f, .49f); // B,L,W,H
		topViewport.setClearColor(new Color(0.1f, 0.1f, 0.3f)); // was 1-7-7, p1 -> light blue
		
		Viewport botViewport = rw.createViewport(.01f, .01f, .99f, .49f);
		botViewport.setClearColor(new Color(0.4f, 0.2f, 0.1f)); // p2 -> orange
	}

    @Override
    protected void setupCameras(SceneManager sm, RenderWindow rw) {
        SceneNode rootNode = sm.getRootSceneNode();
        renderWindow = rw;
        renderSystem = sm.getRenderSystem(); 

        // Camera 1 Set Up - MOUSE/KEYBOARD
        myCamera = sm.createCamera("MainCamera", Projection.PERSPECTIVE);
        rw.getViewport(0).setCamera(myCamera);
		
        myCamera.setRt((Vector3f)Vector3f.createFrom(1.0f, 0.0f, 0.0f));
        myCamera.setUp((Vector3f)Vector3f.createFrom(0.0f, 1.0f, 0.0f));
        myCamera.setFd((Vector3f)Vector3f.createFrom(0.0f, 0.0f, -1.0f));
		
        myCamera.setPo((Vector3f)Vector3f.createFrom(0.0f, 0.0f, 3.0f));

        myCameraN = rootNode.createChildSceneNode(myCamera.getName() + "Node");
        myCameraN.attachObject(myCamera);
        myCamera.setMode('n'); // Set to Node Mode.
        myCamera.getFrustum().setFarClipDistance(1000.0f);
        
        // Camera 2 Set Up - GAME-PAD
        myCamera2 = sm.createCamera("MainCamera2", Projection.PERSPECTIVE);
        rw.getViewport(1).setCamera(myCamera2);
        
        myCamera2.setPo((Vector3f)Vector3f.createFrom(1.0f, 0.0f, 3.0f));
        
        myCameraN2 = rootNode.createChildSceneNode(myCamera2.getName() + "Node");
        myCameraN2.attachObject(myCamera2);
        myCamera2.setMode('n'); // Set to Node Mode.
        myCamera2.getFrustum().setFarClipDistance(1000.0f);
    }

	private void setupOrbitCamera(Engine eng, SceneManager sm) 
	{
		// Goal is to set up Orbit Camera for player 1 (for now).
		SceneNode dolphinN = sm.getSceneNode("myDolphinNode");
		SceneNode cameraN = sm.getSceneNode("MainCameraNode");
		
		SceneNode dolphin2N = sm.getSceneNode("myDolphin2Node");
		SceneNode camera2N = sm.getSceneNode("MainCamera2Node");
		
		Camera camera = sm.getCamera("MainCamera"); // view port 0
		orbitController1 = new Camera3Pcontroller(cameraN, dolphinN, "keyboard", im, renderSystem, renderWindow);
		
		Camera camera2 = sm.getCamera("MainCamera2"); // view port 1
		orbitController2 = new Camera3Pcontroller(camera2, camera2N, dolphin2N, "gamepad", im);
	}
    
    // Set up the GameWorld
    @Override
    protected void setupScene(Engine eng, SceneManager sm) throws IOException 
    {    	
    	// Setting Up Group Nodes for Hierarchical Objects
    	// Dolphin Group Node
    	SceneNode dolphinNodeGroup = 
    			sm.getRootSceneNode().createChildSceneNode("myDolphinNodeGroup");
    	// Top Node that holds all custom prism generation
    	SceneNode prismNodeGroup = 
    			sm.getRootSceneNode().createChildSceneNode("myPrismNodeGroup");

    	    	
    	// Set Up Earth Object. 
    	Entity earthE = sm.createEntity("earthPlanet", "earth.obj");
    	earthE.setPrimitive(Primitive.TRIANGLES);
    	SceneNode earthN = sm.getRootSceneNode().createChildSceneNode(earthE.getName() + "Node");
    	earthN.attachObject(earthE);
    	earthN.setLocalPosition(0.0f, 0.5f, -1.0f);
    	earthN.setLocalScale(0.1f, 0.1f, 0.1f);

        // Set Up Dolphin for Player 1 ---
		Entity dolphinE = sm.createEntity("myDolphin", "dolphinHighPoly.obj");
        dolphinE.setPrimitive(Primitive.TRIANGLES);
        
        // Set dolphin1 Node to be child of dolphin group node.
        SceneNode dolphinN = dolphinNodeGroup.createChildSceneNode(dolphinE.getName() + "Node");
        dolphinN.moveUp(0.5f);
        dolphinN.rotate(Degreef.createFrom(180.0f), dolphinN.getLocalPosition()); // Trying to position the dolphin to face -z axis
        dolphinN.moveLeft(0.8f);
        dolphinN.moveBackward(0.5f);
        dolphinN.attachObject(dolphinE); // Attach node to model entity
        
        // Set Up Dolphin for Player 2 ---
        Entity dolphin2E = sm.createEntity("myDolphin2", "dolphinHighPoly.obj");
        dolphin2E.setPrimitive(Primitive.TRIANGLES);
        
        // Set dolphin2 Node to be child of dolphin group node.
        SceneNode dolphin2N = dolphinNodeGroup.createChildSceneNode(dolphin2E.getName() + "Node");
        dolphin2N.moveUp(0.5f);
        dolphin2N.rotate(Degreef.createFrom(180.0f), dolphin2N.getLocalPosition()); // Trying to position the dolphin to face -z axis
        dolphin2N.moveRight(0.8f);
        dolphin2N.moveBackward(0.5f);
        dolphin2N.attachObject(dolphin2E); // Attach node to model entity
        
        // Set Up Control Inputs
    	setupInputs(); // new function (defined below) to set up input actions

    	// OrbitCamera Set Up. In this Order
        setupOrbitCamera(eng, sm);
        
        // Lighting Environment
        sm.getAmbientLight().setIntensity(new Color(.4f, .4f, .4f)); // was .3f
        
        /* try different values for setRange(), setConstantAttenuation(), 
         * setLinearAttenuation(), and setQuadraticAttenuation(). 
         * You can also try different types of lights, such as "point" lights versus "spot" lights. */
		Light plight = sm.createLight("testLamp1", Light.Type.SPOT); // Was Point
		plight.setAmbient(new Color(.3f, .3f, .3f));
        plight.setDiffuse(new Color(.8f, .8f, .8f));
		plight.setSpecular(new Color(1.0f, 1.0f, 1.0f));
        plight.setRange(30f);
		
		SceneNode plightNode = sm.getRootSceneNode().createChildSceneNode("plightNode");
        plightNode.attachObject(plight);
        plightNode.setLocalPosition(1.0f, 1.0f, 5.0f);

        //sm.addController(rotateController); // Adds controller to SM.
        //sm.addController(customController);
        
        createGameObstacles(eng, sm, prismNodeGroup); // Generate and place objects that hinder player movement.
       
        // Texture Code -------------------------- 
        TextureManager tm = eng.getTextureManager();
        Texture mainTexture = tm.getAssetByPath("Dolphin_HighPolyUV.png");
        RenderSystem rs = sm.getRenderSystem();
        TextureState state = (TextureState) 
        		rs.createRenderState(RenderState.Type.TEXTURE);
        state.setTexture(mainTexture);
        dolphinE.setRenderState(state);
        
        // Floor Creation 
        // Set Up Floor Manual Object Plane
        ManualObject floor = new GroundPlaneObject().gameFloorObject(eng, sm, "gameFloor"); // Returns custom object data 
        SceneNode floorN = sm.getRootSceneNode().createChildSceneNode("gameFloorNode");
        floorN.setLocalPosition(planeLoc[0], planeLoc[1], planeLoc[2]); // planeLoc
        floorN.scale(1.0f, 6.0f, 1.0f);
        floorN.attachObject(floor);
        
        // Set Up Floor Manual Object Plane
        ManualObject floor2 = new GroundPlaneObject().gameFloorObject2(eng, sm, "gameFloor2"); // Returns custom object data 
        SceneNode floorN2 = sm.getRootSceneNode().createChildSceneNode("gameFloorNode2");
        floorN2.setLocalPosition(planeLoc[0], planeLoc[1], planeLoc[2]);
        floorN2.scale(1.0f, 6.0f, 1.0f);
        floorN2.attachObject(floor2);
    }

	protected void setupInputs()
    { 
    	im = new GenericInputManager();
    	
	    ArrayList<Controller> controllers = im.getControllers(); // Get list of controllers
	    Controller inputComponent;                               // Manage controllers
	    int i;													 // Loop count.
	    
	    SceneNode p1DolphinNode = getEngine().getSceneManager().getSceneNode("myDolphinNode");
	    SceneNode p2DolphinNode = getEngine().getSceneManager().getSceneNode("myDolphin2Node");
	    
    	// build some action objects for doing things in response to user input
    	quitGameAction = new QuitGameAction(this);
	    
	    moveFrontBackAction = new MoveFrontBackAction(p1DolphinNode, this);
	    moveFrontBackActionGP = new MoveFrontBackAction(p2DolphinNode, this); // movement controls for dolphin 2. 
	    
	    moveLeftRightAction = new MoveLeftRightAction(p1DolphinNode, this);
	    moveLeftRightActionGP = new MoveLeftRightAction(p2DolphinNode, this);
	    		
	    yawNodeAction = new MoveYawAction(p1DolphinNode, this);
	    yawNodeActionGP = new MoveYawAction(p2DolphinNode, this);
	    
	    pitchNodeAction = new MovePitchAction(p1DolphinNode, this);
	    pitchNodeActionGP = new MovePitchAction(p2DolphinNode, this);
	    
	    for (i = 0; i < controllers.size(); i++)
	    {
	    	inputComponent = controllers.get(i);
	    	
	    	if (inputComponent.getType() == Controller.Type.KEYBOARD)
	    	{
	    		im.associateAction(inputComponent, 
	    	    		net.java.games.input.Component.Identifier.Key.ESCAPE, // Give which key/button to attach action with.
	    	    		quitGameAction, 									  // Give Action Object you want to set as.
	    	    		InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);        // Give the conditions when active.
	    	    im.associateAction(inputComponent,
	    	    		net.java.games.input.Component.Identifier.Key.W,
	    	    		moveFrontBackAction,
	    	    		InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
	    	    im.associateAction(inputComponent,
	    	    		net.java.games.input.Component.Identifier.Key.S,
	    	    		moveFrontBackAction,
	    	    		InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
	    	    im.associateAction(inputComponent,
		 	    		net.java.games.input.Component.Identifier.Key.A,
		 	    		moveLeftRightAction,
		 	    		InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
	    	    im.associateAction(inputComponent,
		 	    		net.java.games.input.Component.Identifier.Key.D,
		 	    		moveLeftRightAction,
		 	    		InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
	    	    im.associateAction(inputComponent,
		 	    		net.java.games.input.Component.Identifier.Key.LEFT,
		 	    		yawNodeAction,
		 	    		InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
	    	    im.associateAction(inputComponent,
		 	    		net.java.games.input.Component.Identifier.Key.RIGHT,
		 	    		yawNodeAction,
		 	    		InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
	    	    
	    	    im.associateAction(inputComponent,
		 	    		net.java.games.input.Component.Identifier.Key.UP,
		 	    		pitchNodeAction,
		 	    		InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
	    	    im.associateAction(inputComponent,
		 	    		net.java.games.input.Component.Identifier.Key.DOWN,
		 	    		pitchNodeAction,
		 	    		InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
	    	}
	    	else if (inputComponent.getType() == Controller.Type.GAMEPAD || 
	    			 inputComponent.getType() == Controller.Type.STICK) 
	    	{   
	    		im.associateAction(inputComponent,
		 	    		net.java.games.input.Component.Identifier.Button._9,
		 	    		quitGameAction,
		 	    		InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
		 	    im.associateAction(inputComponent,
		 	    		net.java.games.input.Component.Identifier.Axis.Y,
		 	    		moveFrontBackActionGP,
		 	    		InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		 	    im.associateAction(inputComponent,
		 	    		net.java.games.input.Component.Identifier.Axis.X,
		 	    		moveLeftRightActionGP,
		 	    		InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
			 	im.associateAction(inputComponent,
		 	    		net.java.games.input.Component.Identifier.Axis.RY,
		 	    		pitchNodeActionGP,
		 	    		InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
			 	im.associateAction(inputComponent,
		 	    		net.java.games.input.Component.Identifier.Axis.RX,
		 	    		yawNodeActionGP,
		 	    		InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
	    	}
	    };
    }

	// Game Logic Goes here. 
    @Override
    protected void update(Engine engine) 
    {
    	int p1ViewX = renderWindow.getViewport(0).getActualLeft(), 
    		p1ViewY = renderWindow.getViewport(0).getActualBottom();
    	int p2ViewX = renderWindow.getViewport(1).getActualLeft(), 
        	p2ViewY = renderWindow.getViewport(1).getActualBottom();
    	
		// build and set HUD
		rs = (GL4RenderSystem) engine.getRenderSystem();
		elapsTime += engine.getElapsedTimeMillis();
		elapsTimeSec = Math.round(elapsTime/1000.0f);
		elapsTimeStr = Integer.toString(elapsTimeSec);
		counterStr = Integer.toString(counter);
		
		dispStr = "Time = " + elapsTimeStr + ".  Player 1 Score = " + p1Score;
		rs.setHUD2(dispStr, p1ViewX, p1ViewY); //rs.setHUD2(dispStr, 15, 15);
		
		dispStr = "Time = " + elapsTimeStr + ". Player 2 Score = " + p2Score;
		rs.setHUD(dispStr, p2ViewX, p2ViewY); //rs.setHUD(dispStr, 15, 345);
		
		// tell the input manager to process the inputs
		im.update(elapsTime);
		
		// Go through game objects and manage game count.
		gamePlanetEnvironment();
		
		// Process Network Needs
		processNetworking(elapsTime/1000.0f);
		
		orbitController1.updateCameraPosition(); // Orbit Controller 
		orbitController2.updateCameraPosition(); // Orbit Controller 
				
		if (timeCount == false) // Record the initial time
		{
			time1 = elapsTime/1000.0f;
			timeCount = true;
		}
		else if (timeCount == true) // Determine the time passed between updates.
		{
			time2 = elapsTime/1000.0f;
			elapsedTimeHolder = time2 - time1;
			timeCount = false;
		}
	}
    
	private void setupNetworking(float elpsTime)
	{ 
		gameObjectsToRemove = new ArrayList<UUID>();
		isClientConnected = false;
		try
		{ 
			protClient = new ProtocolClient(InetAddress.getByName(serverAddress), 
					serverPort, serverProtocol, this);
		} 
		catch (UnknownHostException e)
		{   e.printStackTrace();   } 
		catch (IOException e) 
		{   e.printStackTrace();   }
		if (protClient == null)
		{   System.out.println("ProtocolClient Null - missing protocol host");   }
		else
		{   // ask client protocol to send initial join message
		    // to server, with a unique identifier for this client
			//System.out.println("Sending JOIN msg to server thru Protocol Client");
		    protClient.sendJoinMessage();
		} 
	}
    
    // Constantly called to Process received packets & remove any players that have left.
    protected void processNetworking(float elapsTime)
    { 
    	SceneManager sm = getEngine().getSceneManager();
    	
    	// Process packets received by the client from the server
    	if (protClient != null)
    	{   protClient.processPackets();   }
    	
    	// remove ghost avatars for players who have left the game
    	Iterator<UUID> it = gameObjectsToRemove.iterator();
    
	    while(it.hasNext())
	    { 
	    	sm.destroySceneNode(it.next().toString());
	    }
	    gameObjectsToRemove.clear();
    }
    
	// Manages the Game rules such as point increase when in C mode.
    private void gamePlanetEnvironment()
    {
    	SceneManager sm = getEngine().getSceneManager();
    	
    	SceneNode dolphinN = getEngine().getSceneManager().getSceneNode("myDolphinNode");
    	SceneNode dolphinN2 = getEngine().getSceneManager().getSceneNode("myDolphin2Node");

    	String objectName;
    	Iterable<SceneNode> sceneWorld = sm.getSceneNodes(); // Get a collection of Scene objects.
    	
    	int i = 0, addScore=0, addScore2=0;
    	float distanceP1, distanceP2;
    	float planetRadius = 0.8f, earthRadius = 0.4f;
    	
    	for (SceneNode objectN : sceneWorld)
    	{
    		objectName = objectN.getName(); // Get name of node object
    		//System.out.println("objectN: " + objectN.getName());
    		if (objectName.contains("Planet")) // Found a planet, now check distance.
    		{
    			// Check the distance from the dolphin.
    			if ( (!planetGameRecord.contains(objectName)) && (myCamera.getMode() == 'n') ) // If the planet hasn't been visited. 
    			{
    				// May need to change this collision detection for other objects. 
    				distanceP1 = returnDistance((Vector3f) dolphinN.getLocalPosition(), (Vector3f) objectN.getLocalPosition());
    				distanceP2 = returnDistance((Vector3f) dolphinN2.getLocalPosition(), (Vector3f) objectN.getLocalPosition());
    				
    				if (objectName.contains("earth"))
    				{
    					addScore = (Math.abs(distanceP1) < earthRadius) ? 10 : 0; 
        				addScore2 = (Math.abs(distanceP2) < earthRadius) ? 10 : 0;
    				}
    				else
    				{
    					addScore = (Math.abs(distanceP1) < planetRadius) ? 10 : 0; 
        				addScore2 = (Math.abs(distanceP2) < planetRadius) ? 10 : 0;
    				}
    				//vController
    				if (addScore != 0) // If there was contact then add rotation controllers.
    				{
    					planetGameRecord.add(objectName);
    			    	//rotateController.addNode(objectN);
    				}
    				else if (addScore2 != 0) // Means player 2 scored. 
    				{
    					planetGameRecord.add(objectName);
    				}
    				
    				p1Score += addScore;
    				p2Score += addScore2;
    			}
    		}
    		else if (objectName.contains("PrismGroup"))
    		{
    			if (determinePrismCollision(objectN, dolphinN))
    			{
    				// If true that collision found. 
    				if (!prismGameRecord1.contains(objectName)) // If not already recorded.
    				{
    					prismGameRecord1.add(objectName); // Add to hit places. 
    					p1Score -= 10;                    // Reduce score based on first time collision.
    				}
    				// Return dolphin to start of map.
    				dolphinN.setLocalPosition(1.0f, 0.6f, 1.5f);
        		}
    			if (determinePrismCollision(objectN, dolphinN2))
    			{
    				if (!prismGameRecord2.contains(objectName)) // If not already recorded.
    				{
    					prismGameRecord2.add(objectName); // Add to hit places. 
    					p2Score -= 10;                    // Reduce score based on first time collision.
    				}
    				// Return dolphin to start of map.
    				dolphinN2.setLocalPosition(-1.0f, 0.6f, 1.5f);
    			}

    		}
    	}
    }
    
    // Creates groups of hierarchical objects. 
	private void createGameObstacles(Engine eng, SceneManager sm, SceneNode givenNodeGroup) throws IOException 
	{
		// Create Top Node to manage all prism generation
		SceneNode prismRootNode = givenNodeGroup; // Parent Node - To better manage Node Controllers
		SceneNode prismNodeGroup1 = prismRootNode.createChildSceneNode("PrismGroup1Node");
		createPrismGroup(prismNodeGroup1, eng, sm, -2.5f, 0.6f, -1.0f, "Grp1");
	}
	
	// Build the complex structure based on the basic Prism model. Goal: Model "Wind Turbine".
	public void createPrismGroup(SceneNode givenPrismGroupNode, Engine eng, SceneManager sm, float x, float y, float z, String ID) throws IOException
	{
		Vector3f axisX = (Vector3f) Vector3f.createFrom(1.0f, 0.0f, 0.0f);
		Vector3f axisZ = (Vector3f) Vector3f.createFrom(0.0f, 0.0f, 1.0f);

		Angle angle1 = Degreef.createFrom(-90.0f);
		Angle angle2 = Degreef.createFrom(-45.0f);
		Angle angle3 = Degreef.createFrom(45.0f);
		Angle angle4 = Degreef.createFrom(180.0f);
		
		//Second Object Creation/
		ManualObject corePrism = new SpaceObject().obtainPrismObject(eng, sm, ("LCorePrism_" + ID + "_")); 
		corePrism.setPrimitive(Primitive.TRIANGLES);
        
        SceneNode corePrismN = givenPrismGroupNode.createChildSceneNode(corePrism.getName() + "Node");
        corePrismN.scale(0.2f, 0.3f, 0.2f);
        corePrismN.rotate(angle1, axisX);
        corePrismN.attachObject(corePrism); // Attach node to model entity
        
        // Create First Wing
		ManualObject subPrism1 = new SpaceObject().obtainPrismObject(eng, sm, ("mySubPrism_1" + ID + "_")); 
		subPrism1.setPrimitive(Primitive.TRIANGLES);
		
		SceneNode subPrism1N = givenPrismGroupNode.createChildSceneNode(subPrism1.getName() + "Node");
		subPrism1N.scale(0.15f, 0.5f, 0.15f);
		subPrism1N.rotate(angle2, axisZ);
		subPrism1N.moveUp(0.5f);
		subPrism1N.attachObject(subPrism1);
        
		// Create Second Wing
		ManualObject subPrism2 = new SpaceObject().obtainPrismObject(eng, sm, ("mySubPrism_2" + ID + "_")); 
		subPrism2.setPrimitive(Primitive.TRIANGLES);
		
		SceneNode subPrism2N = givenPrismGroupNode.createChildSceneNode(subPrism2.getName() + "Node");
		subPrism2N.scale(0.15f, 0.5f, 0.15f);
		subPrism2N.rotate(angle3, axisZ);
		subPrism2N.moveUp(0.5f);
		subPrism2N.attachObject(subPrism2);

		// Create Third Wing. 
		ManualObject subPrism3 = new SpaceObject().obtainPrismObject(eng, sm, ("mySubPrism_3" + ID + "_")); 
		subPrism3.setPrimitive(Primitive.TRIANGLES);
		
		SceneNode subPrism3N = givenPrismGroupNode.createChildSceneNode(subPrism3.getName() + "Node");
		subPrism3N.scale(0.15f, 0.5f, 0.15f);
		subPrism3N.rotate(angle4, axisZ);
		subPrism3N.moveUp(0.5f);
		subPrism3N.attachObject(subPrism3);
		
		givenPrismGroupNode.setLocalPosition(x, y, z); // setLocalPosition(-1.5f, 0.6f, -1.5f);
		//customController.addNode(givenPrismGroupNode); // Sets the object to spin around Z-axis of middle obj.
	}
    
    private boolean determinePrismCollision(SceneNode objectN, SceneNode dolphinN) 
    {
		// Goal: Provides accurate collision detection of prism structure and players. 
    	Vector3f prismLoc = (Vector3f) objectN.getLocalPosition();    // Get prism position.
    	Vector3f dolphinLoc = (Vector3f) dolphinN.getLocalPosition(); // Get player position.
    
    	float boundryLimit = 1.0f;
    	
    	if (returnDistance(prismLoc, dolphinLoc) < boundryLimit)
    	{
    		return true;
    	}
    	
		return false;
	}

	
	// Holds the operation of the Distance formula
	private float returnDistance(Vector3f p1, Vector3f p2)
	{
		float x = (p1.x() - p2.x()), 
	    	  y = (p1.y() - p2.y()), 
	    	  z = (p1.z() - p2.z());
		
		float distance = (float) Math.sqrt((x*x) + (y*y) + (z*z));
		
		return distance;
	}
	
	public float obtainTime1() 
	{
		// returns the time of the last update call
		return time1;
	}
	public float[] obtainPlaneLoc()
	{
		// Returns the limit of dolphin movement.
		return planeLoc;
	}

	// Network Used Function - Connection Status Setter*
	public void setIsConnected(boolean b) 
	{
		// Info returned from the UDP Client manager.
		isClientConnected = b;
	}

	// * Assuming we are making a one view person game with networking, 
	// There will be only consistent player to refer to .
	// For now, I'm passing localDolphin pos. 
	public Vector3f getPlayerPosition() 
	{
		// return DolphinPos.
    	SceneNode dolphinN = getEngine().getSceneManager().getSceneNode("myDolphinNode");

		return (Vector3f) dolphinN.getLocalPosition();
	}
	
	public void addGhostAvatarToGameWorld(GhostAvatar avatar) throws IOException
	{ 
    	SceneManager sm = getEngine().getSceneManager();

		if (avatar != null)
		{ 
			Entity ghostE = sm.createEntity("ghost", "whatever.obj");
			ghostE.setPrimitive(Primitive.TRIANGLES);
			SceneNode ghostN = sm.getRootSceneNode().createChildSceneNode(avatar.obtainGhostID().toString());
			ghostN.attachObject(ghostE);
			//ghostN.setLocalPosition(desired location...);
			avatar.setGhostNode(ghostN);
			avatar.setGhostEntity(ghostE);
			//avatar.setPosition(node’s position... maybe redundant);
		} 
	}
			
	public void removeGhostAvatarFromGameWorld(GhostAvatar avatar)
	{ 
		if(avatar != null) 
		{   gameObjectsToRemove.add(avatar.obtainGhostID());   }
	}
	
}
