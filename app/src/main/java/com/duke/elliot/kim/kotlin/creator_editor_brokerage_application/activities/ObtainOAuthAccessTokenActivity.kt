package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.activities

import android.accounts.AccountManager
import android.accounts.AccountManagerCallback
import android.accounts.AccountManagerFuture
import android.app.PendingIntent
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsService
import androidx.browser.customtabs.CustomTabsServiceConnection
import androidx.core.content.ContextCompat
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.R
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.broadcast_receiver.CustomTabsReceiver
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.showToast
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_obtain_o_auth_access_token.*
import okhttp3.*
import java.io.IOException


class ObtainOAuthAccessTokenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_obtain_o_auth_access_token)
        //runCustomTabs()

        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboardManager.addPrimaryClipChangedListener {
            showToast(this, "CLLLCLLC")
            val builder = AlertDialog.Builder(this)
            //builder.setTitle("로그인 요청")
            builder.setMessage(getString(R.string.sign_in_and_profile_creation_request_message))
            builder.setPositiveButton(getString(R.string.login)) { _, _ -> showToast(this, "CLIPED")
            }.setNegativeButton(getString(R.string.cancel)) { _, _ -> }
                .create().show()
        }

        val connection = object : CustomTabsServiceConnection() {
            override fun onCustomTabsServiceConnected(
                name: ComponentName,
                client: CustomTabsClient
            ) {
                val builder = CustomTabsIntent.Builder()
                val icon = BitmapFactory.decodeResource(resources, android.R.drawable.ic_btn_speak_now)
                val title = getString(R.string.complete)

                builder.setToolbarColor(ContextCompat.getColor(applicationContext, R.color.colorPrimary))
                //builder.addDefaultShareMenuItem()
                builder.setActionButton(icon, title,createPendingIntent(0), true)
                builder.setExitAnimations(applicationContext, R.anim.anim_slide_in_left, R.anim.anim_slide_out_right)
                builder.setStartAnimations(applicationContext, R.anim.anim_slide_in_right, R.anim.anim_slide_out_left)

                val customTabsIntent = builder.build()
                customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                customTabsIntent.intent.setPackage(CUSTOM_TAB_PACKAGE_NAME)
                customTabsIntent.launchUrl(applicationContext, Uri.parse(GOOGLE_AUTHORIZATION_SERVER_URI))


            }

            override fun onServiceDisconnected(p0: ComponentName?) {

            }

        }

        button_get_access_token.setOnClickListener {
            val text = edit_text_code.text.toString()
            getAccessCode(text)

        }

        if(CustomTabsClient.bindCustomTabsService(this, CUSTOM_TAB_PACKAGE_NAME, connection))
            showToast(this, "connected")


    }

    override fun onStart() {
        super.onStart()

        //testWithGoogle()
    }

    private fun createPendingIntent(actionId: Int) : PendingIntent {
        val intent = Intent(applicationContext, CustomTabsReceiver::class.java)
        // add some actions
        println("HEEEEEEEEEEEEEEEEE")
        return PendingIntent.getBroadcast(applicationContext, actionId, intent, 0)
    }

    private fun getAccessCode(code: String) {
        val url = "https://accounts.google.com/o/oauth2/token"

        val cid = "279682383751-rdjblp84735k1piq5ra4c61ti6a1mlgr.apps.googleusercontent.com"
        val css = "E4m6F5AnTdAiZT6nFxVZZvz-"

        val map = hashMapOf(
            "code" to code,
            "client_id" to cid,
            "client_secret" to css,
            "redirect_uri" to "https://creator-editor-brokerage-1c935.firebaseapp.com/__/auth/handler",
            "grant_type" to "authorization_code"
        )

        val requestBody =
            FormBody.Builder().add("code", code)
                .add("client_id", CLIENT_ID)
                .add("redirect_uri", REDIRECT_URI)
                .add("grant_type", "authorization_code").build()

        //val rrbb = MultipartBody.Builder().add

        val request = Request.Builder().header("Content-Type", "application/x-www-form-urlencoded")
            .url(url)
            .post(requestBody)
            .build()

        showToast(this, "HEEEEEE")

        val okHttpClient = OkHttpClient()
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                //showToast(applicationContext, getString(R.string.chat_message_sending_failure_message))
                println("$TAG FAKMAAAAAAA: ${e?.message}")
                showToast(this@ObtainOAuthAccessTokenActivity, "HJJJJJ")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful == true) {

                    val map: Map<*, *>? =
                        Gson().fromJson(response.body?.string(), Map::class.java)
                    //if (map != null)
                    println("SEXINGXXXX : $map")
                    showToast(this@ObtainOAuthAccessTokenActivity, "HEEEEJJJJJEE")

                } else {
                    //showToast(applicationContext, getString(R.string.chat_message_sending_failure_message))
                    println("$TAG: message sending failed")
                }
            }
        })
    }

    private fun runCustomTabs() {
        val connection: CustomTabsServiceConnection = object : CustomTabsServiceConnection() {
            override fun onCustomTabsServiceConnected(
                name: ComponentName,
                client: CustomTabsClient
            ) {
                val builder = CustomTabsIntent.Builder()
                val customTabsIntent = builder.build()
                customTabsIntent.launchUrl(this@ObtainOAuthAccessTokenActivity,
                    Uri.parse(GOOGLE_AUTHORIZATION_SERVER_URI))
            }

            override fun onServiceDisconnected(name: ComponentName) {}
        }
        val ok =
            CustomTabsClient.bindCustomTabsService(this, packageName, connection)

        if (ok)
            showToast(this, "SUCCESS")
        else
            showToast(this, "FALSE")
    }

    private fun testWithGoogle() {

        val am: AccountManager = AccountManager.get(this) // "this" references the current Context

        val accounts = am.getAccountsByType("com.google")
        println("GAGAGAGA" + accounts)
        println("GAGAGAGA" + accounts.size)
        println("GAGAGAGA" + accounts[0])

        val options = Bundle()

        /*
        am.getAuthToken(
            accounts[0],                     // Account retrieved using getAccountsByType()
            YOUTUBE_SCOPE,            // Auth scope
            options,                        // Authenticator-specific options
            this,                           // Your activity
            OnTokenAcquired(),              // Callback called when a token is successfully acquired
            null)              // Callback called if an error occurs // 코드보고 넣을 것.d

         */

        am.getAuthToken(
            accounts[0],                     // Account retrieved using getAccountsByType()
            YOUTUBE_SCOPE,            // Auth scope
            options,                        // Authenticator-specific options
            this,                           // Your activity
            OnTokenAcquired(0),              // Callback called when a token is successfully acquired
            null)
    }



    private class OnTokenAcquired(private val type: Int) : AccountManagerCallback<Bundle> {
        override fun run(result: AccountManagerFuture<Bundle>) {
            if (type == 0) {
                // Get the result of the operation from the AccountManagerFuture.
                val bundle: Bundle = result.result

                // The token is a named value in the bundle. The name of the value
                // is stored in the constant AccountManager.KEY_AUTHTOKEN.
                val token: String? = bundle.getString(AccountManager.KEY_AUTHTOKEN)
                println("TOKTOK $token")
                val kk = bundle.getString(AccountManager.KEY_ACCOUNTS)

                // managedByMe
                val url = "https://www.googleapis.com/youtube/v3/channels?part=id&mine=true"
                //val url = "https://www.googleapis.com/youtube/v3/search?part=id,snippet&forMine=true"

                val request = Request.Builder().header("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer $token")
                    .url(url)
                    .get()
                    .build()

                val okHttpClient = OkHttpClient()
                okHttpClient.newCall(request).enqueue(object : Callback {

                    override fun onFailure(call: Call, e: IOException) {
                        println("$TAG FAKMAAAAA: ${e?.message}")
                    }

                    override fun onResponse(call: Call, response: Response) {
                        if (response?.isSuccessful == true) {

                            val map: Map<*, *>? =
                                Gson().fromJson(response.body?.string(), Map::class.java)
                            //if (map != null)
                            println("SEXING : $map")

                        } else {
                            //showToast(applicationContext, getString(R.string.chat_message_sending_failure_message))
                            println("$TAG: message sending failed")
                        }
                    }
                })
            }


        }
    }

    companion object {
        private const val TAG = "ObtainOAuthAccessTokenActivity"

        private const val CUSTOM_TAB_PACKAGE_NAME = "com.android.chrome"

        //279682383751-rdjblp84735k1piq5ra4c61ti6a1mlgr.apps.googleusercontent.com // web
        //279682383751-8gp8j37rsri53neu6qgjc2lt80bouvjo.apps.googleusercontent.com
        private const val CLIENT_ID = "279682383751-8gp8j37rsri53neu6qgjc2lt80bouvjo.apps.googleusercontent.com"
        private const val REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob"
        private const val RESPONSE_TYPE = "code"
        private const val SCOPE = "https://www.googleapis.com/auth/youtube"

        private const val YOUTUBE_SCOPE = "oauth2:https://www.googleapis.com/auth/youtube"
        private const val NEW_SCOPE = "oauth2:https://www.googleapis.com/auth/youtubepartner"
        //"Manage your YouTube account"  // AUTH_TOKEN_TYPE

        const val GOOGLE_AUTHORIZATION_SERVER_URI = "https://accounts.google.com/o/oauth2/auth?" +
                "client_id=$CLIENT_ID&" +
                "redirect_uri=$REDIRECT_URI&" +
                "scope=$SCOPE&" +
                "response_type=$RESPONSE_TYPE&" +
                "access_type=offline"
    }
}
