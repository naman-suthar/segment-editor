package app.ijp.segmentation_editor.gradient_editor

import android.content.Context
import android.util.AttributeSet
import android.widget.GridView

class ExpandedGridView : GridView {
    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(
        context,
        attrs
    ) {
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(
            widthMeasureSpec,
            MeasureSpec.makeMeasureSpec(Int.MAX_VALUE shr 4, MeasureSpec.AT_MOST)
        )
    }
}