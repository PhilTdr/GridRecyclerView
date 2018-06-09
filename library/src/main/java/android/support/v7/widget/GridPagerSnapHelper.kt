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
import android.view.View

class GridPagerSnapHelper : SnapHelper() {

    companion object {
        private const val MAX_SCROLL_ON_FLING_DURATION = 100 // ms
    }

    private var rowNum = 1
    private var columnNum = 1

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

    override fun calculateDistanceToFinalSnap(layoutManager: RecyclerView.LayoutManager, targetView: View): IntArray? {
        val out = IntArray(2)

        if (layoutManager.canScrollHorizontally()) {
            out[0] = distanceToCenter(layoutManager, targetView, getHorizontalHelper(layoutManager))
        } else {
            out[0] = 0
        }

        if (layoutManager.canScrollVertically()) {
            out[1] = distanceToCenter(layoutManager, targetView, getVerticalHelper(layoutManager))
        } else {
            out[1] = 0
        }

        return out
    }

    private fun distanceToCenter(layoutManager: RecyclerView.LayoutManager, targetView: View, helper: OrientationHelper): Int {
        if (layoutManager.canScrollHorizontally()) {
            val totalWidth = mRecyclerView.width
            val itemWidth = totalWidth / rowNum

            val position = layoutManager.getPosition(targetView)
            val pageIndex = pageIndex(position)
            val currentPageStart = pageIndex * countOfpage()

            val distance = (position - currentPageStart) / columnNum * itemWidth
            val childStart = helper.getDecoratedStart(targetView)

            return childStart - distance
        } else {
            val totalHeight = mRecyclerView.height
            val itemHeight = totalHeight / rowNum

            val position = layoutManager.getPosition(targetView)
            val pageIndex = pageIndex(position)
            val currentPageStart = pageIndex * countOfpage()

            val distance = (position - currentPageStart) / columnNum * itemHeight
            val childStart = helper.getDecoratedStart(targetView)

            return childStart - distance
        }
    }

    override fun findSnapView(layoutManager: RecyclerView.LayoutManager): View? {
        return when {
            layoutManager.canScrollVertically() -> getCenterView(layoutManager, getVerticalHelper(layoutManager))
            layoutManager.canScrollHorizontally() -> getCenterView(layoutManager, getHorizontalHelper(layoutManager))
            else -> null
        }
    }

    /**
     * get the page for position
     */
    private fun pageIndex(position: Int): Int {
        return position / countOfpage()
    }

    /**
     * the total count of items per page
     */
    private fun countOfpage(): Int {
        return rowNum * columnNum
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

    override fun findTargetSnapPosition(layoutManager: RecyclerView.LayoutManager, velocityX: Int, velocityY: Int): Int {
        val itemCount = layoutManager.itemCount
        if (itemCount == 0) {
            return RecyclerView.NO_POSITION
        }

        var mStartMostChildView: View? = null
        if (layoutManager.canScrollVertically()) {
            mStartMostChildView = getStartView(layoutManager, getVerticalHelper(layoutManager))
        } else if (layoutManager.canScrollHorizontally()) {
            mStartMostChildView = getStartView(layoutManager, getHorizontalHelper(layoutManager))
        }

        if (mStartMostChildView == null) {
            return RecyclerView.NO_POSITION
        }
        val centerPosition = layoutManager.getPosition(mStartMostChildView)
        if (centerPosition == RecyclerView.NO_POSITION) {
            return RecyclerView.NO_POSITION
        }

        val forwardDirection: Boolean = if (layoutManager.canScrollHorizontally()) {
            velocityX > 0
        } else {
            velocityY > 0
        }

        var reverseLayout = false
        if (layoutManager is RecyclerView.SmoothScroller.ScrollVectorProvider) {
            val vectorProvider = layoutManager as RecyclerView.SmoothScroller.ScrollVectorProvider
            val vectorForEnd = vectorProvider.computeScrollVectorForPosition(itemCount - 1)
            if (vectorForEnd != null) {
                reverseLayout = vectorForEnd.x < 0 || vectorForEnd.y < 0
            }
        }

        val pageIndex = pageIndex(centerPosition)

        val currentPageStart = pageIndex * countOfpage()

        return if (reverseLayout)
            if (forwardDirection) currentPageStart - countOfpage() else currentPageStart
        else
            if (forwardDirection) currentPageStart + countOfpage() else currentPageStart + countOfpage() - 1
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

    override fun createSnapScroller(layoutManager: RecyclerView.LayoutManager?): LinearSmoothScroller? {
        return if (layoutManager !is RecyclerView.SmoothScroller.ScrollVectorProvider) {
            null
        } else object : LinearSmoothScroller(mRecyclerView.context) {
            override fun onTargetFound(targetView: View, state: RecyclerView.State, action: RecyclerView.SmoothScroller.Action) {
                val snapDistances = calculateDistanceToFinalSnap(mRecyclerView.layoutManager!!, targetView)!!
                val dx = snapDistances[0]
                val dy = snapDistances[1]
                val time = calculateTimeForDeceleration(Math.max(Math.abs(dx), Math.abs(dy)))
                if (time > 0) {
                    action.update(dx, dy, time, mDecelerateInterpolator)
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
