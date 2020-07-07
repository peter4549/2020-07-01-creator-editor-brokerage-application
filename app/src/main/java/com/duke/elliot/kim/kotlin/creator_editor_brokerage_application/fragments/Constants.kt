package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.fragments

import android.content.res.Resources
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.R

const val USERS = "users"
const val IMAGES = "images"
const val PR_LIST = "pr_list"

const val NAME = "name"
const val IS_VERIFIED = "is_verified"
const val PHONE_NUMBER = "phone_number"
const val AGE = "age"
const val GENDER = "gender"
const val PR = "pr"

const val TITLE = "title"
const val CONTENT = "content"
const val IMAGE_NAMES = "image_names"

val CONTENT_CATEGORIES = arrayListOf(
    Resources.getSystem().getString(R.string.category_car),
    Resources.getSystem().getString(R.string.category_beauty_fashion),
    Resources.getSystem().getString(R.string.category_comedy),
    Resources.getSystem().getString(R.string.category_education),
    Resources.getSystem().getString(R.string.category_entertainment),
    Resources.getSystem().getString(R.string.category_family_entertainment),
    Resources.getSystem().getString(R.string.category_movie_animation),
    Resources.getSystem().getString(R.string.category_food),
    Resources.getSystem().getString(R.string.category_game),
    Resources.getSystem().getString(R.string.category_know_how_style),
    Resources.getSystem().getString(R.string.category_music),
    Resources.getSystem().getString(R.string.category_news_politics),
    Resources.getSystem().getString(R.string.category_non_profit_social_movement),
    Resources.getSystem().getString(R.string.category_people_blog),
    Resources.getSystem().getString(R.string.category_pets_animals),
    Resources.getSystem().getString(R.string.category_science_technology),
    Resources.getSystem().getString(R.string.category_sports),
    Resources.getSystem().getString(R.string.category_travel_event)
)
