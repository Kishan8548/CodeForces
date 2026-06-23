package com.example.codeforces.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.codeforces.databinding.ItemFriendBinding
import com.example.codeforces.models.User
import com.example.codeforces.utils.RankUtils
import com.example.codeforces.utils.ThemeManager

class FriendsAdapter(
    private val onClick: (User) -> Unit,
    private val onAddFriend: ((User) -> Unit)? = null
) : RecyclerView.Adapter<FriendsAdapter.ViewHolder>() {

    private val friends = mutableListOf<User>()
    // A flag or list to know if they are friends or search results
    // For now we assume if onAddFriend is non-null it might be a search result,
    // but a cleaner way is just passing a boolean in the data class. Let's just track it by id.
    private var isSearchResult = false

    inner class ViewHolder(val binding: ItemFriendBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFriendBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = friends[position]
        val ctx = holder.binding.root.context

        val (rankLabel, _) = RankUtils.getRankInfo(user.rating)
        val friendColor = RankUtils.getRankColor(ctx, user.rating)

        holder.binding.apply {
            tvFriendHandle.text = user.handle
            tvFriendHandle.setTextColor(friendColor)
            
            // Verified tick colors
            ivVerified.imageTintList = ColorStateList.valueOf(friendColor)

            tvFriendRank.text = rankLabel.uppercase()
            // No background on the rank text anymore in this new design
            
            // Rating
            tvFriendRating.text = "${user.rating ?: "UNRATED"}"
            tvFriendRating.setTextColor(friendColor)

            // Avatar
            if (!user.titlePhoto.isNullOrBlank()) {
                Glide.with(ctx)
                    .load(user.titlePhoto)
                    .placeholder(com.example.codeforces.R.drawable.ic_profile)
                    .centerCrop()
                    .into(friendAvatar)
            }

            // Avatar border color
            (friendAvatar.parent as View).setBackgroundColor(friendColor)

            if (isSearchResult) {
                layoutRating.visibility = View.GONE
                layoutAddAction.visibility = View.VISIBLE
                
                val theme = ThemeManager.current
                viewAddShadow.setBackgroundColor(theme.primaryDim)
                ivAddFriendIcon.setBackgroundColor(theme.primary)
                ivAddFriendIcon.imageTintList = ColorStateList.valueOf(theme.onPrimary)
                
                layoutAddAction.setOnClickListener { onAddFriend?.invoke(user) }
                root.setOnClickListener(null)
            } else {
                layoutRating.visibility = View.VISIBLE
                layoutAddAction.visibility = View.GONE
                root.setOnClickListener { onClick(user) }
            }
        }
    }

    override fun getItemCount() = friends.size

    fun submitList(list: List<User>, isSearch: Boolean = false) {
        isSearchResult = isSearch
        friends.clear()
        friends.addAll(list)
        notifyDataSetChanged()
    }
}
