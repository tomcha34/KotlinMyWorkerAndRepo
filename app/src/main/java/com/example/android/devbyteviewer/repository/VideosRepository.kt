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

package com.example.android.devbyteviewer.repository

import android.provider.ContactsContract
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.example.android.devbyteviewer.database.VideosDatabase
import com.example.android.devbyteviewer.database.asDomainModel
import com.example.android.devbyteviewer.domain.Video
import com.example.android.devbyteviewer.network.Network
import com.example.android.devbyteviewer.network.asDatabaseModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

//Repository for fetching devbyte videos from the network and storing them on disk.
//Will be used in ViewModel.
class VideosRepository (private val database: VideosDatabase) {

    // A playlist of videos that can be shown on the screen.
    val videos: LiveData<List<Video>> =
            //Trans.map lets us convert from one livedata to another.
            //In this case turning databaseVideo object into domain Video object
            Transformations.map(database.videoDao.getVideos()) {
        it.asDomainModel()
    }


    //suspend means a function is coroutine friendly
    suspend fun refreshVideos() {
        /* make a network call to getPlaylist(),
        and use the await() function to tell the coroutine to
        suspend until the data is available.
        Then call insertAll() to insert the playlist into the database.
         */
        withContext(Dispatchers.IO) {
            val playlist = Network.devbytes.getPlaylist().await()
            //Note the asterisk * is the spread operator.
            // It allows you to pass in an array to a function that expects varargs.
            database.videoDao.insertAll(*playlist.asDatabaseModel())
        }
    }
}