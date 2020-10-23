package a3;

import java.awt.Color;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.vecmath.Vector3f;

import ray.rage.Engine;
import ray.rage.asset.material.Material;
import ray.rage.asset.texture.Texture;
import ray.rage.rendersystem.shader.GpuShaderProgram;
import ray.rage.rendersystem.states.FrontFaceState;
import ray.rage.rendersystem.states.RenderState;
import ray.rage.rendersystem.states.TextureState;
import ray.rage.scene.ManualObject;
import ray.rage.scene.ManualObjectSection;
import ray.rage.scene.SceneManager;
import ray.rage.util.BufferUtil;

public class GroundPlaneObject 
{

	protected ManualObject gameFloorObject(Engine eng, SceneManager sm, String name) throws IOException
	{
		ManualObject floor = sm.createManualObject(name);
    	ManualObjectSection shipSec = floor.createManualSection(name + "Section");
    	
    	floor.setGpuShaderProgram(sm.getRenderSystem().
    			getGpuShaderProgram(GpuShaderProgram.Type.RENDERING));
    	
    	float [] vertices = obtainVertices(true);
    	float [] texCoord = obtainTextureCoord(true);
    	float [] normals = obtainNormals(vertices);
    	int [] indices = obtainIndices();
    	
    	FloatBuffer vertBuf = BufferUtil.directFloatBuffer(vertices);
		FloatBuffer texBuf = BufferUtil.directFloatBuffer(texCoord);
		FloatBuffer normBuf = BufferUtil.directFloatBuffer(normals);
		IntBuffer indexBuf = BufferUtil.directIntBuffer(indices);
		
		shipSec.setVertexBuffer(vertBuf);
		shipSec.setTextureCoordsBuffer(texBuf);
		shipSec.setNormalsBuffer(normBuf);
		shipSec.setIndexBuffer(indexBuf);
		
		Texture tex = eng.getTextureManager().getAssetByPath("moon.jpeg");
		TextureState texState = (TextureState)sm.getRenderSystem().
				createRenderState(RenderState.Type.TEXTURE);
		texState.setTexture(tex);
		
		FrontFaceState faceState = (FrontFaceState) sm.getRenderSystem().
		createRenderState(RenderState.Type.FRONT_FACE);
		
		Material mat = sm.getMaterialManager().getAssetByPath("default.mtl");
	    mat.setEmissive(Color.GRAY);
		
		//ship.setDataSource(DataSource.INDEX_BUFFER);
	    floor.setRenderState(texState);
		floor.setRenderState(faceState);
		floor.setMaterial(mat);
		
		return floor;
	}
	
	protected ManualObject gameFloorObject2(Engine eng, SceneManager sm, String name) throws IOException
	{
		ManualObject floor = sm.createManualObject(name);
    	ManualObjectSection shipSec = floor.createManualSection(name + "Section2");
    	
    	floor.setGpuShaderProgram(sm.getRenderSystem().
    			getGpuShaderProgram(GpuShaderProgram.Type.RENDERING));
    	
    	float [] vertices = obtainVertices(false);
    	float [] texCoord = obtainTextureCoord(false);
    	float [] normals = obtainNormals(vertices);
    	int [] indices = obtainIndices();
    	
    	FloatBuffer vertBuf = BufferUtil.directFloatBuffer(vertices);
		FloatBuffer texBuf = BufferUtil.directFloatBuffer(texCoord);
		FloatBuffer normBuf = BufferUtil.directFloatBuffer(normals);
		IntBuffer indexBuf = BufferUtil.directIntBuffer(indices);
		
		shipSec.setVertexBuffer(vertBuf);
		shipSec.setTextureCoordsBuffer(texBuf);
		shipSec.setNormalsBuffer(normBuf);
		shipSec.setIndexBuffer(indexBuf);
		
		Texture tex = eng.getTextureManager().getAssetByPath("moon.jpeg");
		TextureState texState = (TextureState)sm.getRenderSystem().
				createRenderState(RenderState.Type.TEXTURE);
		texState.setTexture(tex);
		
		FrontFaceState faceState = (FrontFaceState) sm.getRenderSystem().
		createRenderState(RenderState.Type.FRONT_FACE);
		
		Material mat = sm.getMaterialManager().getAssetByPath("default.mtl");
	    mat.setEmissive(Color.GRAY);
		
		//ship.setDataSource(DataSource.INDEX_BUFFER);
	    floor.setRenderState(texState);
		floor.setRenderState(faceState);
		floor.setMaterial(mat);
		
		return floor;
	}
	
	public float[] obtainVertices(boolean set)
	{
		float [] vertices = new float[]
		{
				-5.0f, 0.0f, 5.0f, 
				5.0f, 0.0f, -5.0f, 
				-5.0f, 0.0f, -5.0f// -X axis side of floor.
		};
		
		float [] vertices2 = new float[]
		{
			-5.0f, 0.0f, 5.0f, 
			5.0f, 0.0f, 5.0f,
			5.0f, 0.0f, -5.0f// +X axis of the floor. 
		};
		
		if (set)
		{
			return vertices;
		}

		return vertices2;
	}
	
	public float[] obtainTextureCoord(boolean set)
	{
		float [] texCoord = new float[]
		{
				0.0f, 0.0f, 
				1.0f, 1.0f, 
				0.0f, 1.0f, // Triangle 1
		};
		
		float [] texCoord2 = new float[]
		{
				0.0f, 0.0f, 
				1.0f, 0.0f, 
				1.0f, 1.0f
		};
		
		if (set)
		{
			return texCoord;
		}
		
		return texCoord2;
	}
	public float[] obtainNormals(float[] vertices)
	{
		float[] classVertices = vertices;
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
		  2, 5, 5, 3, 6, 6, 6,10,7, 5,9,8
			
		};
		return indices;
	}
	
}
