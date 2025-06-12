package org.hoshi.reshelper.string

/**
 * Xml 文件中，每一个字符串键值对对应的实体类
 */
data class XmlString(
    val folderName: String, // 字符串所属的组名，也就是 Excel 的工作表名
    val fileName: String, // 字符串所在的文件名，也就是形如 string.xml、string_test.xml
    val valueFolderName: String, // value 文件夹名，即 res 的父目录，形如 values、value-zh-rCN 这种
    val name: String, // 字符串的 key
    val text: String, // 字符串的 value
) {

    fun getText(isWrite2AndroidXml: Boolean): String {
        if (!isWrite2AndroidXml) {
            return text
        }
        // 1.replace 将"'" 直接换成\'
        // 1.replace 将\' 直接换成'
        // 1.replace 将' 直接换成\'
        var s = text + ""
        s = s.replace("\"'\"", "'")
        s = s.replace("\\\'", "'")
        s = s.replace("\\'", "'")
        s = s.replace("\'", "'")
        s = s.replace("'", "\\'")
        return s
    }

}