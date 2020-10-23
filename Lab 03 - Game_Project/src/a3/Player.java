package a2;

import ray.rage.scene.*;
import ray.rml.*;
import myGameEngine.Camera3Pcontroller;
import ray.rage.game.*;

public class Player 
{
   private SceneNode playerAvatar = null;
   private Camera3Pcontroller orbitController = null;
   private float playerSpeed;
   private int points;
   
   private float range = 1.0f;
   private float rotationMod = 20.0f; //a modifier to the speed used in rotation

//CONSTRUCTORS
//Oct16,2020 - Both ctrs set playerSpeed to 1.0f, one also takes a SceneNode
//            to associate a player avatar with a player. This may not be used,
//            however, because the player will most likely be instantiated
//            before the SceneNode exists.
   public Player()
   {
	   playerSpeed = 0.008f;
	   points = 0;
   }
   
   public Player(SceneNode player)
   {
	   playerAvatar = player;
	   playerSpeed = 0.001f;
	   points = 0;
   }
   
//SETTERS AND GETTERS
   public void setAvatar(SceneNode player) {   playerAvatar = player;   }
   public void setSpeed(float speed) {   playerSpeed = speed;   } 
   public SceneNode getAvatar() {   return playerAvatar;   }  
   public Vector3 getLocation() { return playerAvatar.getWorldPosition(); }
   public float getSpeed()  {   return playerSpeed;   }
   public int getPointValue() {   return points;   }
   public String getPoints() {   return Integer.toString(points);   }
   public float getRange() {   return range;   }
   public String getWhich() {   return playerAvatar.getName();   }
   
   public void setUpOrbitCtrl(Camera cam, SceneNode camN) 
   {   
	   orbitController = new Camera3Pcontroller(cam, camN, playerAvatar); 
   }
   
   public void updateOrbit()
   {
	   orbitController.updateCameraPosition();
   }
   
   public void incrementPoints() { ++points;  }

//MOVEMENT
//Oct16,2020 - Moved the code that does the actual movement to the Player class.
//            The goal here is to abstract some of the code away from the Action
//            classes so the code works better with multiple players providing multiple
//            inputs for the same task.
//             Added an input for these actions to attach a modifier to stop the unfair
//            advantage that is gained when the controller moves diagonally.
   public void moveForward(float time, float mod)
   {
	   float moveSpd = playerSpeed * time * mod; 
       Vector3f v = (Vector3f)playerAvatar.getLocalForwardAxis();
       Vector3f p = (Vector3f)playerAvatar.getLocalPosition();
       Vector3f p1 = (Vector3f)Vector3f.createFrom(moveSpd*v.x(), moveSpd*v.y(), moveSpd*v.z());
       Vector3f p2 = (Vector3f)p.add((Vector3)p1);
       playerAvatar.setLocalPosition((Vector3)Vector3f.createFrom(p2.x(),p2.y(),p2.z()));
       updateOrbit();
   }
   
   public void moveBackward(float time, float mod)
   {
	   float moveSpd = -(playerSpeed * time * mod);
       Vector3f v = (Vector3f)playerAvatar.getLocalForwardAxis();
       Vector3f p = (Vector3f)playerAvatar.getLocalPosition();
       Vector3f p1 = (Vector3f)Vector3f.createFrom(moveSpd*v.x(), moveSpd*v.y(), moveSpd*v.z());
       Vector3f p2 = (Vector3f)p.add((Vector3)p1);
       //if(game.rangeCheck(p2))
       playerAvatar.setLocalPosition((Vector3)Vector3f.createFrom(p2.x(),p2.y(),p2.z()));
       updateOrbit();
   }
   
   public void moveLeft(float time, float mod)
   {
	   float moveSpd = playerSpeed * time * mod;      
       Matrix3 matRot = Matrix3f.createRotationFrom(Degreef.createFrom(moveSpd * rotationMod), orbitController.getWorldUpVec());
       playerAvatar.setLocalRotation(matRot.mult(playerAvatar.getWorldRotation()));
       
       orbitController.updateCameraAzimuth(Degreef.createFrom(moveSpd * rotationMod).valueDegrees());
       orbitController.updateCameraPosition();
   }
   
   public void moveRight(float time, float mod)
   {
	   float moveSpd = -(playerSpeed * time * mod);     
       Matrix3 matRot = Matrix3f.createRotationFrom(Degreef.createFrom(moveSpd * rotationMod), orbitController.getWorldUpVec());
       playerAvatar.setLocalRotation(matRot.mult(playerAvatar.getWorldRotation()));  
       
       orbitController.updateCameraAzimuth(Degreef.createFrom(moveSpd * rotationMod).valueDegrees());
       orbitController.updateCameraPosition();
       
   }
   
   public void orbitAround(float eventVal)
   {
	   float rotAmount;      
       if (eventVal < 0.0f)        
       { 
      	 rotAmount= - 1.2f; 
       }          
       else          
       { 
      	 if (eventVal > 0.2)   
      	 { 
      		 rotAmount = 1.2f; 
      	 }          
      	 else          
      	 {
      		 rotAmount = 0.0f; 
      	 }      
      }       
      orbitController.updateCameraAzimuth(rotAmount);  
      orbitController.updateCameraPosition();
   }
   
   public void orbitRadias(float eventVal)
   {
	  float zoomAmount;      
      if (eventVal < 0.0f)        
      { 
         zoomAmount= - 0.2f; 
      }          
      else          
      { 
         if (eventVal > 0.2f)    
      	   zoomAmount = 0.2f;           
         else          
      	   zoomAmount = 0.0f;      
      }
      if(orbitController.getRadias() + zoomAmount <= 8.0f  && orbitController.getRadias() + zoomAmount > 0.5f)
      {
	     orbitController.updateRadias(zoomAmount);		  
	     orbitController.updateCameraPosition();
      }
   }
   
   public void orbitElevation(float eventVal)
   {
	   float liftAmount;      
       if (eventVal < 0.0f)        
       { 
    	   liftAmount = -1.2f; 
       }          
       else          
       { 
      	 if (eventVal > 0.2f)   
      	 { 
      		liftAmount = 1.2f; 
      	 }          
      	 else          
      	 {
      		liftAmount = 0.0f; 
      	 }      
      } 
	   orbitController.updateElevation(liftAmount);
	   orbitController.updateCameraPosition();
   }
   
}
