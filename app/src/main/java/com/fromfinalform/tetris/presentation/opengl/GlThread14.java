/*
 * Created by S.Dobranos on 17.11.20 15:25
 * Copyright (c) 2020. All rights reserved.
 */

package com.fromfinalform.tetris.presentation.opengl;

import android.graphics.Bitmap;
import android.opengl.EGL14;
import android.util.Log;

import com.fromfinalform.tetris.presentation.ThreadUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class GlThread14 extends Thread {

    WeakReference<GLSurfaceViewEGL14> mGLViewWeakRef;
    android.opengl.EGLDisplay mEglDisplay;
    android.opengl.EGLSurface mEglSurface;
    android.opengl.EGLConfig mEglConfig;
    android.opengl.EGLContext mEglContext;

    GlThread14(final WeakReference<GLSurfaceViewEGL14> instanceWeakRef) {
        super();
        this.mWidth = 0;
        this.mHeight = 0;
        this.mRequestRender = true;
        this.mRenderMode = GLSurfaceViewEGL14.RENDERMODE_CONTINUOUSLY;
        this.mGLViewWeakRef = instanceWeakRef;
    }

    public void initEGL() {
        if (GLSurfaceViewEGL14.LOG_EGL) {
            Log.w(GLSurfaceViewEGL14.TAG.concat(".EglHelper"), "start() tid=".concat(Long.toString(Thread.currentThread().getId())));
        }

        /*
         * Get to the default display.
         */
        this.mEglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);

        if (this.mEglDisplay == EGL14.EGL_NO_DISPLAY) {
            throw new RuntimeException("eglGetDisplay failed");
        }

        /*
         * We can now initialize EGL for that display
         */
        int[] version = new int[2];
        if (EGL14.eglInitialize(this.mEglDisplay, version, 0, version, 1) == false) {
            throw new RuntimeException("eglInitialize failed");
        }

        GLSurfaceViewEGL14 view = this.mGLViewWeakRef.get();

        if (view == null) {
            this.mEglConfig = null;
            this.mEglContext = null;
        } else {
            this.mEglConfig = EGL14Config.chooseConfig(this.mEglDisplay, view.mRecordable);

            /*
             * Create an EGL context. We want to do this as rarely as we can, because an EGL context is a somewhat heavy object.
             */
            this.mEglContext = view.mEGLContextFactory.createContext(this.mEglDisplay, this.mEglConfig);
        }

        if (this.mEglContext == null || this.mEglContext == EGL14.EGL_NO_CONTEXT) {
            this.mEglContext = null;
            //EglHelper.throwEglException("createContext");
        }

        if (GLSurfaceViewEGL14.LOG_EGL) {
            Log.w(GLSurfaceViewEGL14.TAG.concat(".EglHelper"), "createContext " + this.mEglContext + " tid=" + Long.toString(Thread.currentThread().getId()));
        }

        this.mEglSurface = null;
    }

    /**
     * Create an egl surface for the current SurfaceHolder surface. If a surface already exists, destroy it before creating the new surface.
     *
     * @return true if the surface was created successfully.
     */
    public boolean createSurface() {
        if (GLSurfaceViewEGL14.LOG_EGL) {
            Log.w(GLSurfaceViewEGL14.TAG.concat(".EglHelper"), "tid=" + Long.toString(Thread.currentThread().getId()));
        }

        if (this.mEglDisplay == null) {
            throw new RuntimeException("eglDisplay not initialized");
        }

        if (this.mEglConfig == null) {
            throw new RuntimeException("mEglConfig not initialized");
        }

        // The window size has changed, so we need to create a new surface.

        this.destroySurfaceImp();

        // Create an EGL surface we can render into.

        GLSurfaceViewEGL14 view = this.mGLViewWeakRef.get();

        if (view != null) {
            this.mEglSurface = view.mEGLWindowSurfaceFactory.createWindowSurface(this.mEglDisplay, this.mEglConfig, view.getHolder());
        } else {
            this.mEglSurface = null;
        }

        if (this.mEglSurface == null || this.mEglSurface == EGL14.EGL_NO_SURFACE) {
            int error = EGL14.eglGetError();

            if (error == EGL14.EGL_BAD_NATIVE_WINDOW) {
                Log.e(GLSurfaceViewEGL14.TAG.concat(".EglHelper"), "createWindowSurface returned EGL_BAD_NATIVE_WINDOW.");
            }

            return false;
        }

        // Before we can issue GL commands, we need to make sure the context is current and bound to a surface.

        final boolean status = this.makeCurrent();

        return status;
    }

    public boolean makeCurrent() {
        if (this.mEglDisplay == null || this.mEglSurface == null || this.mEglContext == null) {
            return false;
        }

        if (EGL14.eglMakeCurrent(this.mEglDisplay, this.mEglSurface, this.mEglSurface, this.mEglContext) == false) {
            if (EGL14.eglMakeCurrent(this.mEglDisplay, this.mEglSurface, this.mEglSurface, this.mEglContext) == false) {
                if (EGL14.eglMakeCurrent(this.mEglDisplay, this.mEglSurface, this.mEglSurface, this.mEglContext) == false) {
                    final int errorCode = EGL14.eglGetError();

                    // Could not make the context current, probably because the underlying SurfaceView surface has been destroyed.
                    //EglHelper.logEglErrorAsWarning(GLSurfaceViewEGL14.TAG.concat(".EglHelper"), "eglMakeCurrent", errorCode);
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Display the current render surface.
     *
     * @return the EGL error code from eglSwapBuffers.
     */
    public int swap() {
        if (this.mEglDisplay == null) {
            final int error = EGL14.eglGetError();
            return error != 0 ? error : EGL14.EGL_BAD_DISPLAY;
        }

        if (this.mEglSurface == null) {
            final int error = EGL14.eglGetError();
            return error != 0 ? error : EGL14.EGL_BAD_SURFACE;
        }

        if (EGL14.eglSwapBuffers(this.mEglDisplay, this.mEglSurface) == false) {
            return EGL14.eglGetError();
        }

        return EGL14.EGL_SUCCESS;
    }

    public void destroySurface() {
        if (GLSurfaceViewEGL14.LOG_EGL) {
            Log.w(GLSurfaceViewEGL14.TAG.concat(".EglHelper"), "Destroying surface. tid=" + Long.toString(Thread.currentThread().getId()));
        }

        this.destroySurfaceImp();
    }

    private void destroySurfaceImp() {
        if (this.mEglSurface != null && this.mEglSurface != EGL14.EGL_NO_SURFACE) {
            EGL14.eglMakeCurrent(this.mEglDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);
            GLSurfaceViewEGL14 view = this.mGLViewWeakRef.get();

            if (view != null) {
                view.mEGLWindowSurfaceFactory.destroySurface(this.mEglDisplay, this.mEglSurface);
            }

            this.mEglSurface = null;
        }
    }

    public void finish() {
        if (GLSurfaceViewEGL14.LOG_EGL) {
            Log.w(GLSurfaceViewEGL14.TAG.concat(".EglHelper"), "Finishing. tid=" + Long.toString(Thread.currentThread().getId()));
        }

        if (this.mEglContext != null) {
            final GLSurfaceViewEGL14 view = this.mGLViewWeakRef.get();

            if (view != null) {
                view.mEGLContextFactory.destroyContext(this.mEglDisplay, this.mEglContext);
            }

            this.mEglContext = null;
        }

        if (this.mEglDisplay != null) {
            EGL14.eglTerminate(this.mEglDisplay);
            this.mEglDisplay = null;
        }
    }

    @Override
    public void run() {
        this.setName("GLThread " + Long.toString(this.getId()));

        if (GLSurfaceViewEGL14.LOG_THREADS) {
            Log.i(GLSurfaceViewEGL14.TAG.concat(getThreadStr()), "starting tid=" + Long.toString(this.getId()));
        }

        try {
            this.guardedRun();
        } catch (InterruptedException e) {
            // fall thru and exit normally
        } finally {
            GLSurfaceViewEGL14.sGLThreadManager.threadExiting(this);
        }
    }

    /*
     * This private method should only be called inside a synchronized(sGLThreadManager) block.
     */
    private void stopEglSurfaceLocked() {
        if (this.mHaveEglSurface) {
            this.mHaveEglSurface = false;
            destroySurface();
        }
    }

    /*
     * This private method should only be called inside a synchronized(sGLThreadManager) block.
     */
    private void stopEglContextLocked() {
        final GLSurfaceViewEGL14 view = this.mGLViewWeakRef.get();

        if (view != null && view.mRenderer != null) {
            view.mRenderer.onDestroy();
        }

        if (this.mHaveEglContext) {
            finish();
            this.mHaveEglContext = false;
            GLSurfaceViewEGL14.sGLThreadManager.releaseEglContextLocked(this);
        }
    }

    private void guardedRun() throws InterruptedException {
        // this.mEglHelper = new GLSurfaceViewEGL14.EglHelper(this.mGLViewWeakRef);
        this.mHaveEglContext = false;
        this.mHaveEglSurface = false;

        try {
            boolean createEglContext = false;
            boolean createEglSurface = false;
            boolean createGlInterface = false;
            boolean lostEglContext = false;
            boolean sizeChanged = false;
            boolean wantRenderNotification = false;
            boolean doRenderNotification = false;
            boolean askedToReleaseEglContext = false;
            int w = 0;
            int h = 0;
            Runnable event = null;

            while (true) {
                synchronized (GLSurfaceViewEGL14.sGLThreadManager) {
                    {
                        GLSurfaceViewEGL14 view = this.mGLViewWeakRef.get();
                        if (view != null) {
                            Bitmap bmp = (view.requestStaticBitmap || view.onScreenshotDone != null) ? view.mRenderer.screenshot() : null;
                            // view.mRenderer.onDrawFrame();
                            if (view.requestStaticBitmap) {
                                view.staticBitmap = bmp;
                                view.requestStaticBitmap = false;
                                view.postInvalidate();
                                if (view.onStaticImageReadyCallback != null) view.onStaticImageReadyCallback.run();
                            }
                            if (view.onScreenshotDone != null) {
                                view.onScreenshotDone.onScreenshot(bmp);
                                view.onScreenshotDone = null;
                            }
                        }
                    }

                    while (true) {
                        if (this.mShouldExit) return;

                        if (!this.mEventQueue.isEmpty()) {
                            event = this.mEventQueue.remove(0);
                            break;
                        }

                        // Update the pause state.
                        boolean pausing = false;

                        if (this.mPaused != this.mRequestPaused) {
                            pausing = this.mRequestPaused;
                            this.mPaused = this.mRequestPaused;
                            GLSurfaceViewEGL14.sGLThreadManager.notifyAll();

                            if (GLSurfaceViewEGL14.LOG_PAUSE_RESUME)
                                Log.i(GLSurfaceViewEGL14.TAG.concat(getThreadStr()), "mPaused is now " + Boolean.toString(this.mPaused) + ". tid=" + Long.toString(this.getId()));
                        }

                        // Do we need to give up the EGL context?
                        if (this.mShouldReleaseEglContext) {
                            if (GLSurfaceViewEGL14.LOG_SURFACE) Log.i(GLSurfaceViewEGL14.TAG.concat(getThreadStr()), "Releasing EGL context because asked to tid=" + Long.toString(this.getId()));


                            this.stopEglSurfaceLocked();
                            this.stopEglContextLocked();
                            this.mShouldReleaseEglContext = false;
                            askedToReleaseEglContext = true;
                        }

                        // Have we lost the EGL context?
                        if (lostEglContext) {
                            this.stopEglSurfaceLocked();
                            this.stopEglContextLocked();
                            lostEglContext = false;
                        }

                        // When pausing, release the EGL surface:
                        if (pausing && this.mHaveEglSurface) {
                            if (GLSurfaceViewEGL14.LOG_SURFACE) Log.i(GLSurfaceViewEGL14.TAG.concat(getThreadStr()), "Releasing EGL surface because paused tid=" + Long.toString(this.getId()));
                            this.stopEglSurfaceLocked();
                        }

                        // When pausing, optionally release the EGL Context:
                        if (pausing == true && this.mHaveEglContext == true) {
                            GLSurfaceViewEGL14 view = this.mGLViewWeakRef.get();
                            boolean preserveEglContextOnPause = view == null ? false : view.mPreserveEGLContextOnPause;

                            if (preserveEglContextOnPause == false || GLSurfaceViewEGL14.sGLThreadManager.shouldReleaseEGLContextWhenPausing()) {
                                this.stopEglContextLocked();
                                if (GLSurfaceViewEGL14.LOG_SURFACE) Log.i(GLSurfaceViewEGL14.TAG.concat(getThreadStr()), "Releasing EGL context because paused tid=" + Long.toString(this.getId()));
                            }
                        }

                        // Have we lost the SurfaceView surface?
                        if ((this.mHasSurface == false) && (this.mWaitingForSurface == false)) {
                            if (GLSurfaceViewEGL14.LOG_SURFACE) Log.i(GLSurfaceViewEGL14.TAG.concat(getThreadStr()), "Noticed surfaceView surface lost tid=" + Long.toString(this.getId()));
                            if (this.mHaveEglSurface == true) this.stopEglSurfaceLocked();
                            this.mWaitingForSurface = true;
                            this.mSurfaceIsBad = false;
                            GLSurfaceViewEGL14.sGLThreadManager.notifyAll();
                        }

                        // Have we acquired the surface view surface?
                        if (this.mHasSurface == true && this.mWaitingForSurface == true) {
                            if (GLSurfaceViewEGL14.LOG_SURFACE) Log.i(GLSurfaceViewEGL14.TAG.concat(getThreadStr()), "Noticed surfaceView surface acquired tid=" + Long.toString(this.getId()));
                            this.mWaitingForSurface = false;
                            GLSurfaceViewEGL14.sGLThreadManager.notifyAll();
                        }

                        if (doRenderNotification == true) {
                            if (GLSurfaceViewEGL14.LOG_SURFACE) Log.i(GLSurfaceViewEGL14.TAG.concat(getThreadStr()), "Sending render notification tid=" + Long.toString(this.getId()));
                            wantRenderNotification = false;
                            doRenderNotification = false;
                            this.mRenderComplete = true;
                            GLSurfaceViewEGL14.sGLThreadManager.notifyAll();
                        }

                        // Ready to draw?
                        if (this.readyToDraw() == true) {

                            // If we don't have an EGL context, try to acquire one.
                            if (this.mHaveEglContext == false) {
                                if (askedToReleaseEglContext == true) {
                                    askedToReleaseEglContext = false;
                                } else if (GLSurfaceViewEGL14.sGLThreadManager.tryAcquireEglContextLocked(this)) {
                                    try {
                                        initEGL();
                                    } catch (RuntimeException t) {
                                        //CrashlyticsHelper.INSTANCE.logInfo(t);
                                        GLSurfaceViewEGL14.sGLThreadManager.releaseEglContextLocked(this);
                                        throw t;
                                    }

                                    this.mHaveEglContext = true;
                                    createEglContext = true;

                                    GLSurfaceViewEGL14.sGLThreadManager.notifyAll();
                                }
                            }

                            if (this.mHaveEglContext == true && this.mHaveEglSurface == false) {
                                this.mHaveEglSurface = true;
                                createEglSurface = true;
                                createGlInterface = true;
                                sizeChanged = true;
                            }

                            if (this.mHaveEglSurface == true) {
                                if (this.mSizeChanged == true) {
                                    sizeChanged = true;
                                    w = this.mWidth;
                                    h = this.mHeight;
                                    wantRenderNotification = true;
                                    if (GLSurfaceViewEGL14.LOG_SURFACE)
                                        Log.i(GLSurfaceViewEGL14.TAG.concat(getThreadStr()), "Noticing that we want render notification tid=" + Long.toString(this.getId()));
                                    // Destroy and recreate the EGL surface.
                                    createEglSurface = true;

                                    this.mSizeChanged = false;
                                }

                                this.mRequestRender = false;
                                GLSurfaceViewEGL14.sGLThreadManager.notifyAll();
                                break;
                            }
                        }

                        // By design, this is the only place in a GLThread thread where we wait().
                        if (GLSurfaceViewEGL14.LOG_THREADS_WAIT) {
                            Log.i(GLSurfaceViewEGL14.TAG.concat(getThreadStr()), "waiting tid=" + Long.toString(this.getId()) + " mHaveEglContext: " + Boolean.toString(this.mHaveEglContext) + " mHaveEglSurface: " + Boolean.toString(this.mHaveEglSurface) + " mFinishedCreatingEglSurface: " + Boolean.toString(this.mFinishedCreatingEglSurface) + " mPaused: " + Boolean.toString(this.mPaused) + " mHasSurface: " + Boolean.toString(this.mHasSurface) + " mSurfaceIsBad: "
                                    + Boolean.toString(this.mSurfaceIsBad) + " mWaitingForSurface: " + Boolean.toString(this.mWaitingForSurface) + " mWidth: " + Integer.toString(this.mWidth) + " mHeight: " + Integer.toString(this.mHeight) + " mRequestRender: " + Boolean.toString(this.mRequestRender) + " mRenderMode: " + Integer.toString(this.mRenderMode));
                        }

                        //GLSurfaceViewEGL14.sGLThreadManager.wait();
                        waitTillNotify();
                    }
                } // end of synchronized(sGLThreadManager)

                if (event != null) {
                    event.run();
                    event = null;
                    continue;
                }

                if (createEglSurface == true) {
                    if (GLSurfaceViewEGL14.LOG_SURFACE) {
                        Log.w(GLSurfaceViewEGL14.TAG.concat(getThreadStr()), "egl createSurface");
                    }

                    if (createSurface() == true) {
                        synchronized (GLSurfaceViewEGL14.sGLThreadManager) {
                            this.mFinishedCreatingEglSurface = true;
                            GLSurfaceViewEGL14.sGLThreadManager.notifyAll();
                        }
                    } else {
                        synchronized (GLSurfaceViewEGL14.sGLThreadManager) {
                            this.mFinishedCreatingEglSurface = false;
                            this.mSurfaceIsBad = false;
                            GLSurfaceViewEGL14.sGLThreadManager.notifyAll();
                        }

                        continue;
                    }

                    createEglSurface = false;
                }

                if (createGlInterface == true) {
                    createGlInterface = false;
                }

                if (createEglContext == true) {
                    if (GLSurfaceViewEGL14.LOG_RENDERER) {
                        Log.w(GLSurfaceViewEGL14.TAG.concat(getThreadStr()), "onSurfaceCreated");
                    }

                    GLSurfaceViewEGL14 view = this.mGLViewWeakRef.get();

                    if (view != null) {
                        view.mRenderer.onSurfaceCreated();
                    }

                    createEglContext = false;
                }

                if (sizeChanged == true) {
                    if (GLSurfaceViewEGL14.LOG_RENDERER) Log.w(GLSurfaceViewEGL14.TAG.concat(getThreadStr()), "onSurfaceChanged(" + Integer.toString(w) + ", " + Integer.toString(h) + ")");
                    GLSurfaceViewEGL14 view = this.mGLViewWeakRef.get();
                    if (view != null) view.mRenderer.onSurfaceChanged(w, h);
                    sizeChanged = false;
                }

                synchronized (GLSurfaceViewEGL14.sGLThreadManager) {
                    if (!this.mPaused && this.mHasSurface && !mShouldExit) {
                        if (GLSurfaceViewEGL14.LOG_RENDERER_DRAW_FRAME) Log.w(GLSurfaceViewEGL14.TAG.concat(getThreadStr()), "onDrawFrame tid=" + Long.toString(this.getId()));

                        GLSurfaceViewEGL14 view = this.mGLViewWeakRef.get();
                        if (view != null && view.allowDraw) view.mRenderer.onDrawFrame();
                    }
                }

                //int swapError = this.mEglHelper.swap();
                int swapError = swap();
                // Thread.sleep(2);

                switch (swapError) {
                    case EGL14.EGL_SUCCESS:
                        break;

                    case EGL14.EGL_CONTEXT_LOST:

                        if (GLSurfaceViewEGL14.LOG_SURFACE) {
                            Log.i(GLSurfaceViewEGL14.TAG.concat(getThreadStr()), "egl context lost tid=" + Long.toString(this.getId()));
                        }

                        lostEglContext = true;
                        break;

                    default:

                        // Other errors typically mean that the current surface is bad,
                        // probably because the SurfaceView surface has been destroyed,
                        // but we haven't been notified yet.
                        // Log the error to help developers understand why rendering stopped.
                        //GLSurfaceViewEGL14.EglHelper.logEglErrorAsWarning(GLSurfaceViewEGL14.TAG.concat(".GLThread"), "eglSwapBuffers", swapError);

                        synchronized (GLSurfaceViewEGL14.sGLThreadManager) {
                            this.mSurfaceIsBad = true;
                            GLSurfaceViewEGL14.sGLThreadManager.notifyAll();
                        }
                        break;
                }

                if (wantRenderNotification == true) {
                    doRenderNotification = true;
                }
            }
        } finally {
            /*
             * clean-up everything...
             */
            synchronized (GLSurfaceViewEGL14.sGLThreadManager) {
                this.stopEglSurfaceLocked();
                this.stopEglContextLocked();
            }
        }
    }

    public boolean ableToDraw() {
        return this.mHaveEglContext && this.mHaveEglSurface && this.readyToDraw();
    }

    private boolean readyToDraw() {
        return (!this.mPaused) && this.mHasSurface && (!this.mSurfaceIsBad) && (this.mWidth > 0) && (this.mHeight > 0) && (this.mRequestRender || (this.mRenderMode == GLSurfaceViewEGL14.RENDERMODE_CONTINUOUSLY));
    }

    public void setRenderMode(final int renderMode) {
        if (!((GLSurfaceViewEGL14.RENDERMODE_WHEN_DIRTY <= renderMode) && (renderMode <= GLSurfaceViewEGL14.RENDERMODE_CONTINUOUSLY))) {
            throw new IllegalArgumentException("renderMode");
        }
        synchronized (GLSurfaceViewEGL14.sGLThreadManager) {
            this.mRenderMode = renderMode;
            GLSurfaceViewEGL14.sGLThreadManager.notifyAll();
        }
    }

    public int getRenderMode() {
        synchronized (GLSurfaceViewEGL14.sGLThreadManager) {
            return this.mRenderMode;
        }
    }

    public void requestRender() {
        synchronized (GLSurfaceViewEGL14.sGLThreadManager) {
            if (this.mRequestPaused == false) {
                this.mRequestRender = true;
                GLSurfaceViewEGL14.sGLThreadManager.notifyAll();
            }
        }
    }

    public void surfaceCreated() {
        synchronized (GLSurfaceViewEGL14.sGLThreadManager) {
            if (GLSurfaceViewEGL14.LOG_THREADS) {
                Log.i(GLSurfaceViewEGL14.TAG.concat(getThreadStr()), "surfaceCreated tid=" + Long.toString(this.getId()));
            }

            this.mHasSurface = true;
            this.mFinishedCreatingEglSurface = false;
            GLSurfaceViewEGL14.sGLThreadManager.notifyAll();

            waitMe(() -> (this.mWaitingForSurface) && (!this.mFinishedCreatingEglSurface) && (!this.mExited), null);

//            while ((this.mWaitingForSurface) && (!this.mFinishedCreatingEglSurface) && (!this.mExited)) {
//                try {
//                    GLSurfaceViewEGL14.sGLThreadManager.wait();
//                } catch (InterruptedException e) {
//                    Thread.currentThread().interrupt();
//                }
//            }
        }
    }

    public void surfaceDestroyed() {
        synchronized (GLSurfaceViewEGL14.sGLThreadManager) {
            if (GLSurfaceViewEGL14.LOG_THREADS) {
                Log.i(GLSurfaceViewEGL14.TAG.concat(getThreadStr()), "surfaceDestroyed tid=" + Long.toString(this.getId()));
            }

            this.mHasSurface = false;
            GLSurfaceViewEGL14.sGLThreadManager.notifyAll();

            waitMe(() -> (this.mWaitingForSurface == false) && (this.mExited == false), null);

//            while ((this.mWaitingForSurface == false) && (this.mExited == false)) {
//                try {
//                    GLSurfaceViewEGL14.sGLThreadManager.wait();
//                } catch (InterruptedException e) {
//                    Thread.currentThread().interrupt();
//                }
//            }
        }
    }

    public void onPause() {
        synchronized (GLSurfaceViewEGL14.sGLThreadManager) {
            if (GLSurfaceViewEGL14.LOG_PAUSE_RESUME) {
                Log.i(GLSurfaceViewEGL14.TAG.concat(getThreadStr()), "onPause tid=" + Long.toString(this.getId()));
            }

            this.mRequestPaused = true;
            GLSurfaceViewEGL14.sGLThreadManager.notifyAll();

            waitMe(() -> (this.mExited == false) && (this.mPaused == false), () -> {
                if (GLSurfaceViewEGL14.LOG_PAUSE_RESUME) {
                    Log.i(GLSurfaceViewEGL14.TAG.concat(getThreadStr()), "onPause waiting for mPaused.");
                }
            });

//            while ((this.mExited == false) && (this.mPaused == false)) {
//                if (GLSurfaceViewEGL14.LOG_PAUSE_RESUME) {
//                    Log.i(GLSurfaceViewEGL14.TAG.concat(getThreadStr()), "onPause waiting for mPaused.");
//                }
//
//                try {
//                    GLSurfaceViewEGL14.sGLThreadManager.wait();
//                } catch (InterruptedException ex) {
//                    Thread.currentThread().interrupt();
//                }
//            }
        }
    }

    public void onResume() {
        synchronized (GLSurfaceViewEGL14.sGLThreadManager) {
            if (GLSurfaceViewEGL14.LOG_PAUSE_RESUME) {
                Log.i(GLSurfaceViewEGL14.TAG.concat(getThreadStr()), "onResume tid=" + Long.toString(this.getId()));
            }

            this.mRequestPaused = false;
            this.mRequestRender = true;
            this.mRenderComplete = false;
            GLSurfaceViewEGL14.sGLThreadManager.notifyAll();

            waitMe(() -> (this.mExited == false) && this.mPaused == true && (this.mRenderComplete == false), () -> {
                if (GLSurfaceViewEGL14.LOG_PAUSE_RESUME) {
                    Log.i(GLSurfaceViewEGL14.TAG.concat(getThreadStr()), "onResume waiting for !mPaused.");
                }
            });

//            while ((this.mExited == false) && this.mPaused == true && (this.mRenderComplete == false)) {
//                if (GLSurfaceViewEGL14.LOG_PAUSE_RESUME) {
//                    Log.i(GLSurfaceViewEGL14.TAG.concat(getThreadStr()), "onResume waiting for !mPaused.");
//                }
//
//                try {
//                    GLSurfaceViewEGL14.sGLThreadManager.wait();
//                } catch (InterruptedException ex) {
//                    Thread.currentThread().interrupt();
//                }
//            }
        }
    }

    public void onWindowResize(final int w, final int h) {
        synchronized (GLSurfaceViewEGL14.sGLThreadManager) {
            if (GLSurfaceViewEGL14.LOG_SURFACE) {
                Log.i(GLSurfaceViewEGL14.TAG.concat(getThreadStr()), "onWindowResize tid=" + Long.toString(this.getId()));
            }

            this.mWidth = w;
            this.mHeight = h;
            this.mSizeChanged = true;
            this.mRequestRender = true;
            this.mRenderComplete = false;

            GLSurfaceViewEGL14.sGLThreadManager.notifyAll();

            waitMe(() -> this.mExited == false && this.mPaused == false && this.mRenderComplete == false && this.ableToDraw() == true, () -> {
                if (GLSurfaceViewEGL14.LOG_SURFACE) {
                    Log.i(GLSurfaceViewEGL14.TAG.concat(getThreadStr()), "onWindowResize waiting for render complete from tid=" + Long.toString(this.getId()));
                }
            });

            // Wait for thread to react to resize and render a frame
//            while (this.mExited == false && this.mPaused == false && this.mRenderComplete == false && this.ableToDraw() == true) {
//                if (GLSurfaceViewEGL14.LOG_SURFACE) {
//                    Log.i(GLSurfaceViewEGL14.TAG.concat(getThreadStr()), "onWindowResize waiting for render complete from tid=" + Long.toString(this.getId()));
//                }
//
//                try {
//                    GLSurfaceViewEGL14.sGLThreadManager.wait();
//                } catch (InterruptedException ex) {
//                    Thread.currentThread().interrupt();
//                }
//            }
        }
    }

    public void requestExitAndWait() {
        // don't call this from GLThread thread or it is a guaranteed deadlock!
        synchronized (GLSurfaceViewEGL14.sGLThreadManager) {
            if (GLSurfaceViewEGL14.LOG_SURFACE) {
                Log.i(GLSurfaceViewEGL14.TAG.concat(getThreadStr()), "requestExitAndWait tid=" + Long.toString(this.getId()));
            }

            synchronized (mShouldExitLo) { this.mShouldExit = true; }
            GLSurfaceViewEGL14.sGLThreadManager.notifyAll();

            waitMe(() -> this.mExited == false, () -> {
                if (GLSurfaceViewEGL14.LOG_SURFACE) {
                    Log.i(GLSurfaceViewEGL14.TAG.concat(getThreadStr()), "requestExitAndWait waiting for mExited from tid=" + Long.toString(this.getId()));
                }
            });

//            while (this.mExited == false) {
//                if (GLSurfaceViewEGL14.LOG_SURFACE) {
//                    Log.i(GLSurfaceViewEGL14.TAG.concat(getThreadStr()), "requestExitAndWait waiting for mExited from tid=" + Long.toString(this.getId()));
//                }
//
//                try {
//                    GLSurfaceViewEGL14.sGLThreadManager.wait();
//                } catch (InterruptedException ex) {
//                    Thread.currentThread().interrupt();
//                }
//            }
        }
    }

    static String getThreadStr() {
        return ThreadUtil.Companion.isGuiThread() ? ".Main-Thread" : ".GLThread";
    }

    private interface Waitable { void onBefore(); }
    private interface Predicatable { boolean calc(); }
    private void waitMe(Predicatable predicate, Waitable waiter) {
        while (predicate.calc()) {
            if (waiter != null)
                waiter.onBefore();
            if (!waitTillNotify())
                break;
        }
    }

    private boolean waitTillNotify() { return waitTillNotify(0); }
    private boolean waitTillNotify(long timeout) {
        if (ThreadUtil.Companion.isGuiThread())
            return false/*stop waiting*/;

        synchronized (GLSurfaceViewEGL14.sGLThreadManager) {
            try { GLSurfaceViewEGL14.sGLThreadManager.wait(timeout); }
            catch (InterruptedException ex) { Thread.currentThread().interrupt(); }
        }

        return true/*can continue waiting*/;
    }

    public void requestReleaseEglContextLocked() {
        this.mShouldReleaseEglContext = true;
        GLSurfaceViewEGL14.sGLThreadManager.notifyAll();
    }

    /**
     * Queue an "event" to be run on the GL rendering thread.
     *
     * @param runnable the runnable to be run on the GL rendering thread.
     */
    public void queueEvent(final Runnable runnable) {
        if (runnable == null) {
            throw new IllegalArgumentException("'runnable' must not be null");
        }

        synchronized (GLSurfaceViewEGL14.sGLThreadManager) {
            this.mEventQueue.add(runnable);
            Log.d(GLSurfaceViewEGL14.TAG, "Queued events: ".concat(this.mEventQueue == null ? "0" : Integer.toString(this.mEventQueue.size())));

            GLSurfaceViewEGL14.sGLThreadManager.notifyAll();
        }
    }

    // Once the thread is started, all accesses to the following member
    // variables are protected by the sGLThreadManager monitor
    private boolean mShouldExit;
    private Object mShouldExitLo = new Object(); // нам нужна дополнительная синхронизация самого метода отрисовки и флага необходимости выхода
                                                 // ибо после того как мы перестали блокировать ГУИ поток вейтами при выходе может падать внутри отрисовки
                                                 // ибо мы уже вышли а оно еще рисует

    protected boolean mExited;
    private boolean mRequestPaused;
    private boolean mPaused;
    private boolean mHasSurface;
    private boolean mSurfaceIsBad;
    private boolean mWaitingForSurface;
    private boolean mHaveEglContext;
    private boolean mHaveEglSurface;
    private boolean mFinishedCreatingEglSurface;
    private boolean mShouldReleaseEglContext;
    private int mWidth;
    private int mHeight;
    private int mRenderMode;
    private boolean mRequestRender;
    private boolean mRenderComplete;
    public final ArrayList<Runnable> mEventQueue = new ArrayList<>();
    private boolean mSizeChanged = true;

    // End of member variables protected by the sGLThreadManager monitor.

    //GLSurfaceViewEGL14.EglHelper mEglHelper;

    /**
     * Set once at thread construction time, nulled out when the parent view is garbage called. This weak reference allows the GLSurfaceViewEGL14
     * to be garbage collected while the GLThread is still alive.
     */
}
