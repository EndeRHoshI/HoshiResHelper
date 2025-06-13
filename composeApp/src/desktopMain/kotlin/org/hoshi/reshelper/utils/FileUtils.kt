package org.hoshi.reshelper.utils

import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileSystemView

object FileUtils {

    fun openDirectorySelector(mode: Int): File? {
        val chooser = JFileChooser(FileSystemView.getFileSystemView()).apply {
            dialogTitle = "选择目标" + when(mode) {
                JFileChooser.DIRECTORIES_ONLY -> "文件夹"
                JFileChooser.FILES_ONLY  -> "文件"
                JFileChooser.FILES_AND_DIRECTORIES  -> "文件夹或文件"
                else -> "未知"
            }
            fileSelectionMode = mode // 关键：只允许选文件夹
            isAcceptAllFileFilterUsed = false
        }

        return when (chooser.showOpenDialog(null)) {
            JFileChooser.APPROVE_OPTION -> chooser.selectedFile
            else -> null
        }
    }
}