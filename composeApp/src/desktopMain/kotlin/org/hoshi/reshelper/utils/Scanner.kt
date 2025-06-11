package org.hoshi.reshelper.utils

import java.io.File

object Scanner {

    /**
     * 扫描
     * @param folderPath 文件夹目录
     */
    fun scan(folderPath: String, xmlFileList: MutableList<String>) {
        val file = File(folderPath)
        if (file.isDirectory) {
            file.listFiles().forEach {
                scan(it.path, xmlFileList)
            }
        } else {
            if (isTargetFile(file)) {
                // 是目标文件，添加
                xmlFileList.add(file.path)
            } else {
                // 不是目标文件，不进行处理
            }
        }
    }

    private fun isTargetFile(file: File): Boolean {
        val filePath = file.path
        val fileName = file.name
        return !filePath.contains("build") // 不能包含 build，避免扫描到生成的临时文件
                && filePath.contains("res") // 需要包含 res，其他都是无关的
                && fileName.endsWith(".xml") // 需要.xml 结尾
                && !filePath.contains("custom") // 不包含 custom 文件夹
                && (fileName.startsWith("string") || fileName.startsWith("arrays")) // string 或者 arrays 文件
    }

}