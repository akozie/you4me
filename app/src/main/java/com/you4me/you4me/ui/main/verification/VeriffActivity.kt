package com.you4me.you4me.ui.main.verification

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.veriff.Result
import com.veriff.Sdk
import com.you4me.you4me.R
import com.you4me.you4me.utils.SharedPrefHelper.Companion.SESSION_ID

//class VeriffActivity : AppCompatActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_veriff)
//    }
//}

class VeriffActivity : AppCompatActivity() {

    companion object {
        const val REQUEST_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sessionId = intent.getStringExtra(SESSION_ID)

        // Replace with session URL received from your backend
        val sessionUrl = "https://magic.veriff.me/v/$sessionId"

        val intent = Sdk.createLaunchIntent(this, sessionUrl)
        startActivityForResult(intent, REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE) {
            val result = com.veriff.Result.fromResultIntent(data)
            result?.let {
                handleResult(it)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun handleResult(result: com.veriff.Result) {
        when (result.status) {
            com.veriff.Result.Status.DONE -> {
                // The end-user successfully submitted the session, the session is completed from their perspective
            }
            com.veriff.Result.Status.CANCELED -> {
                // The end-user canceled the verification process
            }
            Result.Status.ERROR -> {
                // An error occurred during the flow, Veriff has already shown UI, no need to display
                // a separate error message here
                Log.d("Verification error occurred:", "${result.error}")
            }
        }
    }


}
