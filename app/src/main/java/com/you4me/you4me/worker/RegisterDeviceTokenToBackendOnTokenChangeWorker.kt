package com.you4me.you4me.worker

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.gson.JsonObject
import com.you4me.you4me.network.ApiCollector
import com.you4me.you4me.network.RemoteDataSource
import com.you4me.you4me.repository.MainRepository

//class RegisterDeviceTokenToBackendOnTokenChangeWorker(
//    val context: Context,
//    private val workParameters: WorkerParameters,
//) : Worker(context, workParameters) {
//    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
//
//    private val notificationRepository =
//        MainRepository(RemoteDataSource().buildApi(ApiCollector::class.java))
//
//    override fun doWork(): Result {
//        val deviceToken = inputData.getString(WORKER_INPUT_FIREBASE_DEVICE_TOKEN_TAG)
//
//        val obj = JsonObject()
//        obj.addProperty("pushToken", token)
//        val repo = MainRepository(RemoteDataSource().buildApi(ApiCollector::class.java))
//        scope.launch { repo.updatePushToken(userId, obj) }
//
//        val response = terminalId?.let { tid ->
//            var tokenResponse: RegisterDeviceTokenResponse? = null
//            deviceToken?.let { token ->
//                val req = NotificationRegisterDeviceTokenModel(token, tid, userName)
//                notificationRepository.registerDeviceToken(req)
//                    .compose(getSingleTransformer(NOTIFICATION_ERROR))
//                    .subscribe { value ->
//                        tokenResponse = value
//                    }.disposeWith(compositeDisposable)
//            }
//            tokenResponse?.success
//        } ?: false
//
//        return if (response) {
//            Result.success()
//        } else {
//            Result.retry()
//        }
//    }
//}
