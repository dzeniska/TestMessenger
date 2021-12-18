package com.dzenis_ska.testmessenger.ui.fragments

import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.dzenis_ska.testmessenger.R
import com.dzenis_ska.testmessenger.databinding.FragmentUserNameBinding
import com.dzenis_ska.testmessenger.db.Messages
import com.dzenis_ska.testmessenger.ui.MainApp
import com.dzenis_ska.testmessenger.ui.activities.ViewModelMain
import com.dzenis_ska.testmessenger.ui.adapters.EditMessage
import com.dzenis_ska.testmessenger.ui.adapters.MessageAdapter
import com.dzenis_ska.testmessenger.utils.ImageManager
import com.google.firebase.Timestamp
import com.squareup.picasso.Picasso
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class UserNameFragment : Fragment(R.layout.fragment_user_name) {

    var changeText = 0

    var editMess: Messages.MyMessage? = null

    var uri: Uri? = null

    private var binding: FragmentUserNameBinding? = null

    private var adapter: MessageAdapter? = null

    private val args by navArgs<UserNameFragmentArgs>()

    private var job: Job? = null

    private val viewModelMain: ViewModelMain by activityViewModels {
        ViewModelMain.MainViewModelFactory(
            (context?.applicationContext as MainApp).fbAuth,
            (context?.applicationContext as MainApp).fbFirestore
        )
    }

    private var pickImage: ActivityResultLauncher<String>? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentUserNameBinding.bind(view)

        (activity as AppCompatActivity).setSupportActionBar(binding!!.toolbar)

        (activity as AppCompatActivity).supportActionBar?.title = ""
        initClick()
        initUI()
        initListMess()
        initVM()
        initTextChanged()
        showPhoto()

    }

    private fun initTextChanged() = with(binding!!){

        viewModelMain.readWriting(args){
            when (it){
                ViewModelMain.WRITING-> tvWriting.text = ViewModelMain.WRITING
                ViewModelMain.DELETING-> tvWriting.text = ViewModelMain.DELETING
                ViewModelMain.STOP-> tvWriting.text = ""
            }
        }

        var textLenght = etMessage.text.length
        val timer = object : CountDownTimer(3000, 1000){
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() { viewModelMain.writing(ViewModelMain.STOP) }
        }
        etMessage.addTextChangedListener {
            timer.cancel()
            timer.start()
            val oldLenght = textLenght
            val newLenght = it.toString().length
            val diff = newLenght - oldLenght
            textLenght = newLenght
            when (diff){
                1-> viewModelMain.writing(ViewModelMain.WRITING)
                -1-> viewModelMain.writing(ViewModelMain.DELETING)
                else -> viewModelMain.writing(ViewModelMain.STOP)
            }
        }
    }

    private fun showPhoto() {
        pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { contentUri ->
            val rotation = ImageManager.imageRotationNew(contentUri, requireActivity())
            uri = contentUri
            if(contentUri != null){
                Picasso.get()
                    .load(contentUri)
                    .rotate(rotation.toFloat())
                    .into(binding!!.ivImage)
                binding!!.ivImage.isVisible = true
            }
        }
    }

    private fun initVM() {
        viewModelMain.listMess.observe(viewLifecycleOwner, {
            val listMess = changeClass(it)
            adapter?.messages = listMess
            binding!!.rcViewMessage.scrollToPosition(0)
            isRead()
        })
    }

    private fun isRead() {
        viewModelMain.isRead(args.message)
    }

    private fun changeClass(arrayList: ArrayList<Messages.MyMessage>): List<Messages> {
        val listMess = arrayListOf<Messages>()
        var dateCh = 0
        arrayList.forEach {
            val day = returnDateDay(it.time.toLong())
            if (dateCh != day && dateCh != 0) {
                val mess = Messages.TimeSpace(
                    it.time
                )
                listMess.add(mess)
                dateCh = day
            } else {
                dateCh = day
            }

            if (it.email != args.message.email) {

                listMess.add(it)
            } else {
                val mess = Messages.HisMessage(
                    it.name,
                    it.email,
                    it.message,
                    it.isRead,
                    it.time,
                    it.photoUrl
                )
                listMess.add(mess)
            }
        }
        return listMess
    }

    private fun returnDateDay(timeMillis: Long): Int {
        val sdf = SimpleDateFormat("dd")
        val resultDate = Date(timeMillis)
        return sdf.format(resultDate).toInt()
    }

    private fun initListMess() {
        viewModelMain.getListMess(args)
    }


    private fun initClick() = with(binding!!) {

        fab.setOnClickListener {
            clickSend()
        }

        imbSendPhoto.setOnClickListener {
            pickImage?.launch(MIMETYPE_IMAGES)
        }
        imbSendMessage.setOnClickListener {
            if(changeText == EDIT_MESS) {
                clickEditText()
            } else {
                clickSend()
            }
        }
    }

    private fun clickEditText() = with(binding!!){
        if(etMessage.text.isNotEmpty()) viewModelMain.editText(etMessage.text.toString(), editMess, args){
            if(it){
                etMessage.text.clear()
                changeText = 0
            }
            if(!it) Toast.makeText(context, "Error!", Toast.LENGTH_LONG).show()
        }
    }
    private fun clickDeleteText(message: Messages.MyMessage) {
        viewModelMain.deleteText( message, args){
            if(!it) Toast.makeText(context, "Error!", Toast.LENGTH_LONG).show()
            else Toast.makeText(context, "Deleted!!", Toast.LENGTH_LONG).show()
        }
    }

    private fun clickSend() = with(binding!!){

        if(changeText == EDIT_PHOTO) {
            prBarImage.isVisible = true
            job = CoroutineScope(Dispatchers.Main).launch {
                val byteArray = photoFromImageView()
                viewModelMain.reselectImage(byteArray, editMess!!, args) {
                    prBarImage.isVisible = false
                    ivImage.isVisible = false
                    changeText = 0
                }
            }
        } else {
//            val time = System.currentTimeMillis().toString()
            if(!ivImage.isVisible){
                sendMess(null, null)
            } else {
                prBarImage.isVisible = true
                job = CoroutineScope(Dispatchers.Main).launch {
                    val byteArray = photoFromImageView()
                    viewModelMain.uploadImage(byteArray) {uri, timeStamp ->
                        prBarImage.isVisible = false
                        sendMess(uri, timeStamp)
                    }
                }
            }
        }
    }



    private suspend fun photoFromImageView(): ByteArray = withContext(Dispatchers.IO) {
//        val bitmap = (binding!!.ivImage.drawable as BitmapDrawable).bitmap

        lateinit var byteArray: ByteArray
        ImageManager.imageResize(uri!!, requireActivity()) { byteAr ->
            byteArray = byteAr
            Log.d("!!!contentUri111", "$byteArray")
        }
        Log.d("!!!contentUri222", "$byteArray")
        return@withContext byteArray
    }

    private fun sendMess(photoUrl: Uri?, time: String?) = with(binding!!){
        if(photoUrl == null){
            if (etMessage.text.isNotEmpty()) {
                subSendMess(photoUrl, time)
            }
        } else {
            subSendMess(photoUrl, time)
        }

    }
    private fun subSendMess(photoUrl: Uri?, time: String?) = with(binding!!) {
        prBarMess.isVisible = true
        viewModelMain.sendMessage(etMessage.text.toString(), args, photoUrl, time) {
            if (it) Toast.makeText(context, "Ok!", Toast.LENGTH_LONG).show()
            else Toast.makeText(context, "No send!", Toast.LENGTH_LONG).show()
            prBarMess.isVisible = false
            ivImage.isVisible = false
        }
        etMessage.text.clear()
    }

    private fun initUI() = with(binding!!) {
        tvToolbar.text = args.message.name
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, true)
        adapter = MessageAdapter(object : EditMessage{
            override fun editMessage(message: Messages.MyMessage) {
                editMess = message
                etMessage.setText(message.message)
                changeText = EDIT_MESS
            }

            override fun deleteMessage(message: Messages.MyMessage) {
                clickDeleteText(message)
            }

            override fun editPhoto(message: Messages.MyMessage) {
                changeText = EDIT_PHOTO
                editMess = message
                pickImage?.launch(MIMETYPE_IMAGES)
            }

            override fun deletePhoto(message: Messages.MyMessage) {
                viewModelMain.deletePhoto(message, args){
                    etMessage.text.clear()
                    if (it) Toast.makeText(context, "Photo deleted!", Toast.LENGTH_LONG).show()
                    else Toast.makeText(context, "Error!", Toast.LENGTH_LONG).show()
                }
            }
        })
//        layoutManager.stackFromEnd
        rcViewMessage.layoutManager = layoutManager
        rcViewMessage.adapter = adapter
        val itemAnimator = rcViewMessage.itemAnimator
        if(itemAnimator is DefaultItemAnimator){itemAnimator.supportsChangeAnimations = false}
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModelMain.cancelListenerWriting(args)
        viewModelMain.cancelListenerMess(args)
        viewModelMain.clearListMess()
        binding = null
    }

    companion object {
        const val MIMETYPE_IMAGES = "image/*"

        const val EDIT_MESS = 1
        const val EDIT_PHOTO = 2
    }

}