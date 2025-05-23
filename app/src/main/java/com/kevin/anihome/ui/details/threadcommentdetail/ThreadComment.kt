package com.kevin.anihome.ui.details.threadcommentdetail

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun ThreadCommentScreen(
    commentId: Int,
    navigateBack: () -> Unit,
) {
    Text(text = "Showing thread comment with comment id $commentId")
}
