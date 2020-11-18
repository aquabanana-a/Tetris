/*
 * Created by S.Dobranos on 17.11.20 15:25
 * Copyright (c) 2020. All rights reserved.
 */

package com.fromfinalform.tetris.presentation.opengl.renderUnit

import android.opengl.GLES20
import com.fromfinalform.tetris.data.repository.StaticShaderDrawerRepository
import com.fromfinalform.tetris.domain.model.renderer.IRenderUnit
import com.fromfinalform.tetris.domain.model.renderer.IRenderer
import com.fromfinalform.tetris.domain.model.renderer.ShaderDrawerTypeId
import com.fromfinalform.tetris.domain.repository.IShaderDrawerRepository
import com.fromfinalform.tetris.presentation.opengl.SceneParams

class RenderUnit : RenderItem(), IRenderUnit {

    companion object {
        var shaderRepo: IShaderDrawerRepository? = null
    }

    override fun render(renderer: IRenderer, params: SceneParams, timeMs: Long, deltaTimeMs: Long) {
        renderImpl(this, renderer, params, timeMs, deltaTimeMs)
    }

    private fun renderImpl(item: RenderItem, renderer: IRenderer, params: SceneParams, timeMs: Long, deltaTimeMs: Long) {
        val drawer = shaderRepo!![item.shaderTypeId]

        when (item.shaderTypeId) {
            ShaderDrawerTypeId.SOLID    -> drawer!!.setUniforms(item.color)
            ShaderDrawerTypeId.FLAT     -> drawer!!.setUniforms(item.textureId ?: -1)
        }

        if (item.usedBlend) {
            GLES20.glEnable(GLES20.GL_BLEND)
            if (item.usedBlendSeparate) GLES20.glBlendFuncSeparate(item.blendSrcRGB!!, item.blendDstRGB!!, item.blendSrcAlpha!!, item.blendDstAlpha!!)
            else if (item. usedBlendFactor) GLES20.glBlendFunc(item.blendSrc, item.blendDst)
        } else GLES20.glDisable(GLES20.GL_BLEND)

        if (drawer != null) {
            drawer.draw(this, params, item.dstRect, item.srcRect, item.rotation)
            drawer.cleanUniforms()
        }

        if (item.childs != null)
            for (c in item.childs!!)
                renderImpl(c, renderer, params, timeMs, deltaTimeMs)
    }

    override fun prerender(renderer: IRenderer) {

    }

    override fun postrender(renderer: IRenderer) {

    }


}