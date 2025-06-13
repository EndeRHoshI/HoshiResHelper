package org.hoshi.reshelper.page

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.hoshi.reshelper.drawable.DrawableUtils
import org.hoshi.reshelper.utils.FileUtils
import org.hoshi.reshelper.widget.SingleConfirmDialog
import javax.swing.JFileChooser

@Composable
fun DrawableSimplifyPage(backAction: () -> Unit) {
    var folderPath by remember { mutableStateOf("") }
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

            Box(Modifier.fillMaxSize()) { // 外部相当于父布局的部分
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
                            FileUtils.openDirectorySelector(JFileChooser.DIRECTORIES_ONLY)?.path?.let {
                                folderPath = it
                            }
                        }) {
                        Text("选择 res 目录")
                    }
                    Text("res 目录为：$folderPath")
                    Text(
                        "注意开始处理之后，会删除 res 内多余的图片文件，请确保有做版本管理，或者先做好备份",
                        modifier = Modifier.padding(top = 20.dp)
                    )
                    Button(
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.padding(top = 20.dp),
                        onClick = {
                            if (folderPath.isEmpty()) {
                                openAlertDialog.value = Pair("提示", "res 目录为空，请选择后再继续")
                            } else if (!folderPath.endsWith("res")) {
                                openAlertDialog.value = Pair("提示", "选择的目录不是 res 目录，请重新选择")
                            } else {
                                loading = true
                                MainScope().launch(Dispatchers.IO) {
                                    val result = DrawableUtils.execute(folderPath)
                                    if (result.isNotEmpty()) {
                                        loading = false
                                        successTips = result
                                    }
                                }

                            }
                        }) {
                        Text("开始处理")
                    }
                    Text(successTips)
                    // ScrollableLazyColumn(xmlFileList)
                    // ScrollableLazyColumn(xmlStringList)
                    SingleConfirmDialog(openAlertDialog)
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