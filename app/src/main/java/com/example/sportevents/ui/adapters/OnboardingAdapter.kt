package com.example.sportevents.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.airbnb.lottie.LottieAnimationView
import com.example.sportevents.databinding.ItemOnboardingBinding

class OnboardingAdapter(
    private val items: List<OnboardingItem>,
    private val onFinish: () -> Unit
) : RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardingViewHolder {
        val binding = ItemOnboardingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OnboardingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OnboardingViewHolder, position: Int) {
        holder.bind(items[position], position == items.lastIndex, onFinish)
    }

    override fun getItemCount(): Int = items.size

    class OnboardingViewHolder(private val binding: ItemOnboardingBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: OnboardingItem, isLast: Boolean, onFinish: () -> Unit) {
            binding.title.text = item.title
            binding.description.text = item.description
            binding.lottieView.setAnimation(item.lottieFile)
            binding.lottieView.playAnimation()
            binding.buttonStart.text = if (isLast) "Начать" else "Далее"
            binding.buttonStart.setOnClickListener {
                if (isLast) onFinish() else {
                    var parent = binding.root.parent
                    while (parent != null && parent !is ViewPager2) {
                        parent = (parent as? View)?.parent
                    }
                    if (parent is ViewPager2) {
                        parent.currentItem = bindingAdapterPosition + 1
                    }
                }
            }
            binding.buttonStart.visibility = View.VISIBLE
        }
    }
} 