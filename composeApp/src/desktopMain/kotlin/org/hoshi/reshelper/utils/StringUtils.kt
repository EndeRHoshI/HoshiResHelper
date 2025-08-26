package org.hoshi.reshelper.utils

import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.hoshi.reshelper.data.XmlString
import java.io.File
import java.io.FileOutputStream
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

/**
 * 字符串处理工具
 */
object StringUtils {

    /**
     * 处理 xml 文件，耗时方法，带一下 suspend
     *
     * @param folderPath 项目根目录或者单独某个 res 文件夹的路径
     * @param outputFolder 导出为 Excel 的路径
     * @param outputFileName 导出的 Excel 的名称，不输入则默认用当前时间格式化作为名称
     * @return Pair，first -> 是否成功，second -> 附带信息
     */
    suspend fun xml2Xlsx(
        folderPath: String,
        outputFolder: String,
        outputFileName: String? = null
    ): Pair<Boolean, String> {
        if (folderPath.isEmpty()) {
            return Pair(false, "项目或 res 目录为空，请选择后再继续")
        }
        if (outputFolder.isEmpty()) {
            return Pair(false, "输出目录为空，请选择后再继续")
        }
        LogMgr.clear() // 首先清空下日志管理器里面的 sb
        val allStringFileList = mutableListOf<String>() // 在外部创建一个列表，遍历时把找到的 string.xml 文件路径放进去
        findAllStringFiles(folderPath, allStringFileList)

        LogMgr.printlnLog("共找到 " + allStringFileList.size + " 个 string.xml/arrays.xml 文件", true)

        // 去掉前方统一的父目录，然后截掉 res 后一致的部分，得到不一致的部分来进行一下分组
        // 组数对应生成的 Excel 文件的工作表数，组名对应 Excel 工作表名
        val resPathMap = allStringFileList.groupBy { it.substringAfter(folderPath).substringBefore("/res") }
        LogMgr.printlnLog("共有 " + resPathMap.size + " 组", true)

        val excelBook = XSSFWorkbook()
        resPathMap.forEach {
            val folderName = it.key // 取得 folderName，就是组名或者说工作表名
            val resPathList = it.value
            LogMgr.printlnLog(folderName)

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
            lanCellMap["fileName"] = 0 // 存储 fileName 列和列下标的映射
            LogMgr.printlnLog("在第 $rowIndex 行 0 列写入 fileName")
            row.createCell(cellIndex++).setCellValue("fileName")

            lanCellMap["name"] = cellIndex // 存储 name 列和列下标的映射
            LogMgr.printlnLog("在第 $rowIndex 行 $cellIndex 列写入 name")
            row.createCell(cellIndex++).setCellValue("name")

            val allXmlStringList = resPathList.flatMap { xmlPath -> readStringFromXml(xmlPath, folderName) }
            val valueFolderMap = allXmlStringList.groupBy { xmlString -> xmlString.valueFolderName }

            valueFolderMap.forEach { mapEntry ->
                val valueFolderName = mapEntry.key
                if (lanCellMap.keys.contains(valueFolderName)) {
                    return@forEach // 如果已经有这一列了，跳过
                }
                lanCellMap[valueFolderName] = cellIndex // 存储语言和列的映射
                LogMgr.printlnLog("在第 $rowIndex 行 $cellIndex 列写入 $valueFolderName")
                row.createCell(cellIndex++).setCellValue(valueFolderName)
            }

            val baseCellName = "values"
            if (valueFolderMap.containsKey(baseCellName)) {
                // 如果有 values 文件夹才继续处理，否则是不合法的多语言翻译，不用管了
                val baseList = valueFolderMap[baseCellName] // 取得基准列的各个项，后面用来给其它列找下标
                if (baseList != null) {
                    val baseNameList =
                        baseList.map { xmlString -> xmlString.name } // 基准列里面的 name 列表，用来过滤掉基准列里面不存在但是在其他列存在的字符串
                    valueFolderMap.forEach { mapEntry ->
                        val valueFolderName = mapEntry.key // value 文件夹名，形如 values-es-rES、values-zh-rTW
                        val xmlStringList = mapEntry.value // 对应的 Xml 字符串列表
                        rowIndex = 1 // 将行数重置回 1
                        xmlStringList.forEach { xmlString ->
                            if (baseNameList.contains(xmlString.name)) {
                                // 如果有基准列里面有才继续处理，否则是不需要翻译，或者是基准列里面不存在但是在其他列存在的字符串，不需要的
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
                                val fileName = xmlString.fileName
                                val name = xmlString.name
                                LogMgr.printlnLog("在第 $rowIndex 行 0 列写入 $fileName")
                                row.createCell(0).setCellValue(fileName)
                                LogMgr.printlnLog("在第 $rowIndex 行 1 列写入 $name")
                                row.createCell(1).setCellValue(name)

                                val textCellIndex = lanCellMap[valueFolderName]
                                if (textCellIndex != null) {
                                    val text = xmlString.text
                                    LogMgr.printlnLog("在第 $rowIndex 行 $textCellIndex 列写入 $text")
                                    row.createCell(textCellIndex).setCellValue(text)
                                }
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
        val excelPath = "$outputFolder/$excelName"
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

        // 输出一下日志
        val logName = excelName.replace(".xlsx", "_log.txt")
        LogMgr.writeTxt("$outputFolder/$logName")

        return Pair(true, excelPath)
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
                    val absolutePath = it.absolutePath
                    LogMgr.printlnLog("匹配成功，添加 $absolutePath")
                    allStringFileList.add(absolutePath)
                }
            }
        }
        return allStringFileList
    }

    private fun readStringFromXml(xmlFilePath: String?, folderName: String): List<XmlString> {
        if (xmlFilePath.isNullOrEmpty()) {
            LogMgr.printlnLog("目标 xml 路径为空，请检查", true)
            return listOf()
        }
        val xmlFile = File(xmlFilePath)
        return readStringFromXml(xmlFile, folderName)
    }

    private fun readStringFromXml(xmlFile: File?, folderName: String): List<XmlString> {
        val resultList = mutableListOf<XmlString>()
        if (xmlFile == null || !xmlFile.exists()) {
            LogMgr.printlnLog("目标 xml 文件为空或不存在，请检查", true)
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

    /**
     * xlsx 转 xml
     * @return Pair，first -> 是否成功，second -> 附带信息
     */
    suspend fun xlsx2Xml(xlsxPath: String, outputFolder: String): Pair<Boolean, String> {
        if (xlsxPath.isEmpty()) {
            return Pair(false, "xlsx 文件路径为空，请选择后再继续")
        }
        if (!xlsxPath.endsWith(".xlsx")) {
            return Pair(false, "未选择正确的 xlsx 文件，请重新选择")
        }
        val xlsxFile = File(xlsxPath)
        if (!xlsxFile.exists()) {
            return Pair(false, "传入的 xlsx 文件不存在，请重新选择")
        }
        if (outputFolder.isEmpty()) {
            return Pair(false, "输出目录为空，请选择后再继续")
        }

        // 创建一个总的输出文件夹
        val sdf = SimpleDateFormat("yyyyMMddHHmmss")
        val resultFolder = "$outputFolder/Output_${sdf.format(Date())}"
        File(resultFolder).mkdirs()

        LogMgr.clear() // 首先清空下日志管理器里面的 sb
        val allXmlStringList = mutableListOf<XmlString>()

        val workbook = XSSFWorkbook(File(xlsxPath))
        workbook.sheetIterator().forEach { sheet -> // 读取每个页签内的内容，根据页签来创建不同文件夹，表示不同的项目、模块
            val folderName = sheet.sheetName // 取得文件夹名（页签名、工作表名）
            val cellDesList = mutableListOf<String>() // 列描述列表
            var rowIndex = 0 // 行
            sheet.rowIterator().forEach { row ->
                var cellIndex = 0 // 列
                var fileName = ""
                var name = ""
                var text = ""
                row.cellIterator().forEach { cell ->
                    if (rowIndex == 0) {
                        // 第 0 行是列的描述，将符合 fileName、name、和 values、values-xxx 之类的项读入列描述列表中
                        val stringCellValue = cell.stringCellValue
                        cellDesList.add(stringCellValue)
                    } else {
                        // 其他行是实际的字符串数据
                        // 通过列号取得对应的列描述，也就是 values、value-zh-rCN 这种
                        val valueFolderName = cellDesList.getOrNull(cellIndex).orEmpty()
                        when (cellIndex) {
                            0 -> fileName = cell.stringCellValue // 第 0 列，是 fileName 列，取得文件名
                            1 -> name = cell.stringCellValue // 第 1 列，是 name 列，取得 key
                            else -> {
                                // 其他列，是不同语种下的 value
                                text = cell.stringCellValue
                                // 创建对应的 XmlString 对象并加入到列表中
                                val xmlString = XmlString(folderName, fileName, valueFolderName, name, text)
                                allXmlStringList.add(xmlString)
                                LogMgr.printlnLog("读取 $rowIndex 行 $cellIndex 列加入列表中，$xmlString")
                            }
                        }
                    }
                    cellIndex++
                }
                rowIndex++
            }
        }
        LogMgr.printlnLog("共有 ${allXmlStringList.size} 个数据")

        val groupByFolderNameMap = allXmlStringList.groupBy { it.folderName }
        groupByFolderNameMap.forEach { folderNameEntry ->
            val folderName = folderNameEntry.key
            val xmlStringList = folderNameEntry.value
            val sheetFolderPath = "$resultFolder/$folderName"
            File(sheetFolderPath).mkdirs() // 创建对应的页签文件夹

            val groupByValueFolderNameMap = xmlStringList.groupBy { it.valueFolderName }
            groupByValueFolderNameMap.forEach { valueFolderNameEntry ->
                val valueFolderName = valueFolderNameEntry.key
                val xmlStringList = valueFolderNameEntry.value
                val valueFolderPath = "$sheetFolderPath/$valueFolderName"
                File(valueFolderPath).mkdirs() // 创建对应的语种文件夹

                val groupByFileNameMap = xmlStringList.groupBy { it.fileName }
                groupByFileNameMap.forEach { fileNameEntry ->
                    val fileName = fileNameEntry.key
                    val xmlStringList = fileNameEntry.value
                    val xmlPath = "$valueFolderPath/$fileName"
                    val xmlFile = File(xmlPath)
                    xmlFile.createNewFile() // 创建 xml 文件

                    // 获取 doc 对象
                    val factory = DocumentBuilderFactory.newInstance()
                    val builder = factory.newDocumentBuilder()
                    val doc = builder.newDocument();

                    // doc 中添加生成 root 节点
                    val root = doc.createElement("resources")
                    doc.appendChild(root)

                    xmlStringList.forEach {
                        val element = doc.createElement("string")
                        element.setAttribute("name", it.name) // 添加 name 属性，即 key
                        element.textContent = it.getText(true) // 填充 value 内容
                        doc.documentElement.appendChild(element) // 将节点放到 root 节点下面
                    }
                    val source = DOMSource(doc)

                    val transformer = TransformerFactory.newInstance().newTransformer().apply {
                        setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4") // 未知，后续查下是干嘛的
                        setOutputProperty(OutputKeys.ENCODING, "UTF-8") // 编码
                        setOutputProperty(OutputKeys.INDENT, "yes") // 设置文档的换行与缩进
                    }

                    FileOutputStream(xmlFile).use { fos ->
                        PrintWriter(fos).use { pw ->
                            val result = StreamResult(pw)
                            transformer.transform(source, result)
                        }
                    }
                }
            }
        }

        val logPath = "$resultFolder/log.txt"
        LogMgr.writeTxt(logPath)

        return Pair(true, resultFolder)
    }

}