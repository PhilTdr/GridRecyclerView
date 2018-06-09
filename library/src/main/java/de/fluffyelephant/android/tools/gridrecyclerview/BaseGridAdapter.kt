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

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup

abstract class BaseGridAdapter<T> : BaseAdapter<T>() {

    private var scrollDirection = GridRecyclerView.ScrollDirection.Horizontal
    private var rowNum: Int = 1
    private var colNum: Int = 1

    internal fun setup(rowNum: Int, colNum: Int, scrollDirection: GridRecyclerView.ScrollDirection) {
        this.scrollDirection = scrollDirection
        this.rowNum = rowNum
        this.colNum = colNum
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val viewHolder = createViewHolder(parent)

        var width = 0
        var height = 0
        when (scrollDirection) {
            GridRecyclerView.ScrollDirection.Horizontal -> {
                width = parent.width / rowNum
                height = parent.height / colNum
            }
            GridRecyclerView.ScrollDirection.Vertical -> {
                width = parent.width / colNum
                height = parent.height / rowNum
            }
        }

        val layoutParams = viewHolder.itemView.layoutParams
        layoutParams.width = width
        layoutParams.height = height

        return viewHolder
    }
}