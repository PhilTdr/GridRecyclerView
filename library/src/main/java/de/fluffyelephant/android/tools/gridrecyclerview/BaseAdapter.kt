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

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import java.util.*

abstract class BaseAdapter<T> : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Iterable<T> {

    private val items = ArrayList<T>()
    private var onClickListener: OnItemClickListener<T>? = null

    interface OnItemClickListener<in T> {
        fun onItemClicked(v: View, item: T)
    }

    abstract class BaseViewHolder<T>(parent: ViewGroup, layout: Int) : RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(layout, parent, false)) {
        var item: T? = null
            private set

        fun assign(item: T, onClickListener: OnItemClickListener<T>?) {
            this.item = item
            hold(item, onClickListener)
        }

        fun onDetached() {}

        protected abstract fun hold(item: T, onClickListener: OnItemClickListener<T>?)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return createItemViewHolder(parent, viewType)
    }

    abstract fun createItemViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<T>

    abstract override fun getItemViewType(position: Int): Int

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        if (viewHolder !is BaseViewHolder<*>) throw IllegalArgumentException("viewHolder must extend BaseViewHolder")
        val holder = viewHolder as BaseViewHolder<T>
        holder.assign(items[position], onClickListener)
    }

    override fun onViewRecycled(viewHolder: RecyclerView.ViewHolder) {
        val holder = viewHolder as BaseViewHolder<T>
        holder.onDetached()
        super.onViewRecycled(holder)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun setOnItemClickListener(listener: OnItemClickListener<T>) {
        this.onClickListener = listener
    }

    fun getItemList(): ArrayList<T> {
        return items
    }

    fun getItem(position: Int): T {
        if (position < 0 || position >= items.size)
            throw IllegalArgumentException("size is " + items.size + ", requested position is " + position)
        return items[position]
    }

    fun remove(position: Int): T {
        if (position < 0 || position >= items.size)
            throw IllegalArgumentException("size is " + items.size + ", requested position is " + position)
        val item = items.removeAt(position)
        notifyItemRemoved(position)
        return item
    }

    @JvmOverloads
    fun add(item: T, position: Int = items.size) {
        items.add(position, item)
        notifyItemInserted(position)
    }

    fun remove(item: T) {
        val position = items.indexOf(item)
        if (-1 != position) {
            items.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    @JvmOverloads
    fun replace(item: T, position: Int = items.indexOf(item)) {
        if (-1 == position) {
            add(item)
        } else {
            items[position] = item
            notifyItemChanged(position)
        }
    }

    fun indexOf(item: T): Int {
        return items.indexOf(item)
    }

    fun clear() {
        items.clear()
        notifyDataSetChanged()
    }

    fun addAll(collection: Collection<T>) {
        for (item in collection) { //just to trigger proper animation
            add(item)
        }
    }

    /**
     * Adapts new set into adapter.
     * Changed items (.equals) are replaced. New ones added to the end
     *
     * @param collection
     */
    fun adapt(collection: Collection<T>) {
        for (item in collection) {
            replace(item)
        }
    }

    /**
     * Clears the adapter and adds all items from the new collection.
     * No animation. Triggers notifyDataSetChanged
     *
     * @param collection
     */
    fun set(collection: Collection<T>?) {
        if (collection == null) return
        items.clear()
        items.addAll(collection)
        notifyDataSetChanged()
    }

    override fun iterator(): Iterator<T> {
        return items.iterator()
    }

    fun sort(comparator: Comparator<T>) {
        Collections.sort(items, comparator)
        notifyDataSetChanged()
    }
}
