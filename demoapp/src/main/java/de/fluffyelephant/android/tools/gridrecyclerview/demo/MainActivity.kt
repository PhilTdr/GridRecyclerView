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

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import de.fluffyelephant.android.tools.gridrecyclerview.GridRecyclerView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object {
        private const val defaultRowNum = 3
        private const val defaultColNum = 3
        private val defaultScrollDirection = GridRecyclerView.ScrollDirection.Horizontal
        private val defaultSnapRule = GridRecyclerView.SnapRule.SnapPage
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initInput(defaultRowNum, defaultColNum, defaultScrollDirection, defaultSnapRule)
        showContent(defaultRowNum, defaultColNum, defaultScrollDirection, defaultSnapRule)
    }

    private fun showContent(rowNum: Int, colNum: Int, scrollDirection: GridRecyclerView.ScrollDirection, snapRule: GridRecyclerView.SnapRule) {
        val gridAdapter = GridAdapter()
        val gridRecyclerView = GridRecyclerView(this)

        gridRecyclerView.setup(
                rowNum = rowNum,
                columnNum = colNum,
                scrollDirection = scrollDirection,
                snapRule = snapRule,
                gridAdapter = gridAdapter)

        val fakeList = ArrayList<String>()
        for (i in 0 until 50) {
            fakeList.add("Item $i")
        }

        gridAdapter.set(fakeList)

        gridRecyclerViewContainer.removeAllViews()
        gridRecyclerViewContainer.addView(gridRecyclerView, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
    }

    private fun initInput(rowNum: Int, colNum: Int, scrollDirection: GridRecyclerView.ScrollDirection, snapRule: GridRecyclerView.SnapRule) {
        rowNumInput.setText(rowNum.toString())
        colNumInput.setText(colNum.toString())

        when (scrollDirection) {
            GridRecyclerView.ScrollDirection.Horizontal -> scrollDirectionHorizontalInput.performClick()
            GridRecyclerView.ScrollDirection.Vertical -> scrollDirectionVerticalInput.performClick()
        }

        when (snapRule) {
            GridRecyclerView.SnapRule.ContinuousScroll -> snapRuleContinuousScrollInput.performClick()
            GridRecyclerView.SnapRule.SnapSingeItem -> snapRuleSnapSingeItemInput.performClick()
            GridRecyclerView.SnapRule.SnapPage -> snapRuleSnapPageInput.performClick()
        }

        submitButton.setOnClickListener {
            closeKeyboard(this, rowNumInput)
            closeKeyboard(this, colNumInput)

            val scrollDirectionInput = when {
                scrollDirectionHorizontalInput.isChecked -> GridRecyclerView.ScrollDirection.Horizontal
                scrollDirectionVerticalInput.isChecked -> GridRecyclerView.ScrollDirection.Vertical
                else -> GridRecyclerView.ScrollDirection.Horizontal
            }
            val snapRuleInput = when {
                snapRuleContinuousScrollInput.isChecked -> GridRecyclerView.SnapRule.ContinuousScroll
                snapRuleSnapSingeItemInput.isChecked -> GridRecyclerView.SnapRule.SnapSingeItem
                snapRuleSnapPageInput.isChecked -> GridRecyclerView.SnapRule.SnapPage
                else -> GridRecyclerView.SnapRule.ContinuousScroll
            }

            showContent(
                    rowNum = rowNumInput.text.toString().toInt(),
                    colNum = colNumInput.text.toString().toInt(),
                    scrollDirection = scrollDirectionInput,
                    snapRule = snapRuleInput
            )
        }
    }

    fun closeKeyboard(context: Context, view: View) {
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}
