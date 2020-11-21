/*
 * Created by S.Dobranos on 19.11.20 15:02
 * Copyright (c) 2020. All rights reserved.
 */

package com.fromfinalform.tetris.presentation.model.opengl;

import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;

public class EGL10ContextFactory implements GLSurfaceView.EGLContextFactory {

    public EGLContext context = null;
    public EGL10 egl = null;

    public EGLContext createContext(EGL10 egl, EGLDisplay display, EGLConfig config) {
        this.egl = egl;
        context = egl.eglCreateContext(display, config, EGL10.EGL_NO_CONTEXT, new int[]{0x3098, 2, EGL10.EGL_NONE});
        return context;
    }

    public void destroyContext(EGL10 egl, EGLDisplay display, EGLContext context) {
        if (!egl.eglDestroyContext(display, context)) throw new RuntimeException("eglDestroyContex error:" + egl.eglGetError());
        this.context = null;
        this.egl = null;
    }
}
