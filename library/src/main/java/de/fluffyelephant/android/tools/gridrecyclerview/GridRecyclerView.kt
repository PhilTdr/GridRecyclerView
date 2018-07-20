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
import android.graphics.Point
import android.support.annotation.DimenRes
import android.support.v7.widget.*
import android.util.AttributeSet
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import de.fluffyelephant.android.tools.gridrecyclerview.decoration.GridContinuousItemDecoration
import de.fluffyelephant.android.tools.gridrecyclerview.decoration.GridPagerItemDecoration
import de.fluffyelephant.android.tools.gridrecyclerview.util.ItemSizeCalculationHelper


class GridRecyclerView : RecyclerView {

    interface StateListener {
        fun onPageVisible(page: Int)
    }

    enum class ScrollDirection {
        Horizontal,
        Vertical
    }

    enum class SnapRule {
        ContinuousScroll,
        SnapSingeItem,
        SnapPage
    }

    private var rowNum: Int = 0
    private var columnNum: Int = 0
    private lateinit var scrollDirection: ScrollDirection
    private lateinit var snapRule: SnapRule
    private lateinit var gridAdapter: BaseGridAdapter<*>
    private lateinit var itemRatio: String
    private var itemDividerSizePx: Int = 0

    private var itemSize: Point? = null

    var onStateListener: StateListener? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    fun setup(rowNum: Int, columnNum: Int, scrollDirection: ScrollDirection, snapRule: SnapRule, gridAdapter: BaseGridAdapter<*>) {
        setupGridRecyclerView(rowNum, columnNum, scrollDirection, snapRule, gridAdapter, "", 0)
    }

    fun setup(rowNum: Int, columnNum: Int, scrollDirection: ScrollDirection, snapRule: SnapRule, gridAdapter: BaseGridAdapter<*>, itemRatio: String) {
        setupGridRecyclerView(rowNum, columnNum, scrollDirection, snapRule, gridAdapter, itemRatio, 0)
    }

    fun setup(rowNum: Int, columnNum: Int, scrollDirection: ScrollDirection, snapRule: SnapRule, gridAdapter: BaseGridAdapter<*>, @DimenRes itemDivider: Int) {
        setupGridRecyclerView(rowNum, columnNum, scrollDirection, snapRule, gridAdapter, "", context.resources.getDimensionPixelSize(itemDivider))
    }

    fun setup(rowNum: Int, columnNum: Int, scrollDirection: ScrollDirection, snapRule: SnapRule, gridAdapter: BaseGridAdapter<*>, itemRatio: String, @DimenRes itemDivider: Int) {
        setupGridRecyclerView(rowNum, columnNum, scrollDirection, snapRule, gridAdapter, itemRatio, context.resources.getDimensionPixelSize(itemDivider))
    }

    private fun setupGridRecyclerView(rowNum: Int, columnNum: Int, scrollDirection: ScrollDirection, snapRule: SnapRule, gridAdapter: BaseGridAdapter<*>, itemRatio: String, itemDividerSizePx: Int) {
        this.rowNum = rowNum
        this.columnNum = columnNum
        this.scrollDirection = scrollDirection
        this.snapRule = snapRule
        this.gridAdapter = gridAdapter
        this.itemRatio = itemRatio
        this.itemDividerSizePx = itemDividerSizePx
        applySetup()

        if (width == 0 && height == 0) {
            viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    viewTreeObserver.removeOnGlobalLayoutListener(this)
                    val width = measuredWidth
                    val height = measuredHeight

                    itemSize = null
                    setupSnapping()
                    setupItemDecoration()
                    this@GridRecyclerView.adapter = gridAdapter
                    adapter.notifyDataSetChanged()
                }
            })
        }
    }

    private fun applySetup() {
        val orientation = when (scrollDirection) {
            ScrollDirection.Horizontal -> HORIZONTAL
            ScrollDirection.Vertical -> VERTICAL
        }

        // setup
        this.setHasFixedSize(true)

        // caching
        this.setItemViewCacheSize(rowNum * columnNum * 3)
        this.isDrawingCacheEnabled = true
        this.drawingCacheQuality = View.DRAWING_CACHE_QUALITY_HIGH

        // layout manager
        val layoutManager = GridLayoutManager(context, columnNum, orientation, false)
        this.layoutManager = layoutManager

        // adapter
        gridAdapter.setup(this)
        this.adapter = gridAdapter

        // snapping - paging
        setupSnapping()

        // item decoration
        setupItemDecoration()

        // scroll listener
        this.addOnScrollListener(scrollListener)

        // notify current page is 0
        onStateListener?.onPageVisible(0)
    }

    private fun setupItemDecoration() {
        for (i in 0 until this.itemDecorationCount) {
            this.removeItemDecorationAt(i)
        }
        if (itemDividerSizePx > 0) {
            val itemDecoration = when (snapRule) {
                SnapRule.SnapPage -> GridPagerItemDecoration(
                        this,
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

    private fun setupSnapping() {
        this.onFlingListener = null // avoid IllegalStateException: An instance of OnFlingListener already set.
        when (snapRule) {
            SnapRule.SnapSingeItem -> {
                StartSnapHelper(itemDividerSizePx).attachToRecyclerView(this)
            }
            SnapRule.SnapPage -> {
                val gridPagerSnapHelper = GridPagerSnapHelper(itemDividerSizePx)
                gridPagerSnapHelper.setRow(rowNum).setColumn(columnNum)
                gridPagerSnapHelper.attachToRecyclerView(this)
            }
            else -> {
                // do nothing
            }
        }
    }

    private val scrollListener = object : OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)

            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                onStateListener?.onPageVisible(getCurrentPage())
            }
        }

        override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
        }
    }

    fun getCurrentPage(): Int {
        val layoutManager = this.layoutManager

        var position = 0
        if (layoutManager is GridLayoutManager) {
            val itemsPerPage = rowNum * columnNum
            position = layoutManager.findFirstCompletelyVisibleItemPosition() / itemsPerPage
        } else if (layoutManager is LinearLayoutManager) {
            position = layoutManager.findFirstCompletelyVisibleItemPosition()
        }
        return position
    }

    internal fun getItemSize(): Point {
        if (itemSize == null) {
            itemSize = ItemSizeCalculationHelper.getItemSize(
                    parentView = this,
                    rowNum = rowNum,
                    colNum = columnNum,
                    scrollDirection = scrollDirection,
                    itemRatio = itemRatio,
                    itemDividerSizePx = itemDividerSizePx)
        }
        return itemSize!!
    }

}
