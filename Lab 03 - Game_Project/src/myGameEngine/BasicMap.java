/// BASIC MAP
// Things needed for this class to work correctly:
// In CODE:
//  Needs to be instantiated in the game with a "new BasicMap(string, string, string)" call.
// The parameters are the name of the map (used differentiate between different maps), 
// the name of the height map, and the name of the texture used for the map.
// Inside the overloaded setUpScene() function, a call to "createMap(Engine, SceneManager)"
// will create the map and add it to the scene.
// Oct26,2020 - Just a basic shell that puts the code to generate a tessellated map object
//             in one place. Contains a function to get the height at a specified location
//             by passing a vector containing the object's world coords.

package myGameEngine;

import ray.rage.Engine;
import ray.rage.scene.*;
import ray.rml.Vector3;

public class BasicMap 
{
   String mapName;	
   String heightMap;
   String textureName;
   
   Tessellation tessEntity;
   
   public BasicMap(String mN, String htMap, String texture)
   {
	  mapName = mN;
	  heightMap = htMap;
	  textureName = texture;
   }
   
   public void createMap(Engine engine, SceneManager sm)
   {
	  tessEntity = sm.createTessellation(mapName + "Map" + "Entity", 6);// subdivisions per patch:  min=0, try up to 32
	  tessEntity.setSubdivisions(8f);
	  SceneNode tessN = sm.getRootSceneNode().createChildSceneNode(mapName + "Map" + "Node");
	  tessN.attachObject(tessEntity);  
	  
	  // to move it, note that X and Z must BOTH be positive OR negative
	  // tessN.translate(Vector3f.createFrom(-6.2f, -2.2f, 2.7f));
	  // tessN.yaw(Degreef.createFrom(37.2f));
	  
	  tessN.scale(50, 50, 50);  
	  tessEntity.setHeightMap(engine, heightMap);
	  tessEntity.setTexture(engine, textureName);
	  
	  //tessE.setNormalMap();
   }
   
   public float getHeight(Vector3 avatarWorldPos)
   {
	  return tessEntity.getWorldHeight(avatarWorldPos.x(), avatarWorldPos.z());
   }
}
