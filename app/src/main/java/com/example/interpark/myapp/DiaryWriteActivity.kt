package com.example.interpark.myapp

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import com.example.interpark.myapp.DiaryReadActivity
import com.example.interpark.myapp.DiaryWriteActivity
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class DiaryWriteActivity() : BaseActivity(), View.OnClickListener {
    private var mImageCaptureUri: Uri? = null
    private var ivPic: ImageView? = null
    private val id_view = 0
    private var id: String? = null
    private var date: String? = null
    private var no: String? = null
    private var dimgpath: String? = null
    private var content: String? = null
    private var updateFlag = "NON"
    private var absoultePath: String? = null
    private var tvDate: TextView? = null
    private val tvRead: TextView? = null
    private var tvNo: TextView? = null
    private var btSave: Button? = null
    private var btCancel: Button? = null
    private var etWrite: EditText? = null
    var helper: dbHelper? = null
    var db: SQLiteDatabase? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_write)
        val textView = findViewById(R.id.icHeader) as View
        val title = textView.findViewById<View>(R.id.tvHeader) as TextView
        val ibMenu = textView.findViewById<View>(R.id.ibMenu) as ImageButton
        val ibLogout = textView.findViewById<View>(R.id.ibLogout) as ImageButton
        title.text = "일기작성"
        tvDate = findViewById<View>(R.id.tvDate) as TextView
        ivPic = findViewById<View>(R.id.ivPic) as ImageView
        btSave = findViewById<View>(R.id.btSave) as Button
        btCancel = findViewById<View>(R.id.btCancel) as Button
        etWrite = findViewById<View>(R.id.etWrite) as EditText
        tvNo = findViewById<View>(R.id.tvNo) as TextView
        if (MainpageActivity.UPDATEIS === "O") {
            val updatIntent = intent
            updateFlag = updatIntent.extras.getString("FLAG")
            no = updatIntent.extras.getString("no")
            date = updatIntent.extras.getString("date")
            dimgpath = updatIntent.extras.getString("dimgpath")
            content = updatIntent.extras.getString("content")
            id = updatIntent.extras.getString("id")
            absoultePath = dimgpath
            tvNo!!.text = no
            tvDate!!.text = date
            if (dimgpath != null) {
                ivPic!!.setImageURI(Uri.parse(dimgpath))
            }
            if (content != null) {
                etWrite!!.setText(content)
            }
            MainpageActivity.UPDATEIS = "X"
        } else {
            val intent = intent
            id = intent.extras.getString("Id")
            date = intent.extras.getString("date")
            tvDate!!.text = date
        }
        ivPic!!.setOnClickListener(this)
        btSave!!.setOnClickListener(this)
        btCancel!!.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        super.onClick(v)
        when (v.id) {
            R.id.ivPic -> {
                val cameraListener: DialogInterface.OnClickListener = DialogInterface.OnClickListener { dialog, which -> doTakePhotoAction() }
                val albumListener: DialogInterface.OnClickListener = object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface, which: Int) {
                        doTakeAlbumAction()
                    }
                }
                val cancelListener: DialogInterface.OnClickListener = object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface, which: Int) {
                        dialog.dismiss()
                    }
                }
                AlertDialog.Builder(this)
                        .setTitle("이미지 선택")
                        .setPositiveButton("사진촬영", cameraListener)
                        .setNeutralButton("앨범선택", albumListener)
                        .setNegativeButton("취소", cancelListener)
                        .show()
            }
            R.id.btSave -> {
                run {
                    Log.d("저장할때엔", absoultePath + etWrite!!.getText().toString())
                    try {
                        helper = dbHelper(this@DiaryWriteActivity)
                        db = helper!!.getWritableDatabase()
                    } catch (e: SQLiteException) {
                        db = helper!!.getReadableDatabase()
                    }
                    val appDel: MApp = getApplication() as MApp
                    val diary: DiaryVO = DiaryVO()
                    diary.id = (id)!!
                    diary.ddate = (date)!!
                    diary.dcontent = etWrite!!.getText().toString()
                    diary.dimgpath = (absoultePath)!!
                    if ((updateFlag == "UPDATESIGN")) {
                        val sb: StringBuffer = StringBuffer()
                        sb.append("UPDATE diary ")
                        sb.append("SET dimgpath = #imgpath#, dcontent = #content# ")
                        sb.append("WHERE id = #id# AND dno = #no#")
                        var query: String = sb.toString()
                        query = query.replace("#imgpath#", "'" + diary.dimgpath + "'")
                        query = query.replace("#content#", "'" + diary.dcontent + "'")
                        query = query.replace("#no#", tvNo!!.getText().toString())
                        query = query.replace("#id#", "'" + diary.id + "'")
                        db!!.execSQL(query)
                        db!!.close()
                        val intent: Intent = Intent(this@DiaryWriteActivity, DiaryReadActivity::class.java)
                        finish()
                    } else {
                        val sb: StringBuffer = StringBuffer()
                        sb.append("INSERT INTO diary (")
                        sb.append("dno, ddate, dimgpath, dcontent, id )")
                        sb.append("VALUES (?, ?, ?, ?, ?)")
                        db!!.execSQL(sb.toString(), arrayOf<Any?>(null, diary.ddate, diary.dimgpath, diary.dcontent, diary.id))
                        db!!.close()
                        finish()
                    }
                    MainpageActivity.UPDATEIS = "X"
                }
                run { finish() }
            }
            R.id.btCancel -> {
                finish()
            }
        }
    }

    fun doTakePhotoAction() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val url = "tmp_" + System.currentTimeMillis().toString() + ".jpg"
        mImageCaptureUri = Uri.fromFile(File(Environment.getExternalStorageDirectory(), url))
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri)
        startActivityForResult(intent, PICK_FROM_CAMERA)
    }

    fun doTakeAlbumAction() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = MediaStore.Images.Media.CONTENT_TYPE
        startActivityForResult(intent, PICK_FROM_ALBUM)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK) {
            return
        }
        when (requestCode) {
            PICK_FROM_ALBUM -> {
                run {
                    mImageCaptureUri = data.getData()
                    Log.d("저장경로", data.getData().getPath().toString()) //mImageCaptureUri.getPath() 오류
                }
                run {
                    val intent: Intent = Intent("com.android.camera.action.CROP")
                    intent.setDataAndType(mImageCaptureUri, "image/*")
                    intent.putExtra("outputX", 200)
                    intent.putExtra("outputY", 200)
                    intent.putExtra("aspectX", 1)
                    intent.putExtra("aspectY", 1)
                    intent.putExtra("scale", true)
                    intent.putExtra("return-data", true)
                    startActivityForResult(intent, CROP_FROM_IMAGE)
                }
            }
            PICK_FROM_CAMERA -> {
                val intent = Intent("com.android.camera.action.CROP")
                intent.setDataAndType(mImageCaptureUri, "image/*")
                intent.putExtra("outputX", 200)
                intent.putExtra("outputY", 200)
                intent.putExtra("aspectX", 1)
                intent.putExtra("aspectY", 1)
                intent.putExtra("scale", true)
                intent.putExtra("return-data", true)
                startActivityForResult(intent, CROP_FROM_IMAGE)
            }
            CROP_FROM_IMAGE -> {
                if (resultCode != RESULT_OK) {
                    return
                }
                val extras = data.extras
                val filePath = Environment.getExternalStorageDirectory().absolutePath + "/Diary/" + System.currentTimeMillis() + ".jpg"
                if (extras != null) {
                    val photo = extras.getParcelable<Bitmap>("data")
                    ivPic!!.setImageBitmap(photo)
                    storeCropImage(photo, filePath)
                    absoultePath = filePath
                }
                val f = File(mImageCaptureUri!!.path)
                if (f.exists()) {
                    f.delete()
                }
            }
        }
    }

    private fun storeCropImage(bitmap: Bitmap, filePath: String) {
        val dirPath = Environment.getExternalStorageDirectory().absolutePath + "/Diary"
        val directory_Diary = File(dirPath)
        if (!directory_Diary.exists()) {
            directory_Diary.mkdir()
        }
        val copyFile = File(filePath)
        var out: BufferedOutputStream? = null
        try {
            copyFile.createNewFile()
            out = BufferedOutputStream(FileOutputStream(copyFile))
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(copyFile)))
            out.flush()
            out.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun onBackPressed() {
        val alertDialogBuilder = AlertDialog.Builder(this@DiaryWriteActivity)
        alertDialogBuilder
                .setMessage("페이지를 나가시겠습니까?")
                .setCancelable(false)
                .setPositiveButton("예", object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface, which: Int) {
                        finish()
                    }
                })
                .setNegativeButton("아니요", object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface, which: Int) {
                        dialog.cancel()
                    }
                })
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    companion object {
        private val PICK_FROM_CAMERA = 0
        private val PICK_FROM_ALBUM = 1
        private val CROP_FROM_IMAGE = 2
    }
}