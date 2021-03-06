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

package de.fluffyelephant.android.tools.gridrecyclerview.demo

import android.view.ViewGroup
import de.fluffyelephant.android.tools.gridrecyclerview.BaseAdapter
import kotlinx.android.synthetic.main.grid_item.view.*

class GridItemViewHolder(parent: ViewGroup) : BaseAdapter.BaseViewHolder<String>(parent, R.layout.grid_item) {

    override fun hold(item: String, onClickListener: BaseAdapter.OnItemClickListener<String>?) {
        itemView.content.text = item
    }

}
