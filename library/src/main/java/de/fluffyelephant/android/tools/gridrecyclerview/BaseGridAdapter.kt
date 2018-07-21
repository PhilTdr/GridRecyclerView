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

abstract class BaseGridAdapter<T> : BaseAdapter<T>() {

    lateinit var recyclerView: GridRecyclerView
        private set

    internal fun setup(recyclerView: GridRecyclerView) {
        this.recyclerView = recyclerView
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        super.onBindViewHolder(viewHolder, position)

        // get size
        val size = recyclerView.getItemSize()

        // set width and height to layout
        val layoutParams = viewHolder.itemView.layoutParams
        layoutParams.width = size.x
        layoutParams.height = size.y
        viewHolder.itemView.layoutParams = layoutParams
    }

}
