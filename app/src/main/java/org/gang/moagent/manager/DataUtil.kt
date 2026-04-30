package org.gang.moagent.manager

import android.content.Context
import java.io.File
import java.io.FileOutputStream

object DataUtil {

    /**
     * 지정된 폴더 내에 파일을 생성하고 내용을 작성함 (폴더가 없으면 자동 생성)
     * @param subDir "agents/config" 처럼 경로 전달 가능
     */
    fun saveFile(context: Context, subDir: String, fileName: String, content: String): Boolean {
        return try {
            val directory = File(context.filesDir, subDir)
            if (!directory.exists()) {
                directory.mkdirs() // 하위 폴더까지 한 번에 생성
            }

            val file = File(directory, fileName)
            FileOutputStream(file).use { output ->
                output.write(content.toByteArray())
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    /**
     * 특정 경로(subDir) 안에 있는 모든 "하위 폴더"의 이름 목록을 가져옴
     */
    fun getSubDirectories(context: Context, subDir: String): List<String> {
        val root = File(context.filesDir, subDir)
        if (!root.exists() || !root.isDirectory) return emptyList()

        return root.listFiles()
            ?.filter { it.isDirectory } // 폴더만 필터링
            ?.map { it.name } ?: emptyList()
    }
    /**
     * 지정된 경로의 파일 내용을 읽어옴
     */
    fun readFile(context: Context, subDir: String, fileName: String): String? {
        return try {
            val file = File(File(context.filesDir, subDir), fileName)
            if (!file.exists()) return null
            file.readText(Charsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 특정 폴더 내의 모든 파일 이름 목록을 가져옴 (에이전트 목록 불러오기 등에 유용)
     */
    fun getFileList(context: Context, subDir: String): List<String> {
        val directory = File(context.filesDir, subDir)
        if (!directory.exists() || !directory.isDirectory) return emptyList()

        return directory.listFiles()
            ?.filter { it.isFile }
            ?.map { it.name } ?: emptyList()
    }

    /**
     * 파일 삭제
     */
    fun deleteFile(context: Context, subDir: String, fileName: String): Boolean {
        val file = File(File(context.filesDir, subDir), fileName)
        return if (file.exists()) file.delete() else false
    }

    /**
     * 폴더 전체 삭제 (하위 파일 포함)
     */
    fun deleteDirectory(context: Context, subDir: String): Boolean {
        val directory = File(context.filesDir, subDir)
        return directory.deleteRecursively()
    }

    /**
     * 파일 존재 여부 확인
     */
    fun isFileExists(context: Context, subDir: String, fileName: String): Boolean {
        return File(File(context.filesDir, subDir), fileName).exists()
    }
}