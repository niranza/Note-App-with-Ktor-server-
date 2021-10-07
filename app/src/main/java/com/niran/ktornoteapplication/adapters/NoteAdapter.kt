package com.niran.ktornoteapplication.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.niran.ktornoteapplication.R
import com.niran.ktornoteapplication.databinding.ItemNoteBinding
import com.niran.ktornoteapplication.dataset.models.Note
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class NoteAdapter : ListAdapter<Note, NoteAdapter.NoteViewHolder>(NoteCallBack) {

    class NoteViewHolder private constructor(
        private val binding: ItemNoteBinding,
        private val onItemClickListener: ((Note) -> Unit)?
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(note: Note) = binding.apply {
            tvTitle.text = note.title
            ivSynced.apply {
                val drawableId = if (note.isSynced) R.drawable.ic_check else R.drawable.ic_cross
                setImageResource(drawableId)
            }
            tvSynced.apply {
                val stringId = if (note.isSynced) R.string.synced else R.string.note_synced
                text = context.getString(stringId)
            }
            tvDate.apply {
                val sdf = SimpleDateFormat("dd.MM.yy, HH:mm", Locale.getDefault())
                text = sdf.format(note.date)
            }
            viewNoteColor.apply {
                ResourcesCompat.getDrawable(resources, R.drawable.circle_shape, null)?.let {
                    val wrappedDrawable = DrawableCompat.wrap(it)
                    val color = Color.parseColor("#${note.color}")
                    DrawableCompat.setTint(wrappedDrawable, color)
                    background = wrappedDrawable
                }
            }
            itemView.apply {
                setOnClickListener { onItemClickListener?.let { it((note)) } }
            }
        }

        companion object {
            fun create(parent: ViewGroup, onItemClickListener: ((Note) -> Unit)?): NoteViewHolder {
                val binding =
                    ItemNoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return NoteViewHolder(binding, onItemClickListener)
            }
        }
    }

    private var onItemClickListener: ((Note) -> Unit)? = null

    fun setOnClickListener(onItemClick: (Note) -> Unit) {
        onItemClickListener = onItemClick
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder =
        NoteViewHolder.create(parent, onItemClickListener)

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    object NoteCallBack : DiffUtil.ItemCallback<Note>() {

        override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean =
            newItem.id == oldItem.id

        override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean =
            newItem.hashCode() == oldItem.hashCode()
    }
}