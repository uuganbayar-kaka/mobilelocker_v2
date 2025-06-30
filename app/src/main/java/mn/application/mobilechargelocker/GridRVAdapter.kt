package mn.application.mobilechargelocker

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat

internal class GridRVAdapter(
    // on below line we are creating two
    // variables for course list and context
    private val lockerList: List<GridViewModal>,
    private val context: Context
) :
    BaseAdapter() {
    private var layoutInflater: LayoutInflater? = null
    private lateinit var courseTV: TextView
    private var gridCvId: CardView? = null

    override fun getCount(): Int {
        return lockerList.size
    }

    override fun getItem(position: Int): Any? {
        return null
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    @SuppressLint("SetTextI18n")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        var convertView = convertView
        if (layoutInflater == null) {
            layoutInflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        }

        if (convertView == null) {
            convertView = layoutInflater!!.inflate(R.layout.gridview_item, null)
        }
        courseTV = convertView!!.findViewById(R.id.idTVCourse)
        courseTV.text = lockerList[position].lockerId.toString()

        gridCvId = convertView.findViewById(R.id.grid_cv_id)
        val statusIcon = convertView.findViewById<ImageView>(R.id.statusIcon)
        if(lockerList[position].isUsed) {
            gridCvId?.setCardBackgroundColor(ContextCompat.getColor(this.context, R.color.red))
            statusIcon.setImageResource(R.drawable.baseline_cancel_24)
        } else {
            gridCvId?.setCardBackgroundColor(ContextCompat.getColor(this.context, R.color.green))
            statusIcon.setImageResource(R.drawable.baseline_check_circle_24)
        }
        return convertView
    }
}