package com.example.habita.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.habita.R

class ImageSliderAdapter(private val images: List<String>) : RecyclerView.Adapter<ImageSliderAdapter.ImageViewHolder>() {

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imgSliderItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_slider_image, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imagePath = images[position]
        try {
            val resId = imagePath.toIntOrNull()
            if (resId != null) {
                holder.imageView.setImageResource(resId)
            } else {
                holder.imageView.setImageURI(Uri.parse(imagePath))
            }
        } catch (e: Exception) {
            holder.imageView.setImageResource(R.mipmap.ic_launcher)
        }
    }

    override fun getItemCount(): Int = images.size
}