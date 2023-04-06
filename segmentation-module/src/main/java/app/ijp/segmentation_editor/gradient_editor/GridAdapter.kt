package app.ijp.segmentation_editor.gradient_editor

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import app.ijp.segmentation_editor.R
import app.ijp.segmentation_editor.segment_editor.CustomComponentsCallback



class GridAdapter(
    context: Context,
    val colorList: ArrayList<String>,
    val callback: CustomComponentsCallback
) :
    ArrayAdapter<String>(context, R.layout.grid_item,colorList) {


    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        val context= parent.context
        val inflater = LayoutInflater.from(context)
        val rowView = inflater.inflate(R.layout.grid_item, null, true)

        val frame = rowView.findViewById(R.id.gridBox) as LinearLayout
        val delBtn = rowView.findViewById(R.id.deleteGridBox) as TextView
        delBtn.setOnClickListener {
            if(colorList.size<3){
                Toast.makeText(context,context.getString(R.string.min_text),Toast.LENGTH_SHORT).show()
            }else{
                colorList.removeAt(position)
                callback.deleteGridColor(colorList)
            }

        }
        frame.setBackgroundColor(Color.parseColor(colorList[position]))
        return rowView
    }

//    inner class ViewHolder(listItemView: View) : RecyclerView.ViewHolder(listItemView) {
//
//        val frameLayout: FrameLayout = listItemView.findViewById(R.id.gridBox)
//
//        init {
//            listItemView.setOnClickListener {
//
//
//
//                val colorDialog =
//                    ColorDialog.newInstance(defaultColor, object : OnColorChangedListener {
//                        override fun colorChanged(color: Int) {
//                            frameLayout.setBackgroundColor(color)
////                            clbk.onAppColorChange(
////                                adapterPosition + 1,
////                                "#" + Integer.toHexString(color).substring(2)
////                            )
//                            colorList[adapterPosition] =
//                                "#" + Integer.toHexString(color).substring(2)
//                        }
//                    })
//                colorDialog.show(supportFragmentManager, this.getString(R.string.single_tag))
//            }
//        }
//
//
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GridAdapter.ViewHolder {
//        val context = parent.context
//        val inflater = LayoutInflater.from(context)
//        // Inflate the custom layout
//        val appView = inflater.inflate(R.layout.grid_item, parent, false)
//        // Return a new holder instance
//        return ViewHolder(appView)
//    }
//
//    override fun getItemCount(): Int {
//        return colorList.size
//    }
//
//
//    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        // Get the data model based on position
//
//        val clr = colorList[position]
//        // Set item views based on your views and data model
//        val gbox = holder.frameLayout
//        gbox.setBackgroundColor(Color.parseColor(clr))
//
//    }




}