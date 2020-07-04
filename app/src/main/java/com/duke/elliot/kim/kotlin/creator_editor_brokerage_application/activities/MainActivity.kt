package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.activities

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.Mode
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.R
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.adapters.PagerFragmentStateAdapter
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.fragments.LoginFragment
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.setColorFilter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.activity_main.*
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        printHashKey(this)

        view_pager.adapter = PagerFragmentStateAdapter(this)

        TabLayoutMediator(tab_layout, view_pager) { tab, position ->
            tab.text = tabTexts[position]
            tab.setIcon(tabIcons[position])
            tab.icon!!.setColorFilter(
                ContextCompat.getColor(
                    this@MainActivity, R.color.colorTabIconUnselected), Mode.SRC_IN)
        }.attach()

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
                if (tab?.text == "내정보") {
                    // 로그인 여부 확인 조건문 추가.
                    requestLogin()
                }

            }

        })
    }

    override fun onBackPressed() {
        when {
            supportFragmentManager.findFragmentByTag(LOGIN_FRAGMENT_TAG) != null -> super.onBackPressed()
            view_pager.currentItem == 0 -> {
                super.onBackPressed()
            }
            else -> {
                view_pager.currentItem = view_pager.currentItem - 1
            }
        }
    }

    fun popAllFragments() {
        while(supportFragmentManager.backStackEntryCount > 0)
            supportFragmentManager.popBackStackImmediate()
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
            .show()
    }

    fun requestAuthentication() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("인증 요청")
        builder.setMessage("본인인증을 하셔야 해당 서비스를 이용하실 수 있습니다.")
        builder.setPositiveButton("본인인증") { _, _ ->
            startFragment(LoginFragment(), R.id.main_activity_container_view)
        }.setNegativeButton("취소") { _, _ -> }
            .show()
    }

    fun showToast(text: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this, text, duration).show()
    }

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

    companion object {
        const val LOGIN_FRAGMENT_TAG = "login_fragment_tag"

        private val tabIcons = arrayOf(
            R.drawable.ic_tab_home_24dp,
            R.drawable.ic_tab_edit_24dp,
            R.drawable.ic_tab_chat_24dp,
            R.drawable.ic_tab_person_24dp
        )

        private val tabTexts = arrayOf("홈", "글쓰기", "채팅", "내정보")
    }
}
