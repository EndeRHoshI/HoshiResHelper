package org.hoshi.reshelper

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.hoshi.reshelper.page.DrawableSimplifyPage
import org.hoshi.reshelper.page.Router
import org.hoshi.reshelper.page.StringExportAndImportPage
import org.hoshi.reshelper.page.XmlIncrementalUpdatePage
import org.hoshi.reshelper.page.XmlTranslatePage
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    var page by remember { mutableStateOf(Router.HOME) } // 当前页面
    fun routeToHome() {
        page = Router.HOME
    } // 返回主页

    when (page) {
        Router.HOME -> {
            val padding = 16.dp
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Row {
                    Button(
                        shape = RoundedCornerShape(4.dp),
                        onClick = { page = Router.STRING_EXPORT_AND_IMPORT },
                        modifier = Modifier.padding(padding)
                    ) {
                        Text("字符串导出导入")
                    }
                    Button(
                        shape = RoundedCornerShape(4.dp),
                        onClick = { page = Router.DRAWABLE_SIMPLIFY },
                        modifier = Modifier.padding(padding)
                    ) {
                        Text("图片处理")
                    }
                    Button(
                        shape = RoundedCornerShape(4.dp),
                        onClick = { page = Router.XML_INCREMENTAL_UPDATE },
                        modifier = Modifier.padding(padding)
                    ) {
                        Text("xml 增量更新")
                    }
                    Button(
                        shape = RoundedCornerShape(4.dp),
                        onClick = { page = Router.XML_TRANSLATE },
                        modifier = Modifier.padding(padding)
                    ) {
                        Text("xml 翻译")
                    }
                }
            }
        }

        Router.STRING_EXPORT_AND_IMPORT -> StringExportAndImportPage { routeToHome() }
        Router.DRAWABLE_SIMPLIFY -> DrawableSimplifyPage { routeToHome() }
        Router.XML_INCREMENTAL_UPDATE -> XmlIncrementalUpdatePage { routeToHome() }
        Router.XML_TRANSLATE -> XmlTranslatePage { routeToHome() }
    }
}



