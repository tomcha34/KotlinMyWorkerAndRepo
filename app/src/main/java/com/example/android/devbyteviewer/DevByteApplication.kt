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

package com.example.android.devbyteviewer

import android.app.Application
import android.os.Build
import androidx.work.*
import com.example.android.devbyteviewer.work.RefreshDataWork
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Override application to setup background work via WorkManager
 */
class DevByteApplication : Application() {

    private val applicationScope = CoroutineScope(Dispatchers.Default)

    /**
     * onCreate is called before the first screen is shown to the user.
     *
     * Use it to setup any background tasks, running expensive setup operations in a background
     * thread to avoid delaying app start.
     */
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        delayedInit()
    }

    private fun delayedInit() = applicationScope.launch {
        setupRecurringWork()
    }

    private fun setupRecurringWork() {

        val constraints = Constraints.Builder()
                //Only unmetered networks.
                // user wont be charged for the network request
                .setRequiredNetworkType(NetworkType.UNMETERED)
                //battery cant be low
                .setRequiresBatteryNotLow(true)
                //device must be charging
                .setRequiresCharging(true)
                .apply {
                    //for Marshmellow and above we can request to run
                    // only if device is idle.
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    setRequiresDeviceIdle(true)
                    }
                }.build()


        val repeatingRequest = PeriodicWorkRequestBuilder<RefreshDataWork>(
                1,TimeUnit.DAYS)
                .setConstraints(constraints)
                .build()


        WorkManager.getInstance().enqueueUniquePeriodicWork(
                //WORK_NAME is a companion object in WorkManager (CoroutineWorker) class.
                RefreshDataWork.WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                repeatingRequest)

    }
}
