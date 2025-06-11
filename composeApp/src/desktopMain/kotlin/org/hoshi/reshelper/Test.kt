import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class TranslationResponse(val data: TranslationData)

@Serializable
data class TranslationData(val translations: List<TranslatedText>)

@Serializable
data class TranslatedText(val translatedText: String)

/*fun main() {
    // 替换为你的Google Cloud API密钥
    val apiKey = "YOUR_API_KEY_HERE"

    // 要翻译的文本列表
    val textsToTranslate = listOf("Hello, world!", "How are you?")

    // 目标语言代码 (zh-CN: 简体中文, en: 英语)
    val targetLanguage = "zh-CN"

    runBlocking {
        try {
            val results = translateTexts(apiKey, textsToTranslate, targetLanguage)
            println("翻译结果:")
            results.forEachIndexed { index, translation ->
                println("${textsToTranslate[index]} -> $translation")
            }
        } catch (e: Exception) {
            println("翻译失败: ${e.message}")
        }
    }
}*/

suspend fun translateTexts(
    apiKey: String,
    texts: List<String>,
    targetLang: String
): List<String> {
    // 创建HTTP客户端
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    return try {
        // 发送翻译请求
        /*val response: TranslationResponse = client.post("https://translation.googleapis.com/language/translate/v2") {
            parameter("key", apiKey)
            parameter("target", targetLang)
            parameter("format", "text") // 确保返回纯文本

            // 添加要翻译的文本（支持批量）
            setBody(
                texts.joinToString("") { "&q=${it.encodeURLParameter()}" }
            )

            contentType(ContentType.Application.FormUrlEncoded)
        }.body()*/

        val response: TranslationResponse = client.post("https://translate.googleapis.com/ltranslate_a/single") {
            parameter("client", "gtx")
            parameter("dt", "t")
            parameter("sl", "en")
            parameter("tl", "zh-CN")
            parameter("q", "test")

            // 添加要翻译的文本（支持批量）
            setBody(
                texts.joinToString("") { "&q=${it.encodeURLParameter()}" }
            )

            contentType(ContentType.Application.FormUrlEncoded)
        }.body()

        // 提取翻译结果
        response.data.translations.map { it.translatedText }
    } finally {
        client.close() // 关闭客户端
    }
}

// URL编码辅助函数
fun String.encodeURLParameter(): String {
    return this.replace(Regex("""[^a-zA-Z0-9\-_.~]""")) {
        it.value.toByteArray(Charsets.UTF_8).joinToString("") {
            "%${it.toString(16).padStart(2, '0').uppercase()}"
        }
    }
}