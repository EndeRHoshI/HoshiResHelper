package org.hoshi.reshelper

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.hoshi.reshelper.utils.FileUtils
import org.hoshi.reshelper.utils.Scanner
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme {
        val scrollState = rememberScrollState() // 记住滚动状态
        var path by remember { mutableStateOf("") }
        var xmlFileListStr by remember { mutableStateOf("") }
        Column(
            modifier = Modifier
                .safeContentPadding()
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(onClick = {
                FileUtils.openDirectorySelector()?.path?.let { path = it }
            }) {
                Text("点击这里选择项目目录")
            }
            Text("你选择的目录为：$path")
            Button(onClick = {
                val xmlFileList = mutableListOf<String>()
                Scanner.scan(path, xmlFileList)
                xmlFileListStr = xmlFileList.toString()
            }) {
                Text("开始扫描")
            }
            Column (
                modifier = Modifier
                    .verticalScroll(scrollState) // 使整个 Column 可滚动
                    .fillMaxSize() // 填充父容器
            ) {
                Text(xmlFileListStr)
            }
        }
    }
}