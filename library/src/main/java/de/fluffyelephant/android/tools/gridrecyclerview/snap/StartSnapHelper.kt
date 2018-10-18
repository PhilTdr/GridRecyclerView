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

package androidx.recyclerview.widget

import android.view.View

class StartSnapHelper() : LinearSnapHelper() {

    constructor(cellSpacing: Int) : this() {
        this.cellSpacing = cellSpacing
    }

    private var cellSpacing: Int = 0
    private var verticalHelper: OrientationHelper? = null
    private var horizontalHelper: OrientationHelper? = null

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
        return helper.getDecoratedStart(targetView) - helper.startAfterPadding - cellSpacing / 2
    }

    override fun findSnapView(layoutManager: RecyclerView.LayoutManager): View? {
        if (layoutManager !is LinearLayoutManager) {
            return super.findSnapView(layoutManager)
        }

        return when {
            layoutManager.canScrollVertically() -> getStartView(layoutManager, getVerticalHelper(layoutManager))
            layoutManager.canScrollHorizontally() -> getStartView(layoutManager, getHorizontalHelper(layoutManager))
            else -> null
        }
    }

    /**
     * @param layoutManager The [RecyclerView.LayoutManager] associated with the attached[RecyclerView].
     * @param helper        The relevant [android.support.v7.widget.OrientationHelper] for the attached [RecyclerView].
     * @return the child view that is currently closest to the start.
     */
    private fun getStartView(layoutManager: RecyclerView.LayoutManager, helper: OrientationHelper): View? {
        if (layoutManager !is GridLayoutManager && layoutManager !is LinearLayoutManager) {
            return super.findSnapView(layoutManager)
        }

        val spanCount = when (layoutManager) {
            is GridLayoutManager -> layoutManager.spanCount
            is LinearLayoutManager -> 1
            else -> 1
        }

        layoutManager as LinearLayoutManager

        val firstChild = layoutManager.findFirstVisibleItemPosition()
        val isLastItem = layoutManager.findLastCompletelyVisibleItemPosition() == layoutManager.itemCount - 1

        if (firstChild == RecyclerView.NO_POSITION || isLastItem) return null
        val child = layoutManager.findViewByPosition(firstChild)

        return if (helper.getDecoratedEnd(child) >= helper.getDecoratedMeasurement(child) / 2 && helper.getDecoratedEnd(child) > 0) {
            child
        } else {
            layoutManager.findViewByPosition(firstChild + spanCount)
        }
    }

    private fun getVerticalHelper(layoutManager: RecyclerView.LayoutManager): OrientationHelper {
        if (verticalHelper == null) {
            verticalHelper = OrientationHelper.createVerticalHelper(layoutManager)
        }
        return verticalHelper!!
    }

    private fun getHorizontalHelper(layoutManager: RecyclerView.LayoutManager): OrientationHelper {
        if (horizontalHelper == null) {
            horizontalHelper = OrientationHelper.createHorizontalHelper(layoutManager)
        }
        return horizontalHelper!!
    }

}