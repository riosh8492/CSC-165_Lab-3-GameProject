package a3;

import java.awt.Color;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.vecmath.Vector3f;

import ray.rage.Engine;
import ray.rage.asset.material.Material;
import ray.rage.asset.texture.Texture;
import ray.rage.scene.ManualObject;
import ray.rage.scene.ManualObjectSection;
import ray.rage.scene.SceneManager;
import ray.rage.util.BufferUtil;
import ray.rage.rendersystem.Renderable.DataSource;
import ray.rage.rendersystem.shader.GpuShaderProgram;
import ray.rage.rendersystem.states.FrontFaceState;
import ray.rage.rendersystem.states.TextureState;
import ray.rage.rendersystem.states.RenderState;

public class SpaceObject 
{
	
	public SpaceObject()
	{
		// None needed at the moment
	}
	
	protected ManualObject obtainPrismObject(Engine eng, SceneManager sm, String name) throws IOException
	{
		ManualObject ship = sm.createManualObject(name);
    	ManualObjectSection shipSec = ship.createManualSection(name + "Section");
    	
    	ship.setGpuShaderProgram(sm.getRenderSystem().
    			getGpuShaderProgram(GpuShaderProgram.Type.RENDERING));
    	
    	float [] vertices = obtainVertices();
    	float [] texCoord = obtainTextureCoord();
    	float [] normals = obtainNormals();
    	int [] indices = obtainIndices();
    	
    	FloatBuffer vertBuf = BufferUtil.directFloatBuffer(vertices);
		FloatBuffer texBuf = BufferUtil.directFloatBuffer(texCoord);
		FloatBuffer normBuf = BufferUtil.directFloatBuffer(normals);
		IntBuffer indexBuf = BufferUtil.directIntBuffer(indices);
		
		shipSec.setVertexBuffer(vertBuf);
		shipSec.setTextureCoordsBuffer(texBuf);
		shipSec.setNormalsBuffer(normBuf);
		shipSec.setIndexBuffer(indexBuf);
		
		Texture tex = eng.getTextureManager().getAssetByPath("chain-fence.jpeg");
		TextureState texState = (TextureState)sm.getRenderSystem().
				createRenderState(RenderState.Type.TEXTURE);
		texState.setTexture(tex);
		FrontFaceState faceState = (FrontFaceState) sm.getRenderSystem().
		createRenderState(RenderState.Type.FRONT_FACE);
		
		Material mat = sm.getMaterialManager().getAssetByPath("default.mtl");
	    mat.setEmissive(Color.GREEN);
		
		//ship.setDataSource(DataSource.INDEX_BUFFER);
		ship.setRenderState(texState);
		ship.setRenderState(faceState);
		ship.setMaterial(mat);
		
		return ship;
	}
	
	public float[] obtainVertices()
	{
		float [] vertices = new float[]
		{
				-1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f, // Top Face  
				
				1.0f, 1.0f, 0.0f,    1.0f, -1.0f, 0.0f,  -1.0f, 1.0f, 0.0f,    // Body: 3 sides.
				-1.0f, 1.0f, 0.0f,   1.0f, -1.0f, 0.0f,   -1.0f, -1.0f, 0.0f,// Back side.
				
				-1.0f, 1.0f, 0.0f, -1.0f, -1.0f, 0.0f, 0.0f, 1.0f, 1.0f,  // Body: 3 sides.
				-1.0f, -1.0f, 0.0f, 0.0f, -1.0f, 1.0f, 0.0f, 1.0f, 1.0f, // Left sides.
				
				0.0f, 1.0f, 1.0f, 1.0f, -1.0f, 0.0f, 1.0f, 1.0f, 0.0f, // Body: 3 sides.
				0.0f, 1.0f, 1.0f, 0.0f, -1.0f, 1.0f, 1.0f, -1.0f, 0.0f, // Rightsides.
				
				
				 1.0f, -1.0f, 0.0f,  0.0f, -1.0f, 1.0f, -1.0f, -1.0f, 0.0f,// Bottom Face

		};

		return vertices;
	}
	public float[] obtainTextureCoord()
	{
		float [] texCoord = new float[]
		{
				0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f, // Top triangle.
				
				0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, // Back side
				0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, // 1 Side
				
				0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, // 1 Side
				0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, // left side
				
				1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, // 1 Side
				0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, // Right side
				
				0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f, // Bottom triangle.
		};
		return texCoord;
	}
	public float[] obtainNormals()
	{
		float[] classVertices = obtainVertices();
		float[] nPoints = new float[(classVertices.length / 3)];
		
		Vector3f QPoint = new Vector3f(0.0f, 0.0f, 0.0f);
		Vector3f RPoint = new Vector3f(0.0f, 0.0f, 0.0f);
		Vector3f SPoint = new Vector3f(0.0f, 0.0f, 0.0f);
		Vector3f resultPoint = new Vector3f(0.0f, 0.0f, 0.0f);
				
		// Determine the Normal for each vertex.
		for (int i = 0, k = 0; i < classVertices.length; i +=9, k+=3)
		{
			QPoint.setX(classVertices[i]);
			QPoint.setY(classVertices[i+1]);
			QPoint.setZ(classVertices[i+2]);
			
			RPoint.setX(classVertices[i+3]);
			RPoint.setY(classVertices[i+4]);
			RPoint.setZ(classVertices[i+5]);
			
			SPoint.setX(classVertices[i+6]);
			SPoint.setY(classVertices[i+7]);
			SPoint.setZ(classVertices[i+8]);
			
			RPoint.sub(QPoint); // R = R - Q
			SPoint.sub(QPoint); // S = S - Q
			
			resultPoint.setX(RPoint.getX() * SPoint.getX());
			resultPoint.setY(RPoint.getY() * SPoint.getY());
			resultPoint.setZ(RPoint.getZ() * SPoint.getZ());
			
			nPoints[k] = resultPoint.getX();
			nPoints[k+1] = resultPoint.getY();
			nPoints[k+2] = resultPoint.getZ();
		}
		
		return nPoints;
	}
	public int[] obtainIndices()
	{
		int [] indices = new int[] 
		{ 1, 4, 1, 2,5,2, 5,9,3, 4,8,4,
		  2, 5, 5, 3, 6, 6, 6,10,7, 5,9,8,
		  3, 6, 9, 1, 7, 10, 4, 11,11, 6,10,12,
		  1,1,13, 3,3,14, 2,2,15,
		  4,13,16, 5,12,17, 6,14,18
			
		};
		return indices;
	}
}
