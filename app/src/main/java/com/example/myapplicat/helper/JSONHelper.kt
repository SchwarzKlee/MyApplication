package com.example.myapplicat.helper

import android.content.Context
import android.util.Log
import com.example.myapplicat.objects.Product
import com.google.gson.Gson
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader

object JSONHelper {
    fun exportToJSON(context: Context, dataList: ArrayList<Product>): Boolean {
        val gson = Gson()
        val jsonString = gson.toJson(dataList)
        var fileOutputStream: FileOutputStream? = null
        try {
            fileOutputStream = context.openFileOutput("orders.json", Context.MODE_PRIVATE)
            fileOutputStream.write(jsonString.toByteArray())
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return false
    }

    fun importFromJSON(context: Context): List<Product>? {
        var streamReader: InputStreamReader? = null
        var fileInputStream: FileInputStream? = null
        try {
            fileInputStream = context.openFileInput("orders.json")
            streamReader = InputStreamReader(fileInputStream)
            val gson = Gson()
            val data = gson.fromJson(streamReader, Array<Product>::class.java).toList()
            Log.d("MyLog", "importFromJSON: $data")
            return data
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            if (streamReader != null) {
                try {
                    streamReader.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            if (fileInputStream != null) {
                try {
                    fileInputStream.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return null
    }
}