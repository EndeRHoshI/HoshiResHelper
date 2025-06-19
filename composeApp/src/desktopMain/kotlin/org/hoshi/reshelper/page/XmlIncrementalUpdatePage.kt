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
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.hoshi.reshelper.utils.IncrementalUpdateUtils
import org.hoshi.reshelper.utils.FileUtils
import org.hoshi.reshelper.widget.SingleConfirmDialog
import java.awt.Desktop
import java.io.File
import javax.swing.JFileChooser

@Composable
fun XmlIncrementalUpdatePage(backAction: () -> Unit) {
    var originXmlPath by remember { mutableStateOf("") }
    var targetXmlPath by remember { mutableStateOf("") }
    var outputXmlFolder by remember { mutableStateOf("") }
    var outputXmlName by remember { mutableStateOf<String?>(null) }
    var successTips by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    val openAlertDialog = remember { mutableStateOf<Pair<String, String>?>(null) }

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

            Column(
                // 相当于 LinearLayout
                modifier = Modifier
                    .safeContentPadding()
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Button(
                    shape = RoundedCornerShape(4.dp),
                    onClick = {
                        FileUtils.openDirectorySelector(JFileChooser.FILES_ONLY)?.path?.let { originXmlPath = it }
                    }
                ) {
                    Text("选择原 xml")
                }

                Text("原 xml 路径为")
                BasicTextField(
                    value = originXmlPath,
                    onValueChange = { originXmlPath = it },
                    modifier = Modifier
                        .padding(top = 10.dp, start = 130.dp, end = 130.dp)
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
                    modifier = Modifier.padding(top = 20.dp),
                    onClick = {
                        FileUtils.openDirectorySelector(JFileChooser.FILES_ONLY)?.path?.let { targetXmlPath = it }
                    }
                ) {
                    Text("选择目标 xml")
                }
                Text("目标 xml 路径为")
                BasicTextField(
                    value = targetXmlPath,
                    onValueChange = { targetXmlPath = it },
                    modifier = Modifier
                        .padding(top = 10.dp, start = 130.dp, end = 130.dp)
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
                    modifier = Modifier.padding(top = 20.dp),
                    onClick = {
                        FileUtils.openDirectorySelector(JFileChooser.DIRECTORIES_ONLY)?.path?.let {
                            outputXmlFolder = it
                        }
                    }
                ) {
                    Text("选择输出目录")
                }
                Text("输出目录为：$outputXmlFolder")
                Text("导出文件名（可选，自带 .xml 后缀）", modifier = Modifier.padding(top = 20.dp))
                BasicTextField(
                    value = outputXmlName.orEmpty(),
                    onValueChange = { outputXmlName = it },
                    modifier = Modifier
                        .padding(top = 10.dp, start = 130.dp, end = 130.dp)
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
                    modifier = Modifier.padding(top = 20.dp),
                    onClick = {
                        if (originXmlPath.isEmpty()) {
                            openAlertDialog.value = Pair("提示", "原 xml 路径为空，请选择后再继续")
                        } else if (targetXmlPath.isEmpty()) {
                            openAlertDialog.value = Pair("提示", "目标 xml 路径为空，请选择后再继续")
                        } else if (outputXmlFolder.isEmpty()) {
                            openAlertDialog.value = Pair("提示", "输出目录为空，请选择后再继续")
                        } else {
                            loading = true
                            MainScope().launch(Dispatchers.IO) {
                                val result = IncrementalUpdateUtils.execute(
                                    originXmlPath,
                                    targetXmlPath,
                                    outputXmlFolder,
                                    outputXmlName
                                )
                                loading = false
                                successTips = result
                            }
                        }
                    }) {
                    Text("开始处理")
                }
                Text(
                    successTips,
                    modifier = Modifier.clickable {
                        if (successTips.isNotEmpty()) {
                            Desktop.getDesktop().open(File("$outputXmlFolder/$outputXmlName.xml"))
                        }
                    }
                )
                SingleConfirmDialog(openAlertDialog)
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