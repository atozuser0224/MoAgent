package org.gang.moagent.manager

import android.content.Context
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

object JsonFileManager {

    // JSON 형식 설정 (들여쓰기 포함, 알 수 없는 키 무시)
    val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    /**
     * 객체를 JSON 파일로 저장
     * @param data 저장할 데이터 클래스 객체
     * @param subDir "agents" 등 저장할 폴더
     * @param fileName "agent_01.json" 등 파일 이름
     */
    inline fun <reified T> saveJson(context: Context, subDir: String, fileName: String, data: T): Boolean {
        return try {
            val jsonString = json.encodeToString(data)
            DataUtil.saveFile(context, subDir, fileName, jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * JSON 파일을 읽어서 객체로 변환
     * @return 성공 시 객체 T, 실패 시 null
     */
    inline fun <reified T> loadJson(context: Context, subDir: String, fileName: String): T? {
        return try {
            val jsonString = DataUtil.readFile(context, subDir, fileName)
            if (jsonString.isNullOrEmpty()) return null
            json.decodeFromString<T>(jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}