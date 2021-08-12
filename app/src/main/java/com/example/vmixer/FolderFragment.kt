package com.example.vmixer

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vmixer.MainActivity.Companion.folderList
import com.example.vmixer.MainActivity.Companion.videoFiles


class FolderFragment : Fragment() {

    lateinit var folderAdapter: FolderAdapter
    lateinit var recyclerView: RecyclerView
    lateinit var layoutManager: RecyclerView.LayoutManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_folder, container, false)

        recyclerView = view.findViewById(R.id.folderRV)
        if (folderList != null && folderList.size > 0 && videoFiles != null){
            //set adapter
            folderAdapter = FolderAdapter(activity as Context , folderList, videoFiles)
            recyclerView.adapter = folderAdapter
            //set layoutManager
            layoutManager = LinearLayoutManager(activity as Context, RecyclerView.VERTICAL,false)
            recyclerView.layoutManager = layoutManager

        }


        return view
    }


}