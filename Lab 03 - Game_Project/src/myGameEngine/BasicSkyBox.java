/// SKY BOX
// Things needed for this class to work correctly:
// In CODE:
//  Needs to be instantiated in the game with a "new BasicSkyBox(engine, string)" call.
// The parameters are the game engine and a string containing the name of the box.
// Inside the overloaded setUpScene() function, a call to ".setUpSkyBox()" will
// create the box and add it to the scene.
// In FILES:
//  The file "rage.properties" inside assets/config needs to contain the correct file
// path for the skybox and it's textures.
// Oct26,2020 - As we develop the game further, I imagine the string sent in to create
//             the box could be also used to designate which asset path to follow. Not
//             sure if it will end up being used, but for now it works.

package myGameEngine;

import java.awt.geom.AffineTransform;
import java.io.*;

import ray.rage.*;
import ray.rage.asset.texture.*;
import ray.rage.scene.*;
import ray.rage.util.*;

public class BasicSkyBox
{
   private SkyBox skyBox;
   private String boxName;
   
   private Engine engine; //ptr to the engine
  
   private Texture front;
   private Texture back;
   private Texture right;
   private Texture left;
   private Texture top;
   private Texture bottom;

   public BasicSkyBox(Engine eg, String nm)
   {
	  engine = eg;
	  boxName = nm;
   }
   
   
   public void setUpSkyBox() throws IOException
   {
	   Configuration config = engine.getConfiguration();	
	   TextureManager textureMgr = engine.getTextureManager();
	   
	   textureMgr.setBaseDirectoryPath(config.valueOf("assets.skyboxes.path"));
	   
	   front = textureMgr.getAssetByPath("front.jpeg");
	   back = textureMgr.getAssetByPath("back.jpeg");
	   right = textureMgr.getAssetByPath("right.jpeg");
	   left = textureMgr.getAssetByPath("left.jpeg");
	   top = textureMgr.getAssetByPath("top.jpeg");
	   bottom = textureMgr.getAssetByPath("bottom.jpeg");
	   
	   textureMgr.setBaseDirectoryPath(config.valueOf("assets.textures.path"));
	   
	   applyTransform();
	   createSkyBox();
   }
   
   private void applyTransform()
   {
      AffineTransform xform = new AffineTransform();
      xform.translate(0, front.getImage().getHeight()); //its a box, so all sides have
      xform.scale(1d, -1d);                             //same dimensions
      
      front.transform(xform);
      back.transform(xform);
      right.transform(xform);
      left.transform(xform);
      top.transform(xform);
      bottom.transform(xform);
   }
   
   private void createSkyBox()
   {
	   SceneManager sceneMgr = engine.getSceneManager();
	   skyBox = sceneMgr.createSkyBox(boxName);
	   
	   skyBox.setTexture(front, SkyBox.Face.FRONT);        
	   skyBox.setTexture(back, SkyBox.Face.BACK);        
	   skyBox.setTexture(left, SkyBox.Face.LEFT);        
	   skyBox.setTexture(right, SkyBox.Face.RIGHT);       
	   skyBox.setTexture(top, SkyBox.Face.TOP);        
	   skyBox.setTexture(bottom, SkyBox.Face.BOTTOM);       
	   
	   sceneMgr.setActiveSkyBox(skyBox);
   }
}
