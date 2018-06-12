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

package de.fluffyelephant.android.tools.gridrecyclerview

import android.content.Context
import android.support.annotation.DimenRes
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.GridPagerSnapHelper
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StartSnapHelper
import android.util.AttributeSet
import de.fluffyelephant.android.tools.gridrecyclerview.decoration.GridContinuousItemDecoration
import de.fluffyelephant.android.tools.gridrecyclerview.decoration.GridPagerItemDecoration

class GridRecyclerView : RecyclerView {

    enum class ScrollDirection {
        Horizontal,
        Vertical
    }

    enum class SnapRule {
        ContinuousScroll,
        SnapSingeItem,
        SnapPage
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    fun setup(rowNum: Int, columnNum: Int, scrollDirection: ScrollDirection, snapRule: SnapRule, gridAdapter: BaseGridAdapter<*>) {
        setupGridRecyclerView(rowNum, columnNum, scrollDirection, snapRule, gridAdapter, 0)
    }

    fun setup(rowNum: Int, columnNum: Int, scrollDirection: ScrollDirection, snapRule: SnapRule, gridAdapter: BaseGridAdapter<*>, @DimenRes itemDivider: Int) {
        setupGridRecyclerView(rowNum, columnNum, scrollDirection, snapRule, gridAdapter, context.resources.getDimensionPixelSize(itemDivider))
    }

    private fun setupGridRecyclerView(rowNum: Int, columnNum: Int, scrollDirection: ScrollDirection, snapRule: SnapRule, gridAdapter: BaseGridAdapter<*>, itemDividerSizePx: Int) {
        val orientation = when (scrollDirection) {
            ScrollDirection.Horizontal -> HORIZONTAL
            ScrollDirection.Vertical -> VERTICAL
        }

        // layout manager
        val layoutManager = GridLayoutManager(context, columnNum, orientation, false)
        this.layoutManager = layoutManager

        // adapter
        gridAdapter.setup(rowNum, columnNum, scrollDirection, itemDividerSizePx)
        this.adapter = gridAdapter

        // snapping - paging
        when (snapRule) {
            SnapRule.SnapSingeItem -> {
                StartSnapHelper(itemDividerSizePx).attachToRecyclerView(this)
            }
            SnapRule.SnapPage -> {
                val gridPagerSnapHelper = GridPagerSnapHelper()
                gridPagerSnapHelper.setRow(rowNum).setColumn(columnNum)
                gridPagerSnapHelper.attachToRecyclerView(this)
            }
            else -> {
                // do nothing
            }
        }

        // item decoration
        if (itemDividerSizePx > 0) {
            val itemDecoration = when (snapRule) {
                SnapRule.SnapPage -> GridPagerItemDecoration(
                        itemDividerSizePx,
                        rowNum,
                        columnNum)
                else -> GridContinuousItemDecoration(
                        itemDividerSizePx,
                        rowNum,
                        columnNum)
            }
            this.addItemDecoration(itemDecoration)
        }
    }


}