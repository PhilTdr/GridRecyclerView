/*
 * Copyright (C) 2018 Philipp Treder
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package android.support.v7.widget

import android.graphics.PointF
import android.util.DisplayMetrics
import android.util.Log
import android.view.View

class GridPagerSnapHelper() : SnapHelper() {

    companion object {
        private const val TAG = "GridPagerSnapHelper"
        private const val MAX_SCROLL_ON_FLING_DURATION = 200 // ms
    }

    constructor(cellSpacing: Int) : this() {
        this.cellSpacing = cellSpacing
    }

    private var rowNum = 1
    private var columnNum = 1

    private var cellSpacing: Int = 0
    private var mVerticalHelper: OrientationHelper? = null
    private var mHorizontalHelper: OrientationHelper? = null

    /**
     * sets the rowNum num of your grid
     */
    fun setRow(rowNum: Int): GridPagerSnapHelper {
        if (this.rowNum <= 0) {
            throw IllegalArgumentException("rowNum must be greater than zero")
        }
        this.rowNum = rowNum
        return this
    }

    /**
     * sets the columnNum num of your grid
     */
    fun setColumn(columnNum: Int): GridPagerSnapHelper {
        if (this.columnNum <= 0) {
            throw IllegalArgumentException("columnNum must be greater than zero")
        }
        this.columnNum = columnNum
        return this
    }

    /**
     * get the page for position
     */
    private fun getPageForPosition(position: Int): Int {
        return position / countOfItemsPerPage()
    }

    /**
     * the total count of items per page
     */
    private fun countOfItemsPerPage(): Int {
        return rowNum * columnNum
    }

    override fun calculateDistanceToFinalSnap(layoutManager: RecyclerView.LayoutManager, targetView: View): IntArray? {
        val out = IntArray(2)

        if (layoutManager.canScrollHorizontally()) {
            out[0] = distanceToStart(layoutManager, targetView, getHorizontalHelper(layoutManager))
        } else {
            out[0] = 0
        }

        if (layoutManager.canScrollVertically()) {
            out[1] = distanceToStart(layoutManager, targetView, getVerticalHelper(layoutManager))
        } else {
            out[1] = 0
        }

        return out
    }

    private fun distanceToStart(layoutManager: RecyclerView.LayoutManager, targetView: View, helper: OrientationHelper): Int {
        if (layoutManager.canScrollHorizontally()) {
            val totalWidthPx = helper.end
            val itemWidthMaxPx = (totalWidthPx - ((rowNum + 1) * cellSpacing)) / rowNum
            val itemWidthCurrentPx = targetView.width
            val startSpacing = (totalWidthPx - rowNum * itemWidthCurrentPx - (rowNum - 1) * cellSpacing) / 2
            val roundingErrorPx = Math.abs(totalWidthPx - rowNum * itemWidthCurrentPx - (rowNum - 1) * cellSpacing - startSpacing * 2)

            val position = layoutManager.getPosition(targetView)
            val pageIndex = getPageForPosition(position)
            val currentPageStart = pageIndex * countOfItemsPerPage()

            val itemsBetweenPageStartAndCurrentScrollPosition = (position - currentPageStart) / columnNum
            val distanceFromPageStartToChildStartPx = if (position % countOfItemsPerPage() < columnNum) {
                0
            } else {
                0 +
                        startSpacing +
                        itemsBetweenPageStartAndCurrentScrollPosition * itemWidthCurrentPx +
                        Math.max(0, itemsBetweenPageStartAndCurrentScrollPosition - 1) * cellSpacing +
                        (if (itemsBetweenPageStartAndCurrentScrollPosition > 0) cellSpacing / 2 else 0)
            }
            val distanceFromCurrentScrollPositionToChildStart = helper.getDecoratedStart(targetView)

            val scrollDistanceToStartPx = distanceFromCurrentScrollPositionToChildStart - distanceFromPageStartToChildStartPx
            val scrollDistanceToEndPx = totalWidthPx - Math.abs(scrollDistanceToStartPx)
            return when {
                Math.abs(Math.abs(scrollDistanceToStartPx) - totalWidthPx) <= Math.max(5, roundingErrorPx) -> 0
                else -> scrollDistanceToStartPx
            }
        } else {
            val totalHeightPx = helper.end
            val itemHeightMaxPx = (totalHeightPx - ((rowNum + 1) * cellSpacing)) / rowNum
            val itemHeightCurrentPx = targetView.height
            val startSpacing = (totalHeightPx - rowNum * itemHeightCurrentPx - (rowNum - 1) * cellSpacing) / 2
            val roundingErrorPx = Math.abs(totalHeightPx - rowNum * itemHeightCurrentPx - (rowNum - 1) * cellSpacing - startSpacing * 2)

            val position = layoutManager.getPosition(targetView)
            val pageIndex = getPageForPosition(position)
            val currentPageStart = pageIndex * countOfItemsPerPage()

            val itemsBetweenPageStartAndCurrentScrollPosition = (position - currentPageStart) / columnNum
            val distanceFromPageStartToChildStartPx = if (position % countOfItemsPerPage() < columnNum) {
                0
            } else {
                0 +
                        startSpacing +
                        itemsBetweenPageStartAndCurrentScrollPosition * itemHeightCurrentPx +
                        Math.max(0, itemsBetweenPageStartAndCurrentScrollPosition - 1) * cellSpacing +
                        (if (itemsBetweenPageStartAndCurrentScrollPosition > 0) cellSpacing / 2 else 0)
            }
            val distanceFromCurrentScrollPositionToChildStart = helper.getDecoratedStart(targetView)

            val scrollDistanceToStartPx = distanceFromCurrentScrollPositionToChildStart - distanceFromPageStartToChildStartPx
            val scrollDistanceToEndPx = totalHeightPx - Math.abs(scrollDistanceToStartPx)
            return when {
                Math.abs(Math.abs(scrollDistanceToStartPx) - totalHeightPx) <= Math.max(5, roundingErrorPx) -> 0
                else -> scrollDistanceToStartPx
            }
        }
        // TODO can not handle snap to end like:
        // TODO   Math.abs(scrollDistanceToStartPx) < Math.abs(scrollDistanceToEndPx) -> scrollDistanceToStartPx
        // TODO   else -> scrollDistanceToEndPx
        // TODO because otherwise the flying (findTargetSnapPosition) doesn't work anymore
    }

    override fun findSnapView(layoutManager: RecyclerView.LayoutManager): View? {
        return when {
            layoutManager.canScrollVertically() -> getStartView(layoutManager, getVerticalHelper(layoutManager))
            layoutManager.canScrollHorizontally() -> getStartView(layoutManager, getHorizontalHelper(layoutManager))
            else -> null
        }
    }

    override fun findTargetSnapPosition(layoutManager: RecyclerView.LayoutManager, velocityX: Int, velocityY: Int): Int {
        val itemCount = layoutManager.itemCount
        if (itemCount == 0) {
            return RecyclerView.NO_POSITION
        }

        val snapView: View? = when {
            layoutManager.canScrollHorizontally() -> getCenterView(layoutManager, getHorizontalHelper(layoutManager))
            layoutManager.canScrollVertically() -> getCenterView(layoutManager, getVerticalHelper(layoutManager))
            else -> return RecyclerView.NO_POSITION
        }

        val snapViewPosition = layoutManager.getPosition(snapView)
        if (snapViewPosition == RecyclerView.NO_POSITION) {
            return RecyclerView.NO_POSITION
        }

        val forwardDirection: Boolean = if (layoutManager.canScrollHorizontally()) {
            velocityX > 0
        } else {
            velocityY > 0
        }

        val fastScrolling = Math.abs(velocityX) > 1000 || Math.abs(velocityY) > 1000
        Log.d("snap", "fastScrolling: $fastScrolling")

        var reverseLayout = false
        if (layoutManager is RecyclerView.SmoothScroller.ScrollVectorProvider) {
            val vectorProvider = layoutManager as RecyclerView.SmoothScroller.ScrollVectorProvider
            val vectorForEnd = vectorProvider.computeScrollVectorForPosition(itemCount - 1)
            if (vectorForEnd != null) {
                reverseLayout = vectorForEnd.x < 0 || vectorForEnd.y < 0
            }
        }

        val page = getPageForPosition(snapViewPosition)
        val pageStartIndex = page * countOfItemsPerPage()

        val pageStartIndexNext = if (reverseLayout) {
            if (forwardDirection) pageStartIndex - countOfItemsPerPage() else pageStartIndex + countOfItemsPerPage()
        } else {
            if (forwardDirection) pageStartIndex + countOfItemsPerPage() else pageStartIndex - countOfItemsPerPage()
        }

        return if (fastScrolling) {
            pageStartIndexNext
        } else {
            pageStartIndex
        }
    }

    /**
     * @param layoutManager The [RecyclerView.LayoutManager] associated with the attached[RecyclerView].
     * @param helper        The relevant [android.support.v7.widget.OrientationHelper] for the attached [RecyclerView].
     * @return the child view that is currently closest to the start.
     */
    private fun getStartView(layoutManager: RecyclerView.LayoutManager, helper: OrientationHelper): View? {
        val childCount = layoutManager.childCount
        if (childCount == 0) {
            return null
        }

        var closestChild: View? = null
        var startest = Integer.MAX_VALUE

        for (i in 0 until childCount) {
            val child = layoutManager.getChildAt(i)
            val childStart = helper.getDecoratedStart(child)

            /** if child is more to start than previous closest, set it as closest   */
            if (childStart < startest) {
                startest = childStart
                closestChild = child
            }
        }

        return closestChild
    }

    /**
     * @param layoutManager The [RecyclerView.LayoutManager] associated with the attached[RecyclerView].
     * @param helper        The relevant [android.support.v7.widget.OrientationHelper] for the attached [RecyclerView].
     * @return the child view that is currently closest to the center.
     */
    private fun getCenterView(layoutManager: RecyclerView.LayoutManager, helper: OrientationHelper): View? {
        val childCount = layoutManager.childCount
        if (childCount == 0) {
            return null
        }

        var closestChild: View? = null
        val center: Int = if (layoutManager.clipToPadding) {
            helper.startAfterPadding + helper.totalSpace / 2
        } else {
            helper.end / 2
        }
        var absClosest = Integer.MAX_VALUE

        for (i in 0 until childCount) {
            val child = layoutManager.getChildAt(i)
            val childCenter = helper.getDecoratedStart(child) + helper.getDecoratedMeasurement(child) / 2
            val absDistance = Math.abs(childCenter - center)

            /** if child center is closer than previous closest, set it as closest   */
            if (absDistance < absClosest) {
                absClosest = absDistance
                closestChild = child
            }
        }

        return closestChild
    }

    override fun createScroller(layoutManager: RecyclerView.LayoutManager?): LinearSmoothScroller? {
        return if (layoutManager !is RecyclerView.SmoothScroller.ScrollVectorProvider) {
            null
        } else object : LinearSmoothScroller(mRecyclerView.context) {
            /**
             * onTargetFound: Called if the target view is known when scrolling
             * My observation: only called form RecyclerView when scrolling forward
             */
            override fun onTargetFound(targetView: View, state: RecyclerView.State, action: RecyclerView.SmoothScroller.Action) {
                val snapDistances = calculateDistanceToFinalSnap(mRecyclerView.layoutManager!!, targetView)!!
                val dx = snapDistances[0]
                val dy = snapDistances[1]
                val time = calculateTimeForDeceleration(Math.max(Math.abs(dx), Math.abs(dy)))
                if (time > 0) {
                    action.update(dx, dy, time, mDecelerateInterpolator)
                }
            }

            /**
             * onSeekTargetStep: Called if the target view is not known when scrolling
             * My observation: called form RecyclerView when scrolling backward
             */
            override fun onSeekTargetStep(dx: Int, dy: Int, state: RecyclerView.State, action: Action) {
                val targetView = findSnapView(layoutManager)
                when {
                    targetView != null -> {
                        // when findSnapView found an view to snap
                        onTargetFound(targetView, state, action)
                    }
                    layoutManager is LinearLayoutManager -> {
                        // when findSnapView not found an view to snap but layoutManager is LinearLayoutManager snap to first visible view
                        val view = layoutManager.findOneVisibleChild(0, layoutManager.childCount, false, true)
                        onTargetFound(view, state, action)
                    }
                    else -> {
                        super.onSeekTargetStep(dx, dy, state, action)
                    }
                }
            }

            override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float {
                return SnapHelper.MILLISECONDS_PER_INCH / displayMetrics.densityDpi
            }

            override fun calculateTimeForScrolling(dx: Int): Int {
                return Math.min(MAX_SCROLL_ON_FLING_DURATION, super.calculateTimeForScrolling(dx))
            }

            override fun computeScrollVectorForPosition(targetPosition: Int): PointF? {
                return null
            }
        }
    }

    private fun getVerticalHelper(layoutManager: RecyclerView.LayoutManager): OrientationHelper {
        if (mVerticalHelper == null || mVerticalHelper!!.mLayoutManager !== layoutManager) {
            mVerticalHelper = OrientationHelper.createVerticalHelper(layoutManager)
        }
        return mVerticalHelper!!
    }

    private fun getHorizontalHelper(
            layoutManager: RecyclerView.LayoutManager): OrientationHelper {
        if (mHorizontalHelper == null || mHorizontalHelper!!.mLayoutManager !== layoutManager) {
            mHorizontalHelper = OrientationHelper.createHorizontalHelper(layoutManager)
        }
        return mHorizontalHelper!!
    }

}
