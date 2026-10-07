package com.saksham.deepfakeaudio.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.max
import kotlin.random.Random

class WaveformView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    private val amplitudes = ArrayDeque<Float>()
    private var pulse = 0f
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { strokeWidth = 7f; strokeCap = Paint.Cap.ROUND }
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = 18f; strokeCap = Paint.Cap.ROUND
        maskFilter = BlurMaskFilter(18f, BlurMaskFilter.Blur.NORMAL)
    }

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 1400; repeatCount = ValueAnimator.INFINITE
            addUpdateListener { pulse = it.animatedValue as Float; invalidate() }
            start()
        }
        repeat(44) { amplitudes.add(Random.nextFloat() * 0.3f) }
    }

    fun addAmplitude(value: Int) {
        val normalized = (value / 32767f).coerceIn(0f, 1f)
        if (amplitudes.size > 54) amplitudes.removeFirst()
        amplitudes.add(max(0.08f, normalized))
        invalidate()
    }

    fun idleWave() {
        if (amplitudes.size > 54) amplitudes.removeFirst()
        amplitudes.add(0.12f + Random.nextFloat() * 0.18f)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val centerY = height / 2f
        val barGap = width / 58f
        val shader = LinearGradient(0f, 0f, width.toFloat(), 0f, intArrayOf(
            Color.rgb(103, 232, 249), Color.rgb(167, 139, 250), Color.rgb(251, 113, 133)
        ), null, Shader.TileMode.CLAMP)
        linePaint.shader = shader
        glowPaint.shader = shader
        glowPaint.alpha = (55 + pulse * 50).toInt()
        amplitudes.forEachIndexed { i, amp ->
            val x = 12f + i * barGap
            val barH = 12f + amp * height * 0.76f
            canvas.drawLine(x, centerY - barH / 2, x, centerY + barH / 2, glowPaint)
            canvas.drawLine(x, centerY - barH / 2, x, centerY + barH / 2, linePaint)
        }
    }
}
