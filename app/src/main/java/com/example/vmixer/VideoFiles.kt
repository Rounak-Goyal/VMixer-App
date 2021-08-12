package com.example.vmixer

data class VideoFiles(
     val id: String?,
     val path: String,
     val title: String,
     val fileName: String,
     val size: String?,
     val dateAdded: String,
     val duration: String?
) {
}