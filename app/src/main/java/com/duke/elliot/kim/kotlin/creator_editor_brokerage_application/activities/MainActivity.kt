package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.activities

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.*
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.adapters.PagerFragmentStateAdapter
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.constants.COLLECTION_CHAT
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.constants.PERMISSIONS_REQUEST_CODE
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.constants.COLLECTION_USERS
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.constants.PREFERENCES_EXCLUDED_CATEGORIES
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.fragments.*
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.model.ChatRoomModel
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.model.UserModel
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.services.FirebaseMessagingService
import com.facebook.CallbackManager
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_my_info_drawer.*

class MainActivity : AppCompatActivity() {

    lateinit var chatRoomsFragment: ChatRoomsFragment
    private lateinit var authStateListener: AuthStateListener
    private lateinit var firebaseAuth: FirebaseAuth
    private val myInfoTabIndex = 3
    private val homeTabIndex = 0
    private var selectedTabIndex = 0
    val callbackManager: CallbackManager? = CallbackManager.Factory.create()
    val homeFragment = HomeFragment()
    val partnersFragment = PartnersFragment()
    val prListFragment = PrListFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val permissionsRequired = getPermissionsRequired(this)
        if (permissionsRequired.isNotEmpty()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                requestPermissions(permissionsRequired,
                    PERMISSIONS_REQUEST_CODE
                )
        }

        // printHashKey(this)

        firebaseAuth = FirebaseAuth.getInstance()
        authStateListener = AuthStateListener { firebaseAuth ->
            if (firebaseAuth.currentUser != null)
                eventAfterSignIn()
            else
                eventAfterSignOut()
        }

        firebaseAuth.addAuthStateListener(authStateListener)

        initFragments()
        initViewPagerAndTabLayout()
    }

    override fun onResume() {
        super.onResume()
        val intent = intent
        if (intent.action != null) {
            when (intent.action) {
                FirebaseMessagingService.ACTION_CHAT_NOTIFICATION -> {
                    val chatRoomId = intent.getStringExtra(FirebaseMessagingService.KEY_CHAT_ROOM_ID)

                    if (chatRoomId != null)
                        enterChatRoom(chatRoomId)
                    else {
                        showToast(this, getString(R.string.failed_to_find_chat_room_message))
                        println("$TAG: chat room not found")
                    }
                }
            }
        }
    }

    private fun enterChatRoom(chatRoomId: String) {
        FirebaseFirestore.getInstance()
            .collection(COLLECTION_CHAT)
            .document(chatRoomId)
            .get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val chatRoom = ChatRoomModel(task.result?.data)
                    startFragment(ChatFragment(chatRoom), R.id.relative_layout_activity_main, CHAT_FRAGMENT_TAG)
                } else {
                    showToast(this, getString(R.string.failed_to_find_chat_room_message))
                    println("$TAG: ${task.exception}")
                }
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
                    showToast(this, getString(R.string.sms_receive_permission_request_message))
            }
        }
    }

    override fun onBackPressed() {
        when {
            supportFragmentManager.findFragmentByTag(CHAT_FRAGMENT_TAG) != null -> super.onBackPressed()
            supportFragmentManager.findFragmentByTag(FILTER_FRAGMENT_TAG) != null -> super.onBackPressed()
            supportFragmentManager.findFragmentByTag(LOGIN_FRAGMENT_TAG) != null -> super.onBackPressed()
            supportFragmentManager.findFragmentByTag(OBTAIN_OAUTH_ACCESS_TOKEN_FRAGMENT) != null -> super.onBackPressed()
            supportFragmentManager.findFragmentByTag(PHONE_AUTH_FRAGMENT_TAG) != null -> super.onBackPressed()
            supportFragmentManager.findFragmentByTag(PR_FRAGMENT_TAG) != null -> super.onBackPressed()
            view_pager.currentItem == homeTabIndex -> {
                if (homeFragment.tabLayout == null)
                    super.onBackPressed()
                else {
                    if (homeFragment.selectedTabIndex == HomeFragment.PARTNERS_TAB_INDEX)
                        homeFragment.tabLayout!!.getTabAt(HomeFragment.PR_LIST_TAB_INDEX)?.select()
                    else
                        super.onBackPressed()
                }
            }
            view_pager.currentItem == myInfoTabIndex -> {
                when {
                    drawer_layout_fragment_my_info.isDrawerOpen(GravityCompat.END) ->
                        drawer_layout_fragment_my_info.closeDrawer(GravityCompat.END)
                    currentUser == null -> super.onBackPressed()
                    else -> view_pager.currentItem = view_pager.currentItem - 1
                }
            }
            else -> {
                view_pager.currentItem = view_pager.currentItem - 1
            }
        }
    }

    override fun onDestroy() {
        firebaseAuth.removeAuthStateListener(authStateListener)
        chatRoomsFragment.removeChatRoomSnapshotListener()
        partnersFragment.removePartnersSnapshotListener()
        prListFragment.removePrSnapshotListener()
        super.onDestroy()
    }

    private fun initViewPagerAndTabLayout() {
        view_pager.adapter = PagerFragmentStateAdapter(this)
        view_pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            var pageChanged = false
            var position = 0

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                pageChanged = true
                this.position = position
            }

            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)

                if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    if (pageChanged) {
                        showToast(this@MainActivity, "curr page $position")
                    }
                }
            }
        })

        TabLayoutMediator(tab_layout, view_pager) { tab, position ->
            tab.tag = position
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

                    when {
                        firebaseAuth.currentUser == null -> {
                            if (i != homeTabIndex)
                                requestSignIn()
                        }
                        currentUser == null -> {
                            if (i == myInfoTabIndex || i == homeTabIndex)
                                tab_layout.getTabAt(i)?.select()
                            else
                                requestProfileCreation()
                        }
                        else -> tab_layout.getTabAt(i)?.select()
                    }
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

                if ((tab?.tag as Int) == homeTabIndex)
                    view_pager.isUserInputEnabled = false
                else if (firebaseAuth.currentUser != null)
                    view_pager.isUserInputEnabled = true
            }
        })
    }

    private fun initFragments() {
        chatRoomsFragment = ChatRoomsFragment()
    }

    private fun eventAfterSignIn() {
        readData()
        popAllFragments()
    }

    private fun popAllFragments() {
        while (supportFragmentManager.backStackEntryCount > 0)
            supportFragmentManager.popBackStackImmediate()

        if (supportFragmentManager.findFragmentByTag(LOGIN_FRAGMENT_TAG) != null && !signUp) {
            showToast(this, getString(R.string.sign_in_message))
            signUp = false
        } else if (signUp) {
            showToast(this, getString(R.string.sign_up_message))
            signUp = false
        }
    }

    private fun eventAfterSignOut() {
        if (supportFragmentManager.findFragmentByTag(LOGIN_FRAGMENT_TAG) != null)
            showToast(this, getString(R.string.sign_out_message))

        clearPreferences()

        currentUser = null
        signUp = false
        tab_layout.getTabAt(homeTabIndex)?.select()
        view_pager.isUserInputEnabled = false
        popAllFragments()
    }

    private fun clearPreferences() {
        this.getSharedPreferences(
            PREFERENCES_EXCLUDED_CATEGORIES, Context.MODE_PRIVATE).edit().clear().apply()
    }

    fun startFragment(fragment: Fragment,
                      containerViewId: Int,
                      tag: String? = null) {
        supportFragmentManager.beginTransaction()
            .addToBackStack(null)
            .setCustomAnimations(
                R.anim.anim_slide_in_left,
                R.anim.anim_slide_out_left,
                R.anim.anim_slide_in_right,
                R.anim.anim_slide_out_right
            ).replace(containerViewId, fragment, tag).commit()
    }

    fun requestSignIn() {
        val builder = AlertDialog.Builder(this)
        //builder.setTitle("로그인 요청")
        builder.setMessage(getString(R.string.sign_in_and_profile_creation_request_message))
        builder.setPositiveButton(getString(R.string.login)) { _, _ ->
                startFragment(LoginFragment(), R.id.relative_layout_activity_main, LOGIN_FRAGMENT_TAG)
        }.setNegativeButton(getString(R.string.cancel)) { _, _ -> }
            .create().show()
    }

    fun requestProfileCreation() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(getString(R.string.profile_creation_request_message))
        builder.setPositiveButton(getString(R.string.profile_creation)) { _, _ -> tab_layout.getTabAt(myInfoTabIndex)?.select() }
            .create().show()
    }

    private fun readData() {
        FirebaseFirestore.getInstance()
            .collection(COLLECTION_USERS)
            .document(firebaseAuth.currentUser?.uid.toString())
            .get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (task.result != null)
                        if (task.result!!.data == null)
                            requestProfileCreation()
                        else
                            setCurrentUser(task.result!!.data as Map<String, Any>)
                } else {
                    showToast(this, getString(R.string.data_read_failed_message))
                    println("$TAG: ${task.exception}")
                }
            }
    }

    private fun setCurrentUser(map: Map<String, Any>) {
        if (currentUser != null)
            currentUser?.setData(map)
        else
            currentUser = UserModel(map)

        view_pager.isUserInputEnabled = view_pager.currentItem != homeTabIndex
        createToken()
    }

    private fun createToken() {
        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val pushToken = task.result?.token

                if (pushToken != null) {
                    val map = mapOf(
                        UserModel.KEY_PUSH_TOKEN to pushToken
                    )

                    currentUser?.pushToken = pushToken
                    println("${MyInfoFragment.TAG}: token generated")

                    FirebaseFirestore.getInstance()
                        .collection(COLLECTION_USERS)
                        .document(firebaseAuth.currentUser?.uid.toString())
                        .update(map).addOnCompleteListener { updateTask ->
                            if (updateTask.isSuccessful)
                                println("$TAG: token updated")
                            else {
                                showToast(this, getString(R.string.token_storage_failure_message))
                                println("$TAG: ${task.exception}")
                            }
                        }
                } else
                    showToast(this, getString(R.string.token_generation_failure_message))
            } else {
                showToast(this, getString(R.string.token_generation_failure_message))
                println("$TAG: token generation failed")
            }
        }
    }

    fun moveToNextTab() {
        tab_layout.getTabAt(view_pager.currentItem + 1)?.select()
    }

    /*
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
        const val FILTER_FRAGMENT_TAG = "filter_fragment_tag"
        const val LOGIN_FRAGMENT_TAG = "login_fragment_tag"
        const val OBTAIN_OAUTH_ACCESS_TOKEN_FRAGMENT = "obtain_oauth_access_token_fragment"
        const val PHONE_AUTH_FRAGMENT_TAG = "phone_auth_fragment_tag"
        const val PR_FRAGMENT_TAG = "pr_fragment_tag"

        var currentChatRoomId: String? = null
        var currentUser: UserModel? = null
        var signUp = false

        private val tabIcons = arrayOf(
            R.drawable.ic_tab_home_24dp,
            R.drawable.ic_tab_edit_24dp,
            R.drawable.ic_tab_chat_24dp,
            R.drawable.ic_tab_person_24dp
        )

        private val tabTexts = arrayOf("홈", "글쓰기", "채팅", "내정보")
    }
}
