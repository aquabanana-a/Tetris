/*
 * Created by S.Dobranos on 19.11.20 15:02
 * Copyright (c) 2020. All rights reserved.
 */

package com.fromfinalform.tetris.presentation.model.repository

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES20
import com.fromfinalform.tetris.presentation.model.opengl.common.GLUtils

class TextureRepository(val context: Context) : ITextureRepository {

    private val texturesByAssetId = hashMapOf<Int, Int>()
    private val texturesLo = Any()

    override fun get(assetId: Int): Int { synchronized(texturesLo) {
        var textureId = texturesByAssetId[assetId]
        if (textureId == null) {
            var bmp = BitmapFactory.decodeResource(context.resources, assetId)
            textureId = GLUtils.bitmap2texture(bmp, GLES20.GL_TEXTURE_2D, null, true, "")
            bmp.recycle()
            texturesByAssetId[assetId] = textureId
        }

        return textureId
    } }

    override fun get(path: String): Int {
        TODO("Not yet implemented")
    }
}