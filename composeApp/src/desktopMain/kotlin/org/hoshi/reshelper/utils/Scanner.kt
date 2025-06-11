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
                // 无事发生
            }
        }
    }

    private fun isTargetFile(file: File): Boolean {
        val filePath = file.path
        val fileName = file.name
        val notBuildFile = !filePath.contains("build") // 不能包含 build，避免扫描到生成的临时文件
        val isResFile = filePath.contains("res") // 需要包含 res，其他都是无关的
        val endsWithXml = fileName.endsWith(".xml") // 需要.xml 结尾
        return notBuildFile && isResFile && endsWithXml
                && (fileName.startsWith("string") || fileName.startsWith("arrays")) // string 或者 arrays 文件
    }

}