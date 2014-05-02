package com.aleios.testapp.gltest.gltest;

import android.opengl.GLES20;
import android.os.Build;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Iterator;
import java.util.List;

/**
 * Created by alex on 5/1/2014.
 */
public class VertexBufferObject
{
    private int[] vboID = new int[1];
    public int vertexCount = 0;

    VertexBufferObject()
    {
        vboID[0] = -1;
    }

    public void SetData(List<Vertex> vertices, int usage)
    {
        if(vboID[0] == -1)
            GLES20.glGenBuffers(1, vboID, 0);

        vertexCount = vertices.size();

        // Get the total size in bytes that we need to allocate.
        final int floatBytes = 4;
        final int vertexClassSize = 36;
        final int totalSize = vertices.size() * vertexClassSize;

        // Allocate a buffer to hold our data.
        FloatBuffer buffer = ByteBuffer.allocateDirect(totalSize).order(ByteOrder.nativeOrder()).asFloatBuffer();

        // Put our vertex data in the float buffer.
        for(Vertex v : vertices)
        {
            buffer.put(v.x).put(v.y).put(v.z);
            buffer.put(v.r).put(v.g).put(v.b).put(v.a);
            buffer.put(v.s).put(v.t);
        }

        buffer.rewind().position(0);

        // Upload.
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboID[0]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, buffer.capacity() * floatBytes, buffer, usage);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
    }

    public int GetNumVertices()
    {
        return vertexCount;
    }

    public int GetVBOID()
    {
        return vboID[0];
    }

    public static void Bind(VertexBufferObject vbo)
    {
        if(vbo != null)
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo.GetVBOID());
        else
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
    }

    public void Draw(Shader shader, IndexBufferObject ibo)
    {
        // Render Data
        final int bytesPerFloat = 4;
        final int mStrideBytes = 9 * bytesPerFloat;

        final int mPositionOffset = 0;
        final int mPositionDataSize = 3;

        final int mColorOffset = 3;
        final int mColorDataSize = 4;

        final int mTexOffset = 7;
        final int mTexDataSize = 2;

        if(shader == null)
            throw new RuntimeException("Error: Shader must be provided for VertexBufferObject.Draw()");

        Shader.Bind(shader);

        final int vertPosition = shader.GetAttributeLocation("VertexPosition");
        final int colorPosition = shader.GetAttributeLocation("VertexColor");
        final int texPosition = shader.GetAttributeLocation("STCoords");

        if(ibo != null)
            IndexBufferObject.Bind(ibo);

        VertexBufferObject.Bind(this);

        if(Build.VERSION.SDK_INT == Build.VERSION_CODES.FROYO)
        {
            //Log.w("Renderer", "Using GL Fix!");
            GLES20Fix.glVertexAttribPointer(vertPosition, mPositionDataSize, GLES20.GL_FLOAT, false, mStrideBytes, mPositionOffset * bytesPerFloat);
            GLES20Fix.glVertexAttribPointer(colorPosition, mColorDataSize, GLES20.GL_FLOAT, false, mStrideBytes, mColorOffset * bytesPerFloat);
            GLES20Fix.glVertexAttribPointer(texPosition, mTexDataSize, GLES20.GL_FLOAT, false, mStrideBytes, mTexOffset * bytesPerFloat);
        }
        else
        {
            GLES20.glVertexAttribPointer(vertPosition, mPositionDataSize, GLES20.GL_FLOAT, false, mStrideBytes, mPositionOffset * bytesPerFloat);
            GLES20.glVertexAttribPointer(colorPosition, mColorDataSize, GLES20.GL_FLOAT, false, mStrideBytes, mColorOffset * bytesPerFloat);
            GLES20.glVertexAttribPointer(texPosition, mTexDataSize, GLES20.GL_FLOAT, false, mStrideBytes, mTexOffset * bytesPerFloat);
        }


        // Enable the vertex attribute arrays.
        GLES20.glEnableVertexAttribArray(vertPosition);
        GLES20.glEnableVertexAttribArray(colorPosition);
        GLES20.glEnableVertexAttribArray(texPosition);

        if(ibo == null)
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, GetNumVertices());
        else
        {
            if(Build.VERSION.SDK_INT == Build.VERSION_CODES.FROYO)
            {
                GLES20Fix.glDrawElements(GLES20.GL_TRIANGLES, ibo.GetNumIndices(), GLES20.GL_UNSIGNED_INT, 0);
            }
            else
            {
                GLES20.glDrawElements(GLES20.GL_TRIANGLES, ibo.GetNumIndices(), GLES20.GL_UNSIGNED_INT, 0);
            }
        }

        // Unbind
        VertexBufferObject.Bind(null);

        if(ibo != null)
            IndexBufferObject.Bind(null);

        Shader.Bind(null);
    }
}
