package org.hoshi.reshelper.utils

import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileSystemView

object FileUtils {
    fun openDirectorySelector(): File? {
        val chooser = JFileChooser(FileSystemView.getFileSystemView()).apply {
            dialogTitle = "选择目标文件夹"
            fileSelectionMode = JFileChooser.DIRECTORIES_ONLY // 关键：只允许选文件夹
            isAcceptAllFileFilterUsed = false
        }

        return when (chooser.showOpenDialog(null)) {
            JFileChooser.APPROVE_OPTION -> chooser.selectedFile
            else -> null
        }
    }
}