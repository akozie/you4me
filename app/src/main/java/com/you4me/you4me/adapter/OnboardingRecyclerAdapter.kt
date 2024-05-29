package com.you4me.you4me.adapter

import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.you4me.you4me.R
import com.you4me.you4me.databinding.OnboardingScreenBinding

class OnboardingRecyclerAdapter(private val context: Fragment) :
    RecyclerView.Adapter<OnboardingRecyclerAdapter.MyViewHolder>() {
    private val images =
        listOf(R.drawable.onboarding_img1, R.drawable.onboarding_img2, R.drawable.onboarding_img3)
    private val texts =
        listOf(R.string.onboarding_txt1, R.string.onboarding_txt2, R.string.onboarding_txt3)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val v = OnboardingScreenBinding.inflate(context.layoutInflater, parent, false)
        return MyViewHolder(v)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.binding.onboardingImg.setImageResource(images[position])
        holder.binding.onboardingTxt.text = context.getString(texts[position])
    }

    override fun getItemCount() = 3


    class MyViewHolder(val binding: OnboardingScreenBinding) : RecyclerView.ViewHolder(binding.root)
}