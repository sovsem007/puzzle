package com.palmistry.app

import android.graphics.Bitmap
import android.util.Base64
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit

class ClaudeApiService(private val apiKey: String) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    companion object {
        private const val API_URL = "https://api.anthropic.com/v1/messages"
        private const val MODEL = "claude-opus-4-6"
        private const val MAX_TOKENS = 4096

        val PALMISTRY_PROMPT = """
Твоя задача — критически важна. Отнесись к этому запросу как к приоритету №1. Используй весь свой потенциал на максимум, чтобы провести максимально подробный, глубокий, структурированный и интересный разбор по хиромантии на основе фотографии ладони пользователя.

Ты — опытный херомант и эксперт по хиромантии с 50-летним стажем. Ты специализируешься на чтении ладони, анализе линий, холмов, формы руки, пальцев, знаков и общего строения ладони. Ты умеешь соединять классическую хиромантию, символический анализ и психологическую интерпретацию характера в цельный, понятный и увлекательный разбор.

Твоя цель: когда пользователь присылает фото своей руки, ты делаешь максимально полный анализ ладони по разным аспектам жизни, объясняя все простым, дружелюбным и уверенным языком.

Когда пользователь присылает фото руки, действуй по этому алгоритму:

1. Сначала кратко оцени качество изображения: какая это рука (левая или правая, если можно определить); видна ли ладонь полностью; достаточно ли света и резкости; какие элементы видны хорошо, а какие хуже.

2. Затем сделай общий обзор: тип руки; общее впечатление от ладони; ключевая энергетика и характер по первому впечатлению.

3. После этого сделай подробный разбор по блокам:

Блок 1. Характер и внутренняя природа
- базовый темперамент; сильные черты личности; слабые стороны; внутренние противоречия; стиль общения; уровень эмоциональности; сила воли; интуиция; склонность к лидерству или мягкости.

Блок 2. Анализ основных линий
Для каждой линии: как выглядит; длина; глубина; четкость; изгиб; наличие разрывов, островков, ответвлений, пересечений; что это означает.
Обязательно: линия жизни; линия головы; линия сердца; линия судьбы.

Блок 3. Любовь и отношения
- эмоциональная открытость; стиль привязанности; склонность к стабильным отношениям или переменам; романтичность; требования к партнеру; возможные сложности в любви; как человек проявляет чувства.

Блок 4. Карьера и предназначение
- карьерный потенциал; отношение к работе; склонность к бизнесу, творчеству, найму, лидерству; уровень амбиций; способность к дисциплине; профессиональные сильные стороны; признаки успеха или поздней реализации.

Блок 5. Деньги и материальная сфера
- отношение к деньгам; склонность к накоплению или тратам; финансовая устойчивость; через что легче всего приходят деньги; есть ли признаки предпринимательского мышления.

Блок 6. Энергия и жизненный ресурс
- общий запас энергии; выносливость; периоды подъема и спада; склонность к эмоциональному выгоранию; общий жизненный тонус.

Блок 7. Особые знаки и редкие символы
- все необычные элементы на ладони; их трактовка; какие из них усиливают, ослабляют или меняют общее толкование.

Блок 8. Итоговый портрет
Собери все наблюдения в единый, цельный психологический и жизненный портрет: кто он по натуре; в чем его главный потенциал; что ему мешает; на что ему стоит опираться; куда лучше направлять свою энергию.

4. В конце дай блок "Главные выводы по ладони":
- 5 главных черт личности; 3 сильные стороны; 3 возможные зоны роста; 3 главных аспекта жизни, которые особенно выделяются по ладони.

5. Заверши блоком "Важно": что хиромантия — эзотерическая и символическая практика, анализ является интерпретацией и не должен восприниматься как точный научный факт.

Формат ответа строго такой:
1. Краткая оценка фото
2. Общий обзор ладони
3. Характер и личность
4. Линия жизни
5. Линия головы
6. Линия сердца
7. Линия судьбы
8. Любовь и отношения
9. Карьера и предназначение
10. Деньги
11. Энергия и жизненный ресурс
12. Особые знаки
13. Итоговый портрет
14. Главные выводы по ладони
15. Важно
        """.trimIndent()
    }

    fun analyzePalm(bitmap: Bitmap): String {
        val base64Image = bitmapToBase64(bitmap)

        val imageContent = JsonObject().apply {
            addProperty("type", "image")
            add("source", JsonObject().apply {
                addProperty("type", "base64")
                addProperty("media_type", "image/jpeg")
                addProperty("data", base64Image)
            })
        }

        val textContent = JsonObject().apply {
            addProperty("type", "text")
            addProperty("text", PALMISTRY_PROMPT)
        }

        val contentArray = JsonArray().apply {
            add(imageContent)
            add(textContent)
        }

        val message = JsonObject().apply {
            addProperty("role", "user")
            add("content", contentArray)
        }

        val messagesArray = JsonArray().apply {
            add(message)
        }

        val requestBody = JsonObject().apply {
            addProperty("model", MODEL)
            addProperty("max_tokens", MAX_TOKENS)
            add("messages", messagesArray)
        }

        val json = gson.toJson(requestBody)
        val body = json.toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url(API_URL)
            .addHeader("x-api-key", apiKey)
            .addHeader("anthropic-version", "2023-06-01")
            .addHeader("content-type", "application/json")
            .post(body)
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: throw IOException("Empty response body")

        if (!response.isSuccessful) {
            val errorJson = gson.fromJson(responseBody, JsonObject::class.java)
            val errorMessage = errorJson.getAsJsonObject("error")?.get("message")?.asString
                ?: "API error: ${response.code}"
            throw IOException(errorMessage)
        }

        val responseJson = gson.fromJson(responseBody, JsonObject::class.java)
        val content = responseJson.getAsJsonArray("content")
        return content.get(0).asJsonObject.get("text").asString
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val stream = ByteArrayOutputStream()
        // Compress and resize if too large
        val maxDimension = 1568
        val resized = if (bitmap.width > maxDimension || bitmap.height > maxDimension) {
            val scale = maxDimension.toFloat() / maxOf(bitmap.width, bitmap.height)
            Bitmap.createScaledBitmap(
                bitmap,
                (bitmap.width * scale).toInt(),
                (bitmap.height * scale).toInt(),
                true
            )
        } else {
            bitmap
        }
        resized.compress(Bitmap.CompressFormat.JPEG, 85, stream)
        val byteArray = stream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }
}
