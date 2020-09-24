package com.anwesh.uiprojects.bimidlinejoinview

/**
 * Created by anweshmishra on 24/09/20.
 */

import android.view.View
import android.view.MotionEvent
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Color
import android.content.Context
import android.app.Activity

val colors : Array<Int> = arrayOf(
        "#F44336",
        "#2196F3",
        "#3F51B5",
        "#4CAF50",
        "#FFC107"
).map({Color.parseColor(it)}).toTypedArray()
val lines : Int = 4
val parts : Int = lines + 1
val scGap : Float = 0.02f / parts
val strokeFactor : Float = 90f
val sizeFactor : Float = 3.4f
val gapFactor : Float = 2.9f
val backColor : Int = Color.parseColor("#BDBDBD")
val delay : Long = 20
