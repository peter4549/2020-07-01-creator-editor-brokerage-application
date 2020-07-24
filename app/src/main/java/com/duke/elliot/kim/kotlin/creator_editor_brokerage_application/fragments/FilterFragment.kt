package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import androidx.appcompat.widget.Toolbar

import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.R
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.activities.MainActivity
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.constants.ContentCategories
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.constants.KEY_EXCLUDED_CATEGORIES
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.constants.PREFERENCES_EXCLUDED_CATEGORIES
import kotlinx.android.synthetic.main.fragment_filter.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class FilterFragment : Fragment() {

    private lateinit var toolbar: Toolbar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_filter, container, false)
        toolbar = view.toolbar

        (activity as MainActivity).setSupportActionBar(toolbar)
        (activity as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setHasOptionsMenu(true)

        toolbar.title = getString(R.string.category_filter)

        initCheckBoxes(view, (activity as MainActivity).homeFragment.excludedCategories)

        return view
    }

    override fun onStop() {
        saveCheckBoxesStates()
        super.onStop()
    }

    private fun initCheckBoxes(view: View, excludedCategories: MutableSet<String>) {
        view.check_box_car.isChecked = ContentCategories.CAR !in excludedCategories
        view.check_box_beauty_fashion.isChecked = ContentCategories.BEAUTY_FASHION !in excludedCategories
        view.check_box_comedy.isChecked = ContentCategories.COMEDY !in excludedCategories
        view.check_box_education.isChecked = ContentCategories.EDUCATION !in excludedCategories
        view.check_box_entertainment.isChecked = ContentCategories.ENTERTAINMENT !in excludedCategories
        view.check_box_family_entertainment.isChecked = ContentCategories.FAMILY_ENTERTAINMENT !in excludedCategories
        view.check_box_movie_animation.isChecked = ContentCategories.MOVIE_ANIMATION !in excludedCategories
        view.check_box_food.isChecked = ContentCategories.FOOD !in excludedCategories
        view.check_box_game.isChecked = ContentCategories.GAME !in excludedCategories
        view.check_box_know_how_style.isChecked = ContentCategories.KNOW_HOW_STYLE !in excludedCategories
        view.check_box_music.isChecked = ContentCategories.MUSIC !in excludedCategories
        view.check_box_news_politics.isChecked = ContentCategories.NEWS_POLITICS !in excludedCategories
        view.check_box_non_profit_social_movement.isChecked = ContentCategories.NON_PROFIT_SOCIAL_MOVEMENT !in excludedCategories
        view.check_box_people_blog.isChecked = ContentCategories.PEOPLE_BLOG !in excludedCategories
        view.check_box_pets_animals.isChecked = ContentCategories.PETS_ANIMALS !in excludedCategories
        view.check_box_science_technology.isChecked = ContentCategories.SCIENCE_TECHNOLOGY !in excludedCategories
        view.check_box_sports.isChecked = ContentCategories.SPORTS !in excludedCategories
        view.check_box_travel_event.isChecked = ContentCategories.TRAVEL_EVENT !in excludedCategories

        view.check_box_car.setOnCheckedChangeListener(onCheckedChangeListener)
        view.check_box_beauty_fashion.setOnCheckedChangeListener(onCheckedChangeListener)
        view.check_box_comedy.setOnCheckedChangeListener(onCheckedChangeListener)
        view.check_box_education.setOnCheckedChangeListener(onCheckedChangeListener)
        view.check_box_entertainment.setOnCheckedChangeListener(onCheckedChangeListener)
        view.check_box_family_entertainment.setOnCheckedChangeListener(onCheckedChangeListener)
        view.check_box_food.setOnCheckedChangeListener(onCheckedChangeListener)
        view.check_box_game.setOnCheckedChangeListener(onCheckedChangeListener)
        view.check_box_know_how_style.setOnCheckedChangeListener(onCheckedChangeListener)
        view.check_box_music.setOnCheckedChangeListener(onCheckedChangeListener)
        view.check_box_news_politics.setOnCheckedChangeListener(onCheckedChangeListener)
        view.check_box_non_profit_social_movement.setOnCheckedChangeListener(onCheckedChangeListener)
        view.check_box_people_blog.setOnCheckedChangeListener(onCheckedChangeListener)
        view.check_box_pets_animals.setOnCheckedChangeListener(onCheckedChangeListener)
        view.check_box_science_technology.setOnCheckedChangeListener(onCheckedChangeListener)
        view.check_box_sports.setOnCheckedChangeListener(onCheckedChangeListener)
        view.check_box_travel_event.setOnCheckedChangeListener(onCheckedChangeListener)
    }

    private fun saveCheckBoxesStates() {
        val job = Job()
        CoroutineScope(Dispatchers.IO + job).launch {
            val preferences =
                requireContext().getSharedPreferences(
                    PREFERENCES_EXCLUDED_CATEGORIES,
                    Context.MODE_PRIVATE
                )
            val editor = preferences.edit()

            editor.putStringSet(KEY_EXCLUDED_CATEGORIES,
                (activity as MainActivity).homeFragment.excludedCategories)
            editor.apply()
        }
    }

    private val onCheckedChangeListener = CompoundButton.OnCheckedChangeListener { view, checked ->
        when (view.id) {
            R.id.check_box_car -> {
                if (!checked)
                    (activity as MainActivity).homeFragment.excludedCategories
                        .add(ContentCategories.CAR)
                else
                    (activity as MainActivity).homeFragment.excludedCategories
                        .remove(ContentCategories.CAR)
            }
            R.id.check_box_beauty_fashion -> {
                if (!checked)
                    (activity as MainActivity).homeFragment.excludedCategories
                        .add(ContentCategories.BEAUTY_FASHION)
                else
                    (activity as MainActivity).homeFragment.excludedCategories
                        .remove(ContentCategories.BEAUTY_FASHION)
            }
            R.id.check_box_comedy -> {
                if (!checked)
                    (activity as MainActivity).homeFragment.excludedCategories
                        .add(ContentCategories.COMEDY)
                else
                    (activity as MainActivity).homeFragment.excludedCategories
                        .remove(ContentCategories.COMEDY)
            }
            R.id.check_box_education -> {
                if (!checked)
                    (activity as MainActivity).homeFragment.excludedCategories
                        .add(ContentCategories.EDUCATION)
                else
                    (activity as MainActivity).homeFragment.excludedCategories
                        .remove(ContentCategories.EDUCATION)
            }
            R.id.check_box_entertainment -> {
                if (!checked)
                    (activity as MainActivity).homeFragment.excludedCategories
                        .add(ContentCategories.ENTERTAINMENT)
                else
                    (activity as MainActivity).homeFragment.excludedCategories
                        .remove(ContentCategories.ENTERTAINMENT)
            }
            R.id.check_box_family_entertainment -> {
                if (!checked)
                    (activity as MainActivity).homeFragment.excludedCategories
                        .add(ContentCategories.FAMILY_ENTERTAINMENT)
                else
                    (activity as MainActivity).homeFragment.excludedCategories
                        .remove(ContentCategories.FAMILY_ENTERTAINMENT)
            }
            R.id.check_box_movie_animation -> {
                if (!checked)
                    (activity as MainActivity).homeFragment.excludedCategories
                        .add(ContentCategories.MOVIE_ANIMATION)
                else
                    (activity as MainActivity).homeFragment.excludedCategories
                        .remove(ContentCategories.MOVIE_ANIMATION)
            }
            R.id.check_box_food -> {
                if (!checked)
                    (activity as MainActivity).homeFragment.excludedCategories
                        .add(ContentCategories.FOOD)
                else
                    (activity as MainActivity).homeFragment.excludedCategories
                        .remove(ContentCategories.FOOD)
            }
            R.id.check_box_game -> {
                if (!checked)
                    (activity as MainActivity).homeFragment.excludedCategories
                        .add(ContentCategories.GAME)
                else
                    (activity as MainActivity).homeFragment.excludedCategories
                        .remove(ContentCategories.GAME)
            }
            R.id.check_box_know_how_style -> {
                if (!checked)
                    (activity as MainActivity).homeFragment.excludedCategories
                        .add(ContentCategories.KNOW_HOW_STYLE)
                else
                    (activity as MainActivity).homeFragment.excludedCategories
                        .remove(ContentCategories.KNOW_HOW_STYLE)
            }
            R.id.check_box_music -> {
                if (!checked)
                    (activity as MainActivity).homeFragment.excludedCategories
                        .add(ContentCategories.MUSIC)
                else
                    (activity as MainActivity).homeFragment.excludedCategories
                        .remove(ContentCategories.MUSIC)
            }
            R.id.check_box_news_politics -> {
                if (!checked)
                    (activity as MainActivity).homeFragment.excludedCategories
                        .add(ContentCategories.NEWS_POLITICS)
                else
                    (activity as MainActivity).homeFragment.excludedCategories
                        .remove(ContentCategories.NEWS_POLITICS)
            }
            R.id.check_box_non_profit_social_movement -> {
                if (!checked)
                    (activity as MainActivity).homeFragment.excludedCategories
                        .add(ContentCategories.NON_PROFIT_SOCIAL_MOVEMENT)
                else
                    (activity as MainActivity).homeFragment.excludedCategories
                        .remove(ContentCategories.NON_PROFIT_SOCIAL_MOVEMENT)
            }
            R.id.check_box_people_blog -> {
                if (!checked)
                    (activity as MainActivity).homeFragment.excludedCategories
                        .add(ContentCategories.PEOPLE_BLOG)
                else
                    (activity as MainActivity).homeFragment.excludedCategories
                        .remove(ContentCategories.PEOPLE_BLOG)
            }
            R.id.check_box_pets_animals -> {
                if (!checked)
                    (activity as MainActivity).homeFragment.excludedCategories
                        .add(ContentCategories.PETS_ANIMALS)
                else
                    (activity as MainActivity).homeFragment.excludedCategories
                        .remove(ContentCategories.PETS_ANIMALS)
            }
            R.id.check_box_science_technology -> {
                if (!checked)
                    (activity as MainActivity).homeFragment.excludedCategories
                        .add(ContentCategories.SCIENCE_TECHNOLOGY)
                else
                    (activity as MainActivity).homeFragment.excludedCategories
                        .remove(ContentCategories.SCIENCE_TECHNOLOGY)
            }
            R.id.check_box_sports -> {
                if (!checked)
                    (activity as MainActivity).homeFragment.excludedCategories
                        .add(ContentCategories.SPORTS)
                else
                    (activity as MainActivity).homeFragment.excludedCategories
                        .remove(ContentCategories.SPORTS)
            }
            R.id.check_box_travel_event -> {
                if (!checked)
                    (activity as MainActivity).homeFragment.excludedCategories
                        .add(ContentCategories.TRAVEL_EVENT)
                else
                    (activity as MainActivity).homeFragment.excludedCategories
                        .remove(ContentCategories.TRAVEL_EVENT)
            }
        }
    }
}
