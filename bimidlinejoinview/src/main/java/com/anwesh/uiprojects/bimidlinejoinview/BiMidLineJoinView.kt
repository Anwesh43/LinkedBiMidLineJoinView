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

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.sinify() : Float = Math.sin(this * Math.PI).toFloat()

fun Canvas.drawBiMidLineJoin(scale : Float, w : Float, h : Float, paint : Paint) {
    val sf : Float = scale.sinify()
    val sf1 : Float = sf.divideScale(0, parts)
    val gap : Float = w / gapFactor
    val hSize : Float = h / sizeFactor
    save()
    translate(w / 2, h)
    for (j in 0..1) {
        save()
        scale(1f - 2 * j, 1f)
        save()
        translate(-gap / 2, 0f)
        drawLine(0f, 0f, 0f, -hSize * sf1, paint)
        val hGap : Float = hSize / lines
        for (k in 0..(lines - 1)) {
            val sfj : Float = sf.divideScale(k + 1, lines)
            save()
            translate(0f, -hSize + hGap * j)
            drawLine(0f, 0f, gap * sfj, 0f, paint)
            restore()
        }
        restore()
        restore()
    }
    restore()
}

fun Canvas.drawBMLJNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    paint.color = colors[i]
    paint.strokeCap = Paint.Cap.ROUND
    paint.strokeWidth = Math.min(w, h) / strokeFactor
    drawBiMidLineJoin(scale, w, h, paint)
}

class BiMidLineJoinView(ctx : Context) : View(ctx) {

    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += dir * scGap
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(delay)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class BMLJNode(var i : Int, val state : State = State()) {

        private var next : BMLJNode? = null
        private var prev : BMLJNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < colors.size - 1) {
                next = BMLJNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawBMLJNode(i, state.scale, paint)
        }

        fun update(cb : (Float) -> Unit) {
            state.update(cb)
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit)  : BMLJNode {
            var curr : BMLJNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class BiMidLineJoin(var i : Int) {

        private var curr : BMLJNode = BMLJNode(0)
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            curr.draw(canvas, paint)
        }

        fun update(cb : (Float) -> Unit) {
            curr.update {
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : BiMidLineJoinView) {

        private val animator : Animator = Animator(view)
        private val bimidlinejoin : BiMidLineJoin = BiMidLineJoin(0)
        private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

        fun render(canvas : Canvas) {
            canvas.drawColor(backColor)
            bimidlinejoin.draw(canvas, paint)
            animator.animate {
                bimidlinejoin.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            bimidlinejoin.startUpdating {
                animator.start()
            }
        }
    }

    companion object {

        fun create(activity : Activity) : BiMidLineJoinView {
            val view : BiMidLineJoinView = BiMidLineJoinView(activity)
            activity.setContentView(view)
            return view
        }
    }
}