/*
 * Created by S.Dobranos on 19.11.20 15:02
 * Copyright (c) 2020. All rights reserved.
 */

package com.fromfinalform.tetris.presentation.model.opengl.common;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.util.Log;

import java.util.ArrayList;

import kotlin.collections.ArraysKt;

public class GLUtils {
    private static final String TAG = "GlUtils";
    private static final int[] ignoredGlExceptions = new int[]{1282};
    public static void checkGlError(String op) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, op + ": glError " + error);
            if (ArraysKt.contains(ignoredGlExceptions,error))continue;
            throw new RuntimeException(op + ": glError " + error);
        }
    }


    public static void clear(float argb[]) {
        GLES20.glClearColor(argb[1], argb[2], argb[3], argb[0]);//rgba
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glEnable(GLES20.GL_BLEND);
        //  GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
    }

    public static int genTexture(int type, boolean buildMipMap) {
        //GLES11Ext.GL_TEXTURE_EXTERNAL_OES
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        GLES20.glBindTexture(type, textures[0]);
        checkGlError("glBindTexture mTextureID");
        GLES20.glTexParameterf(type, GLES20.GL_TEXTURE_MIN_FILTER, buildMipMap ? GLES20.GL_LINEAR_MIPMAP_LINEAR : GLES20.GL_LINEAR);
        GLES20.glTexParameterf(type, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(type, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(type, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        checkGlError("glTexParameter");
        return textures[0];
    }

    public static int nearestPOT(int in) {
        int out = 2;
        while (out < in) out *= 2;
        return out;
    }

    public static int asset2texture(Resources r, int assetId) {
        Bitmap bmp = BitmapFactory.decodeResource(r, assetId);
        if(bmp == null) return -1;
        return bitmap2texture(bmp, GLES20.GL_TEXTURE_2D, null, false, "load asset id:" + assetId + " w:" + bmp.getWidth() + " h: " + bmp.getHeight());
    }

    //GLES11Ext.GL_TEXTURE_EXTERNAL_OES | GLES20.GL_TEXTURE_2D
    public static int bitmap2texture(Bitmap bmp, int type, float[] boundary, boolean genMipMap, String reason) {
        if(bmp == null) return -1;
        checkGlError("before load texture");
        int textureId = genTexture(type, genMipMap);
        if(boundary != null) {
            int w = nearestPOT(bmp.getWidth());
            int h = nearestPOT(bmp.getHeight());
            boundary[0] = (float) bmp.getWidth() / (float) w;
            boundary[1] = (float) bmp.getHeight() / (float) h;
            Bitmap bmpPOT = Bitmap.createBitmap(bmp, 0, 0, w, h);
            android.opengl.GLUtils.texImage2D(type, 0, bmpPOT, 0);
            bmpPOT.recycle();
            GLES20.glBindTexture(type, 0);
        } else {
            android.opengl.GLUtils.texImage2D(type, 0, bmp, 0);
            if(genMipMap) GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
        }
        checkGlError(reason != null ? reason : "on load texture " + bmp.getWidth() + "x" + bmp.getHeight());
        GLES20.glBindTexture(type, 0);
       /* Bitmap b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        c.drawColor(0x80808080, PorterDuff.Mode.ADD);*/
        // v.draw(c);
        return textureId;
    }

    public static void rotateMesh(float[] verticies, float angle, float canvasWidth, float canvasHeight) {
        float s = (float) Math.sin(-angle);
        float c = (float) Math.cos(-angle);
        //pivot point in screen space
        //rotate around center
        float cx = ((verticies[0] + verticies[12]) * .5f + 1f) * canvasWidth;
        float cy = ((verticies[1] + verticies[13]) * .5f + 1f) * canvasHeight;

        for(int i = 0; i < verticies.length; i += 4) {

            //int offset = i * 4;
            // translate point
            float px = (verticies[i    ] + 1f) * canvasWidth  - cx;
            float py = (verticies[i + 1] + 1f) * canvasHeight - cy;
            // rotate point
            float xnew = px * c - py * s;
            float ynew = px * s + py * c;
            // translate point back:
            verticies[i    ] = (xnew + cx) / canvasWidth  - 1f;
            verticies[i + 1] = (ynew + cy) / canvasHeight - 1f;
        }
    }

    /*--*/

    public static int[] genTextures(int type, int count) {
        int[] textureIds = new int[count];
        GLES20.glGenTextures(count, textureIds, 0);
        for(int id : textureIds) {
            GLES20.glBindTexture(type, id);
            checkGlError("glBindTexture mTextureID");
            GLES20.glTexParameterf(type, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(type, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(type, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(type, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
            checkGlError("glTexParameter");
            GLES20.glBindTexture(type, 0);
        }
        return textureIds;
    }

    public static int[] bitmaps2textures(ArrayList<Bitmap> bmps, int type) {
        int[] textureIds = genTextures(type, bmps.size());
        for(int i=0; i<bmps.size(); i++) {
            int id = textureIds[i];
            Bitmap bmp = bmps.get(i);

            GLES20.glBindTexture(type, id);
            android.opengl.GLUtils.texImage2D(type, 0, bmp, 0);
            GLES20.glBindTexture(type, 0);

            bmp.recycle();
        }
        return textureIds;
    }

    public static int genFrameBuffer(final int width, final int height) {
        int[] mFrameBuffers = new int[1];
        int[] mFrameBufferTextures = new int[1];

        GLES20.glGenFramebuffers(1, mFrameBuffers, 0);

        GLES20.glGenTextures(1, mFrameBufferTextures, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mFrameBufferTextures[0]);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0,
                GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBuffers[0]);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D, mFrameBufferTextures[0], 0);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        return mFrameBuffers[0];
    }

    private static int loadShader(int shaderType, String source) {
        int shader = GLES20.glCreateShader(shaderType);
        checkGlError("glCreateShader type=" + shaderType);
        GLES20.glShaderSource(shader, source);
        GLES20.glCompileShader(shader);
        int[] compiled = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            Log.e(TAG, "Could not compile shader " + shaderType + ":");
            Log.e(TAG, " " + GLES20.glGetShaderInfoLog(shader));
            GLES20.glDeleteShader(shader);
            shader = 0;
        }
        return shader;
    }

    public static int createShaderProgram(String vertexProgramSrc, String fragmentProgramSrc) {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexProgramSrc);
        if (vertexShader == 0) return 0;

        int pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentProgramSrc);
        if (pixelShader == 0) return 0;

        int program = GLES20.glCreateProgram();
        checkGlError("glCreateProgram");
        if (program == 0) Log.e(TAG, "Could not create program");

        GLES20.glAttachShader(program, vertexShader);
        checkGlError("glAttachShader");
        GLES20.glAttachShader(program, pixelShader);
        checkGlError("glAttachShader");
        GLES20.glLinkProgram(program);
        int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] != GLES20.GL_TRUE) {
            Log.e(TAG, "Could not link program: ");
            Log.e(TAG, GLES20.glGetProgramInfoLog(program));
            GLES20.glDeleteProgram(program);
            program = 0;
        }
        return program;
    }
}
