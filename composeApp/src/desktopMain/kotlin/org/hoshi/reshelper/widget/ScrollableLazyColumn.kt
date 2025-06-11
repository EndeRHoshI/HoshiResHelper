package org.hoshi.reshelper.widget

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * 带进度条的 LazyColumn
 */
@Composable
fun <T> ScrollableLazyColumn(dataList: List<T>) {
    // 创建 LazyColumn 的状态对象
    val scrollState = rememberLazyListState()

    Box(Modifier.fillMaxSize()) {
        // 创建LazyColumn内容
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = scrollState
        ) {
            itemsIndexed(dataList) { index: Int, item: T ->
                Text(item.toString())
            }
        }

        // 添加垂直滚动条（进度条）
        VerticalScrollbar(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight(),
            adapter = rememberScrollbarAdapter(scrollState)
        )
    }
}