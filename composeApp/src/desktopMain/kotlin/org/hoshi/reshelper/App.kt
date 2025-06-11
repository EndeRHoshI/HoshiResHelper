package org.hoshi.reshelper

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.hoshi.reshelper.data.XmlString
import org.hoshi.reshelper.utils.FileUtils
import org.hoshi.reshelper.utils.Parser
import org.hoshi.reshelper.utils.Scanner
import org.hoshi.reshelper.widget.ScrollableLazyColumn
import org.hoshi.reshelper.widget.SingleConfirmDialog
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme {
        var projectPath by remember { mutableStateOf("") }
        var outputPath by remember { mutableStateOf("") }
        val xmlFileList = mutableStateListOf<String>()
        val xmlStringList = mutableStateListOf<XmlString>()
        val openAlertDialog = remember { mutableStateOf<Pair<String, String>?>(null) }

        Column(
            modifier = Modifier
                .safeContentPadding()
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(onClick = {
                FileUtils.openDirectorySelector()?.path?.let { projectPath = it }
            }) {
                Text("选择项目目录")
            }
            Text("项目目录为：$projectPath")
            Button(onClick = {
                FileUtils.openDirectorySelector()?.path?.let { outputPath = it }
            }) {
                Text("选择输出目录")
            }
            Text("输出目录为：$outputPath")
            Button(onClick = {
                if (projectPath.isEmpty()) {
                    openAlertDialog.value = Pair("提示", "项目目录为空，请选择后再继续")
                } else if (outputPath.isEmpty()) {
                    openAlertDialog.value = Pair("提示", "输出目录为空，请选择后再继续")
                } else {
                    Scanner.scan(projectPath, xmlFileList)
                    xmlStringList.clear()
                    xmlStringList.addAll(Parser.parseStringsXml(xmlFileList))
                    print("一共有 ${xmlStringList.size} 条数据")
                }
            }) {
                Text("开始扫描")
            }
            // ScrollableLazyColumn(xmlFileList)
            ScrollableLazyColumn(xmlStringList)
            SingleConfirmDialog(openAlertDialog)
        }
    }
}



