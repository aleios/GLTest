package com.aleios.testapp.gltest.gltest;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES10;
import android.opengl.GLES11Ext;
import android.util.Log;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;

/**
 * Created by alex on 4/29/2014.
 */
public class GLRenderer implements GLSurfaceView.Renderer
{

    private FloatBuffer squareBuffer;
    private final int bytesPerFloat = 4;

    private int vertPosition = 0, colorPosition = 1, texPosition = 2;

    // Shaders
    final String vertexShader = "uniform mat4 mvp;\n" +
                                "attribute vec4 VertexPosition;\n" +
                                "attribute vec4 VertexColor;\n" +
                                "attribute vec2 STCoords;\n" +
                                "varying vec4 Color;\n" +
                                "varying vec2 TexCoords;\n" +
                                "void main() {\n" +
                                "Color = VertexColor;\n" +
                                "TexCoords = STCoords;\n" +
                                "gl_Position = mvp * VertexPosition;\n" +
                                "}\n";

    final String fragmentShader = "precision mediump float;\n" +
                                  "varying vec4 Color;\n" +
                                  "varying vec2 TexCoords;\n" +
                                  "uniform sampler2D tex;\n" +
                                  "void main() {\n" +
                                  "gl_FragColor = texture2D(tex, TexCoords);\n" +
                                  "}\n";

    private int vertID = 0, fragID = 0, programID = 0;

    // Matrices.
    private float[] projMat = new float[16];
    private float[] viewMat = new float[16];
    private float[] modelMat = new float[16];
    private float[] mvpMat = new float[16];

    private int mvpPosition = 0;

    // Render Data
    private final int mStrideBytes = 9 * bytesPerFloat;

    private final int mPositionOffset = 0;
    private final int mPositionDataSize = 3;

    private final int mColorOffset = 3;
    private final int mColorDataSize = 4;

    private final int mTexOffset = 7;
    private final int mTexDataSize = 2;

    // Square Data
    float currentX = 0.0f;
    float currentY = 0.0f;

    // tex
    private Texture tex = new Texture();
    private Shader basicShader = new Shader();
    private VertexBufferObject vbo = new VertexBufferObject();
    private IndexBufferObject ibo = new IndexBufferObject();

    public GLRenderer()
    {
        // Allocate memory for 2 tris that form a quad.
        float hw = 64.0f;
        float hh = 64.0f;

        float r = 1.0f;
        float g = 1.0f;
        float b = 1.0f;
        float a = 1.0f;

        final float[] vertexData =
        {
            // X, Y, Z
            // R, G, B, A
            // S, T
            -hw, -hh, 0.0f,
            r, g, b, a,
            0.0f, 0.0f,
             hw, -hh, 0.0f,
            r, g, b, a,
            1.0f, 0.0f,
            -hw,  hh, 0.0f,
            r, g, b, a,
            0.0f, 1.0f,

            -hw,  hh, 0.0f,
            r, g, b, a,
            0.0f, 1.0f,
             hw, -hh, 0.0f,
            r, g, b, a,
            1.0f, 0.0f,
             hw,  hh, 0.0f,
            r, g, b, a,
            1.0f, 1.0f
        };

        squareBuffer = ByteBuffer.allocateDirect(vertexData.length * bytesPerFloat).order(ByteOrder.nativeOrder()).asFloatBuffer();
        squareBuffer.put(vertexData).position(0);
    }

    public void onSurfaceCreated(GL10 gl, EGLConfig config)
    {
        try {
            // Clear to black.
            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
            GLES20.glEnable(GLES20.GL_BLEND);
            GLES20.glDisable(GLES20.GL_CULL_FACE);
            GLES20.glDisable(GLES20.GL_DEPTH_TEST);
            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

            // Texture loading4
            if(!tex.LoadTexture("test.png"))
                Log.w("GLRenderer", "Failed to load texture.");
            //throw new RuntimeException("Error: Failed to load texture.");

            // Load Shader.
            basicShader.AttachShader("basic.vert", Shader.Type.Vertex);
            basicShader.AttachShader("basic.frag", Shader.Type.Fragment);
            basicShader.InitShader();

            Shader.Bind(basicShader);
            mvpPosition = basicShader.GetUniformLocation("mvp");

            vertPosition = basicShader.GetAttributeLocation("VertexPosition");
            colorPosition = basicShader.GetAttributeLocation("VertexColor");
            texPosition = basicShader.GetAttributeLocation("STCoords");

            // VBO

            float hw = 64.0f;
            float hh = 64.0f;

            float r = 1.0f;
            float g = 1.0f;
            float b = 1.0f;
            float a = 1.0f;

            List<Vertex> vertexList = new ArrayList<Vertex>();

            Vertex v = new Vertex();
            v.x = -hw; v.y = -hh; v.z = 0.0f;
            v.r = r; v.g = g; v.b = b; v.a = a;
            v.s = 0.0f; v.t = 0.0f;
            vertexList.add(v);

            v = new Vertex();
            v.x = hw; v.y = -hh; v.z = 0.0f;
            v.r = r; v.g = g; v.b = b; v.a = a;
            v.s = 1.0f; v.t = 0.0f;
            vertexList.add(v);

            v = new Vertex();
            v.x = hw; v.y = hh; v.z = 0.0f;
            v.r = r; v.g = g; v.b = b; v.a = a;
            v.s = 1.0f; v.t = 1.0f;
            vertexList.add(v);

            v = new Vertex();
            v.x = -hw; v.y = hh; v.z = 0.0f;
            v.r = r; v.g = g; v.b = b; v.a = a;
            v.s = 0.0f; v.t = 1.0f;
            vertexList.add(v);

            vbo.SetData(vertexList, GLES20.GL_STATIC_DRAW);

            List<Integer> indexList = new ArrayList<Integer>();
            indexList.add(0); indexList.add(2); indexList.add(1);
            indexList.add(0); indexList.add(3); indexList.add(2);
            ibo.SetData(indexList, GLES20.GL_STATIC_DRAW);

            //VertexBufferObject.Bind(vbo);
        }
        catch(RuntimeException ex)
        {
            Log.w("GLRenderer", ex.getMessage());
            System.out.println("!! Runtime exception !! " + ex.getMessage());
            /*AlertDialog.Builder builder1 = new AlertDialog.Builder(parent);
            builder1.setMessage(ex.getMessage());
            builder1.setCancelable(false);
            builder1.setPositiveButton("Ok",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

            AlertDialog alert11 = builder1.create();
            alert11.show();*/
        }
        catch(Exception ex)
        {
            Log.w("GLRenderer", ex.getMessage());
            //System.out.println("!! Exception !!" + ex.getMessage());
            /*AlertDialog.Builder builder1 = new AlertDialog.Builder(parent);
            builder1.setMessage(ex.getMessage());
            builder1.setCancelable(false);
            builder1.setPositiveButton("Ok",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

            AlertDialog alert11 = builder1.create();
            alert11.show();*/
        }
    }

    public void MoveSquare(float x, float y)
    {
        currentX = x;
        currentY = y;
    }

    public void onSurfaceChanged(GL10 gl, int width, int height)
    {
        GLES20.glViewport(0, 0, width, height);

        Matrix.orthoM(projMat, 0, 0, width, height, 0, -1, 1);
    }

    public void onDrawFrame(GL10 gl)
    {
        if(basicShader == null)
            return;

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // Set view matrix
        Matrix.setIdentityM(viewMat, 0);

        // Set model matrix
        Matrix.setIdentityM(modelMat, 0);
        Matrix.translateM(modelMat, 0, currentX, currentY, 0.0f);

        // Combine matrices
        Matrix.setIdentityM(mvpMat, 0);
        Matrix.multiplyMM(mvpMat, 0, viewMat, 0, modelMat, 0);
        Matrix.multiplyMM(mvpMat, 0, projMat, 0, mvpMat, 0);

        // Render
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        tex.Bind();
        Shader.Bind(basicShader);

        basicShader.SetParameter("tex", 0);
        GLES20.glUniformMatrix4fv(mvpPosition, 1, false, mvpMat, 0);
        //GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);

        vbo.Draw(basicShader, ibo);
    }

    public void onPause()
    {

    }

    public void onResume()
    {
        basicShader.Reset();
    }

}
