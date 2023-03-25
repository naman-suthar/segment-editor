package app.ijp.segmentation_editor.multi_color_dialog

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.Paint.Join
import android.graphics.Shader.TileMode
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

/*
* Copyright 2013 Piotr Adamus
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/   class MultiColorPicker : View {
    private var paramColorCount = 2
    private var colorWheelPaint: Paint? = null
    private var valueSliderPaint: Paint? = null
    private var colorViewPaint: Paint? = null
    private var colorPointerPaint: Paint? = null
    private var colorPointerCoords: RectF? = null
    private var valuePointerPaint: Paint? = null
    private var valuePointerArrowPaint: Paint? = null
    private var outerWheelRect: RectF? = null
    private var innerWheelRect: RectF? = null
    private var colorViewPath: Path? = null
    private var valueSliderPath: Path? = null
    private var arrowPointerPath: Path? = null
    private var colorWheelBitmap: Bitmap? = null
    private var arrowPointerSize = 0
    private var outerWheelRadius = 0
    private var innerWheelRadius = 0
    private var colorWheelRadius = 0
    private var gradientRotationMatrix: Matrix? = null

    /**
     * Currently selected color
     */
    private var colorHSV: FloatArray? = floatArrayOf(0f, 0f, 1f)
    private var adjacentHue = FloatArray(paramColorCount)

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyle: Int
    ) : super(context, attrs, defStyle) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(
        context,
        attrs
    ) {
        init()
    }

    constructor(context: Context?) : super(context) {
        init()
    }

    fun setColorCount(count: Int) {
        paramColorCount = count
        adjacentHue = FloatArray(paramColorCount)
    }

    private fun init() {
        colorPointerPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        colorPointerPaint!!.style = Paint.Style.STROKE
        colorPointerPaint!!.strokeWidth = 2f
        colorPointerPaint!!.setARGB(128, 0, 0, 0)
        valuePointerPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        valuePointerPaint!!.style = Paint.Style.STROKE
        valuePointerPaint!!.strokeWidth = 2f
        valuePointerArrowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        colorWheelPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        colorWheelPaint!!.isAntiAlias = true
        colorWheelPaint!!.isDither = true
        valueSliderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        valueSliderPaint!!.isAntiAlias = true
        valueSliderPaint!!.isDither = true
        colorViewPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        colorViewPaint!!.isAntiAlias = true
        colorViewPath = Path()
        valueSliderPath = Path()
        arrowPointerPath = Path()
        outerWheelRect = RectF()
        innerWheelRect = RectF()
        colorPointerCoords = RectF()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val size = Math.min(widthSize, heightSize)
        setMeasuredDimension(size, size)
    }


    fun getMultiColor(): Int {
        return Color.HSVToColor(colorHSV)
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        val centerX = width / 2
        val centerY = height / 2

        // drawing color wheel
        canvas.drawBitmap(
            colorWheelBitmap!!,
            centerX - colorWheelRadius.toFloat(),
            centerY - colorWheelRadius.toFloat(),
            null
        )

        // drawing color view
        val segmentColors = colors
        val sweepAngleStep = 180f / paramColorCount
        for (i in 0 until paramColorCount) {
            colorViewPath!!.reset()
            colorViewPath!!.arcTo(outerWheelRect!!, 270 - i * sweepAngleStep, -sweepAngleStep)
            colorViewPath!!.arcTo(
                innerWheelRect!!,
                90 + (paramColorCount - i - 1) * sweepAngleStep,
                sweepAngleStep
            )
            colorViewPaint!!.color = segmentColors[i]
            canvas.drawPath(colorViewPath!!, colorViewPaint!!)
        }

        // drawing value slider
        val hsv = floatArrayOf(colorHSV!![0], colorHSV!![1], 1f)
        val sweepGradient = SweepGradient(
            centerX.toFloat(),
            centerY.toFloat(),
            intArrayOf(
                Color.BLACK,
                Color.HSVToColor(hsv),
                Color.WHITE
            ),
            null
        )
        sweepGradient.setLocalMatrix(gradientRotationMatrix)
        valueSliderPaint!!.shader = sweepGradient
        canvas.drawPath(valueSliderPath!!, valueSliderPaint!!)

        // drawing color wheel pointer
        for (i in 0 until paramColorCount) {
            drawColorWheelPointer(
                canvas,
                Math.toRadians(adjacentHue[i].toDouble()).toFloat()
            )
        }

        // drawing value pointer
        valuePointerPaint!!.color = Color.HSVToColor(
            floatArrayOf(
                0f,
                0f,
                1f - colorHSV!![2]
            )
        )
        val valueAngle = (colorHSV!![2] - 0.5f) * Math.PI
        val valueAngleX = Math.cos(valueAngle).toFloat()
        val valueAngleY = Math.sin(valueAngle).toFloat()
        canvas.drawLine(
            valueAngleX * innerWheelRadius + centerX,
            valueAngleY * innerWheelRadius + centerY,
            valueAngleX * outerWheelRadius + centerX,
            valueAngleY * outerWheelRadius + centerY,
            valuePointerPaint!!
        )

        // drawing pointer arrow
        if (arrowPointerSize > 0) {
            drawPointerArrow(canvas)
        }
    }

    private fun drawColorWheelPointer(
        canvas: Canvas,
        hueAngle: Float
    ) {
        val centerX = width / 2
        val centerY = height / 2
        val colorPointX =
            (-Math.cos(hueAngle.toDouble()) * colorHSV!![1] * colorWheelRadius).toInt() + centerX
        val colorPointY =
            (-Math.sin(hueAngle.toDouble()) * colorHSV!![1] * colorWheelRadius).toInt() + centerY
        val pointerRadius = 0.075f * colorWheelRadius
        val pointerX = (colorPointX - pointerRadius / 2).toInt()
        val pointerY = (colorPointY - pointerRadius / 2).toInt()
        colorPointerCoords!![pointerX.toFloat(), pointerY.toFloat(), pointerX + pointerRadius] =
            pointerY + pointerRadius
        canvas.drawOval(colorPointerCoords!!, colorPointerPaint!!)
    }

    private fun drawPointerArrow(canvas: Canvas) {
        val centerX = width / 2
        val centerY = height / 2
        val tipAngle = (colorHSV!![2] - 0.5f) * Math.PI
        val leftAngle = tipAngle + Math.PI / 96
        val rightAngle = tipAngle - Math.PI / 96
        val tipAngleX = Math.cos(tipAngle) * outerWheelRadius
        val tipAngleY = Math.sin(tipAngle) * outerWheelRadius
        val leftAngleX =
            Math.cos(leftAngle) * (outerWheelRadius + arrowPointerSize)
        val leftAngleY =
            Math.sin(leftAngle) * (outerWheelRadius + arrowPointerSize)
        val rightAngleX =
            Math.cos(rightAngle) * (outerWheelRadius + arrowPointerSize)
        val rightAngleY =
            Math.sin(rightAngle) * (outerWheelRadius + arrowPointerSize)
        arrowPointerPath!!.reset()
        arrowPointerPath!!.moveTo(
            tipAngleX.toFloat() + centerX,
            tipAngleY.toFloat() + centerY
        )
        arrowPointerPath!!.lineTo(
            leftAngleX.toFloat() + centerX,
            leftAngleY.toFloat() + centerY
        )
        arrowPointerPath!!.lineTo(
            rightAngleX.toFloat() + centerX,
            rightAngleY.toFloat() + centerY
        )
        arrowPointerPath!!.lineTo(
            tipAngleX.toFloat() + centerX,
            tipAngleY.toFloat() + centerY
        )
        valuePointerArrowPaint!!.color = Color.HSVToColor(colorHSV)
        valuePointerArrowPaint!!.style = Paint.Style.FILL
        canvas.drawPath(arrowPointerPath!!, valuePointerArrowPaint!!)
        valuePointerArrowPaint!!.style = Paint.Style.STROKE
        valuePointerArrowPaint!!.strokeJoin = Join.ROUND
        valuePointerArrowPaint!!.color = Color.BLACK
        canvas.drawPath(arrowPointerPath!!, valuePointerArrowPaint!!)
    }

    override fun onSizeChanged(
        width: Int,
        height: Int,
        oldw: Int,
        oldh: Int
    ) {
        val centerX = width / 2
        val centerY = height / 2
        val paramInnerPadding = 5
        val innerPadding = paramInnerPadding * width / 100
        /*
      Customizable display parameters (in percents)
     */
        val paramOuterPadding = 2
        val outerPadding = paramOuterPadding * width / 100
        val paramArrowPointerSize = 4
        arrowPointerSize = paramArrowPointerSize * width / 100
        val paramValueSliderWidth = 10
        val valueSliderWidth = paramValueSliderWidth * width / 100
        outerWheelRadius = width / 2 - outerPadding - arrowPointerSize
        innerWheelRadius = outerWheelRadius - valueSliderWidth
        colorWheelRadius = innerWheelRadius - innerPadding
        outerWheelRect!![centerX - outerWheelRadius.toFloat(), centerY - outerWheelRadius.toFloat(), centerX + outerWheelRadius.toFloat()] =
            centerY + outerWheelRadius.toFloat()
        innerWheelRect!![centerX - innerWheelRadius.toFloat(), centerY - innerWheelRadius.toFloat(), centerX + innerWheelRadius.toFloat()] =
            centerY + innerWheelRadius.toFloat()
        colorWheelBitmap = createColorWheelBitmap(colorWheelRadius * 2, colorWheelRadius * 2)
        gradientRotationMatrix = Matrix()
        gradientRotationMatrix!!.preRotate(270f, width / 2.toFloat(), height / 2.toFloat())
        valueSliderPath!!.arcTo(outerWheelRect!!, 270f, 180f)
        valueSliderPath!!.arcTo(innerWheelRect!!, 90f, -180f)
    }

    private fun createColorWheelBitmap(width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val colorCount = 12
        val colorAngleStep = 360 / 12
        val colors = IntArray(colorCount + 1)
        val hsv = floatArrayOf(0f, 1f, 1f)
        for (i in colors.indices) {
            hsv[0] = ((i * colorAngleStep + 180) % 360).toFloat()
            colors[i] = Color.HSVToColor(hsv)
        }
        colors[colorCount] = colors[0]
        val sweepGradient = SweepGradient(
            (width / 2).toFloat(),
            (height / 2).toFloat(), colors, null
        )
        val radialGradient = RadialGradient(
            width / 2f,
            height / 2f,
            colorWheelRadius.toFloat(),
            -0x1,
            0x00FFFFFF,
            TileMode.CLAMP
        )
        val composeShader =
            ComposeShader(sweepGradient, radialGradient, PorterDuff.Mode.SRC_OVER)
        colorWheelPaint!!.shader = composeShader
        val canvas = Canvas(bitmap)
        canvas.drawCircle(
            width / 2.toFloat(),
            height / 2.toFloat(),
            colorWheelRadius.toFloat(),
            colorWheelPaint!!
        )
        return bitmap
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val action = event.action
        when (action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                val x = event.x.toInt()
                val y = event.y.toInt()
                val cx = x - width / 2
                val cy = y - height / 2
                val d = Math.sqrt(cx * cx + cy * cy.toDouble())
                if (d <= colorWheelRadius) {
                    colorHSV!![0] = (Math.toDegrees(
                        Math.atan2(
                            cy.toDouble(),
                            cx.toDouble()
                        )
                    ) + 180f).toFloat()
                    colorHSV!![1] = Math.max(
                        0f,
                        Math.min(1f, (d / colorWheelRadius).toFloat())
                    )
                    updateAdjacentHue()
                    invalidate()
                } else if (x >= width / 2 && d >= innerWheelRadius) {
                    colorHSV!![2] = Math.max(
                        0.0,
                        Math.min(
                            1.0,
                            Math.atan2(
                                cy.toDouble(),
                                cx.toDouble()
                            ) / Math.PI + 0.5f
                        )
                    ).toFloat()
                    updateAdjacentHue()
                    invalidate()
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun updateAdjacentHue() {
        for (i in 0 until paramColorCount) {
            val paramHueSpreadAngle = 30f
            adjacentHue[i] =
                (colorHSV!![0] - paramHueSpreadAngle * (paramColorCount / 2 - i)) % 360.0f
            adjacentHue[i] =
                if (adjacentHue[i] < 0) adjacentHue[i] + 360f else adjacentHue[i]
        }
        adjacentHue[paramColorCount / 2] = colorHSV!![0]
    }

    var color: Int
        get() = Color.HSVToColor(colorHSV)
        set(color) {
            Color.colorToHSV(color, colorHSV)
            updateAdjacentHue()
        }

    val colors: IntArray
        get() {
            val colors = IntArray(paramColorCount)
            val hsv = FloatArray(3)
            for (i in 0 until paramColorCount) {
                hsv[0] = adjacentHue[i]
                hsv[1] = colorHSV!![1]
                hsv[2] = colorHSV!![2]
                colors[i] = Color.HSVToColor(hsv)
            }
            return colors
        }

    override fun onSaveInstanceState(): Parcelable? {
        val state = Bundle()
        state.putFloatArray("color", colorHSV)
        state.putParcelable("super", super.onSaveInstanceState())
        return state
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        if (state is Bundle) {
            val bundle = state
            colorHSV = bundle.getFloatArray("color")
            updateAdjacentHue()
            super.onRestoreInstanceState(bundle.getParcelable("super"))
        } else {
            super.onRestoreInstanceState(state)
        }
    }
}