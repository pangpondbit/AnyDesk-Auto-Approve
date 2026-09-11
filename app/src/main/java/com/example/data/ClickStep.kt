package com.example.data

import org.json.JSONArray
import org.json.JSONObject

data class ClickStep(
    val id: String = java.util.UUID.randomUUID().toString(),
    val keywords: List<String> = emptyList()
) {
    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("id", id)
        val array = JSONArray()
        keywords.forEach { array.put(it) }
        json.put("keywords", array)
        return json
    }

    companion object {
        fun fromJson(json: JSONObject): ClickStep {
            val id = json.optString("id", java.util.UUID.randomUUID().toString())
            val array = json.optJSONArray("keywords") ?: JSONArray()
            val list = mutableListOf<String>()
            for (i in 0 until array.length()) {
                list.add(array.getString(i))
            }
            return ClickStep(id, list)
        }

        fun listToJsonString(steps: List<ClickStep>): String {
            val array = JSONArray()
            steps.forEach { array.put(it.toJson()) }
            return array.toString()
        }

        fun getDefaultSteps(): List<ClickStep> {
            return listOf(
                ClickStep(
                    keywords = listOf(
                        "ทั้งหน้าจอ",
                        "ทั้งหน้า จอ",
                        "entire screen",
                        "แชร์ทั้งหน้าจอ"
                    )
                ),
                ClickStep(
                    keywords = listOf(
                        "เริ่มเลย",
                        "เริ่ม เลย",
                        "start now",
                        "เริ่มทันที",
                        "แชร์หน้าจอ",
                        "แชร์หน้า จอ",
                        "แชร์",
                        "share screen",
                        "อนุญาต",
                        "allow",
                        "accept",
                        "ยอมรับ"
                    )
                )
            )
        }

        fun listFromJsonString(jsonString: String?): List<ClickStep> {
            if (jsonString.isNullOrEmpty()) return getDefaultSteps()
            return try {
                val array = JSONArray(jsonString)
                val list = mutableListOf<ClickStep>()
                for (i in 0 until array.length()) {
                    list.add(fromJson(array.getJSONObject(i)))
                }
                if (list.isEmpty()) getDefaultSteps() else list
            } catch (e: Exception) {
                getDefaultSteps()
            }
        }
    }
}
