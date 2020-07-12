package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.activities

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.*
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.adapters.PagerFragmentStateAdapter
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.fragments.LoginFragment
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.fragments.MyInfoFragment
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.fragments.PRListFragment
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.fragments.USERS
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.model.UserModel
import com.facebook.CallbackManager
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.core.ImagePipelineConfig
import com.facebook.imagepipeline.core.ImageTranscoderType
import com.facebook.imagepipeline.core.MemoryChunkType
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : FragmentActivity() {

    lateinit var currentUserModel: UserModel
    private var selectedTabIndex = 0
    val callbackManager: CallbackManager? = CallbackManager.Factory.create()
    val prListFragment = PRListFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val permissionsRequired = getPermissionsRequired(this)
        if (permissionsRequired.isNotEmpty()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                requestPermissions(permissionsRequired, PERMISSIONS_REQUEST_CODE)
        }

        // printHashKey(this)

        Fresco.initialize(
            applicationContext,
            ImagePipelineConfig.newBuilder(applicationContext)
                .setMemoryChunkType(MemoryChunkType.BUFFER_MEMORY)
                .setImageTranscoderType(ImageTranscoderType.JAVA_TRANSCODER)
                .experiment().setNativeCodeDisabled(true)
                .build())

        view_pager.adapter = PagerFragmentStateAdapter(this)

        /*
        view_pager.registerOnPageChangeCallback(object : OnPageChangeCallback() {

            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
                if (currentUser == null) {
                    if (tab_layout.selectedTabPosition == 0) {
                        requestLogin()
                        view_pager.isUserInputEnabled = false
                    }

                    tab_layout.getTabAt(0)?.select()
                }
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
            }

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
            }
        })

         */

        TabLayoutMediator(tab_layout, view_pager) { tab, position ->
            tab.text = tabTexts[position]
            tab.setIcon(tabIcons[position])
            tab.icon!!.setColorFilter(
                ContextCompat.getColor(
                    this@MainActivity, R.color.colorTabIconUnselected), Mode.SRC_IN)
        }.attach()

        val linearLayout = tab_layout.getChildAt(0) as LinearLayout
        for (i in 0 until linearLayout.childCount) {
            linearLayout.getChildAt(i).setOnTouchListener { _, motionEvent ->
                if (motionEvent.action == MotionEvent.ACTION_UP) {
                    selectedTabIndex = i

                    if (FirebaseAuth.getInstance().currentUser == null) {
                        if (i != 0)
                            requestLogin()
                    } else
                        tab_layout.getTabAt(i)?.select()
                }
                true
            }
        }

        tab_layout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                tab?.icon?.setColorFilter(
                    ContextCompat.getColor(
                        this@MainActivity, R.color.colorTabIconUnselected), Mode.SRC_IN)
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.icon?.setColorFilter(
                    ContextCompat.getColor(
                        this@MainActivity, R.color.colorTabIconSelected), Mode.SRC_IN)
            }
        })
    }

    override fun onResume() {
        super.onResume()
        currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            view_pager.isUserInputEnabled = true
            readData()
        } else {
            view_pager.isUserInputEnabled = false
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager?.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (PackageManager.PERMISSION_GRANTED == grantResults.firstOrNull()) {
                if (hasReceiveSMSPermissions(this))
                    println("$TAG: SMS receive permission granted.")
            } else {
                if (!hasReceiveSMSPermissions(this))
                    showToast("SMS 수신 권한을 승인하셔야 본인인증을 진행하실 수 있습니다.")
            }
        }
    }

    override fun onBackPressed() {
        when {
            supportFragmentManager.findFragmentByTag(CHAT_FRAGMENT_TAG) != null -> super.onBackPressed()
            supportFragmentManager.findFragmentByTag(LOGIN_FRAGMENT_TAG) != null -> super.onBackPressed()
            supportFragmentManager.findFragmentByTag(PHONE_AUTH_FRAGMENT_TAG) != null -> super.onBackPressed()
            supportFragmentManager.findFragmentByTag(PR_FRAGMENT_TAG) != null -> super.onBackPressed()
            view_pager.currentItem == 0 -> {
                super.onBackPressed()
            }
            else -> {
                view_pager.currentItem = view_pager.currentItem - 1
            }
        }
    }

    fun eventAfterLogin(user: FirebaseUser?) {
        currentUser = user
        if (currentUser != null)
            readData()

        view_pager.isUserInputEnabled = true
        popAllFragments()
        tab_layout.getTabAt(selectedTabIndex)?.select()
    }

    private fun popAllFragments() {
        while(supportFragmentManager.backStackEntryCount > 0)
            supportFragmentManager.popBackStackImmediate()
    }

    fun eventAfterLogout() {
        currentUser = null
        if (::currentUserModel.isInitialized)
            currentUserModel.finalize()
        tab_layout.getTabAt(0)?.select()
        view_pager.isUserInputEnabled = false
        popAllFragments()
        showToast("로그아웃 되었습니다.")
    }

    fun startFragment(fragment: Fragment,
                      containerViewId: Int,
                      tag: String? = null) {
        supportFragmentManager.beginTransaction()
            .addToBackStack(null)
            .setCustomAnimations(
                R.anim.anim_slide_in_left_enter,
                R.anim.anim_slide_in_left_exit,
                R.anim.anim_slide_out_right_enter,
                R.anim.anim_slide_out_right_exit
            ).replace(containerViewId, fragment, tag).commit()
    }

    fun requestLogin() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("인증 요청")
        builder.setMessage("로그인 및 본인인증을 하셔야 해당 서비스를 이용하실 수 있습니다.")
        builder.setPositiveButton("로그인") { _, _ ->
            startFragment(LoginFragment(), R.id.main_activity_container_view, LOGIN_FRAGMENT_TAG)
        }.setNegativeButton("취소") { _, _ -> }
            .create().show()
    }

    fun requestAuthentication() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("인증 요청")
        builder.setMessage("본인인증을 하셔야 해당 서비스를 이용하실 수 있습니다.")
        builder.setPositiveButton("본인인증") { _, _ ->
            startFragment(LoginFragment(), R.id.main_activity_container_view)
        }.setNegativeButton("취소") { _, _ -> }
            .create().show()
    }

    fun requestProfileCreation() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("프로필 작성")
        builder.setMessage("프로필이 아직 등록되지 않았습니다. 프로필을 작성해주세요.")
        builder.setPositiveButton("확인") { _, _ -> tab_layout.getTabAt(3)?.select() }
            .create().show()
    }

    private fun readData() {
        FirebaseFirestore.getInstance()
            .collection(USERS)
            .document(currentUser?.uid.toString())
            .get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (task.result != null)
                        if (task.result!!.data == null)
                            requestProfileCreation()
                        else
                            setCurrentUserModel(task.result!!.data as Map<String, Any>)
                } else {
                    showToast("데이터를 읽어올 수 없습니다.")
                    println("$TAG: ${task.exception}")
                }
            }
    }

    private fun setCurrentUserModel(map: Map<String, Any>) {
        if (::currentUserModel.isInitialized)
            currentUserModel.setData(map)
        else {
            currentUserModel = UserModel()
            currentUserModel.setData(map)
        }

        checkPushToken()
    }

    private fun checkPushToken() {
        if (currentUserModel.pushToken == null) {
            FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    currentUserModel.pushToken = task.result?.token
                    println("${MyInfoFragment.TAG}: Token generated")
                } else {
                    showToast("토큰 생성에 실패했습니다.")
                    println("${MyInfoFragment.TAG}: Token generation failed")
                }
            }
        }
    }

    fun isCurrentUserModelInitialized() : Boolean = this::currentUserModel.isInitialized

    fun showToast(text: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this, text, duration).show()
    }


    /*
    // Dq8Gg7LDtrK2P1vH4eJulfzthIY=
    @SuppressLint("PackageManagerGetSignatures")
    private fun printHashKey(context: Context) {
        try {
            val info: PackageInfo =
                context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_SIGNATURES)

            for (signature in info.signatures) {
                val md: MessageDigest = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                val hashKey = String(Base64.encode(md.digest(), 0))
                println("printHashKey() Hash Key: $hashKey")
            }
        } catch (e: NoSuchAlgorithmException) {
            println("printHashKey() $e")
        } catch (e: Exception) {
            println("printHashKey() $e")
        }
    }
     */

    companion object {
        const val TAG = "MainActivity"
        const val CHAT_FRAGMENT_TAG = "chat_fragment_tag"
        const val LOGIN_FRAGMENT_TAG = "login_fragment_tag"
        const val PHONE_AUTH_FRAGMENT_TAG = "phone_auth_fragment_tag"
        const val PR_FRAGMENT_TAG = "pr_fragment_tag"

        var currentUser: FirebaseUser? = null
        var publicName = "회원"

        private val tabIcons = arrayOf(
            R.drawable.ic_tab_home_24dp,
            R.drawable.ic_tab_edit_24dp,
            R.drawable.ic_tab_chat_24dp,
            R.drawable.ic_tab_person_24dp
        )

        private val tabTexts = arrayOf("홈", "글쓰기", "채팅", "내정보")
    }
}
