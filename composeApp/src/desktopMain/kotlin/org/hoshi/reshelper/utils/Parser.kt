package org.hoshi.reshelper.utils

import org.hoshi.reshelper.data.XmlString
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

// StringXmlParser.kt
object Parser {

    fun parseStringsXml(filePathList: List<String>): List<XmlString> {
        val resultList = mutableListOf<XmlString>()
        filePathList.forEach { filePath ->
            val map = parseStringsXml(File(filePath))
            map.forEach {
                val key = it.key
                val value = it.value
                val xmlString = XmlString(key, value, filePath)
                resultList.add(xmlString)
            }
        }
        return resultList
    }

    private fun parseStringsXml(file: File): Map<String, String> {
        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)
        val entries = doc.getElementsByTagName("string")
        return (0 until entries.length).associate {
            val node = entries.item(it)
            node.attributes.getNamedItem("name").nodeValue to node.textContent
        }
    }

}