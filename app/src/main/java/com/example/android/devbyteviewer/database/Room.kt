/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.example.android.devbyteviewer.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface VideoDao{
    //UI will load the videos using getVideos()
    @Query("SELECT * from databasevideo")
    fun getVideos(): LiveData<List<DatabaseVideo>>

    //Store values in the cache
    //vararg is how a function can take an unknown amount of variable in Kotlin
    //we want to overwrite the last saved value with the new one so we call onConflict
    //sometimes referred to as an "upsert"
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg videos: DatabaseVideo)
}

@Database(entities = [DatabaseVideo::class], version = 1)
abstract class VideosDatabase: RoomDatabase() {
    abstract val videoDao: VideoDao
}

//Singleton for Room
private lateinit var INSTANCE: VideosDatabase
fun getDataBase(context: Context): VideosDatabase {
    //make threadsafe by wrapping in synchronized
    synchronized(VideosDatabase::class.java){
    //if instance is NOT initialized
    if (!:: INSTANCE.isInitialized) {
        INSTANCE = Room.databaseBuilder(context.applicationContext,
        VideosDatabase::class.java, "videos").build()
    }
    }
    return INSTANCE
}