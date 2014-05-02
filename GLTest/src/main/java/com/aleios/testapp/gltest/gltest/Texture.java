package com.aleios.testapp.gltest.gltest;

import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by alex on 4/30/2014.
 */
public class Texture
{
    private final int[] texID = new int[1];

    Texture()
    {
        texID[0] = -1;
    }

    public boolean LoadTexture(String filename)
    {
        if(texID[0] == -1)
            GLES20.glGenTextures(1, texID, 0);

        try
        {
            // Get the applications asset manager.
            AssetManager m = GameView.assetManager;

            // Read the image into a stream.
            InputStream texStream = m.open(filename, AssetManager.ACCESS_BUFFER);

            // Make sure our image doesn't get scaled.
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;

            // Decode the image data into a bitmap.
            final Bitmap tex = BitmapFactory.decodeStream(texStream);

            // Bind the texture.
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texID[0]);

            // Set the parameters
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

            // Setup texture
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, tex, 0);

            // Remove old image data
            tex.recycle();

            return true;
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
        }

        return false;
    }

    public void Reset()
    {
        if(texID[0] == -1)
            return;

        GLES20.glDeleteTextures(1, texID, 0);
        texID[0] = -1;
    }

    public int GetTextureID()
    {
        return texID[0];
    }

    public void Bind()
    {
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texID[0]);
    }

    public void Unbind()
    {
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }
}
