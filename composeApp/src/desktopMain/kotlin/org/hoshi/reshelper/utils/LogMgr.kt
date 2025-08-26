package org.hoshi.reshelper.utils

import java.io.File
import java.io.FileOutputStream

/**
 * 全局唯一的 Log 管理器
 */
object LogMgr {

    val sb = StringBuilder()

    fun clear() {
        sb.clear()
    }

    fun printlnLog(content: String, needPrintln: Boolean = true) {
        if (needPrintln) {
            println(content)
        }
        sb.append(content).append("\n")
    }

    fun getStr(): String {
        val str = sb.toString() // 取出字符串
        clear() // 清理一下当前的 sb
        return str
    }

    fun writeTxt(txtPath: String) {
        FileOutputStream(File(txtPath)).use { it.write(getStr().toByteArray()) }
    }

}