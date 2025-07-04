package org.hoshi.reshelper.utils

import org.hoshi.reshelper.data.XmlString
import org.w3c.dom.Document
import java.io.File
import java.io.FileOutputStream
import java.io.PrintWriter
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import kotlin.collections.forEach

/**
 * 翻译工具诶
 */
object TranslateUtils {

    /**
     * 处理 xml，耗时方法，带一下 suspend
     *
     * @param originXmlPath 原 xml 的路径
     * @param targetXmlPath 目标 xml 的路径
     * @param originXmlPath 输出的 xml 的路径
     */
    suspend fun execute(
        originXmlPath: String,
        targetXmlPath: String,
        outputXmlFolder: String,
        outputXmlName: String?
    ): String {
        val outputXmlPath = "$outputXmlFolder/${if (outputXmlName.isNullOrEmpty()) "output_strings" else outputXmlName}.xml"
        println("导出路径为 $outputXmlPath")
        val originXmlFile = File(originXmlPath)
        val targetXmlFile = File(targetXmlPath)
        val outputXmlFile = File(outputXmlPath)

        outputXmlFile.delete()
        outputXmlFile.createNewFile()

        val originXmlStringList = getXmlStringList(originXmlFile)
        val targetXmlStringList = getXmlStringList(targetXmlFile)

        val originXmlStringNameList = originXmlStringList.map { it.name }
        val targetXmlStringMap = targetXmlStringList.associateBy { it.name }

        val outputList = mutableListOf<XmlString>()

        // 第一次遍历，把旧 xml 中的先加到列表里，同时把旧 xml 中有，且新 xml 中也有的，value 替换成 newValue 后，也放进列表里，
        originXmlStringList.forEach {
            val name = it.name
            if (targetXmlStringMap.keys.contains(name)) {
                val newValue = targetXmlStringMap[name]?.text
                if (newValue != null) {
                    val newXmlString = XmlString("", "", "", it.name, newValue)
                    outputList.add(newXmlString)
                } else {
                    outputList.add(it)
                }
            } else {
                outputList.add(it)
            }
        }

        // 第二次遍历，把新 xml 中有，而旧 xml 中没有的放进列表里
        targetXmlStringList.forEach {
            val name = it.name
            if (!originXmlStringNameList.contains(name)) {
                // println(it.name)
                outputList.add(it)
            }
        }

        // 输出 xml
        // 构造 Document
        val doc = getXmlDocumentByFilePath()
        outputList.forEach {
            val element = doc.createElement("string") // 创建 string 标签
            element.setAttribute("name", "" + it.name) // 添加 name 属性
            element.textContent = it.getText(true) // 填入内容
            doc.documentElement.appendChild(element) // 将节点放到 root 节点下面
        }
        // 将 Document 中的内容写入文件中
        val tf = TransformerFactory.newInstance()
        val transformer = tf.newTransformer()
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4")
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8")
        transformer.setOutputProperty(OutputKeys.INDENT, "yes") // 设置文档的换行与缩进
        val source = DOMSource(doc)

        FileOutputStream(outputXmlFile).use { fos ->
            PrintWriter(fos).use { pw ->
                val result = StreamResult(pw)
                transformer.transform(source, result)
            }
        }

        val result =
            "共识别出 ${originXmlStringList.size} 个原 xml 键值对，${targetXmlStringList.size} 个目标 xml 键值对，将要输出 ${outputList.size} 个 xml 键值对"
        println(result)
        return result
    }

    private fun getXmlStringList(file: File): List<XmlString> {
        val resultList = mutableListOf<XmlString>()
        val factory = DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder() // 获取解析对象
        val document = builder.parse(file) // 对象解析文件
        val rootElement = document.documentElement // 获取root节点
        val nodeList = rootElement.getElementsByTagName("string") // 获取父节点下面所有 string 元素节点
        for (i in 0 until nodeList.length) {
            val node = nodeList.item(i)
            val nodeAttributes = node.attributes
            val name = nodeAttributes.getNamedItem("name").nodeValue
            val xmlString = XmlString("", "", "", name, node.textContent)
            resultList.add(xmlString)
        }
        return resultList
    }


    private fun getXmlDocumentByFilePath(): Document {
        // 获取 doc 对象
        val factory = DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder()
        val doc = builder.newDocument()

        // doc 中添加生成 root 节点
        val root = doc.createElement("resources")
        doc.appendChild(root);

        return doc;
    }
}