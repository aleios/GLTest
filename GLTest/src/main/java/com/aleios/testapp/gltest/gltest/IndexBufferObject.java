package com.aleios.testapp.gltest.gltest;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.List;

/**
 * Created by alex on 5/1/2014.
 */
public class IndexBufferObject
{
    private int[] iboID = new int[1];
    private int indexCount;

    public IndexBufferObject()
    {
        iboID[0] = -1;
    }

    public void SetData(List<Integer> indices, int usage)
    {
        if(iboID[0] == -1)
            GLES20.glGenBuffers(1, iboID, 0);

        indexCount = indices.size();

        final int totalSize = indexCount * 4;
        IntBuffer buffer = ByteBuffer.allocateDirect(totalSize).order(ByteOrder.nativeOrder()).asIntBuffer();

        for(int i : indices)
            buffer.put(i);

        buffer.rewind().position(0);

        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, iboID[0]);
        GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, buffer.capacity() * 4, buffer, usage);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    public int GetNumIndices()
    {
        return indexCount;
    }

    public int GetIBOID()
    {
        return iboID[0];
    }

    public static void Bind(IndexBufferObject ibo)
    {
        if(ibo != null)
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ibo.GetIBOID());
        else
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
    }
}
