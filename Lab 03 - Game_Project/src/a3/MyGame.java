// CSC-165. Lab 3. Author(s): Hector R., Peter K.
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
import java.util.Scanner;

import ray.networking.IGameConnection.ProtocolType;
import java.util.UUID;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import myGameEngine.*;
import myGameServer.NetworkingServer;
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
import static ray.rage.scene.SkeletalEntity.EndType.*;


import ray.physics.PhysicsEngine;
import ray.physics.PhysicsObject;
import ray.physics.PhysicsEngineFactory;
import ray.audio.*;
// import com.jogamp.openal.ALFactory;

public class MyGame extends VariableFrameRateGame
{
	// to minimize variable allocation in update()
	GL4RenderSystem rs; //
	float elapsTime = 0.0f;
	String elapsTimeStr, counterStr, dispStr;
	int elapsTimeSec, counter = 0, playerScore = 0, tempPlayerScore = 0;
	float time1 = 0.0f, time2 = 0.0f;
	boolean timeCount = false;
	float elapsedTimeHolder = 0.0f;
	Camera myCamera;
	SceneNode myCameraN;
	
	//Game variables
	private int p1Score = 0;     // Score to be displayed
	private BasicSkyBox skyBox;  //Skybox
	private BasicMap terrainMap; //Map

	private RenderWindow renderWindow;
	private RenderSystem renderSystem;

	private ArrayList<String> planetGameRecord = new ArrayList<String>(10);
	private ArrayList<String> prismGameRecord1 = new ArrayList<String>(6); // main prism group.
	private ArrayList<SceneNode> collisionRecord = new ArrayList<SceneNode>();

	//RotationController rotateController; 
	//CustomNodeController customController;
	
	private float[] planeLoc = new float [] {0.0f, 0.0f, 0.0f}; // Predetermined dolphin positions
	
	// Input-Action Management
	private InputManager im;       // Variable for Input Manager
	private Action quitGameAction; // Action objs to be tied to certain button components.
	private Action moveFrontBackAction;  
	private Action moveLeftRightAction;            
	private Action yawNodeAction, pitchNodeAction; 

	private Camera3Pcontroller orbitController1;
	
	private String serverAddress;    // Needed Variables for Network Operation
	private int serverPort;
	private ProtocolType serverProtocol;
	private ProtocolClient protClient;
	private boolean isClientConnected;
	private ArrayList<UUID> gameObjectsToRemove;
	private static NetworkingServer app; 
	private Vector3f ballPosition; 
	
	private SceneNode gndNode, statNetNode; // Physics Variables. 
	private final static String GROUND_E = "Ground";
	private final static String GROUND_N = "GroundNode";
	private PhysicsEngine physicsEng; 
	private PhysicsObject ball1PhysObj, ball2PhysObj;
	private PhysicsObject gndPlaneP, gameBallPhysObj, clientPhysObj, courtNetPhysObj, npcKnightPhysObj;
	private double[] netPosTransform; 
	private boolean running = true;
	private float ballAngle = 0.0f;
	
	private Tessellation tessTest; // Sound Variables. 
	private float movemt = 0.01f;
	IAudioManager audioMgr;
	Sound oceanSound, bkgdMusic;
	
	private boolean initWalkAnimation = false;     // Animation variables. 
	private boolean initHandsWaveAnimation = false;
	private boolean originBallSender = true; // Networking related. 
	
	private boolean playerStatus = false;
	private Vector3f clientPos1 = (Vector3f) Vector3f.createFrom(0.0f, 0.5f, 3.0f); // Positioning. 
	private Vector3f clientPos2 = (Vector3f) Vector3f.createFrom(0.0f, 0.5f, -3.0f);	
	
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

		setupScriptVariables("a3\\scriptGameInit.js");
	}

    public static void main(String[] args) 
    {
    	Game game;
    	if (args.length == 0){   // a3.MyGame 10.0.0.246 6001 UDP. IP address, Port number, UDP/TCP
    		game = requestGameEnv(); // Created process of getting game mode from user. 
    		// game = new MyGame("10.0.0.246", 6001, "UDP");  
    	}
    	else{
            game = new MyGame(args[0], Integer.parseInt(args[1]), args[2]);
    	}
        try {
            game.startup();
            game.run();
		} 
		catch (Exception e){
			e.printStackTrace(System.err);
			System.out.println("Cause?: " + e.getCause());
		} 
		finally {
            game.shutdown();
            game.exit();
        }
	}
    
    private static Game requestGameEnv()
    {
    	Game tempGame;
    	boolean userConfirm = false; 
    	String command, givenIP = "10.0.0.246";
    	int    givenPort = 6020;
    	String initalRequest = "Welcome to GL Sports Volleyball Game.";
    	String requestIP = "Enter IP address. ";
    	String requestPort = "Enter Port Number. ";
    	String confirmInput = "", confirmRequest = "Enter (Y/N) to Confirm... ";
    	Scanner myObj = new Scanner(System.in);  // Create a Scanner object
        
    	System.out.println(initalRequest); // Greet User. 
        
        System.out.println(requestIP); // Get IP address from user.
    	givenIP = myObj.nextLine();
    	System.out.println(requestPort); // Get Port Number from user. 
    	givenPort = myObj.nextInt();
            	
    	tempGame = new MyGame(givenIP, givenPort, "UDP"); // 127.0.0.1   
		System.out.println("Client Initilization complete."); 

    	return tempGame;
    }
	
	private void setupScriptVariables(String filepath)
	{
    	ScriptEngineManager factory = new ScriptEngineManager();

		String scriptFileName = System.getProperty("user.dir");  // Directory. 
		scriptFileName += "\\a3\\ScriptGameInit.js";
		
		// get the JavaScript engine
		ScriptEngine jsEngine = factory.getEngineByName("js");
		// run the script
		executeScript(jsEngine, scriptFileName);
		// Set variables to initial values via script. 
		elapsTime	      = (int) jsEngine.get("elapsTime");
		counter   	      = (int) jsEngine.get("counter");
		playerScore       = (int) jsEngine.get("playerScore"); 
		tempPlayerScore   = (int) jsEngine.get("tempPlayerScore");
		time1     		  = (int) jsEngine.get("time1"); 
		time2             = (int) jsEngine.get("time2");
		timeCount 		  = (boolean) jsEngine.get("timeCount");
		elapsedTimeHolder = (int) jsEngine.get("elapsedTimeHolder");
	}

    private void executeScript(ScriptEngine engine, String scriptFileName)
    {
	    try
	    { 
	    	FileReader fileReader = new FileReader(scriptFileName);
	    	engine.eval(fileReader); //execute the script statements in the file
	    	fileReader.close();
	    }
	    catch (FileNotFoundException e1)
	    {   System.out.println(scriptFileName + " not found " + e1);   }
	    catch (IOException e2)
	    {   System.out.println("IO problem with " + scriptFileName + e2);   }
	    catch (ScriptException e3)
	    {   System.out.println("ScriptException in " + scriptFileName + e3);   }
	    catch (NullPointerException e4)
	    {   System.out.println ("Null ptr exception in " + scriptFileName + e4);   }
    }
    
    // GETTERS AND SETTERS
	public BasicMap getMap() 
	{   return terrainMap;   } //need to use terrain ht func
	
    // Auto-called by Rage
	@Override
	protected void setupWindow(RenderSystem rs, GraphicsEnvironment ge) {
		rs.createRenderWindow(new DisplayMode(1000, 700, 24, 60), false);
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
    }

	// Goal is to set up Orbit Camera for player 1 (for now).
	private void setupOrbitCamera(Engine eng, SceneManager sm) 
	{
		SceneNode clientN = sm.getSceneNode("clientModelNode");
		SceneNode cameraN = sm.getSceneNode("MainCameraNode");
		
		orbitController1 = new Camera3Pcontroller(cameraN, clientN, "keyboard", im, renderSystem, renderWindow);
	}
    
    // Set up the GameWorld
    @Override
    protected void setupScene(Engine eng, SceneManager sm) throws IOException 
    {    	
    	// Physics Related -> Setting floor limit to falling objects. 
    	gndNode = sm.getRootSceneNode().createChildSceneNode("GroundLevelPosNode");
    	gndNode.setLocalPosition(0.0f, -0.3f, 0.0f);
    	
    	statNetNode = sm.getRootSceneNode().createChildSceneNode("StaticNetNode");
    	statNetNode.setLocalPosition(0.0f, 2.0f, -2.0f);
    	
    	// Set Up Game Ball Object. 
    	Entity sphereE = sm.createEntity("gameBall", "blender_gem01.obj"); // Was: "earth.obj"
    	sphereE.setPrimitive(Primitive.TRIANGLES);
    	SceneNode sphereN = sm.getRootSceneNode().createChildSceneNode(sphereE.getName() + "Node");
    	sphereN.attachObject(sphereE);
    	sphereN.setLocalPosition(0.0f, 0.5f, 2.0f);
    	ballPosition = (Vector3f) sphereN.getLocalPosition(); 
    	//sphereN.scale(0.1f, 0.1f, 0.1f);
  
        
        // Set up Net Object. -----------------------
        Entity courtNetE = sm.createEntity("courtNetModel", "blender-Net.obj"); // dolphinHighPoly.obj-BasicModelUVMapping.obj
        courtNetE.setPrimitive(Primitive.TRIANGLES);
        
        SceneNode courtNetN = sm.getRootSceneNode().createChildSceneNode(courtNetE.getName() + "Node");
        courtNetN.setLocalPosition(0.0f, 1.0f, 0.0f);
        //courtNetN.rotate(Degreef.createFrom(90.0f), courtNetN.getLocalPosition()); // Trying to position the dolphin to face -z axis
        //courtNetN.scale(0.15f, 0.15f, 0.15f);
        //courtNetN.setLocalPosition(0.0f, 1.0f, -2.0f);
        courtNetN.attachObject(courtNetE);
        // End of Set up Net Object. -----------------
        
        // Set Up other NPC Model Obj 1 -------------
        Entity knightE = sm.createEntity("npc_knight", "racoonModel.obj"); // dolphinHighPoly.obj-BasicModelUVMapping.obj
        knightE.setPrimitive(Primitive.TRIANGLES);
        
        SceneNode knightN = sm.getRootSceneNode().createChildSceneNode(knightE.getName() + "Node");
        knightN.setLocalPosition(3.0f, 1.0f, -1.0f);
        //knightN.scale(2.0f, 2.0f, 2.0f);
        knightN.attachObject(knightE);
        // Set Up other NPC Model Obj 1 -----End-----
        
        
        // Set Up Model & Texture for Player 1 ---
        
        // Create Model Object with Animations/Skeleton/Mesh
        // initalizeTestModel("clientModel");
        
        //* Block out this set up to then use animated model in inititalizeModel. 
		Entity clientE = sm.createEntity("clientModel", "racoonModel.obj"); // dolphinHighPoly.obj-BasicModelUVMapping.obj
		clientE.setPrimitive(Primitive.TRIANGLES);
        
        // Set client Node
        SceneNode clientN = sm.getRootSceneNode().createChildSceneNode(clientE.getName() + "Node"); // clientModelNode
        clientN.setLocalPosition(0.0f, 1.0f, 0.0f); // y axis
        clientN.rotate(Degreef.createFrom(180.0f), clientN.getLocalPosition()); // Trying to position the dolphin to face -z axis
        clientN.setLocalPosition(0.0f, 0.5f, 3.0f);
        //clientN.scale(0.2f, 0.2f, 0.2f);
        clientN.attachObject(clientE); // */ 
		        
        // Set Up SkyBox/Map
        skyBox = new BasicSkyBox(eng, "desert");
        // Set Up Map
        terrainMap = new BasicMap("desert","desertStage_htMap.jpeg","desertTexture.jpeg");
        terrainMap.createMap(eng, sm);
        
        // Physics Attempt.
        initPhysicsSystem();
        createRagePhysicsWorld();
		
        // Set Up Control Inputs
    	setupInputs(); // new function (defined below) to set up input actions

    	// OrbitCamera Set Up. In this Order
        setupOrbitCamera(eng, sm);
        
        // Set up any area Lights
        setUpLights(sm);

        //sm.addController(rotateController); // Adds controller to SM.
        //sm.addController(customController);
        
        initAudio(sm); // Begins Sound Setup
        
        System.out.println("End SceneSetup");
        // End Scene Set Up
    }
    
    private void setUpLights(SceneManager sm)
    {
    	// Lighting Environment
        sm.getAmbientLight().setIntensity(new Color(.4f, .4f, .4f)); // was .3f
        
        /* try different values for setRange(), setConstantAttenuation(), 
         * setLinearAttenuation(), and setQuadraticAttenuation(). 
         * You can also try different types of lights, such as "point" lights versus "spot" lights. */
		Light plight = sm.createLight("testLamp1", Light.Type.SPOT); // Was Point
		plight.setAmbient(new Color(.1f, .1f, .1f));
        plight.setDiffuse(new Color(.7f, .7f, .7f));
		plight.setSpecular(new Color(1.0f, 1.0f, 1.0f));
        plight.setRange(30.0f);
		
		SceneNode plightNode = sm.getRootSceneNode().createChildSceneNode("plightNode");
        plightNode.attachObject(plight);
        plightNode.setLocalPosition(0.0f, 3.0f, -3.0f);
    }

    // SOUND SETUP Start
	private void initAudio(SceneManager sm) 
	{
		AudioResource resource1;//, resource2;
		audioMgr = AudioManagerFactory.createAudioManager(
				"ray.audio.joal.JOALAudioManager");
		if (!audioMgr.initialize())
		{   
			System.out.println("Audio Manager failed to initialize!");
			return;
		}
		resource1 = audioMgr.createAudioResource("bensound_dreams_background_music.wav",
				AudioResourceType.AUDIO_SAMPLE);
		
		bkgdMusic = new Sound(resource1, SoundType.SOUND_EFFECT, 100, true);
		bkgdMusic.initialize(audioMgr);
		
		bkgdMusic.setMaxDistance(10.0f);
		bkgdMusic.setMinDistance(0.5f);
		bkgdMusic.setRollOff(5.0f);
		
		SceneNode ballN = sm.getSceneNode("gameBallNode");
		bkgdMusic.setLocation(ballN.getWorldPosition());

		setEarParameters(sm);
		
		bkgdMusic.play();
	}
	
	private void setEarParameters(SceneManager sm) 
	{
		SceneNode clientN = sm.getSceneNode("clientModelNode"); // Change
		Vector3 avDir = orbitController1.obtainCameraPosition(); // Gets forward vector position of camera. 
		
		// note - should get the camera's forward direction
		// - avatar direction plus azimuth
		
		audioMgr.getEar().setLocation(clientN.getWorldPosition());
		audioMgr.getEar().setOrientation(avDir, Vector3f.createFrom(0,1,0));
	}
	
	public void updateGameSounds()
	{
		SceneManager sm = getEngine().getSceneManager();
		SceneNode ballN = sm.getSceneNode("gameBallNode");

		bkgdMusic.setLocation(ballN.getWorldPosition());
		setEarParameters(sm);
	}
	// SOUND SETUP End

	protected void setupInputs()
    { 
    	im = new GenericInputManager();
    	
	    ArrayList<Controller> controllers = im.getControllers(); // Get list of controllers
	    Controller inputComponent;                               // Manage controllers
	    int i;													 // Loop count.
	    
	    SceneNode p1ClientNode = getEngine().getSceneManager().getSceneNode("clientModelNode");
	    
    	// build some action objects for doing things in response to user input
    	quitGameAction = new QuitGameAction(this, protClient);
	    moveFrontBackAction = new MoveFrontBackAction(p1ClientNode, this, protClient);
	    moveLeftRightAction = new MoveLeftRightAction(p1ClientNode, this, protClient);	
		
		yawNodeAction = new MoveYawAction(p1ClientNode, this, protClient);
	    pitchNodeAction = new MovePitchAction(p1ClientNode, this, protClient);
	    
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
	    	{   /*// Remove Game-Pad funcitons for now. 
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
		 	    //*/
	    	}
	    };
    }
	
	// Physics Code Logic Begin.
	private void initPhysicsSystem()
	{ 
		String engine = "ray.physics.JBullet.JBulletPhysicsEngine";
		
		float[] gravity = {0, -2f, 0};
		physicsEng = PhysicsEngineFactory.createPhysicsEngine(engine);
		physicsEng.initSystem();
		physicsEng.setGravity(gravity);
	}
	
	private void createRagePhysicsWorld()
	{ 
		float mass = 1.0f;
		float up[] = {0,1,0};
		float clientHitbox[] = {0.8f, 2.0f, 1.8f};
		float courtNetHitbox[] = {4.0f, 2.0f, .5f};
		double[] temptf;
		
		SceneNode gameBallN = getEngine().getSceneManager().getSceneNode("gameBallNode"); 
		SceneNode clientN = getEngine().getSceneManager().getSceneNode("clientModelNode"); // clientPhysObj
		SceneNode courtNetN = getEngine().getSceneManager().getSceneNode("courtNetModelNode"); 
		SceneNode npcKnightN = getEngine().getSceneManager().getSceneNode("npc_knightNode");
	
		// Set Up Game Ball Object. 
		temptf = toDoubleArray(gameBallN.getLocalTransform().toFloatArray());
		gameBallPhysObj = physicsEng.addSphereObject(physicsEng.nextUID(), mass, temptf, 0.6f);
		gameBallPhysObj.setBounciness(1.3f);
		gameBallPhysObj.setDamping(0.7f, 0.0f); 
		gameBallN.scale(0.1f, 0.1f, 0.1f);
		gameBallN.setPhysicsObject(gameBallPhysObj);
		
		// Set Up Physics NPC Object
		temptf = toDoubleArray(npcKnightN.getLocalTransform().toFloatArray());
		npcKnightPhysObj = physicsEng.addSphereObject(physicsEng.nextUID(), mass, temptf, 0.82f);
		npcKnightPhysObj.setBounciness(0.5f);
		npcKnightN.scale(0.3f, 0.3f, 0.3f);
		npcKnightN.setPhysicsObject(npcKnightPhysObj);
				
		// Set up client Object
		temptf = toDoubleArray(clientN.getLocalTransform().toFloatArray());
		clientPhysObj = physicsEng.addSphereObject(physicsEng.nextUID(), mass, temptf, 0.2f); // 0.2f w sphere.
		clientPhysObj.setBounciness(0.5f);
     
		clientN.scale(0.13f, 0.13f, 0.13f);
		clientN.setLocalPosition(0.0f, 0.5f, 3.0f); // below 1
		clientN.setPhysicsObject(clientPhysObj);
		
		// Set up Court Net Object
		courtNetN.setLocalPosition(0.0f, 1.0f, 0.0f); // statNetNode
		temptf = toDoubleArray(courtNetN.getLocalTransform().toFloatArray());
		netPosTransform = toDoubleArray(courtNetN.getLocalTransform().toFloatArray());
		courtNetPhysObj = physicsEng.addBoxObject(physicsEng.nextUID(), mass, temptf, courtNetHitbox); // 0.2f w sphere.
		courtNetPhysObj.setBounciness(0.3f);
		courtNetN.setLocalPosition(0.0f, 1.0f, 0.0f);
		courtNetN.rotate(Degreef.createFrom(90.0f), courtNetN.getLocalPosition()); // Trying to position the dolphin to face -z axis
		//courtNetN.setLocalPosition(0.0f, 1.0f, 0.0f); // Was: 0, 1, (-2)
		courtNetN.scale(0.15f, 0.15f, 0.15f);
		courtNetN.setPhysicsObject(courtNetPhysObj);
		// 0.0f, 2.0f, 0.0f -> Idea position. 
		
		temptf = toDoubleArray(gndNode.getLocalTransform().toFloatArray());
		gndPlaneP = physicsEng.addStaticPlaneObject(physicsEng.nextUID(), temptf, up, 0.0f);
		gndPlaneP.setFriction(0.2f);
		//gndPlaneP.setBounciness(1.0f);
		gndNode.scale(10f, .05f, 10f);
		gndNode.setLocalPosition(0.0f, 0.0f, 0.0f); // was 0, -7, -2
		gndNode.setPhysicsObject(gndPlaneP);
		// can also set damping, friction, etc.
	}
	
	// Meant to update the Physics World.
	private void updatePhysicsWorld(float time)
	{
		Matrix4 mat;
		PhysicsObject currentPhysObj;
		Angle angle = Degreef.createFrom(ballAngle);
		
		if (running)
		{ 
			physicsEng.update(time);  // Needed to Update Physics World. 

			for (SceneNode s : getEngine().getSceneManager().getSceneNodes())
			{ 
				currentPhysObj = s.getPhysicsObject(); // Get SceneNode Physics OBject. 

				if (currentPhysObj != null)
				{   
					findCollisionPair(s); 
					
					if (s.getName().contains("client"))// || s.getName().contains("Net"))
					{
						mat = s.getLocalTransform();
						currentPhysObj.setTransform(toDoubleArray(mat.toFloatArray())); // Takes in: Double []
					}
					else if (s.getName().contains("Net"))
					{
						currentPhysObj.setTransform(netPosTransform); // Takes in: Double []
					}
					else 
					{
						mat = Matrix4f.createFrom(toFloatArray(currentPhysObj.getTransform()));
						
						s.setLocalPosition(mat.value(0,3), mat.value(1,3), mat.value(2,3));
						if (s.getName().contains("Ball"))
						{
							s.rotate(angle, (Vector3) Vector3f.createFrom(0.0f, 1.0f, 0.0f));
							ballAngle -= (ballAngle <= 0.0f) ? 0.0f : 0.01f;
						}

					}

				} 
			} 
		}
	} // Update Physics Function End. 

	
	// Use current node to check all other Nodes that are higher than it on the X axis. 
	// Stop checking if position of Node/Obj is too far away. 
	private void findCollisionPair(SceneNode currentNode) 
	{
		float curXPos = currentNode.getLocalPosition().x(); 
		float tempXPos; 
		boolean condition1 = false, condition2 = false;
		
		for (SceneNode s : getEngine().getSceneManager().getSceneNodes())
		{
			float bounds = 0.3f;
			
			if ((currentNode.getName().contains("Ground")) || (s.getName().contains("Ground"))) 
			{   break;   }
			
			if ((currentNode.getName() != s.getName()) && (s.getPhysicsObject() != null))
			{ 
				tempXPos = s.getLocalPosition().x();
				
				condition1 = ( (curXPos + bounds) > (tempXPos - bounds)) ? true : false;
				condition2 = ( (curXPos - bounds) < (tempXPos + bounds)) ? true : false;
		
				if (condition1 && condition2) // There is a match pair. 
				{
					collisionRecord.add(currentNode); // Add the matched Pair. 
					collisionRecord.add(s);
					handlePossibleCollision(currentNode, s); // Handle collision possibility here. 
				}
			}
		}
	}
	
	// Function to check into if the given pair is colliding, then if so handle it. So that
	// The next loop doesn't produce any duplicate pairs. 
	private void handlePossibleCollision(SceneNode initialObj, SceneNode tempObj) 
	{
		float bounds = 0.3f; 
		SceneNode obj1 = collisionRecord.get(0);
		SceneNode obj2 = collisionRecord.get(1);
		
		PhysicsObject objPhys1 = initialObj.getPhysicsObject(); 
		PhysicsObject objPhys2 = tempObj.getPhysicsObject();
		
		float curYPos = initialObj.getLocalPosition().y();
		float tempYPos = tempObj.getLocalPosition().y();
		
		float curZPos = initialObj.getLocalPosition().z();
		float tempZPos = tempObj.getLocalPosition().z();
		
		boolean condition3 = ( (curYPos + bounds) > (tempYPos - bounds)) ? true : false;
		boolean condition4 = ( (curYPos - bounds) < (tempYPos + bounds)) ? true : false;
		
		boolean condition5 = ( (curZPos + bounds) > (tempZPos - bounds)) ? true : false;
		boolean condition6 = ( (curZPos - bounds) < (tempZPos + bounds)) ? true : false;
				
		if (collisionRecord.size() != 0) // Ensure we aren't going through empty list. 
		{
			if (condition3 && condition4 && condition5 && condition6)
			{
				System.out.println("Collising occuring.");
				
				if (!(initialObj.getName().contains("Net")) && !(tempObj.getName().contains("Net"))) 
				{
					if (initialObj.getName().contains("Ball"))
					{   
						//System.out.println("Ball encountered.");
						//objPhys1.applyTorque(1.0f, 0.0f, 0.0f);
						movePhysicsBall(initialObj); 
						ballAngle = 1.0f;
					}
					else if (tempObj.getName().contains("Ball"))
					{   
						//System.out.println("Ball encountered.");
						//objPhys2.applyTorque(1.0f, 0.0f, 0.0f); 
						movePhysicsBall(tempObj);
						ballAngle = 1.5f;
					}
				}
			}
		}
		collisionRecord.clear(); // Empty out collision record.
	}
	
	// Serves to make physics ball move at an angle for that game.
	// Basically, for now: when model hits ball, push it at an angle. 
	public void movePhysicsBall(SceneNode givenBall)
	{
		Vector3f ballPos = (Vector3f) givenBall.getLocalPosition();
		PhysicsObject physBall = givenBall.getPhysicsObject(); 
		
		float forceAmt = 4.0f; 
		float forceFloat  [] = new float [] {0.0f, 4.5f, -forceAmt}; 
		float forceFloat2 [] = new float [] {0.0f, 4.5f, forceAmt}; 
		
		//physBall.setAngularVelocity(stopMotion); // Stop ball movement, and shoot ball. 
		//physBall.setLinearVelocity(stopMotion);
		
		if (ballPos.z() > 0.1f)       // On the +Z Axis side of the net. 
		{
			//physBall.applyForce(0.0f, forceAmt, -forceAmt, 
			//		ballPos.x(), ballPos.y(), ballPos.z());
			physBall.setLinearVelocity(forceFloat);
		}
		else if (ballPos.z() < -0.1f) // On the -Z Axis side of the net.
		{
			//physBall.applyForce(0.0f, forceAmt, forceAmt, 
			//		ballPos.x(), ballPos.y(), ballPos.z());
			physBall.setLinearVelocity(forceFloat2);
		}
	}

	// Physics Utility Functions
	private float[] toFloatArray(double[] arr)
	{ 
		if (arr == null) 
		{   return null;   }
		
		int n = arr.length;
		float[] ret = new float[n];
		
		for (int i = 0; i < n; i++)
		{   ret[i] = (float)arr[i];   }
		
		return ret;
	}
	private double[] toDoubleArray(float[] arr)
	{ 
		if (arr == null)
		{   return null;   }
		
		int n = arr.length;
		double[] ret = new double[n];
		for (int i = 0; i < n; i++)
		{   ret[i] = (double)arr[i];   }
		
		return ret;
	}
	public void setPhysicsRun(boolean givenState)
	{   running = givenState;   }
	
	public boolean getPhysicsRun()
	{   return running;         }
	// Physics End.

	// Game Logic Goes here. 
    @Override
    protected void update(Engine engine) 
    {
    	int p1ViewX = renderWindow.getViewport(0).getActualLeft(), 
    		p1ViewY = renderWindow.getViewport(0).getActualBottom();
    	
		// build and set HUD
		rs = (GL4RenderSystem) engine.getRenderSystem();
		elapsTime += engine.getElapsedTimeMillis();
		elapsTimeSec = Math.round(elapsTime/1000.0f);
		elapsTimeStr = Integer.toString(elapsTimeSec);
		counterStr = Integer.toString(counter);
		
		dispStr = "Time = " + elapsTimeStr + ".  Player 1 Score = " + p1Score;
		rs.setHUD(dispStr, p1ViewX, p1ViewY); //rs.setHUD2(dispStr, 15, 15);
		
		// tell the input manager to process the inputs
		im.update(elapsTime);
				
		processNetworking(elapsTime/1000.0f);    // Process Network Needs
		
		orbitController1.updateCameraPosition(); // Updates Orbit Controller 
		
		generateDeltaTime(elapsTime/1000.0f);    // Updates the player's elapsed time rate for movement.

		updatePhysicsWorld(elapsTime); // Updates the Physics World. 

		// updateAnimations(); // Updates Model Animations

		updateServerBallPosition(); // Sends the Server an update on ball position. 
		
		updateGameSounds(); 
		System.out.println("End of Update Call. ");
	}
    
    // Goal to Manage Game Related Things: Scores, Ball Managemn
    public void updateGameworldPlay()
    {
    	// The first client's ball location will be the one that is sent to all clients.
    	// This is to ensure a syrchonized game state. Can be improved toward complete server control.
    	
    }

	// Updates the player's elapsed time rate for movement.
	private void generateDeltaTime(float currentTime) 
	{
		if (timeCount == false) // Record the initial time
		{
			time1 = currentTime;
			timeCount = true;
		}
		else if (timeCount == true) // Determine the time passed between updates.
		{
			time2 = elapsTime/1000.0f;
			elapsedTimeHolder = time2 - time1;
			timeCount = false;
		}
	}

	// Sets up ProtcolClient for client so that client can talk to server. 
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
		{   System.out.println("IO Exception. "); e.printStackTrace();   }
		if (protClient == null)
		{   System.out.println("ProtocolClient Null - missing protocol host");   }
		else
		{   // ask client protocol to send initial join message
		    // to server, with a unique identifier for this client
			System.out.println("Sending JOIN msg to server thru Protocol Client");
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
	public void setIsConnected(boolean connectStatus) 
	{
		// Info returned from the UDP Client manager.
		isClientConnected = connectStatus;
	}

	// * Assuming we are making a one view person game with networking, 
	// There will be only consistent player to refer to .
	// For now, I'm passing localDolphin pos. 
	public Vector3f getPlayerPosition() 
	{
		// return DolphinPos.
    	SceneNode clientN = getEngine().getSceneManager().getSceneNode("clientModelNode");
		return (Vector3f) clientN.getLocalPosition();
	}

	// Sets up the Model Mesh, Skeleton, and Animation(s). Setup: MayaKnight01.
	public void initalizeTestModel(String mainName) throws IOException
	{
		// load skeletal entity – in this case it is an avatar
		// parameters are: entity name, mesh file, skeleton file
		SceneManager sm = getEngine().getSceneManager(); 
		SkeletalEntity mSkeletonE = sm.createSkeletalEntity(mainName, "Client_Mesh02.rkm", "Client_Model02_Skeleton.rks");
		
		if (mSkeletonE == null)
		{
			System.out.println("SkeletalEntity is NULL.");
		}
		else 
		{
			System.out.println("SkeletalEntity Info: " + mSkeletonE.toString());
			System.out.println("SkeletalEntity Name Info: " + mSkeletonE.getName());
			System.out.println("SkeletalEntity Mesh Info: " + mSkeletonE.getMesh().getName());
		}

		// loading its texture in the standard way
		Texture tex12 = sm.getTextureManager().getAssetByPath("Client_UV_Model02.png");
		TextureState tstate12 = (TextureState) sm.getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
		tstate12.setTexture(tex12);
		mSkeletonE.setRenderState(tstate12);
		
		// load the model's animations
		mSkeletonE.loadAnimation("handsWave", 
				"Client_Model02_HandsWave_Animation.rka"); // replace names. 
		mSkeletonE.loadAnimation("walkMovement", 
				"Client_Model02_Walk_Animation.rka");

		// attach the skeletal entity to a scene node
		SceneNode modSkeletonN = sm.getRootSceneNode().createChildSceneNode(mainName + "Node");
		modSkeletonN.attachObject(mSkeletonE);
		modSkeletonN.setLocalPosition(0.0f, 0.5f, 2.5f);
	}

	// Put Animations to be updated constantly in this function
	private void updateAnimations()
	{
		SkeletalEntity manSE = (SkeletalEntity) getEngine().getSceneManager().getEntity("clientModel");
		// update the animation
		if (manSE != null)
		{   manSE.update();   }
		else
		{
			System.out.println("Animation NULL.");
		}
	}
	
	public boolean getWalkAnimationStatus()
	{   return initWalkAnimation;   }
	public boolean getHandsUpAnimationStatus()
	{   return initHandsWaveAnimation;   }
	public void setWalkAnimationStatus(boolean givenInput)
	{   initWalkAnimation = givenInput;   }
	public void setHandsUpAnimationStatus(boolean givenInput)
	{   initHandsWaveAnimation = givenInput;   }
	
	public void doWalkAnimation()
	{ 
		if (initWalkAnimation)
		{
			SkeletalEntity manSE = (SkeletalEntity) getEngine().getSceneManager().getEntity("clientModel");
			manSE.stopAnimation(); // Stop Current Animation (if Any)
			manSE.playAnimation("walkMovement", 0.5f, LOOP, 0);
			initHandsWaveAnimation = false; // Once playing, set other animations status to false.
		}
	}
	public void doHandsUp()
	{ 
		if (initHandsWaveAnimation)
		{
			SkeletalEntity manSE = (SkeletalEntity) getEngine().getSceneManager().getEntity("clientModel");
			manSE.stopAnimation(); // Stop Current Animation (if Any)
			manSE.playAnimation("handsWave", 0.5f, LOOP, 0);
			initWalkAnimation = false; 
			initHandsWaveAnimation = false;
		}
	}
	
	
	
	// Add Ghost avatar to client game-world. parameters contain unique ghost ID, and position. 
	public void addGhostAvatarToGameWorld(GhostAvatar avatar) throws IOException
	{ 
    	SceneManager sm = getEngine().getSceneManager();

		if (avatar != null)
		{ 
			Entity ghostE = sm.createEntity("Ghost_ID:" + avatar.obtainGhostID().toString(), "MayaKnight-Blender.obj");
			ghostE.setPrimitive(Primitive.TRIANGLES);
			
			SceneNode ghostN = sm.getRootSceneNode().createChildSceneNode(ghostE.getName() + ":Node");
			ghostN.attachObject(ghostE);
			ghostN.setLocalPosition(avatar.obtainGhostPosition());
			//ghostN.rotate(Degreef.createFrom(180.0f), ghostN.getLocalPosition()); // Trying to position the dolphin to face -z axis
			avatar.setGhostNode(ghostN);
			avatar.setGhostEntity(ghostE);
			
			TextureManager tm = getEngine().getTextureManager();
	        Texture mainTexture = tm.getAssetByPath("blender_MKnight_UV_texture.png"); // CubModelUV.png - Dolphin_HighPolyUV.png
	        RenderSystem rs = sm.getRenderSystem();
	        TextureState state = (TextureState) 
	        		rs.createRenderState(RenderState.Type.TEXTURE);
	        state.setTexture(mainTexture);
	        ghostE.setRenderState(state);
	        
			System.out.println("LocalGame -> Ghost Creation Name: " + ghostN.getName());
		} 
	}
	
	// Searches and updates the local ghost avatar based on given info. 
	public void updateGhostAvatar(UUID ghostID, Vector3f newPos)
	{
    	String GhostNodeName = "Ghost_ID:" + ghostID.toString() + ":Node";
    	SceneManager sm = getEngine().getSceneManager();
    	SceneNode oldGhost = sm.getSceneNode(GhostNodeName);

    	//System.out.println("* * * Trying to reposition Ghost. Name: " + GhostNodeName);

    	if (oldGhost != null)
    	{
        	oldGhost.setLocalPosition(newPos); // Set node's world position 
    	}
    	else
		{   System.out.println("* * * Error in MyGame.updateGhostAvatar - OldGhost => Null");   }
	}

	// Updates given Ghost via ID by rotation/yaw. moveIndictor format: rotateU/D, or yawL/R.
	// Last char determine which direction to pitch or yaw
	public void updateRotateGhostAvatar(UUID ghostID, String moveIndictor)
	{
		char direction = moveIndictor.charAt(moveIndictor.length() - 1); 
		float standardRate = (direction == 'u' || direction == 'l') ? 1.0f : -1.0f;

		String givenID = ghostID.toString();
    	SceneManager sm = getEngine().getSceneManager();
    	SceneNode oldGhost = sm.getSceneNode("Ghost_ID:" + givenID + ":Node");

    	Vector3 globalY = Vector3f.createFrom(0.0f, 1.0f, 0.0f); // Yaw variables.  
		Matrix3 matRot; 

    	Angle turnRate = Degreef.createFrom(elapsedTimeHolder + standardRate); // Based on elapsed time.

    	if (moveIndictor.contains("pitch"))
    	{
    		oldGhost.pitch(turnRate);   // rate -> turn directions based on passed value by client who sent move update
    	}
    	else if (moveIndictor.contains("yaw")) // May need to change this. 
    	{
    		matRot = Matrix3f.createRotationFrom(Degreef.createFrom(turnRate), globalY);
    		oldGhost.setLocalRotation(matRot.mult(oldGhost.getWorldRotation()));
    	}
	}
			
	public void removeGhostAvatarFromGameWorld(GhostAvatar avatar)
	{ 
		if(avatar != null) 
		{   gameObjectsToRemove.add(avatar.obtainGhostID());   }
	}
	
	// Adds given NPC Ghost to the world. 
	// Create Ghost NPC in game world. NPC controlled by Game Server. 
	public void addGhostNPCtoGameWorld(GhostNPC newNPC) 
	{
		System.out.println("Client -> addGhostNPCtoGameWorld. ID: " + newNPC.obtainID());
		Vector3f pos = (Vector3f) newNPC.getPosition();
		SceneManager sm = getEngine().getSceneManager();
		
		try 
		{
			Entity npcE = sm.createEntity("NPC_" + newNPC.obtainID(), "MayaKnight-Blender.obj"); // dolphinHighPoly.obj-BasicModelUVMapping.obj
	        npcE.setPrimitive(Primitive.TRIANGLES);
	        
	        SceneNode npcNode = sm.getRootSceneNode().createChildSceneNode(npcE.getName() + "_Node");
	        npcNode.setLocalPosition(pos.x(), pos.y()+1.0f, pos.z());
	        npcNode.scale(0.10f, 0.10f, 0.10f);
	        npcNode.attachObject(npcE);
		}
		catch (IOException e) 
		{   
			System.out.println("Error Creating Ghost NPC in Client world.");
			e.printStackTrace();   
		}
	}
	
	// Updates the given ghost NPC with new position. 
	public void updateNPCGhostAvatar(int id, Vector3 pos) 
	{
		// NPC Ghost Node name formate: NPC_" + id + "_Node"
		SceneManager sm = getEngine().getSceneManager();
    	SceneNode npcGhost = sm.getSceneNode("NPC_"+ id + "_Node");
    	
    	if (npcGhost != null)
    	{
    		npcGhost.setLocalPosition(pos.x(), pos.y(), pos.z());
    	}
	}

	// Returns an initial location for the requested NPC. 
	// Goal is to Create an NPC for each client opposite to them.
	public Vector3f getGhostPosition_NPC() 
	{
		Vector3f clientPos = getPlayerPosition(); // Get Client Pos. 
		Vector3f npcPos;
		if (clientPos.z() > 0)
		{   npcPos = (Vector3f) Vector3f.createFrom(clientPos.x(), clientPos.y(), -clientPos.z());    }
		else 
		{   npcPos = (Vector3f) Vector3f.createFrom(clientPos.x(), clientPos.y(), clientPos.z());    }
		
		return npcPos;
	}

	public Vector3f obtainBallLocation() 
	{   // Returns Ball Node Location. 
    	SceneNode ballNode = getEngine().getSceneManager().getSceneNode("gameBallNode");
		return (Vector3f) ballNode.getLocalPosition();
	}
	
	public void updateServerBallPosition() 
	{
		// Constantly called to keep ball position updated. ballPosition
    	SceneNode ballNode = getEngine().getSceneManager().getSceneNode("gameBallNode");

		Vector3f curPos = (Vector3f) ballNode.getLocalPosition(); 
		
		if (determineChangeVectors(curPos, ballPosition))
		{ // If there is a change then update local ref and server. 
			ballPosition = curPos; 
			protClient.sendBallPositionToServer();
		}
	}
	
	// Determines if there was a change in ball position. 
	public boolean determineChangeVectors(Vector3f curPos, Vector3f oldPos) 
	{
		boolean axisX = (curPos.x() == oldPos.x()) ? false : true;
		boolean axisY = (curPos.y() == oldPos.y()) ? false : true;
		boolean axisZ = (curPos.z() == oldPos.z()) ? false : true;
		
		if (axisX || axisY || axisZ)
		{    return true;    } 
		else 
		{    return false;   }
	}

	// Repositions starting positions for client start up 
	// Meant for multiplayer mostly. 
	public void updateClientPosition(int serverClientCount) 
	{
		// clientPos1/2.
    	SceneNode clientN = getEngine().getSceneManager().getSceneNode("clientModelNode");
		
		if (serverClientCount == 1)
		{
			clientN.setLocalPosition(clientPos1.x(), clientPos1.y(), clientPos1.z());
		}
		else if (serverClientCount == 2)
		{
			clientN.setLocalPosition(clientPos2.x(), clientPos2.y(), clientPos2.z());
		}
		else 
		{
			clientN.setLocalPosition(clientPos1.x(), clientPos1.y(), clientPos1.z());
		}
		
	}
	
}
