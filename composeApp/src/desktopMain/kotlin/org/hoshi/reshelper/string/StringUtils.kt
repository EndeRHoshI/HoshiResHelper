package org.hoshi.reshelper.string

import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory

/**
 * 字符串处理工具
 */
object StringUtils {

    /**
     * 处理 xml 文件，耗时方法，带一下 suspend
     * @param folderPath 项目根目录或者单独某个 res 文件夹的路径
     * @param outputPath 导出为 Excel 的路径
     * @param outputFileName 导出的 Excel 的名称，不输入则默认用当前时间格式化作为名称
     */
    suspend fun execute(folderPath: String, outputPath: String, outputFileName: String? = null): String {
        val allStringFileList = mutableListOf<String>() // 在外部创建一个列表，遍历时把找到的 string.xml 文件路径放进去
        findAllStringFiles(folderPath, allStringFileList)

        println("共找到 " + allStringFileList.size + " 个 string.xml/arrays.xml 文件")

        // 去掉前方统一的父目录，然后截掉 res 后一致的部分，得到不一致的部分来进行一下分组
        // 组数对应生成的 Excel 文件的工作表数，组名对应 Excel 工作表名
        val resPathMap = allStringFileList.groupBy { it.substringAfter(folderPath).substringBefore("/res") }
        println("共有 " + resPathMap.size + " 组")

        val excelBook = XSSFWorkbook()
        resPathMap.forEach {
            val folderName = it.key // 取得 folderName，就是组名或者说工作表名
            val resPathList = it.value
            // println(folderName)

            // 创建工作表
            val sheet = excelBook.createSheet(
                folderName
                    .replaceFirst("/", "") // 去除掉第一个斜杠
                    .replace("/", "_") // 斜杠需要替换一下，工作表名不能用斜杠
            )
            var rowIndex = 0 // 行
            var cellIndex = 0 // 列
            val lanCellMap = mutableMapOf<String, Int>() // 语言和列下标的映射

            var row = sheet.createRow(rowIndex++)
            lanCellMap["fileName"] = cellIndex // 存储 fileName 列和列下标的映射
            row.createCell(cellIndex++).setCellValue("fileName")

            lanCellMap["name"] = cellIndex // 存储 name 列和列下标的映射
            row.createCell(cellIndex++).setCellValue("name")

            val allXmlStringList = resPathList.flatMap { xmlPath -> readStringFromXml(xmlPath, folderName) }
            val valueFolderMap = allXmlStringList.groupBy { xmlString -> xmlString.valueFolderName }

            valueFolderMap.forEach { mapEntry ->
                val valueFolderName = mapEntry.key
                if (lanCellMap.keys.contains(valueFolderName)) {
                    return@forEach // 如果已经有这一列了，跳过
                }
                lanCellMap[valueFolderName] = cellIndex // 存储语言和列的映射
                row.createCell(cellIndex++).setCellValue(valueFolderName)
            }

            val baseCellName = "values"
            if (valueFolderMap.containsKey(baseCellName)) {
                // 如果有基准列才继续处理，否则是不合法的，不用管了
                val baseList = valueFolderMap[baseCellName] // 取得基准列的各个项，后面用来给其它列找下标
                if (baseList != null) {
                    valueFolderMap.forEach { mapEntry ->
                        val valueFolderName = mapEntry.key
                        val xmlStringList = mapEntry.value
                        rowIndex = 1 // 将行数重置回 1
                        xmlStringList.forEach { xmlString ->
                            // 如果是基准列，需要从头到尾进行填入内容
                            if (valueFolderName != baseCellName) {
                                // 如果不是基准列，需要找到对应的行来填入内容
                                rowIndex =
                                    baseList.indexOfFirst { baseXmlString -> xmlString.name == baseXmlString.name } + 1
                            }
                            if (rowIndex < 0) {
                                return@forEach
                            }
                            row = sheet.getRow(rowIndex)
                            if (row == null) {
                                row = sheet.createRow(rowIndex)
                            }
                            if (valueFolderName == baseCellName) {
                                rowIndex++
                            }
                            row.createCell(0).setCellValue(xmlString.fileName)
                            row.createCell(1).setCellValue(xmlString.name)

                            val textCellIndex = lanCellMap[valueFolderName]
                            if (textCellIndex != null) {
                                row.createCell(textCellIndex).setCellValue(xmlString.text)
                            }
                        }
                    }
                }
            }
        }

        val sdf = SimpleDateFormat("yyyyMMddHHmmss")
        val excelName = if (outputFileName.isNullOrEmpty()) {
            sdf.format(Date())
        } else {
            outputFileName
        } + ".xlsx"
        val excelPath = "$outputPath/$excelName"
        val excelFile = File(excelPath)
        if (excelFile.exists()) {
            excelFile.delete()
        } else {
            if (!excelFile.parentFile.exists()) {
                excelFile.parentFile.mkdirs()
            }
            File(excelPath).createNewFile()
        }
        FileOutputStream(excelFile).use { excelBook.write(it) }
        return excelPath
    }

    /**
     * 找到所有 string.xml 文件
     */
    private fun findAllStringFiles(projectPath: String, allStringFileList: MutableList<String>): MutableList<String> {
        File(projectPath).listFiles()?.forEach {
            if (it.isDirectory) {
                // 如果是一个文件夹，再往下找
                findAllStringFiles(it.absolutePath, allStringFileList)
            } else {
                // 文件路径规则：在 values 目录下，包括 values-xxx 目录
                if (isTargetFile(it)) {
                    // 文件名和文件路径匹配成功，添加到列表中
                    println(it.absolutePath)
                    allStringFileList.add(it.absolutePath)
                }
            }
        }
        return allStringFileList
    }

    private fun readStringFromXml(xmlFilePath: String?, folderName: String): List<XmlString> {
        if (xmlFilePath.isNullOrEmpty()) {
            println("目标 xml 路径为空，请检查")
            return listOf()
        }
        val xmlFile = File(xmlFilePath)
        return readStringFromXml(xmlFile, folderName)
    }

    private fun readStringFromXml(xmlFile: File?, folderName: String): List<XmlString> {
        val resultList = mutableListOf<XmlString>()
        if (xmlFile == null || !xmlFile.exists()) {
            println("目标 xml 文件为空或不存在，请检查")
            return resultList.toList()
        }

        // 取得 value 文件夹名，即 res 的父目录，形如 values、value-zh-rCN 这种
        val xmlFileParent = xmlFile.parent
        val valueFolderName = xmlFileParent.substring(xmlFileParent.lastIndexOf("/") + 1, xmlFileParent.length)

        val fileName = xmlFile.name
        val factory = DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder() // 获取解析对象
        val document = builder.parse(xmlFile) // 对象解析文件
        val rootElement = document.documentElement // 获取root节点
        val nodeList = rootElement.getElementsByTagName("string") // 获取父节点下面所有 string 元素节点
        for (i in 0 until nodeList.length) {
            val node = nodeList.item(i)
            val nodeAttributes = node.attributes
            val name = nodeAttributes.getNamedItem("name").nodeValue
            val translatable =
                nodeAttributes.getNamedItem("translatable")?.textContent?.toBoolean() ?: true // 如果取不到，直接赋值为 true
            val xmlString = XmlString(folderName, fileName, valueFolderName, name, node.textContent)
            if (translatable) { // 需要翻译的才加进来
                resultList.add(xmlString)
            }
        }
        return resultList.toList()
    }

    private fun isTargetFile(file: File): Boolean {
        val filePath = file.path
        val fileName = file.name
        return !filePath.contains("build") // 不能包含 build，避免扫描到生成的临时文件
                && filePath.contains("res") // 需要包含 res，其他都是无关的，用于减少扫描时间
                && fileName.endsWith(".xml") // 需要.xml 结尾
                && (fileName.startsWith("string") || fileName.startsWith("arrays")) // string 或者 arrays 文件
    }

}