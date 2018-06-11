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
import android.support.v7.widget.RecyclerView
import android.view.View

class GridPagerItemDecoration(
        private val spacingPx: Int,
        private val rowNum: Int,
        private val columnNum: Int)
    : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view)
        val layoutManager = parent.layoutManager ?: return

        val column: Int = position % columnNum
        val row: Int = (position / columnNum) % rowNum

        val left: Int
        val top: Int
        val right: Int
        val bottom: Int
        if (layoutManager.canScrollHorizontally()) {
            left = when (row) {
                0 -> spacingPx
                else -> spacingPx / 2
            }
            top = when (column) {
                0 -> spacingPx
                else -> spacingPx / 2
            }
            right = when (row) {
                rowNum - 1 -> spacingPx
                else -> spacingPx / 2
            }
            bottom = when (column) {
                columnNum - 1 -> spacingPx
                else -> spacingPx / 2
            }
        } else {
            left = when (column) {
                0 -> spacingPx
                else -> spacingPx / 2
            }
            top = when (row) {
                0 -> spacingPx
                else -> spacingPx / 2
            }
            right = when (column) {
                columnNum - 1 -> spacingPx
                else -> spacingPx / 2
            }
            bottom = when (row) {
                rowNum - 1 -> spacingPx
                else -> spacingPx / 2
            }
        }

        outRect.set(left, top, right, bottom)
    }
}
