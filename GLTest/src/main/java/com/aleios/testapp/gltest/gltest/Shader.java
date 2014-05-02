package com.aleios.testapp.gltest.gltest;

import android.opengl.GLES20;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * Created by alex on 4/29/2014.
 */
public class Shader
{
    public enum Type
    {
        Vertex, Fragment
    }

    private int programID, vertID, fragID;
    private boolean isInitialized = false;

    public Shader()
    {
        programID = -1;
        vertID = -1;
        fragID = -1;
    }

    public void AttachShader(String filename, Type type)
    {
        if(programID == -1)
            programID = GLES20.glCreateProgram();

        int shaderID = -1;
        switch(type)
        {
        case Vertex:
            shaderID = vertID = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
            break;
        case Fragment:
            shaderID = fragID = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
            break;
        }

        String shaderSrc = "";

        try
        {
            // Read text file into stream.
            InputStream is = GameView.assetManager.open(filename);
            int size = is.available();

            // Read stream into buffer.
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            shaderSrc = new String(buffer);
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
        }

        GLES20.glShaderSource(shaderID, shaderSrc);

        // Compile shader
        GLES20.glCompileShader(shaderID);

        // Get the status of the compilation.
        final int[] compileStatus = new int[1];

        GLES20.glGetShaderiv(shaderID, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

        if(compileStatus[0] == 0)
        {
            GLES20.glDeleteShader(programID);
            throw new RuntimeException("Error: Failed to compile shader.");
        }

        // Attach shader to program.
        GLES20.glAttachShader(programID, shaderID);
    }

    public void InitShader()
    {
        GLES20.glLinkProgram(programID);
        isInitialized = true;
    }

    public void Reset()
    {
        if(programID == -1 || !isInitialized)
            return;

        GLES20.glDeleteShader(vertID);
        GLES20.glDeleteShader(fragID);
        GLES20.glDeleteProgram(programID);

        vertID = -1;
        fragID = -1;
        programID = -1;
        isInitialized = false;
    }

    public static void Bind(Shader inShader)
    {
        if(inShader != null)
        {
            GLES20.glUseProgram(inShader.programID);
        }
        else
        {
            GLES20.glUseProgram(0);
        }
    }

    // Locations
    public int GetUniformLocation(String name)
    {
        return GLES20.glGetUniformLocation(programID, name);
    }

    public int GetAttributeLocation(String name)
    {
        return GLES20.glGetAttribLocation(programID, name);
    }

    // Uniform Parameters
    public void SetParameter(String name, int id)
    {
        int loc = GetUniformLocation(name);
        GLES20.glUniform1i(loc, id);
    }

    public void SetParameter(String name, float x)
    {
        int loc = GetUniformLocation(name);
        GLES20.glUniform1f(loc, x);
    }

    public void SetParameter(String name, float x, float y)
    {
        int loc = GetUniformLocation(name);
        GLES20.glUniform2f(loc, x, y);
    }

    public void SetParameter(String name, float x, float y, float z)
    {
        int loc = GetUniformLocation(name);
        GLES20.glUniform3f(loc, x, y, z);
    }

    public void SetParameter(String name, float x, float y, float z, float w)
    {
        int loc = GetUniformLocation(name);
        GLES20.glUniform4f(loc, x, y, z, w);
    }

    public void SetParameter(String name, float[] fv)
    {
        int loc = GetUniformLocation(name);
        GLES20.glUniformMatrix4fv(loc, 1, false, fv, 0);
    }
}
