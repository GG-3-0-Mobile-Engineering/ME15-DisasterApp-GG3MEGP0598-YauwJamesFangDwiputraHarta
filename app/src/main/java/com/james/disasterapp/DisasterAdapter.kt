package com.james.disasterapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.james.disasterapp.databinding.ItemDisasterBinding
import com.james.disasterapp.model.Properties

class DisasterAdapter(private val listDisaster: List<Properties>) :
    RecyclerView.Adapter<DisasterAdapter.ViewHolder>() {

    private lateinit var itemDisasterBinding: ItemDisasterBinding

    class ViewHolder(var itemDisasterBinding: ItemDisasterBinding) :
        RecyclerView.ViewHolder(itemDisasterBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        itemDisasterBinding =
            ItemDisasterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(itemDisasterBinding)
    }

    override fun getItemCount(): Int {
        return listDisaster.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (photo, title, caption) = listDisaster[position]
        Glide.with(holder.itemView.context).load(photo)
            .apply {
                if (photo == null){
                    placeholder(R.drawable.round_flood_24)
                }
            }
            .into(holder.itemDisasterBinding.ivDisaster)
        holder.itemDisasterBinding.tvTitle.text = title
        holder.itemDisasterBinding.text.text = caption
    }
}