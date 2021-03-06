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
import android.os.Handler
import android.os.Looper
import androidx.annotation.DimenRes
import androidx.appcompat.widget.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.recyclerview.widget.*
import de.fluffyelephant.android.tools.gridrecyclerview.decoration.GridContinuousItemDecoration
import de.fluffyelephant.android.tools.gridrecyclerview.decoration.GridPagerItemDecoration
import de.fluffyelephant.android.tools.gridrecyclerview.util.ItemSizeCalculationHelper


class GridRecyclerView : RecyclerView {

    companion object {
        private const val TAG = "GridRecyclerView"
    }

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
        set(value) {
            field = if (value > 0) value else 0
        }

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
                    if (width == 0 && height == 0) {
                        return // TODO onGlobalLayout is called in an endless loop when setupGridRecyclerView is called more than once
                    }
                    Log.i(TAG, "View size was measured: W=$width / H=$height")

                    // reset itemSize to recalculate with measured recycler view size
                    itemSize = null

                    // notify components about change
                    setupSnapping()
                    setupItemDecoration()
                    adapter?.notifyDataSetChanged()

                    // invalidate view
                    this@GridRecyclerView.parent?.run {
                        // invalidate parent if possible
                        invalidate()
                        requestLayout()
                    } ?: this@GridRecyclerView.run {
                        // invalidate recycler view itself
                        invalidate()
                        requestLayout()
                    }

                    // notify current page
                    val currentPage = getCurrentPage()
                    if (currentPage != RecyclerView.NO_POSITION) {
                        onStateListener?.onPageVisible(currentPage)
                    }
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

        // notify current page
        val currentPage = getCurrentPage()
        if (currentPage != RecyclerView.NO_POSITION) {
            onStateListener?.onPageVisible(currentPage)
        }
    }

    private fun setupItemDecoration() {
        for (i in 0 until this.itemDecorationCount) {
            this.removeItemDecorationAt(i)
        }

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
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)

            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                // notify current page
                val currentPage = getCurrentPage()
                if (currentPage != RecyclerView.NO_POSITION) {
                    onStateListener?.onPageVisible(currentPage)
                }
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
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

    /**
     * jump to page
     * [page] page number
     */
    fun jumpToPage(page: Int) {
        Handler(Looper.getMainLooper()).postDelayed({
            val itemsPerPage = rowNum * columnNum
            val firstItemOnRequestedPage = itemsPerPage * page
            val lastItemOnRequestedPage = firstItemOnRequestedPage + itemsPerPage - 1
            val firstCompletelyVisibleItemPosition = (this@GridRecyclerView.layoutManager as LinearLayoutManager?)
                    ?.findFirstCompletelyVisibleItemPosition() ?: 0
            val scrollForward = lastItemOnRequestedPage > firstCompletelyVisibleItemPosition

            // get scroll to position for recycler view
            val scrollTo = if (scrollForward) {
                lastItemOnRequestedPage
            } else {
                firstItemOnRequestedPage
            }

            // if the page to be jumped to is more than one page away, you must first jump to the previous page
            if (scrollTo !in firstItemOnRequestedPage - itemsPerPage..lastItemOnRequestedPage + itemsPerPage) {
                this@GridRecyclerView.scrollToPosition(if (scrollForward) {
                    scrollTo - itemsPerPage
                } else {
                    scrollTo + itemsPerPage
                })
            }

            // scroll to requested page
            this@GridRecyclerView.smoothScrollToPosition(scrollTo)
        }, 100)
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
