package com.example.maskdetector

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.example.maskdetector.ml.MaskDetectorModel
import com.example.maskdetector.ui.DetectorAdapter
import com.example.maskdetector.viewmodel.DetectorListViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.tensorflow.lite.support.image.TensorImage
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


private const val REQUEST_IMAGE_CAPTURE = 1

/**
 * A simple [Fragment] subclass.
 */
class ImageFragment : Fragment() {
    private lateinit var imageView: ImageView
    private lateinit var captureButton: FloatingActionButton
    lateinit var currentPhotoPath: String

    private val Fragment.packageManager get() = activity?.packageManager

    // Contains the recognition result. Since it is a viewModel, it will survive screen rotations
    private val detectViewModel: DetectorListViewModel by activityViewModels()

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_image, container, false)

        // Views attachment
        val resultRecyclerView by lazy {
            view.findViewById<RecyclerView>(R.id.detectorResults) // Display the result of analysis
        }

        imageView = view.findViewById(R.id.imageView)
        captureButton = view.findViewById(R.id.floatingActionButton)
        captureButton.show()

        captureButton.setOnClickListener { v ->
            dispatchTakePictureIntent()
        }

        // Request for camera runtime permission
        if (context?.let { ContextCompat.checkSelfPermission(it, android.Manifest.permission.CAMERA) } == -1) {
            activity?.let { ActivityCompat.requestPermissions(it, arrayOf(android.Manifest.permission.CAMERA), REQUEST_IMAGE_CAPTURE) }
        }

        val viewAdapter = DetectorAdapter(requireActivity().applicationContext)
        resultRecyclerView.adapter = viewAdapter

        // Disable recycler view animation to reduce flickering, otherwise items can move, fade in
        // and out as the list change
        resultRecyclerView.itemAnimator = null

        // Attach an observer on the LiveData field of recognitionList
        // This will notify the recycler view to update every time when a new list is set on the
        // LiveData field of recognitionList.
        detectViewModel.detectionList.observe(viewLifecycleOwner,
                Observer {
                    viewAdapter.submitList(it)
                }
        )

        return view
    }

    override fun onDestroyView() {
        detectViewModel.clearData()
        super.onDestroyView()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == AppCompatActivity.RESULT_OK) {
            //val imageBitmap = data?.extras?.get("data") as Bitmap
            setPic()
        }
    }

    private fun setPic() {
        val targetW: Int = imageView.width
        val targetH: Int = imageView.height

        val bmOptions = BitmapFactory.Options().apply {
            // Get the dimensions of the bitmap
            inJustDecodeBounds = true

            BitmapFactory.decodeFile(currentPhotoPath, this)

            val photoW: Int = outWidth
            val photoH: Int = outHeight

            // Determine how much to scale down the image
            val scaleFactor: Int = Math.max(1, Math.min(photoW / targetW, photoH / targetH))

            // Decode the image file into a Bitmap sized to fill the View
            inJustDecodeBounds = false
            inSampleSize = scaleFactor
            inPurgeable = true
        }
        BitmapFactory.decodeFile(currentPhotoPath, bmOptions)?.also { bitmap ->
            analyzeImage(bitmap)
            imageView.setImageBitmap(bitmap)
        }
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            packageManager?.let {
                takePictureIntent.resolveActivity(it)?.also {
                    // Create the File where the photo should go
                    val photoFile: File? = try {
                        createImageFile()
                    } catch (ex: IOException) {
                        // Error occurred while creating the File
                            print("Error: $ex")
                        null
                    }
                    // Continue only if the File was successfully created
                    photoFile?.also {
                        val photoURI: Uri = FileProvider.getUriForFile(
                                requireActivity(),
                                "com.example.android.fileprovider",
                                it
                        )
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                    }
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())

        val storageDir: File = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
                "JPEG_${timeStamp}_", /* prefix */
                ".jpg", /* suffix */
                storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    private fun analyzeImage(image: Bitmap) {
        val detectorModel = activity?.let { MaskDetectorModel.newInstance(it.applicationContext) }

        val tfImage = TensorImage.fromBitmap(image)

        val outputs = detectorModel?.process(tfImage)
                ?.probabilityAsCategoryList?.apply {
                    sortByDescending { it.score }
                }

        if (outputs != null) {
            detectViewModel.updateStaticData(outputs)
        }

        print("Finished")
        captureButton.hide()
    }


}