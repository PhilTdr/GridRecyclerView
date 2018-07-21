GridRecyclerView
================
[![](https://jitpack.io/v/PhilTdr/GridRecyclerView.svg)](https://jitpack.io/#PhilTdr/GridRecyclerView)


Download
--------
Add it in your root build.gradle at the end of repositories:
``` groovy
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```
Add the dependency
``` groovy
dependencies {
    implementation 'com.github.PhilTdr:GridRecyclerView:0.4.6'
}
```

Usage
-----
xml
``` xml
<de.fluffyelephant.android.tools.gridrecyclerview.GridRecyclerView
        android:id="@+id/gridRecyclerView"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        tools:listitem="@layout/grid_item" />
```

code (Kotlin)
``` kotlin
val gridAdapter = GridAdapter()

gridRecyclerView.setup(
    rowNum = 2,
    columnNum = 2,
    scrollDirection = GridRecyclerView.ScrollDirection.Horizontal,
    snapRule = GridRecyclerView.SnapRule.SnapPage,
    gridAdapter = gridAdapter,
    itemDivider = R.dimen.grid_item_divider) // itemDivider ist optional

gridAdapter.setOnItemClickListener(this)

val fakeList = ArrayList<String>()
for (i in 0 until 50) {
    fakeList.add("Item $i")
}

gridAdapter.set(fakeList)
```

GridAdapter
``` kotlin
import android.view.ViewGroup
import de.fluffyelephant.android.tools.gridrecyclerview.BaseGridAdapter

class GridAdapter : BaseGridAdapter<String>() {

    override fun createItemViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<String> {
        return GridItemViewHolder(parent)
    }

    override fun getItemViewType(position: Int): Int {
        return 0
    }
}
```

GridItemViewHolder
``` kotlin
import android.view.ViewGroup
import de.fluffyelephant.android.tools.gridrecyclerview.BaseAdapter
import kotlinx.android.synthetic.main.grid_item.view.*

class GridItemViewHolder(parent: ViewGroup) : BaseAdapter.BaseViewHolder<String>(parent, R.layout.grid_item) {

    override fun hold(item: String, onClickListener: BaseAdapter.OnItemClickListener<String>?) {
        itemView.content.text = item
    }

}
```

Demo
----
Check out the Demo Project
