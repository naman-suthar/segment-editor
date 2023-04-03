package app.ijp.segmentation_editor.bar_preview

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

/**
 * This is View for MergedGradientSegment Bar Preview
 * */
class MergedGradientBar(context: Context, attributeSet: AttributeSet): View(context,attributeSet) {
    var vertex: Float = 0.10f
    var colorArray = intArrayOf(Color.BLUE,Color.RED,Color.GREEN)
    var colorPositionArray = floatArrayOf(0f,0.33f,0.67f)
    val path = Path()

    /**
     * This function sets the dynamic vertex of gradient Quadrilateral Three are static
     * (0.0)   ☀          ☀ (x,y) and the one is dynamic
     *
     * (0,100) ☀                ☀ (100,100),
     * */
    fun updateVertex(x2: Float){
       vertex = x2/100f
       invalidate()
    }

    /**This function update the colors of gradient*/
    fun updateColors(colors: IntArray,colorsPosition: FloatArray){
        colorArray = colors
        colorPositionArray = colorsPosition
        invalidate()
    }
    override fun onDraw(canvas: Canvas?) {

        val gradient = LinearGradient(0f, 0f, 0f, height.toFloat(),
            colorArray, colorPositionArray, Shader.TileMode.REPEAT)

        // Create a paint object with the gradient
        val paint = Paint().apply {
            shader = gradient
        }

        // Draw a rectangle with the gradient paint
//        canvas?.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        val x1 = 0f
        val y1 = 0f
        val x2 = vertex * measuredWidth.toFloat()
        val y2 = 0f
        val x3 = measuredWidth.toFloat()
        val y3 = height.toFloat()
        val x4 = 0f
        val y4 = height.toFloat()
        path.moveTo(x1, y1)

// Draw a line to the second vertex
        path.lineTo(x2, y2)

// Draw a line to the third vertex
        path.lineTo(x3, y3)

// Draw a line to the fourth vertex
        path.lineTo(x4, y4)

// Draw a line back to the first vertex to close the shape
        path.lineTo(x1, y1)

        canvas?.drawPath(path,paint)
    }
}