package myGameEngine;

import java.awt.AWTException;
import java.awt.Canvas;
import java.awt.Cursor;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;

import javax.swing.ImageIcon;

import net.java.games.input.Controller;
import ray.input.InputManager;
import ray.input.action.AbstractInputAction;
import ray.input.action.Action;
import ray.rage.rendersystem.RenderSystem;
import ray.rage.rendersystem.RenderWindow;
import ray.rage.rendersystem.Viewport;
import ray.rage.scene.Camera;
import ray.rage.scene.SceneNode;
import ray.rml.Vector3;
import ray.rml.Vector3f;

public class Camera3Pcontroller// implements MouseListener, MouseMotionListener, MouseWheelListener
{ 
	private Camera camera; //the camera being controlled
	
	private Robot myCog; // Additional variables to manage mouse movement
	private Canvas canvas; 
	private boolean isRecentering; // indicates the robot is in action.
	private boolean turnOffRobot = false; 
	
	private RenderWindow renderWindow;
	private RenderSystem mRenderSystem;
	private PointerInfo mouseLoc; 
	private float prevMouseX, prevMouseY, curMouseX, curMouseY;
	private float centerX, centerY;
	
	
	private String controllerType;
	private SceneNode cameraN; //the node the camera is attached to
	private SceneNode target; //the target the camera looks at
	private float cameraAzimuth; //rotation of camera around Y axis
	private float cameraElevation; //elevation of camera above target
	private float radias; //distance between camera and target
	private float elapsedTime = 0.0f;
	private Vector3 targetPos; //target�s position in the world
	private Vector3 worldUpVec;
    ArrayList<Controller> controllers; // Get list of controllers
    
    Action orbitAAction, orbitRAction, orbitEAction;

	
	public Camera3Pcontroller(Camera cam, SceneNode camN, 
			                  SceneNode targ, String controllerPref, 
			                  InputManager im)
	{ 
		controllerType = controllerPref;
		camera = cam;
		cameraN = camN;
		target = targ;
		cameraAzimuth = 360.0f; // start from BEHIND and ABOVE the target
		cameraElevation = 20.0f; // elevation is in degrees
		radias = 2.0f;
		worldUpVec = Vector3f.createFrom(0.0f, 1.0f, 0.0f);
		setupInput(im);//, controllerName);
		updateCameraPosition();
	}
	
	public Camera3Pcontroller(SceneNode camN, SceneNode targ, 
			String controllerPref, InputManager im, RenderSystem r, 
			RenderWindow w)
	{ 
		if (w == null)
		{  System.out.println(" No window given");  }
		if (r == null)
		{  System.out.println(" No renderSystem given");  }
		renderWindow = w;
		mRenderSystem = r;
		
		controllerType = controllerPref;
		cameraN = camN; //camera = cam;
		target = targ;
		cameraAzimuth = 360.0f; // start from BEHIND and ABOVE the target
		cameraElevation = 20.0f; // elevation is in degrees
		radias = 2.0f; 
		worldUpVec = Vector3f.createFrom(0.0f, 1.0f, 0.0f);
		setupInput(im);//, controllerName);
		initMouseMode(); // Begin Mouse control.

		updateCameraPosition();
	}

	public Camera3Pcontroller(Camera cam, SceneNode camN, SceneNode playerAvatar) {
		// Player Class related constructor
	}

	// Updates camera position: computes azimuth, elevation, and distance
	// relative to the target in spherical coordinates, then converts those
	// to world Cartesian coordinates and setting the camera position
	public void updateCameraPosition()
	{ 
		double theta = Math.toRadians(cameraAzimuth); // rot around target
		double phi = Math.toRadians(cameraElevation); // altitude angle
		double x = radias * Math.cos(phi) * Math.sin(theta);
		double y = radias * Math.sin(phi);
		double z = radias * Math.cos(phi) * Math.cos(theta);
		
		cameraN.setLocalPosition(Vector3f.createFrom
				((float)x, (float)y, (float)z).add(target.getWorldPosition()));
		cameraN.lookAt(target, worldUpVec);
		
		if ((controllerType == "keyboard") && !turnOffRobot)
		{
			mouseManagement(); // Maintains and recenters the Mouse.
		}
	}
	
	
	// Note: Should add a variable that separates control.
	// Keyboard inputs should go to one player.
	// Gamepad inputs should go to second player.
	private void setupInput(InputManager im)//, String cn)
	{ 
	    controllers = im.getControllers(); // Get list of controllers
	    Controller inputComponent;                               // Manage controllers
	    int i;													 // Loop count.

		Action orbitAAction = new OrbitAroundAction();
		Action orbitRAction = new OrbitRadiusAction();
		Action orbitEAction = new OrbitElevationAction();
		Action disableCogAction = new DisableRobotAction();
		
		for (i = 0; i < controllers.size(); i++)
	    {
	    	inputComponent = controllers.get(i);
	    	
	    	if (((inputComponent.getType() == Controller.Type.KEYBOARD) || (inputComponent.getType() == Controller.Type.MOUSE)) 
	    			&& (controllerType == "keyboard"))
	    	{
	    		// Give input action associations
	    		im.associateAction(inputComponent, 
	    				net.java.games.input.Component.Identifier.Key.SPACE,  
	    				disableCogAction,
	    				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
	    		im.associateAction(inputComponent, 
	    				net.java.games.input.Component.Identifier.Axis.X,  
	    				orbitAAction,
	    				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
	    		im.associateAction(inputComponent, 
	    				net.java.games.input.Component.Identifier.Axis.Z, 
	    				orbitRAction,
	    				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
	    		im.associateAction(inputComponent, 
	    				net.java.games.input.Component.Identifier.Axis.Y, 
	    				orbitEAction,
	    				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);	
	    		im.associateAction(inputComponent, 
	    				net.java.games.input.Component.Identifier.Key.J, 
	    				orbitAAction,
	    				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
	    		im.associateAction(inputComponent, 
	    				net.java.games.input.Component.Identifier.Key.L, 
	    				orbitAAction,
	    				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
	    		im.associateAction(inputComponent, 
	    				net.java.games.input.Component.Identifier.Key.I, 
	    				orbitRAction,
	    				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
	    		im.associateAction(inputComponent, 
	    				net.java.games.input.Component.Identifier.Key.K, 
	    				orbitRAction,
	    				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
	    		im.associateAction(inputComponent, 
	    				net.java.games.input.Component.Identifier.Key.U, 
	    				orbitEAction,
	    				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
	    		im.associateAction(inputComponent, 
	    				net.java.games.input.Component.Identifier.Key.O, 
	    				orbitEAction,
	    				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
	    		// Note: add key controls.
	    		// similar input set up for OrbitRadiasAction, OrbitElevationAction */
	    	}
	    	else if ( (inputComponent.getType() == Controller.Type.GAMEPAD || 
	    			 inputComponent.getType() == Controller.Type.STICK) && (controllerType == "gamepad") )
	    	{   //  Give input action associations for the Controllers. 
	    		im.associateAction(inputComponent, 
	    				net.java.games.input.Component.Identifier.Button._1, 
	    				orbitAAction,
	    				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
	    		im.associateAction(inputComponent, 
	    				net.java.games.input.Component.Identifier.Button._2, 
	    				orbitAAction,
	    				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
	    		im.associateAction(inputComponent, 
	    				net.java.games.input.Component.Identifier.Button._3, 
	    				orbitRAction,
	    				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
	    		im.associateAction(inputComponent, 
	    				net.java.games.input.Component.Identifier.Button._4, 
	    				orbitRAction,
	    				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);	
	    		im.associateAction(inputComponent, 
	    				net.java.games.input.Component.Identifier.Button._5, 
	    				orbitEAction,
	    				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
	    		im.associateAction(inputComponent, 
	    				net.java.games.input.Component.Identifier.Button._6, 
	    				orbitEAction,
	    				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);	
	    	}
	    };
	}
	
	private void initMouseMode() 
	{	
		Viewport v = renderWindow.getViewport(0); // Viewport number DOUBLE CHECK
		int left = renderWindow.getLocationLeft(); 
		int top = renderWindow.getLocationTop(); 
		int width = v.getActualScissorWidth();
		int height = v.getActualHeight();
		centerX = left + (width / 2);
		centerY = top + (height / 2);
		isRecentering = false; // To center itself at the start.
		
		
		try // Note that some platforms may not supprt Robot class
		{ myCog = new Robot(); } catch (AWTException ex)
		{ throw new RuntimeException("Couldn't create Robot!"); }
		
		recenterMouse(); // initialize mouse to center itself at the start.
		
		prevMouseX = centerX; // 'prevMouse' defines the initial
		prevMouseY = centerY; // mouse position
		
		// also change the cursor
		Image faceImage = new ImageIcon("./assets/images/rage-logo.png").getImage();
		Cursor faceCursor = Toolkit.getDefaultToolkit().
				createCustomCursor(faceImage, new Point(0,0), "FaceCursor");
		canvas = mRenderSystem.getCanvas();
		canvas.setCursor(faceCursor);
		
	}
	
	private void recenterMouse() 
	{
		// use the robot to move the mouse to the center point.
		// Note that this generates one MouseEvent.
		Viewport v = renderWindow.getViewport(0);
		int left = renderWindow.getLocationLeft();
		int top = renderWindow.getLocationTop();
		int widt = v.getActualScissorWidth();
		int hei = v.getActualScissorHeight();
		centerX = left + widt/2;
		centerY = top + hei/2;
		isRecentering = true;
		myCog.mouseMove((int)centerX, (int)centerY);
	}
	
	
	public void mouseManagement() 
	{
		Point mouseLoc = canvas.getMousePosition();

		//if robot is recentering and the MouseEvent location is in the center,
		// then this event was generated by the robot
		if (mouseLoc != null)
		{
			if (isRecentering && centerX == (float) mouseLoc.getX() && centerY == (float) mouseLoc.getY())  //  centerX == e.getXOnScreen() && centerY == e.getYOnScreen())
			{ isRecentering = false; } // mouse recentered, recentering complete
			else
			{
				prevMouseX = curMouseX;
				prevMouseY = curMouseY;
				// tell robot to put the cursor to the center (since user just moved it)
				recenterMouse();
				prevMouseX = centerX; //reset prev to center
				prevMouseY = centerY;	
			}
		}
	}

private class DisableRobotAction extends AbstractInputAction
{   // Moves the camera around the target (changes camera azimuth).	
	public void performAction(float time, net.java.games.input.Event evt)
	{ 
		// Enable/Disable Re-centering of Mouse.
		turnOffRobot = (turnOffRobot == false) ? true : false; 
	} 
}
	
private class OrbitAroundAction extends AbstractInputAction
{   // Moves the camera around the target (changes camera azimuth).	
	public void performAction(float time, net.java.games.input.Event evt)
	{ 
		float rotAmount=0.0f, deltaTime = time - elapsedTime;
		String command = evt.getComponent().getName(); 
		char letterCommand = command.charAt(0);
		char charCommand = '0'; // Set charCommand to nothing that will match.
		
		//System.out.println("command? " + command);
		//System.out.println("commandLength? " + command.length());
		//System.out.println("Value? " + evt.getValue());
		
		if (command.length() > 3) // if command is larger than a single digit; then its a button.
		{
			charCommand = command.charAt(command.length() - 1); 
		}
		
		if ( ((evt.getValue() < -0.2) || (letterCommand == 'J')) || (charCommand == '1'))
			{ rotAmount=-0.5f; }
		else
		{ 
			if (((evt.getValue() > 0.2) || (letterCommand == 'L')) || (charCommand == '2'))
				{ rotAmount=0.5f; }
			else
				{ rotAmount=0.0f; }
		}
				
		cameraAzimuth += rotAmount;     
		cameraAzimuth = cameraAzimuth % 360; // Unrestricted Orbiting!***
		elapsedTime = time;
		updateCameraPosition();
	} 
}

private class OrbitRadiusAction extends AbstractInputAction
{ // Moves the camera closer to the target (changes camera Radius).	

	public void performAction(float time, net.java.games.input.Event evt)
	{ 
		float radAmount, deltaTime = time - elapsedTime;
		float eventValue = evt.getValue();  
		String command = evt.getComponent().getName(); // Get event name.
		
		float radiusInnerLimit = 0.5f;  // Variables to check zoom radius limits. 
		float radiusOuterLimit = 2.0f; 
		
		char charCommand = '0'; // Set charCommand to nothing that will match.
		char letterCommand = command.charAt(0);

		if (command.length() > 3) // if command is larger than a single digit; then its a button/key.
		{
			charCommand = command.charAt(command.length() - 1); 
		}
		
		if (((eventValue < -0.2f) || (letterCommand == 'I')) || (charCommand == '3'))
			{ radAmount=-0.2f; }
		else
		{ 
			if (((eventValue > 0.2f) || (letterCommand == 'K')) || (charCommand == '4'))
				{ radAmount=0.2f; }
			else
				{ radAmount=0.0f; }
		}
		
		//System.out.println("radAmount: " + radAmount);
		
		radAmount = ((radias + radAmount) < radiusInnerLimit) ? 0.0f : radAmount; // Boundary Checking
		radAmount = ((radias + radAmount) > radiusOuterLimit) ? 0.0f : radAmount;
		
		radias += radAmount;
		elapsedTime = time;
		updateCameraPosition();
		//System.out.println("OrbitController - OrbitRadiusAction");
	} 
}

private class OrbitElevationAction extends AbstractInputAction
{ // Moves the camera's height position (changes camera altitude).
	public void performAction(float time, net.java.games.input.Event evt)
	{ 
		float heightAmount, deltaTime = time - elapsedTime;
		String command    = evt.getComponent().getName(); // Get event name.
		char letterCommand = command.charAt(0);

		float  eventValue = evt.getValue(); 
		
		float heightUpperLimit = 40.0f;  // Variables to check zoom radius limits. 
		float heightLowerLimit = -3.0f; 
		
		char charCommand = '0'; // Set charCommand to nothing that will match.
		
		if (command.length() > 3) // if command is larger than a single digit; then its a button.
		{
			charCommand = command.charAt(command.length() - 1); 
		}
		
		if (((eventValue < -0.2f) || (letterCommand == 'U')) || (charCommand == '5'))
			{ heightAmount=-0.5f; }
		else
		{ 
			if (((eventValue > 0.2f) || (letterCommand == 'O')) || (charCommand == '6'))
				{ heightAmount=0.5f; }
			else
				{ heightAmount=0.0f; }
		}
		
		//System.out.println("heightAmount? " + heightAmount);
		//System.out.println("cameraElevationAmount? " + cameraElevation);

		heightAmount = ((cameraElevation + heightAmount) > heightUpperLimit) ? 0.0f : heightAmount; // Boundary Checking
		heightAmount = ((cameraElevation + heightAmount) < heightLowerLimit) ? 0.0f : heightAmount;
		
		cameraElevation += heightAmount;
		elapsedTime = time;
		updateCameraPosition();
	} 
}


}

