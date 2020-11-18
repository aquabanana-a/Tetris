/*
 * Created by S.Dobranos on 17.11.20 15:25
 * Copyright (c) 2020. All rights reserved.
 */

package com.fromfinalform.tetris.presentation.opengl;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.opengl.EGL14;
import android.opengl.EGLContext;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.fromfinalform.tetris.domain.model.renderer.IRenderer;

import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;

/**
 * GLSurfaceView using EGl14 instead of EGL10
 *
 * @author Perraco Labs (August-2015)
 * @repository https://github.com/perracolabs/GLSurfaceViewEGL14
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
public class GLSurfaceViewEGL14 extends SurfaceView implements SurfaceHolder.Callback {
    OnScreenshotDoneListener onScreenshotDone = null;
    Runnable onStaticImageReadyCallback = null;
    private EGLContext externalEglContext = EGL14.EGL_NO_CONTEXT;

    public void setExtGlContext(@Nullable EGLContext eglContext) {
        this.checkRenderThreadState();
        externalEglContext = eglContext;
    }

    public interface OnScreenshotDoneListener {
        public void onScreenshot(Bitmap bmp);
    }

    static final String TAG = "GLSurfaceViewEGL14";
    final static boolean LOG_ATTACH_DETACH          = false;
    final static boolean LOG_PAUSE_RESUME           = false;
    final static boolean LOG_SURFACE                = false;
    final static boolean LOG_RENDERER               = false;
    final static boolean LOG_EGL                    = false;
    final static boolean LOG_THREADS                = false;
    final static boolean LOG_THREADS_WAIT           = false;
    final static boolean LOG_RENDERER_DRAW_FRAME    = false;
    public Bitmap staticBitmap = null;
    public boolean allowDraw = true;

    /**
     * The renderer only renders when the surface is created, or when {@link #requestRender} is called.
     *
     * @see #setRenderMode(int)
     * @see #requestRender()
     */
    public final static int RENDERMODE_WHEN_DIRTY = 0;

    /**
     * The renderer is called continuously to re-render the scene.
     *
     * @see #setRenderMode(int)
     */
    public final static int RENDERMODE_CONTINUOUSLY = 1;

    /**
     * Check glError() after every GL call and throw an exception if glError indicates that an error has occurred. This can be used to help track down
     * which OpenGL ES call is causing an error.
     * <p>
     * see getDebugFlags, setDebugFlags
     */
    private final static int DEBUG_CHECK_GL_ERROR = 1;

    /**
     * Log GL calls to the system log at "verbose" level with tag "GLSurfaceViewEGL14".
     * <p>
     * see #getDebugFlags, setDebugFlags
     */
    private final static int DEBUG_LOG_GL_CALLS = 2;

    boolean requestStaticBitmap = false;
    boolean isStatic = false;

    /**
     * Standard View constructor. In order to render something, you must call {@link #setRenderer} to register a renderer.
     *
     * @param context Context used for operations
     */
    public GLSurfaceViewEGL14(final Context context) {
        super(context);
        this.setWillNotDraw(false);
        this.init(context);
    }

    /**
     * Standard View constructor. In order to render something, you must call {@link #setRenderer} to register a renderer.
     *
     * @param context Context used for operations
     * @param attrs   Attributes
     */
    public GLSurfaceViewEGL14(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        this.setWillNotDraw(false);
        this.init(context);
    }

    @Override
    public void draw(Canvas canvas) {
        if (isStatic && staticBitmap != null && !staticBitmap.isRecycled() && !requestStaticBitmap) {
            synchronized (staticBitmap) {
                canvas.drawBitmap(staticBitmap, 0, 0, null);
            }
        } else super.draw(canvas);
    }

    public void takeScreenShot(OnScreenshotDoneListener onScreenshotDone) {
        this.onScreenshotDone = onScreenshotDone;
        requestRender();
    }

    public void setStaticState(boolean isStatic, Runnable onReady) {
        this.isStatic = isStatic;
        this.onStaticImageReadyCallback = onReady;
        if(isStatic) {
            requestStaticBitmap = true;
            requestRender();
        } else staticBitmap = null;
        invalidate();
    }

    /**
     * Sets the surface to use the EGL_RECORDABLE_ANDROID flag
     * <p>
     * To take effect must be called before than setRenderer()
     *
     * @param recordable True to set the recordable flag
     */
    public void setRecordable(final boolean recordable) {
        this.mRecordable = recordable;
        Log.i(GLSurfaceViewEGL14.TAG, "Updated recordable flag. State: ".concat(Boolean.toString(recordable)));
    }

    /**
     * @see Object#finalize()
     */
    @Override
    protected void finalize() throws Throwable {
        if (GLSurfaceViewEGL14.LOG_THREADS)
            Log.i(GLSurfaceViewEGL14.TAG.concat(GlThread14.getThreadStr()), "finalize requested tid=" + Long.toString(this.getId()));
        try {
            if (this.mGLThread != null) {
                // GLThread may still be running if this view was never attached to a window.
                this.mGLThread.requestExitAndWait();
            }
        } finally {
            if (GLSurfaceViewEGL14.LOG_THREADS)
                Log.i(GLSurfaceViewEGL14.TAG.concat(GlThread14.getThreadStr()), "finalize processing tid=" + Long.toString(this.getId()));
            super.finalize();
        }
    }

    private void init(final Context context) {
        // Request an 2.0 OpenGL ES compatible context
        this.setEGLContextClientVersion(2);

        if ((context.getApplicationContext().getApplicationInfo().flags &= ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
            this.setDebugFlags(GLSurfaceViewEGL14.DEBUG_LOG_GL_CALLS | GLSurfaceViewEGL14.DEBUG_CHECK_GL_ERROR);
        }

        this.hookCallbacks();
    }

    /**
     * Install a SurfaceHolder.Callback so we get notified when the underlying surface is created and destroyed
     */
    private void hookCallbacks() {
        SurfaceHolder holder = this.getHolder();
        holder.addCallback(this);
    }

    /**
     * Set the debug flags to a new value. The value is constructed by OR-together zero or more of the DEBUG_CHECK_* constants. The debug flags take
     * effect whenever a surface is created. The default value is zero.
     *
     * @param debugFlags the new debug flags see DEBUG_CHECK_GL_ERROR see DEBUG_LOG_GL_CALLS
     */
    public void setDebugFlags(final int debugFlags) {
        this.mDebugFlags = debugFlags;
    }

    /**
     * Get the current value of the debug flags.
     *
     * @return the current value of the debug flags.
     */
    public int getDebugFlags() {
        return this.mDebugFlags;
    }

    /**
     * Control whether the EGL context is preserved when the GLSurfaceViewEGL14 is paused and resumed.
     * <p>
     * If set to true, then the EGL context may be preserved when the GLSurfaceViewEGL14 is paused. Whether the EGL context is actually preserved or
     * not depends upon whether the Android device that the program is running on can support an arbitrary number of EGL contexts or not. Devices that
     * can only support a limited number of EGL contexts must release the EGL context in order to allow multiple applications to share the GPU.
     * <p>
     * If set to false, the EGL context will be released when the GLSurfaceViewEGL14 is paused, and recreated when the GLSurfaceViewEGL14 is resumed.
     * <p>
     * <p>
     * The default is false.
     *
     * @param preserveOnPause preserve the EGL context when paused
     */
    public void setPreserveEGLContextOnPause(final boolean preserveOnPause) {
        this.mPreserveEGLContextOnPause = preserveOnPause;
    }

    /**
     * @return true if the EGL context will be preserved when paused
     */
    public boolean getPreserveEGLContextOnPause() {
        return this.mPreserveEGLContextOnPause;
    }

    /**
     * Set the renderer associated with this view. Also starts the thread that will call the renderer, which in turn causes the rendering to start.
     * <p>
     * This method should be called once and only once in the life-cycle of a GLSurfaceViewEGL14. The following GLSurfaceViewEGL14 methods can only be
     * called <em>after</em> setRenderer is called:
     * <ul>
     * <li>{@link #getRenderMode()}
     * <li>{@link #onPause()}
     * <li>{@link #onResume()}
     * <li>{@link #queueEvent(Runnable)}
     * <li>{@link #requestRender()}
     * <li>{@link #setRenderMode(int)}
     * </ul>
     *
     * @param renderer the renderer to use to perform OpenGL drawing
     */
    public void setRenderer(final IRendererEGL14 renderer) {
        this.checkRenderThreadState();

        if (this.mEGLContextFactory == null) {
            this.mEGLContextFactory = new DefaultContextFactory();
        }

        if (this.mEGLWindowSurfaceFactory == null) {
            this.mEGLWindowSurfaceFactory = new DefaultWindowSurfaceFactory();
        }

        this.mRenderer = renderer;
        this.mGLThread = new GlThread14(this.mThisWeakRef);
        this.mGLThread.start();
    }

    public IRenderer getRenderer() {
        return (IRenderer) mRenderer;
    }

    /**
     * Install a custom EGLContextFactory.
     * <p>
     * If this method is called, it must be called before {@link #setRenderer(IRendererEGL14)} is called.
     * <p>
     * If this method is not called, then by default a context will be created with no shared context and with a null attribute list.
     *
     * @param factory Factory context
     */
    public void setEGLContextFactory(final EGLContextFactory factory) {
        this.checkRenderThreadState();
        this.mEGLContextFactory = factory;
    }

    /**
     * Install a custom EGLWindowSurfaceFactory.
     * <p>
     * If this method is called, it must be called before {@link #setRenderer(IRendererEGL14)} is called.
     * <p>
     * If this method is not called, then by default a window surface will be created with a null attribute list.
     *
     * @param factory Factory context
     */
    public void setEGLWindowSurfaceFactory(final EGLWindowSurfaceFactory factory) {
        this.checkRenderThreadState();
        this.mEGLWindowSurfaceFactory = factory;
    }

    /**
     * Inform the default EGLContextFactory and default EGLConfigChooser which EGLContext client version to pick.
     * <p>
     * Use this method to create an OpenGL ES 2.0-compatible context. Example:
     *
     * <pre class="prettyprint">
     * public MyView(Context context)
     * {
     * 	super(context);
     * 	setEGLContextClientVersion(2); // Pick an OpenGL ES 2.0 context.
     * 	setRenderer(new MyRenderer());
     * }
     * </pre>
     * <p>
     * Note: Activities which require OpenGL ES 2.0 should indicate this by setting @lt;uses-feature android:glEsVersion="0x00020000" /> in the
     * activity's AndroidManifest.xml file.
     * <p>
     * If this method is called, it must be called before {@link #setRenderer(IRendererEGL14)} is called.
     * <p>
     *
     * @param version The EGLContext client version to choose. Use 2 for OpenGL ES 2.0
     */
    public void setEGLContextClientVersion(final int version) {
        this.checkRenderThreadState();
        this.mEGLContextClientVersion = version;
    }

    /**
     * Set the rendering mode. When renderMode is RENDERMODE_CONTINUOUSLY, the renderer is called repeatedly to re-render the scene. When renderMode
     * is RENDERMODE_WHEN_DIRTY, the renderer only rendered when the surface is created, or when {@link #requestRender} is called. Defaults to
     * RENDERMODE_CONTINUOUSLY.
     * <p>
     * Using RENDERMODE_WHEN_DIRTY can improve battery life and overall system performance by allowing the GPU and CPU to idle when the view does not
     * need to be updated.
     * <p>
     * This method can only be called after {@link #setRenderer(IRendererEGL14)}
     *
     * @param renderMode one of the RENDERMODE_X constants see RENDERMODE_CONTINUOUSLY see RENDERMODE_WHEN_DIRTY
     */
    public void setRenderMode(final int renderMode) {
        this.mGLThread.setRenderMode(renderMode);
    }

    /**
     * Get the current rendering mode. May be called from any thread. Must not be called before a renderer has been set.
     *
     * @return the current rendering mode. see RENDERMODE_CONTINUOUSLY see RENDERMODE_WHEN_DIRTY
     */
    public int getRenderMode() {
        return this.mGLThread.getRenderMode();
    }

    /**
     * Request that the renderer render a frame. This method is typically used when the render mode has been set to RENDERMODE_WHEN_DIRTY, so that
     * frames are only rendered on demand. May be called from any thread. Must not be called before a renderer has been set.
     */
    public void requestRender() {
        this.mGLThread.requestRender();
    }

    /**
     * This method is part of the SurfaceHolder.Callback interface, and is not normally called or subclassed by clients of GLSurfaceViewEGL14.
     */
    @Override
    public void surfaceCreated(final SurfaceHolder holder) {
        this.mGLThread.surfaceCreated();
    }

    /**
     * This method is part of the SurfaceHolder.Callback interface, and is not normally called or subclassed by clients of GLSurfaceViewEGL14.
     */
    @Override
    public void surfaceDestroyed(final SurfaceHolder holder) {
        // Surface will be destroyed when we return
        this.mGLThread.surfaceDestroyed();
    }

    /**
     * This method is part of the SurfaceHolder.Callback interface, and is not normally called or subclassed by clients of GLSurfaceViewEGL14.
     */
    @Override
    public void surfaceChanged(final SurfaceHolder holder, final int format, final int w, final int h) {
        this.mGLThread.onWindowResize(w, h);
    }

    /**
     * Inform the view that the activity is paused. The owner of this view must call this method when the activity is paused. Calling this method will
     * pause the rendering thread. Must not be called before a renderer has been set.
     */
    public void onPause() {
        if (this.mGLThread == null || this.mRenderer == null) {
            return;
        }

        this.mGLThread.onPause();
    }

    /**
     * Inform the view that the activity is resumed. The owner of this view must call this method when the activity is resumed. Calling this method
     * will recreate the OpenGL display and resume the rendering thread. Must not be called before a renderer has been set.
     */
    public void onResume() {
        if (this.mGLThread == null || this.mRenderer == null) {
            return;
        }

        this.hookCallbacks();
        this.mGLThread.onResume();
    }

    /**
     * Queue a runnable to be run on the GL rendering thread.
     * <p>
     * This can be used to communicate with the Renderer on the rendering thread.
     * <p>
     * Must not be called before a renderer has been set.
     *
     * @param runnable The runnable to be run on the GL rendering thread.
     */
    public void queueEvent(final Runnable runnable) {
        this.mGLThread.queueEvent(runnable);
    }

    /**
     * This method is used as part of the View class and is not normally called or sub-classed by clients of Control.
     */
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (GLSurfaceViewEGL14.LOG_ATTACH_DETACH) {
            Log.d(GLSurfaceViewEGL14.TAG, "onAttachedToWindow reattach: ".concat(Boolean.toString(this.mDetached)));
        }

        if (this.mDetached && (this.mRenderer != null)) {
            int renderMode = GLSurfaceViewEGL14.RENDERMODE_CONTINUOUSLY;

            if (this.mGLThread != null) {
                renderMode = this.mGLThread.getRenderMode();
            }

            this.mGLThread = new GlThread14(this.mThisWeakRef);

            if (renderMode != GLSurfaceViewEGL14.RENDERMODE_CONTINUOUSLY) {
                this.mGLThread.setRenderMode(renderMode);
            }

            this.mGLThread.start();
        }

        this.mDetached = false;
    }

    /**
     * This method is used as part of the View class and is not normally called or sub-classed by clients of the Control. Must not be called before a
     * renderer has been set.
     */
    @Override
    protected void onDetachedFromWindow() {
        if (GLSurfaceViewEGL14.LOG_ATTACH_DETACH) {
            Log.d(GLSurfaceViewEGL14.TAG, "Detaching from window.");
        }

        if (this.mGLThread != null) {
            this.mGLThread.requestExitAndWait();
        }

        this.mDetached = true;
        super.onDetachedFromWindow();
    }

    // ----------------------------------------------------------------------

    /**
     * An interface for customizing the eglCreateContext and eglDestroyContext calls.
     * <p>
     * This interface must be implemented by clients wishing to call {@link GLSurfaceViewEGL14#setEGLContextFactory(EGLContextFactory)}
     */
    public interface EGLContextFactory {
        /**
         * @param display   EGL Display
         * @param eglConfig EGL Configuration
         * @return EGL Context
         */
        EGLContext createContext(android.opengl.EGLDisplay display, android.opengl.EGLConfig eglConfig);

        /**
         * @param display EGL Display
         * @param context EGL Context
         */
        void destroyContext(android.opengl.EGLDisplay display, EGLContext context);
    }

    private class DefaultContextFactory implements EGLContextFactory {
        private final int EGL_CONTEXT_CLIENT_VERSION = 0x3098;

        public DefaultContextFactory() {
            // Empty
        }

        @Override
        public EGLContext createContext(final android.opengl.EGLDisplay display, final android.opengl.EGLConfig config) {
            final int[] attrib_list = {this.EGL_CONTEXT_CLIENT_VERSION, GLSurfaceViewEGL14.this.mEGLContextClientVersion, EGL14.EGL_NONE};

            return EGL14.eglCreateContext(display, config, externalEglContext, GLSurfaceViewEGL14.this.mEGLContextClientVersion != 0 ? attrib_list : null, 0);
        }

        @Override
        public void destroyContext(final android.opengl.EGLDisplay display, final EGLContext context) {
            if (EGL14.eglDestroyContext(display, context) == false) {
                Log.e(GLSurfaceViewEGL14.TAG.concat(".DefaultContextFactory"), "display:" + display + " context: " + context);

                if (GLSurfaceViewEGL14.LOG_THREADS) {
                    Log.i(GLSurfaceViewEGL14.TAG.concat(".DefaultContextFactory"), "tid=" + Long.toString(Thread.currentThread().getId()));
                }
                //EglHelper.throwEglException("eglDestroyContex", EGL14.eglGetError());
            }
        }
    }

    /**
     * An interface for customizing the eglCreateWindowSurface and eglDestroySurface calls.
     * <p>
     * This interface must be implemented by clients wishing to call {@link GLSurfaceViewEGL14#setEGLWindowSurfaceFactory(EGLWindowSurfaceFactory)}
     */
    public interface EGLWindowSurfaceFactory {
        /**
         * @param display      EGL Display
         * @param config       EGL Configuration
         * @param nativeWindow Native window
         * @return EGL Context or null if the surface cannot be constructed
         */
        android.opengl.EGLSurface createWindowSurface(android.opengl.EGLDisplay display, android.opengl.EGLConfig config, Object nativeWindow);

        /**
         * @param display EGL Display
         * @param surface Surface to be destroyed
         */
        void destroySurface(android.opengl.EGLDisplay display, android.opengl.EGLSurface surface);
    }

    private static class DefaultWindowSurfaceFactory implements EGLWindowSurfaceFactory {
        public DefaultWindowSurfaceFactory() {
            // Empty
        }

        @Override
        public android.opengl.EGLSurface createWindowSurface(final android.opengl.EGLDisplay display, final android.opengl.EGLConfig config, final Object nativeWindow) {
            android.opengl.EGLSurface result = null;

            try {
                final int[] surfaceAttribs = {EGL14.EGL_NONE};
                result = EGL14.eglCreateWindowSurface(display, config, nativeWindow, surfaceAttribs, 0);
            } catch (Throwable ex) {
                //CrashlyticsHelper.INSTANCE.logInfo(ex);

                // This exception indicates that the surface flinger surface
                // is not valid. This can happen if the surface flinger surface has
                // been torn down, but the application has not yet been
                // notified via SurfaceHolder.Callback.surfaceDestroyed.
                // In theory the application should be notified first,
                // but in practice sometimes it is not. See b/4588890
                Log.e(GLSurfaceViewEGL14.TAG, "eglCreateWindowSurface call failed", ex);
            } finally {
                if (result == null) {
                    try {
                        // Hack to avoid pegged CPU bug
                        Thread.sleep(10);
                    } catch (InterruptedException t) {
                        //CrashlyticsHelper.INSTANCE.logInfo(t);
                        Log.e(GLSurfaceViewEGL14.TAG, "CPU was pegged");
                    }
                }
            }

            return result;
        }

        @Override
        public void destroySurface(final android.opengl.EGLDisplay display, final android.opengl.EGLSurface surface) {
            if (EGL14.eglDestroySurface(display, surface) == false) {
                Log.e(GLSurfaceViewEGL14.TAG, "eglDestroySurface Failed");
            }
        }
    }

    /**
     * A generic GL Thread. Takes care of initializing EGL and GL. Delegates to a IRendererEGL14 instance to do the actual drawing. Can be configured
     * to render continuously or on request.
     * <p>
     * All potentially blocking synchronization is done through the sGLThreadManager object. This avoids multiple-lock ordering issues.
     */

    private void checkRenderThreadState() {
        if (this.mGLThread != null) {
            throw new IllegalStateException("setRenderer has already been called for this instance.");
        }
    }

    static class GLThreadManager {
        public GLThreadManager() {
            Log.i(GLSurfaceViewEGL14.TAG.concat(".GLThreadManager"), "GLThreadManager instance created");
        }

        public synchronized void threadExiting(final GlThread14 thread) {
            if (GLSurfaceViewEGL14.LOG_THREADS) {
                Log.i(GLSurfaceViewEGL14.TAG.concat(".GLThreadManager"), "Exiting tid=" + Long.toString(thread.getId()));
            }

            thread.mExited = true;

            if (this.mEglOwner == thread) {
                this.mEglOwner = null;
            }

            this.notifyAll();
        }

        /*
         * Tries once to acquire the right to use an EGL context. Does not block. Requires that we are already in the sGLThreadManager monitor when
         * this is called.
         *
         * @return true if the right to use an EGL context was acquired.
         */
        public boolean tryAcquireEglContextLocked(final GlThread14 thread) {
            if (this.mEglOwner == thread || this.mEglOwner == null) {
                this.mEglOwner = thread;
                this.notifyAll();
            }

            return true;
        }

        /*
         * Releases the EGL context. Requires that we are already in the sGLThreadManager monitor when this is called.
         */
        public void releaseEglContextLocked(final GlThread14 thread) {
            if (this.mEglOwner == thread) {
                this.mEglOwner = null;
            }

            this.notifyAll();
        }

        public synchronized boolean shouldReleaseEGLContextWhenPausing() {
            // Release the EGL context when pausing even if
            // the hardware supports multiple EGL contexts.
            // Otherwise the device could run out of EGL contexts.
            return this.mLimitedGLESContexts;
        }

        /**
         * This check was required for some pre-Android-3.0 hardware. Android 3.0 provides support for hardware-accelerated views, therefore multiple
         * EGL contexts are supported on all Android 3.0+ EGL drivers.
         */
        private boolean mLimitedGLESContexts;
        private GlThread14 mEglOwner;
    }

    public EGLContext getEglContext() {
        return mGLThread.mEglContext;
    }

    /**
     * Gets a GL Error string
     *
     * @param error Error to be resolve
     * @return Resolved error string
     */
    protected static String getErrorString(final int error) {
        Thread.dumpStack();

        switch (error) {
            case EGL14.EGL_SUCCESS:
                return "EGL_SUCCESS";
            case EGL14.EGL_NOT_INITIALIZED:
                return "EGL_NOT_INITIALIZED";
            case EGL14.EGL_BAD_ACCESS:
                return "EGL_BAD_ACCESS";
            case EGL14.EGL_BAD_ALLOC:
                return "EGL_BAD_ALLOC";
            case EGL14.EGL_BAD_ATTRIBUTE:
                return "EGL_BAD_ATTRIBUTE";
            case EGL14.EGL_BAD_CONFIG:
                return "EGL_BAD_CONFIG";
            case EGL14.EGL_BAD_CONTEXT:
                return "EGL_BAD_CONTEXT";
            case EGL14.EGL_BAD_CURRENT_SURFACE:
                return "EGL_BAD_CURRENT_SURFACE";
            case EGL14.EGL_BAD_DISPLAY:
                return "EGL_BAD_DISPLAY";
            case EGL14.EGL_BAD_MATCH:
                return "EGL_BAD_MATCH";
            case EGL14.EGL_BAD_NATIVE_PIXMAP:
                return "EGL_BAD_NATIVE_PIXMAP";
            case EGL14.EGL_BAD_NATIVE_WINDOW:
                return "EGL_BAD_NATIVE_WINDOW";
            case EGL14.EGL_BAD_PARAMETER:
                return "EGL_BAD_PARAMETER";
            case EGL14.EGL_BAD_SURFACE:
                return "EGL_BAD_SURFACE";
            case EGL14.EGL_CONTEXT_LOST:
                return "EGL_CONTEXT_LOST";
            default:
                return "0x" + Integer.toHexString(error);
        }
    }

    protected static final GLThreadManager sGLThreadManager = new GLThreadManager();

    private final WeakReference<GLSurfaceViewEGL14> mThisWeakRef = new WeakReference<>(this);
    private GlThread14 mGLThread;
    protected IRendererEGL14 mRenderer;
    private boolean mDetached;
    protected EGLContextFactory mEGLContextFactory;
    protected EGLWindowSurfaceFactory mEGLWindowSurfaceFactory;
    protected int mDebugFlags;
    protected int mEGLContextClientVersion;
    protected boolean mPreserveEGLContextOnPause;
    protected boolean mRecordable;
}
