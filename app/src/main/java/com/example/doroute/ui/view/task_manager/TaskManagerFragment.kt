package com.example.doroute.ui.view.task_manager

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.doroute.R
import com.example.doroute.data.database.RoomDatabase
import com.example.doroute.data.domain.stores.TaskDbStore
import com.example.doroute.databinding.ActivityMainBinding
import com.example.doroute.databinding.FragmentTaskManagerBinding
import com.example.doroute.data.models.TaskModel
import com.example.doroute.ui.viewmodel.TaskViewModel
import com.example.doroute.ui.viewmodel.TaskViewModelFactory
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.fragment_task_manager.*
import viewLifecycle
import java.util.*
import java.util.UUID.randomUUID


class TaskManagerFragment : Fragment() {

    private val CHANNEL_ID: String = "channel1"
    private lateinit var notificationBuilder:NotificationCompat.Builder
    private lateinit var taskAdapter: TaskRecyclerAdapter
    private lateinit var viewModel: TaskViewModel
    private lateinit var binding: FragmentTaskManagerBinding
    private val main_binding by viewLifecycle{ActivityMainBinding.bind(requireView())}



    // View initialization logic
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?{
        binding = FragmentTaskManagerBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.findViewById<FloatingActionButton>(R.id.fab)?.setOnClickListener{addTask()}
        //View model
        val factory =
            TaskViewModelFactory(
                TaskDbStore(
                    RoomDatabase.getDb(
                        this.requireContext()
                    )
                )
            )
        viewModel = ViewModelProvider(this,factory).get(TaskViewModel::class.java) //.of(this) is deprecated


        //Recycler view
        initRecyclerView()
    }


    private fun initRecyclerView(){

        recycler_view.apply {
            layoutManager = LinearLayoutManager(this@TaskManagerFragment.requireContext())
        }
        viewModel.tasksLiveData.observe(viewLifecycleOwner, Observer {
           taskAdapter=
               TaskRecyclerAdapter(
                   it,
                   this::delete,
                   this::update
               )
            recycler_view.adapter = taskAdapter
        })
        viewModel.retrieveTasks()
    }

    private fun update(task: TaskModel) {
        viewModel.updateTask(task)

    }

    private fun addTask(){
        viewModel.addTask(randomUUID().toString(),"locationId", "stateId","Titlee", "descriptionnn", Calendar.getInstance().time)
    }

    private fun delete(task: TaskModel){
        val builder = AlertDialog.Builder(this@TaskManagerFragment.requireContext())
        with(builder){
            setTitle("Warning!")
            setMessage("Are you sure you want to delete '${task.title}'?")
            //builder.setPositiveButton("OK", DialogInterface.OnClickListener(function = x))

            setPositiveButton(android.R.string.yes) { dialog, which ->

                viewModel.removeTask(task)
                Toast.makeText(this@TaskManagerFragment.requireContext(),
                    "The task '${task.title}' was deleted", Toast.LENGTH_SHORT).show()

            }
            setNegativeButton(android.R.string.no) {_,_->}
            show()
        }
    }


    private fun buildNotification(){
        /*val intent = Intent(this, GoogleMapsFragment::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
        notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
            .setContentTitle("My notification")
            .setContentText("Much longer text that cannot fit one line...")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Much longer text that cannot fit one line..."))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            // Set the intent that will fire when the user taps the notification
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

         */
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Channel name"
            val descriptionText = "Channel Description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            //val notificationManager: NotificationManager =  getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
           // notificationManager.createNotificationChannel(channel)
        }
    }

    private fun notifyNow(){
        buildNotification()
        with(NotificationManagerCompat.from(this@TaskManagerFragment.requireContext())) {
            // notificationId is a unique int for each notification that you must define
            notify(112, notificationBuilder.build())
        }
    }
}