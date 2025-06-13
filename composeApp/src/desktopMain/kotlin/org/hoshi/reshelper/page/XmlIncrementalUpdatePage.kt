package org.hoshi.reshelper.page

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.hoshi.reshelper.string.StringUtils
import org.hoshi.reshelper.string.XmlString
import org.hoshi.reshelper.utils.FileUtils
import org.hoshi.reshelper.widget.SingleConfirmDialog
import java.awt.Desktop
import java.io.File
import javax.swing.JFileChooser

@Composable
fun XmlIncrementalUpdatePage(backAction: () -> Unit) {
    var folderPath by remember { mutableStateOf("") }
    var outputPath by remember { mutableStateOf("") }
    var outputFileName by remember { mutableStateOf(TextFieldValue()) }
    var successTips by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    val xmlFileList = mutableStateListOf<String>()
    val xmlStringList = mutableStateListOf<XmlString>()
    val openAlertDialog = remember { mutableStateOf<Pair<String, String>?>(null) }

    IconButton(
        { backAction.invoke() }
    ) {
        Text("返回")
    }

    Box(Modifier.fillMaxSize()) { // 外部相当于父布局的部分
        Column(
            // 相当于 LinearLayout
            modifier = Modifier
                .safeContentPadding()
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(onClick = {
                FileUtils.openDirectorySelector(JFileChooser.DIRECTORIES_ONLY)?.path?.let { folderPath = it }
            }) {
                Text("选择项目或 res 目录")
            }
            Text("项目或 res 目录为：$folderPath")
            Button(onClick = {
                FileUtils.openDirectorySelector(JFileChooser.DIRECTORIES_ONLY)?.path?.let { outputPath = it }
            }) {
                Text("选择输出目录")
            }
            Text("输出目录为：$outputPath")
            TextField(
                outputFileName,
                { outputFileName = it },
                label = { Text("导出文件名（可选，自带 .xlsx 后缀）") },
                singleLine = true
            )
            Button(onClick = {
                if (folderPath.isEmpty()) {
                    openAlertDialog.value = Pair("提示", "项目或 res 目录为空，请选择后再继续")
                } else if (outputPath.isEmpty()) {
                    openAlertDialog.value = Pair("提示", "输出目录为空，请选择后再继续")
                } else {
                    loading = true
                    MainScope().launch(Dispatchers.IO) {
                        val result = StringUtils.execute(folderPath, outputPath, outputFileName.text)
                        loading = false
                        successTips = "处理完成，文件生成于：${"$outputPath/${outputFileName.text}.xlsx"}"
                    }
                }
            }) {
                Text("开始处理")
            }
            Text(
                successTips,
                modifier = Modifier.clickable {
                    if (successTips.isNotEmpty()) {
                        Desktop.getDesktop().open(File("$outputPath/${outputFileName.text}.xlsx"))
                    }
                }
            )
            // ScrollableLazyColumn(xmlFileList)
            // ScrollableLazyColumn(xmlStringList)
            SingleConfirmDialog(openAlertDialog)
        }

        if (loading) {
            Box( // Loading 的背景
                Modifier.fillMaxSize()
                    .background(Color(0x99333333))
                    .clickable(false) {}, // 把点击事件去掉，不让点击下层
                contentAlignment = Alignment.Center // 使内部控件水平居中
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(64.dp),
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            }
        }
    }
}