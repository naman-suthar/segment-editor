package app.ijp.segmentation_editor.extras.multi_color_dialog

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.*
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import app.ijp.segmentation_editor.R
import app.ijp.segmentation_editor.segment_editor.CustomComponentsCallback
import com.google.android.material.slider.Slider
import com.google.android.material.slider.Slider.OnChangeListener


const val LAST_GRADIENT_COLOR = "rn..adk"
const val GRADIENT_COUNT = "..n@kln"
class MultiColorDialog(val callback: CustomComponentsCallback) : DialogFragment(),
    View.OnClickListener {
    lateinit var multiColorPicker: MultiColorPicker
    private var gradColor = 0
    private lateinit var clbk: CustomComponentsCallback
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        val lp = WindowManager.LayoutParams()

        lp.copyFrom(dialog.window!!.attributes)
        lp.width = (0.8 * lp.width).toInt()

        dialog.window!!.attributes = lp
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return dialog
    }

    /*override fun onStart() {
        super.onStart()

         dialog!!.window
             ?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        *//* dialog!!.window
             ?.setLayout(1200, 1600)*//*
           val lp = WindowManager.LayoutParams()

           lp.copyFrom(dialog!!.window!!.attributes)
           lp.width = (0.8 * lp.width).toInt()

           dialog!!.window!!.attributes = lp
        dialog!!.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

    }*/

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v =
            inflater.inflate(R.layout.multi_color_dialog, container, false)
        val colorCount = requireArguments().getInt(GRADIENT_COUNT)
        multiColorPicker =
            v.findViewById<View>(R.id.multiColorPicker) as MultiColorPicker
        val gradientSeek = v.findViewById<View>(R.id.gradientSeek) as Slider
        val gradValue = v.findViewById<View>(R.id.gradValue) as TextView
        gradientSeek.value = (colorCount).toFloat()
        gradValue.text = colorCount.toString()
        /*  gradientSeek.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
              override fun onProgressChanged(
                  seekBar: SeekBar,
                  progress: Int,
                  fromUser: Boolean
              ) {
                  gradValue.text = (progress + 2).toString()
                  multiColorPicker.setColorCount(progress + 2)
                  multiColorPicker.color = gradColor
                  multiColorPicker.invalidate()
              }

              override fun onStartTrackingTouch(seekBar: SeekBar) {}
              override fun onStopTrackingTouch(seekBar: SeekBar) {}
          })*/
        gradientSeek.addOnChangeListener(OnChangeListener { slider, value, fromUser ->
            if (fromUser && value > 1) {
                gradValue.text = (value.toInt()).toString()
                multiColorPicker.setColorCount((value).toInt())
                multiColorPicker.color = gradColor
                multiColorPicker.invalidate()
            } else {
                slider.value = 2f
            }

        }

        )
        multiColorPicker.setColorCount(colorCount)
        gradColor = PreferenceManager.getDefaultSharedPreferences(context).getInt(
           LAST_GRADIENT_COLOR,
            ContextCompat.getColor(requireContext(), R.color.default_bar_color)
        )

        // Log.v("gradCount",colorCount+"");
        multiColorPicker.color = gradColor
        val cancel: TextView
        val ok: TextView
        cancel = v.findViewById<TextView>(R.id.cancelMultiColor)
        ok = v.findViewById<TextView>(R.id.okMultiColor)
        cancel.setOnClickListener(this)
        ok.setOnClickListener(this)
        return v
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.cancelMultiColor -> dismiss()
            R.id.okMultiColor -> {
                PreferenceManager.getDefaultSharedPreferences(context).edit()
                    .putInt(LAST_GRADIENT_COLOR, multiColorPicker.getMultiColor())
                    .apply()
                val clrs = multiColorPicker.colors
                callback.onGridColorChange(clrs)
                dismiss()
            }
        }
    }

    companion object {
        fun newInstance(colorCount: Int, callback: CustomComponentsCallback): MultiColorDialog {

            val f = MultiColorDialog(callback)
            val bundle = Bundle()
            bundle.putInt(GRADIENT_COUNT, colorCount)

            f.arguments = bundle
            return f
        }
    }
}
