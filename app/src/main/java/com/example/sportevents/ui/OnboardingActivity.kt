package com.example.sportevents.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.airbnb.lottie.LottieAnimationView
import com.example.sportevents.MainActivity
import com.example.sportevents.databinding.ActivityOnboardingBinding
import com.example.sportevents.ui.adapters.OnboardingAdapter
import com.example.sportevents.ui.adapters.OnboardingItem

class OnboardingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOnboardingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val onboardingItems = listOf(
            OnboardingItem(
                title = "Быстрый поиск мероприятий",
                description = "Находите спортивные события рядом с вами за пару секунд!",
                lottieFile = "search_event.json"
            ),
            OnboardingItem(
                title = "Создавайте свои события",
                description = "Организуйте встречи для друзей или создавайте масштабные мероприятия!",
                lottieFile = "create_event.json"
            ),
            OnboardingItem(
                title = "Удобная регистрация и отслеживание",
                description = "Регистрируйтесь на события и следите за своим статусом в один клик.",
                lottieFile = "registration.json"
            ),
            OnboardingItem(
                title = "Просмотр участников",
                description = "Создатель события может легко просматривать и управлять списком участников.",
                lottieFile = "participants.json"
            )
        )

        val adapter = OnboardingAdapter(onboardingItems) { finishOnboarding() }
        binding.viewPager.adapter = adapter
        binding.dotsIndicator.attachTo(binding.viewPager)
    }

    private fun finishOnboarding() {
        getSharedPreferences("onboarding", Context.MODE_PRIVATE)
            .edit().putBoolean("finished", true).apply()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    companion object {
        fun shouldShow(context: Context): Boolean {
            return !context.getSharedPreferences("onboarding", Context.MODE_PRIVATE)
                .getBoolean("finished", false)
        }
    }
} 