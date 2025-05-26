package com.example.roadsignclassifierapp

import android.graphics.BitmapFactory
import android.icu.text.SimpleDateFormat
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.roadsignclassifierapp.databinding.ItemHistoryBinding
import java.util.Date
import java.util.Locale

class HistoryAdapter(
    private val onLongClick: (RecognitionEntry) -> Unit
) : ListAdapter<RecognitionEntry, HistoryAdapter.HistoryViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class HistoryViewHolder(private val binding: ItemHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(entry: RecognitionEntry) {
            binding.categoryText.text = entry.category
            binding.probabilityText.text = "Вероятность: ${entry.probability}%"
            binding.timestampText.text = SimpleDateFormat("HH:mm:ss dd.MM.yyyy", Locale.getDefault())
                .format(Date(entry.timestamp))
            binding.imageView.setImageBitmap(BitmapFactory.decodeByteArray(entry.imageData, 0, entry.imageData.size))

            binding.root.setOnLongClickListener {
                onLongClick(entry)
                true
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<RecognitionEntry>() {
        override fun areItemsTheSame(oldItem: RecognitionEntry, newItem: RecognitionEntry) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: RecognitionEntry, newItem: RecognitionEntry) =
            oldItem == newItem
    }
}
