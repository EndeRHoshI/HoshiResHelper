package org.hoshi.reshelper.page

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
fun StringExportAndImportPage(backAction: () -> Unit) {
    var loading by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column {
            IconButton(
                { backAction.invoke() }
            ) {
                Text("返回")
            }

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .safeContentPadding()
            ) {
                Box(
                    modifier = Modifier.weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    ExportView({ loading = true }, { loading = false })
                }
                Box(
                    modifier = Modifier.weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    ImportView({ loading = true }, { loading = false })
                }
            }

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

@Composable
fun ExportView(showLoading: () -> Unit, hideLoading: () -> Unit) {
    var folderPath by remember { mutableStateOf("") }
    var outputPath by remember { mutableStateOf("") }
    var outputFileName by remember { mutableStateOf<String?>(null) }
    var resultPath by remember { mutableStateOf("") }
    val openAlertDialog = remember { mutableStateOf<Pair<String, String>?>(null) }

    Box(
        Modifier.padding(12.dp)
    ) { // 外部相当于父布局的部分
        Column(
            // 相当于 LinearLayout
            modifier = Modifier
                .safeContentPadding()
                .wrapContentSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("xml 转换为 xlsx", fontSize = 20.sp)
            Button(
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.padding(top = 20.dp),
                onClick = {
                    FileUtils.openDirectorySelector(JFileChooser.DIRECTORIES_ONLY)?.path?.let { folderPath = it }
                }
            ) {
                Text("选择项目或 res 目录")
            }
            Text("项目或 res 目录为：$folderPath")
            Button(
                shape = RoundedCornerShape(4.dp),
                onClick = {
                    FileUtils.openDirectorySelector(JFileChooser.DIRECTORIES_ONLY)?.path?.let { outputPath = it }
                },
                modifier = Modifier.padding(top = 20.dp)
            ) {
                Text("选择输出目录")
            }
            Text("输出目录为：$outputPath")
            Text("导出文件名（可选，自带 .xlsx 后缀）", modifier = Modifier.padding(top = 20.dp))
            BasicTextField(
                value = outputFileName.orEmpty(),
                onValueChange = { outputFileName = it },
                modifier = Modifier
                    .padding(top = 10.dp, start = 30.dp, end = 30.dp)
                    .background(Color.White, RoundedCornerShape(4.dp))
                    .height(35.dp)
                    .fillMaxWidth(),
                decorationBox = { innerTextField ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp)
                    ) {
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            innerTextField()
                        }
                    }
                }
            )
            Button(
                shape = RoundedCornerShape(4.dp),
                onClick = {
                    if (folderPath.isEmpty()) {
                        openAlertDialog.value = Pair("提示", "项目或 res 目录为空，请选择后再继续")
                    } else if (outputPath.isEmpty()) {
                        openAlertDialog.value = Pair("提示", "输出目录为空，请选择后再继续")
                    } else {
                        showLoading.invoke()
                        MainScope().launch(Dispatchers.IO) {
                            resultPath = StringUtils.execute(folderPath, outputPath, outputFileName)
                            hideLoading.invoke()
                        }
                    }
                },
                modifier = Modifier.padding(top = 20.dp)
            ) {
                Text("开始处理")
            }
            Text(
                if (resultPath.isEmpty()) "" else "处理完成，文件生成于：$resultPath",
                modifier = Modifier.clickable {
                    if (resultPath.isNotEmpty()) {
                        Desktop.getDesktop().open(File(resultPath))
                    }
                }
            )
            // ScrollableLazyColumn(xmlFileList)
            // ScrollableLazyColumn(xmlStringList)
            SingleConfirmDialog(openAlertDialog)
        }
    }
}

@Composable
fun ImportView(showLoading: () -> Unit, hideLoading: () -> Unit) {
    var xlsxPath by remember { mutableStateOf("") }
    var outputPath by remember { mutableStateOf("") }
    var resultPath by remember { mutableStateOf("") }
    val openAlertDialog = remember { mutableStateOf<Pair<String, String>?>(null) }

    Box(
        Modifier.padding(12.dp)
    ) { // 外部相当于父布局的部分
        Column(
            // 相当于 LinearLayout
            modifier = Modifier
                .safeContentPadding()
                .wrapContentSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("xlsx 转换为 xml", fontSize = 20.sp)
            Button(
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.padding(top = 20.dp),
                onClick = {
                    FileUtils.openDirectorySelector(JFileChooser.FILES_ONLY)?.path?.let { xlsxPath = it }
                }) {
                Text("选择 xlsx 文件")
            }
            Text("xlsx 文件路径为：$xlsxPath")
            Button(
                modifier = Modifier.padding(top = 20.dp),
                shape = RoundedCornerShape(4.dp),
                onClick = {
                    FileUtils.openDirectorySelector(JFileChooser.FILES_ONLY)?.path?.let { outputPath = it }
                }) {
                Text("选择输出目录")
            }
            Text("输出目录为：$outputPath")
            Button(
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.padding(top = 20.dp),
                onClick = {
                    if (xlsxPath.isEmpty()) {
                        openAlertDialog.value = Pair("提示", "xlsx 文件路径为空，请选择后再继续")
                    } else if (!xlsxPath.endsWith(".xlsx")) {
                        openAlertDialog.value = Pair("提示", "未选择正确的 xlsx 文件，请重新选择")
                    } else if (outputPath.isEmpty()) {
                        openAlertDialog.value = Pair("提示", "输出目录为空，请选择后再继续")
                    } else {
                        showLoading.invoke()
                        MainScope().launch(Dispatchers.IO) {
                            resultPath = StringUtils.execute(xlsxPath, outputPath)
                            hideLoading.invoke()
                        }
                    }
                }) {
                Text("开始处理")
            }
            Text(
                if (resultPath.isEmpty()) "" else "处理完成，文件生成于：$resultPath",
                modifier = Modifier.clickable {
                    if (resultPath.isNotEmpty()) {
                        Desktop.getDesktop().open(File(outputPath))
                    }
                }
            )
            SingleConfirmDialog(openAlertDialog)
        }
    }
}