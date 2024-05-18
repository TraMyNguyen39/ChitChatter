import android.graphics.*
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

//class HeaderItemDecoration(
//    parent: RecyclerView,
//    private val shouldFadeOutHeader: Boolean = false,
//    private val isHeader: (itemPosition: Int) -> Boolean
//) : RecyclerView.ItemDecoration() {
//
//    private var currentHeader: Pair<Int, RecyclerView.ViewHolder>? = null
//
//    init {
//        parent.adapter?.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
//            override fun onChanged() {
//                // clear saved header as it can be outdated now
//                currentHeader = null
//            }
//        })
//
//        parent.doOnEachNextLayout {
//            // clear saved layout as it may need layout update
//            currentHeader = null
//        }
//        // handle click on sticky header
//        parent.addOnItemTouchListener(object : RecyclerView.SimpleOnItemTouchListener() {
//            override fun onInterceptTouchEvent(
//                recyclerView: RecyclerView,
//                motionEvent: MotionEvent
//            ): Boolean {
//                return if (motionEvent.action == MotionEvent.ACTION_DOWN) {
//                    motionEvent.y <= currentHeader?.second?.itemView?.bottom ?: 0
//                } else false
//            }
//        })
//    }
//
//    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
//        super.onDrawOver(c, parent, state)
//        //val topChild = parent.getChildAt(0) ?: return
//        val topChild = parent.findChildViewUnder(
//            (parent.left + parent.width/2).toFloat(),
//            parent.paddingTop.toFloat() /*+ (currentHeader?.second?.itemView?.height ?: 0 )*/
//        ) ?: return
//        val topChildPosition = parent.getChildAdapterPosition(topChild)
//        if (topChildPosition == RecyclerView.NO_POSITION) {
//            return
//        }
//
//        val headerView = getHeaderViewForItem(topChildPosition, parent) ?: return
//
//        val contactPoint = headerView.bottom + parent.paddingTop
//        val childInContact = getChildInContact(parent, contactPoint) ?: return
//
//        if (isHeader(parent.getChildAdapterPosition(childInContact))) {
//            moveHeader(c, headerView, childInContact, parent.paddingTop)
//            return
//        }
//
//        drawHeader(c, headerView, parent.paddingTop)
//    }
//
//    private fun getHeaderViewForItem(itemPosition: Int, parent: RecyclerView): View? {
//        if (parent.adapter == null) {
//            return null
//        }
//        val headerPosition = getHeaderPositionForItem(itemPosition)
//        if (headerPosition == RecyclerView.NO_POSITION) return null
//        val headerType = parent.adapter?.getItemViewType(headerPosition) ?: return null
//        // if match reuse viewHolder
//        if (currentHeader?.first == headerPosition && currentHeader?.second?.itemViewType == headerType) {
//            return currentHeader?.second?.itemView
//        }
//
//        val headerHolder = parent.adapter?.createViewHolder(parent, headerType)
//        if (headerHolder != null) {
//            parent.adapter?.onBindViewHolder(headerHolder, headerPosition)
//            fixLayoutSize(parent, headerHolder.itemView)
//            // save for next draw
//            currentHeader = headerPosition to headerHolder
//        }
//        return headerHolder?.itemView
//    }
//
//    private fun drawHeader(c: Canvas, header: View, paddingTop: Int) {
//        c.save()
//        c.translate(0f, paddingTop.toFloat())
//        header.draw(c)
//        c.restore()
//    }
//
//    private fun moveHeader(c: Canvas, currentHeader: View, nextHeader: View, paddingTop: Int) {
//        c.save()
//        if (!shouldFadeOutHeader) {
//            c.clipRect(0, paddingTop, c.width, paddingTop + currentHeader.height)
//        } else {
//            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
//                c.saveLayerAlpha(
//                    RectF(0f, 0f, c.width.toFloat(), c.height.toFloat()),
//                    (((nextHeader.top - paddingTop) / nextHeader.height.toFloat()) * 255).toInt()
//                )
//            } else {
//                c.saveLayerAlpha(
//                    0f, 0f, c.width.toFloat(), c.height.toFloat(),
//                    (((nextHeader.top - paddingTop) / nextHeader.height.toFloat()) * 255).toInt(),
//                    Canvas.ALL_SAVE_FLAG
//                )
//            }
//
//        }
//        c.translate(0f, (nextHeader.top - currentHeader.height).toFloat() /*+ paddingTop*/)
//
//        currentHeader.draw(c)
//        if (shouldFadeOutHeader) {
//            c.restore()
//        }
//        c.restore()
//    }
//
//    private fun getChildInContact(parent: RecyclerView, contactPoint: Int): View? {
//        var childInContact: View? = null
//        for (i in 0 until parent.childCount) {
//            val child = parent.getChildAt(i)
//            val mBounds = Rect()
//            parent.getDecoratedBoundsWithMargins(child, mBounds)
//            if (mBounds.bottom > contactPoint) {
//                if (mBounds.top <= contactPoint) {
//                    // This child overlaps the contactPoint
//                    childInContact = child
//                    break
//                }
//            }
//        }
//        return childInContact
//    }
//
//    /**
//     * Properly measures and layouts the top sticky header.
//     *
//     * @param parent ViewGroup: RecyclerView in this case.
//     */
//    private fun fixLayoutSize(parent: ViewGroup, view: View) {
//
//        // Specs for parent (RecyclerView)
//        val widthSpec = View.MeasureSpec.makeMeasureSpec(parent.width, View.MeasureSpec.EXACTLY)
//        val heightSpec =
//            View.MeasureSpec.makeMeasureSpec(parent.height, View.MeasureSpec.UNSPECIFIED)
//
//        // Specs for children (headers)
//        val childWidthSpec = ViewGroup.getChildMeasureSpec(
//            widthSpec,
//            parent.paddingLeft + parent.paddingRight,
//            view.layoutParams.width
//        )
//        val childHeightSpec = ViewGroup.getChildMeasureSpec(
//            heightSpec,
//            parent.paddingTop + parent.paddingBottom,
//            view.layoutParams.height
//        )
//
//        view.measure(childWidthSpec, childHeightSpec)
//        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
//    }
//
//    private fun getHeaderPositionForItem(itemPosition: Int): Int {
//        var headerPosition = RecyclerView.NO_POSITION
//        var currentPosition = itemPosition
//        do {
//            if (isHeader(currentPosition)) {
//                headerPosition = currentPosition
//                break
//            }
//            currentPosition -= 1
//        } while (currentPosition >= 0)
//        return headerPosition
//    }
//}
//
//inline fun View.doOnEachNextLayout(crossinline action: (view: View) -> Unit) {
//    addOnLayoutChangeListener { view, _, _, _, _, _, _, _, _ ->
//        action(
//            view
//        )
//    }
//}
class StickyHeaderItemDecoration(recyclerView: RecyclerView, private val mListener: StickyHeaderInterface) : RecyclerView.ItemDecoration() {

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)

        val topChild = parent.getChildAt(0) ?: return

        val topChildPosition = parent.getChildAdapterPosition(topChild)
        if (topChildPosition == RecyclerView.NO_POSITION) {
            return
        }

        val currentHeader = getHeaderViewForItem(topChildPosition, parent)
        fixLayoutSize(parent, currentHeader)
        val contactPoint = currentHeader.bottom
        val childInContact = getChildInContact(parent, contactPoint) ?: return

        if (mListener.isHeader(parent.getChildAdapterPosition(childInContact))) {
            moveHeader(c, currentHeader, childInContact)
            return
        }

        drawHeader(c, currentHeader)
    }

    private fun getHeaderViewForItem(itemPosition: Int, parent: RecyclerView): View {
        val headerPosition = mListener.getHeaderPositionForItem(itemPosition)
        val layoutResId = mListener.getHeaderLayout(headerPosition)
        val header = LayoutInflater.from(parent.context).inflate(layoutResId, parent, false)
        mListener.bindHeaderData(header, headerPosition)
        return header
    }

    private fun drawHeader(c: Canvas, header: View) {
        c.save()
        c.translate(0f, 0f)
        header.draw(c)
        c.restore()
    }

    private fun moveHeader(c: Canvas, currentHeader: View, nextHeader: View) {
        c.save()
        c.translate(0f, (nextHeader.top - currentHeader.height).toFloat())
        currentHeader.draw(c)
        c.restore()
    }

    private fun getChildInContact(parent: RecyclerView, contactPoint: Int): View? {
        var childInContact: View? = null
        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            if (child.bottom > contactPoint) {
                if (child.top <= contactPoint) {
                    // This child overlaps the contactPoint
                    childInContact = child
                    break
                }
            }
        }
        return childInContact
    }

    /**
     * Properly measures and layouts the top sticky header.
     * @param parent ViewGroup: RecyclerView in this case.
     */
    private fun fixLayoutSize(parent: ViewGroup, view: View) {

        // Specs for parent (RecyclerView)
        val widthSpec = View.MeasureSpec.makeMeasureSpec(parent.width, View.MeasureSpec.EXACTLY)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(parent.height, View.MeasureSpec.UNSPECIFIED)

        // Specs for children (headers)
        val childWidthSpec = ViewGroup.getChildMeasureSpec(widthSpec, parent.paddingLeft + parent.paddingRight, view.layoutParams.width)
        val childHeightSpec = ViewGroup.getChildMeasureSpec(heightSpec, parent.paddingTop + parent.paddingBottom, view.layoutParams.height)

        view.measure(childWidthSpec, childHeightSpec)

        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
    }

    interface StickyHeaderInterface {

        /**
         * This method gets called by [HeaderItemDecoration] to fetch the position of the header item in the adapter
         * that is used for (represents) item at specified position.
         * @param itemPosition int. Adapter's position of the item for which to do the search of the position of the header item.
         * @return int. Position of the header item in the adapter.
         */
        fun getHeaderPositionForItem(itemPosition: Int): Int

        /**
         * This method gets called by [HeaderItemDecoration] to get layout resource id for the header item at specified adapter's position.
         * @param headerPosition int. Position of the header item in the adapter.
         * @return int. Layout resource id.
         */
        fun getHeaderLayout(headerPosition: Int): Int

        /**
         * This method gets called by [HeaderItemDecoration] to setup the header View.
         * @param header View. Header to set the data on.
         * @param headerPosition int. Position of the header item in the adapter.
         */
        fun bindHeaderData(header: View, headerPosition: Int)

        /**
         * This method gets called by [HeaderItemDecoration] to verify whether the item represents a header.
         * @param itemPosition int.
         * @return true, if item at the specified adapter's position represents a header.
         */
        fun isHeader(itemPosition: Int): Boolean
    }
}