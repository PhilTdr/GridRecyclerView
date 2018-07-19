/*
 * Copyright (C) 2018 FluffyElephant
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

package de.fluffyelephant.android.tools.gridrecyclerview.util

import android.graphics.Point
import android.util.Log
import android.view.View
import de.fluffyelephant.android.tools.gridrecyclerview.GridRecyclerView


object ItemSizeCalculationHelper {

    fun getItemSize(parentView: View, rowNum: Int, colNum: Int, scrollDirection: GridRecyclerView.ScrollDirection, itemRatio: String, itemDividerSizePx: Int): Point {
        // calculate view holder max size
        var width = 0
        var height = 0
        when (scrollDirection) {
            GridRecyclerView.ScrollDirection.Horizontal -> {
                width = (parentView.width - ((rowNum + 1) * itemDividerSizePx)) / rowNum
                height = (parentView.height - ((colNum + 1) * itemDividerSizePx)) / colNum
            }
            GridRecyclerView.ScrollDirection.Vertical -> {
                width = (parentView.width - ((colNum + 1) * itemDividerSizePx)) / colNum
                height = (parentView.height - ((rowNum + 1) * itemDividerSizePx)) / rowNum
            }
        }

        // apply aspect ratio
        val ratioArray = itemRatio.split(":")
        if (ratioArray.size == 2 && ratioArray[0].toDoubleOrNull() != null && ratioArray[1].toDoubleOrNull() != null) {
            val xRatio = ratioArray[0].toDouble()
            val yRatio = ratioArray[1].toDouble()

            val maxRatio = width.toDouble() / height.toDouble()
            val newRatio = xRatio / yRatio

            val newWidth: Int
            val newHeight: Int
            when {
                maxRatio > newRatio -> {
                    newHeight = height
                    newWidth = (newRatio * height).toInt()
                }
                maxRatio < newRatio -> {
                    newWidth = width
                    newHeight = (width / newRatio).toInt()
                }
                else -> {
                    newHeight = height
                    newWidth = width
                }
            }

            Log.d("ItemSizeCalculation", "resized w:$width/h:$height with ratio w:$xRatio/h:$yRatio to new size w:$newWidth/h:$newHeight")

            width = newWidth
            height = newHeight
        }

        return Point(width, height)
    }
}