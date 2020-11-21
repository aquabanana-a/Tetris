/*
 * Created by S.Dobranos on 19.11.20 0:07
 * Copyright (c) 2020. All rights reserved.
 */

package com.fromfinalform.tetris.presentation.model.common

import android.graphics.PointF
import kotlin.math.*

object GeometryUtil {

    fun triangleArea(A: PointF, B: PointF, C: PointF): Float {
        return abs((A.x * (B.y - C.y) + B.x * (C.y - A.y) + C.x * (A.y - B.y)) * 0.5).toFloat();
    }

    fun rectangleArea(ABC: FloatArray): Float = rectangleArea(PointF(ABC[0], ABC[1]), PointF(ABC[2], ABC[3]), PointF(ABC[4], ABC[5]))
    fun rectangleArea(ABC: ArrayList<PointF>): Float = rectangleArea(ABC[0], ABC[1], ABC[2])
    fun rectangleArea(A: PointF, B: PointF, C: PointF): Float {
        return pointDistance(A, B) * pointDistance(B, C)
    }

    fun pointInRect(p: PointF, r: ArrayList<PointF>): Boolean {
        var area = rectangleArea(r)
        val aAPD = triangleArea(r[0], p, r[3])
        val aDPC = triangleArea(r[3], p, r[2])
        val aCPB = triangleArea(r[2], p, r[1])
        val aBPA = triangleArea(r[1], p, r[0])
        return (aAPD + aDPC + aCPB + aBPA <= area + 0.0001f)
    }

    fun pointInTriangle(p: PointF, r: ArrayList<PointF>): Boolean {
        val a = (r[0].x - p.x) * (r[1].y - r[0].y) - (r[1].x - r[0].x) * (r[0].y - p.y)
        val b = (r[1].x - p.x) * (r[2].y - r[1].y) - (r[2].x - r[1].x) * (r[1].y - p.y)
        val c = (r[2].x - p.x) * (r[0].y - r[2].y) - (r[0].x - r[2].x) * (r[2].y - p.y)

        return (a >= 0 && b >= 0 && c >= 0) || (a <= 0 && b <= 0 && c <= 0)
    }

    // 0 1
    // 3 2
    fun pointInRhombus(p: PointF, r: ArrayList<PointF>): Boolean {
        return pointInTriangle(p, arrayListOf(r[0], r[1], r[2])) || pointInTriangle(p, arrayListOf(r[0], r[2], r[3]))
    }

    fun pointDistance(p1: PointF, p2: PointF): Float {
        return hypot(abs(p2.y - p1.y), abs(p2.x - p1.x))
    }

    fun rotatePoint(point: PointF, pivot: PointF, angleDeg: Float) {
        if(angleDeg % 360 == 0f)
            return

        val angleRad = Math.toRadians(angleDeg.toDouble()) //angleDeg * PI / 180.0

        var x1 = point.x - pivot.x;
        var y1 = point.y - pivot.y;

        var temp_x1 = x1 * cos(angleRad) - y1 * sin(angleRad)
        var temp_y1 = x1 * sin(angleRad) + y1 * cos(angleRad)

        point.x = (temp_x1 + pivot.x).toFloat()
        point.y = (temp_y1 + pivot.y).toFloat()
    }
}


//            "vec2 vect2d(vec2 p1, vec2 p2) {                                                                                \n" +
//            "  vec2 temp;                                                                                                   \n" +
//            "  temp.x = (p2.x - p1.x);                                                                                      \n" +
//            "  temp.y = -1.0 * (p2.y - p1.y);                                                                               \n" +
//            "  return temp;                                                                                                 \n" +
//            "}                                                                                                              \n" +
//            "                                                                                                               \n" +
//            "float pointInRectangle(vec2 m, vec2 A, vec2 B, vec2 C, vec2 D) {                                               \n" +
//            "  vec2 AB = vect2d(A, B);  float C1 = -1.0 * (AB.y*A.x + AB.x*A.y);  float D1 = (AB.y*m.x + AB.x*m.y) + C1;    \n" +
//            "  vec2 AD = vect2d(A, D);  float C2 = -1.0 * (AD.y*A.x + AD.x*A.y);  float D2 = (AD.y*m.x + AD.x*m.y) + C2;    \n" +
//            "  vec2 BC = vect2d(B, C);  float C3 = -1.0 * (BC.y*B.x + BC.x*B.y);  float D3 = (BC.y*m.x + BC.x*m.y) + C3;    \n" +
//            "  vec2 CD = vect2d(C, D);  float C4 = -1.0 * (CD.y*C.x + CD.x*C.y);  float D4 = (CD.y*m.x + CD.x*m.y) + C4;    \n" +
//            "  return 0.0 >= D1 && 0.0 >= D4 && 0.0 <= D2 && 0.0 >= D3 ? 1.0 : 0.0;                                         \n" +
//            "}                                                                                                              \n" +
//            "                                                                                                               \n" +
//            "float insideRect(vec2 m, vec2 r[4]) {                                                                          \n" +
//            "  vec2 AB = r[1] - r[0];                                                                                       \n" +
//            "  vec2 AM = m - r[0];                                                                                          \n" +
//            "  vec2 BC = r[2] - r[1];                                                                                       \n" +
//            "  vec2 BM = m - r[1];                                                                                          \n" +
//            "  float dotABAM = dot(AB, AM);                                                                                 \n" +
//            "  float dotABAB = dot(AB, AB);                                                                                 \n" +
//            "  float dotBCBM = dot(BC, BM);                                                                                 \n" +
//            "  float dotBCBC = dot(BC, BC);                                                                                 \n" +
//            "  return 0.0 <= dotABAM && dotABAM <= dotABAB && 0.0 <= dotBCBM && dotBCBM <= dotBCBC ? 1.0 : 0.0;             \n" +
//            "}                                                                                                              \n" +
//"                                                                                                   \n" +
//            "float triangleArea(vec2 A, vec2 B, vec2 C) {                                                       \n" +
//            "  return abs((A.x*(B.y - C.y) + B.x*(C.y - A.y) + C.x*(A.y - B.y)) * 0.5);                         \n" +
//            "}                                                                                                  \n" +
//            "                                                                                                   \n" +
//            "float pointInRectArea(vec2 p, vec2 r[4], float a) {                                                \n" +
//            "  float aAPD = triangleArea(r[0], p, r[3]);                                                        \n" +
//            "  float aDPC = triangleArea(r[3], p, r[2]);                                                        \n" +
//            "  float aCPB = triangleArea(r[2], p, r[1]);                                                        \n" +
//            "  float aBPA = triangleArea(r[1], p, r[0]);                                                        \n" +
//            "  return aAPD + aDPC + aCPB + aBPA <= a + 0.0001 ? 1.0 : 0.0;                                      \n" +
//            "}                                                                                                  \n" +
//            "                                                                                                   \n" +
//        "                                                                                                               \n" +
//            "// l t r b                                                                                                     \n" +
//            "void clipRectFromVec4(vec4 r, inout vec2 pts[4]) {                                                             \n" +
//            "  pts[0] = vec2(r.x, r.y);                                                                                     \n" +
//            "  pts[1] = vec2(r.z, r.y);                                                                                     \n" +
//            "  pts[2] = vec2(r.z, r.w);                                                                                     \n" +
//            "  pts[3] = vec2(r.x, r.w);                                                                                     \n" +
//            "}                                                                                                              \n" +
//            "                                                                                                               \n" +
//            "float pointInRect(vec2 m, vec2 r[4]) {                                                                         \n" +
//            "  vec2 AB = r[1] - r[0];                                                                                       \n" +
//            "  vec2 AM = m - r[0];                                                                                          \n" +
//            "  vec2 BC = r[2] - r[1];                                                                                       \n" +
//            "  vec2 BM = m - r[1];                                                                                          \n" +
//            "  float dotABAM = dot(AB, AM);                                                                                 \n" +
//            "  float dotABAB = dot(AB, AB);                                                                                 \n" +
//            "  float dotBCBM = dot(BC, BM);                                                                                 \n" +
//            "  float dotBCBC = dot(BC, BC);                                                                                 \n" +
//            "  return 0.0 <= dotABAM && dotABAM <= dotABAB && 0.0 <= dotBCBM && dotBCBM <= dotBCBC ? 1.0 : 0.0;             \n" +
//            "}                                                                                                              \n" +
//            "
//            \n" +

fun rotateMesh(vertices: FloatArray, verticesOffset: Int, verticesCount: Int, angleDeg: Float, pivot: PointF, canvasWH: PointF, vertexSize: Int) {
    val angleRad = angleDeg * PI / 180.0
    val s = sin(-angleRad)
    val c = cos(-angleRad)

    val cx = (pivot.x + 1f) * canvasWH.x
    val cy = (pivot.y + 1f) * canvasWH.y

    var i = verticesOffset
    while (i < verticesOffset + verticesCount) {
        // translate point
        val px = (vertices[i] + 1f) * canvasWH.x - cx
        val py = (vertices[i + 1] + 1f) * canvasWH.y - cy

        // rotate point
        val xnew = px * c - py * s
        val ynew = px * s + py * c

        // translate point back:
        vertices[i] = ((xnew + cx) / canvasWH.x - 1.0).toFloat()
        vertices[i + 1] = ((ynew + cy) / canvasWH.y - 1.0).toFloat()

        i += vertexSize
    }
}