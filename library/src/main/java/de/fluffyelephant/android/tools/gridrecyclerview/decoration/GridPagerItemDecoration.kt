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

package de.fluffyelephant.android.tools.gridrecyclerview.decoration

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import de.fluffyelephant.android.tools.gridrecyclerview.GridRecyclerView

class GridPagerItemDecoration(
        private val recyclerView: GridRecyclerView,
        private val spacingPx: Int,
        private val rowNum: Int,
        private val columnNum: Int)
    : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view)
        val layoutManager = parent.layoutManager ?: return

        val itemSize = recyclerView.getItemSize()

        val horizontalOutSpacing = if (layoutManager.canScrollHorizontally()) {
            (parent.width - rowNum * itemSize.x - (rowNum - 1) * spacingPx) / 2
        } else {
            (parent.width - columnNum * itemSize.x - (columnNum - 1) * spacingPx) / 2
        }
        val verticalOutSpacing = if (layoutManager.canScrollHorizontally()) {
            (parent.height - columnNum * itemSize.y - (columnNum - 1) * spacingPx) / 2
        } else {
            (parent.height - rowNum * itemSize.y - (rowNum - 1) * spacingPx) / 2
        }

        val column: Int = position % columnNum
        val row: Int = (position / columnNum) % rowNum

        val left: Int
        val top: Int
        val right: Int
        val bottom: Int
        if (layoutManager.canScrollHorizontally()) {
            left = when (row) {
                0 -> horizontalOutSpacing
                else -> spacingPx / 2
            }
            right = when (row) {
                rowNum - 1 -> horizontalOutSpacing
                else -> spacingPx / 2
            }
            top = verticalOutSpacing * 2 - spacingPx - (verticalOutSpacing * 2 - spacingPx) * column / columnNum
            bottom = spacingPx * (column + 1) / columnNum
        } else {
            left = verticalOutSpacing * 2 - spacingPx - (verticalOutSpacing * 2 - spacingPx) * column / columnNum
            right = spacingPx * (column + 1) / columnNum
            top = when (row) {
                0 -> verticalOutSpacing
                else -> spacingPx / 2
            }
            bottom = when (row) {
                rowNum - 1 -> verticalOutSpacing
                else -> spacingPx / 2
            }
        }

        outRect.set(left, top, right, bottom)
    }
}
