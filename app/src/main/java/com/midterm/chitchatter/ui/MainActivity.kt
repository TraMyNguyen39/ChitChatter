package com.midterm.chitchatter.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.LayoutParams
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.midterm.chitchatter.R
import com.midterm.chitchatter.data.model.Message
import com.midterm.chitchatter.databinding.ActivityMainBinding
import com.midterm.chitchatter.ui.account.AccountViewModel
import com.midterm.chitchatter.ui.home.HomeViewModel
import com.midterm.chitchatter.utils.ChitChatterUtils


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var cardView: CardView

    private var backPressedTime: Long = 0
    private var connectedRef: DatabaseReference? = null
    private var firestore: FirebaseFirestore? = null
    private var auth: FirebaseAuth? = null
    private val database = FirebaseDatabase.getInstance()
    private lateinit var messagePath: String
    private lateinit var receiver: String

    private lateinit var viewModel: HomeViewModel

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            postNotification()
        } else {
            showMessage(R.string.message_denied, Snackbar.LENGTH_LONG)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBackButton()
        setupNavigation()
        askNotificationPermission()

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()


        // Theo dõi trạng thái kết nối mạng
        connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected")
        connectedRef!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val connected = snapshot.getValue(Boolean::class.java)!!
                updateOnlineStatus(
                    connected,
                    ChitChatterUtils.getCurrentAccount(this@MainActivity) ?: "",
                    ChitChatterUtils.token ?: ""
                )
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("OnlineStatus", "Listener was cancelled")
            }
        })

        receiver =
            (ChitChatterUtils.getCurrentAccount(this@MainActivity) ?: "").substringBefore('@')
        // Đường dẫn để lắng nghe tất cả các tin nhắn đến cho người nhận
        messagePath = "messages/$receiver"

        // Lắng nghe thay đổi tại đường dẫn messagePath
//        listenForMessages()
    }

    private fun setupBackButton() {
        onBackPressedDispatcher.addCallback {
            if (backPressedTime + 2000 > System.currentTimeMillis()) {
                finish()
            } else {
                Toast.makeText(baseContext, "Nhấn back một lần nữa để thoát", Toast.LENGTH_SHORT)
                    .show()
            }
            backPressedTime = System.currentTimeMillis()
        }
    }

    override fun onStart() {
        super.onStart()
        updateOnlineStatus(
            true,
            ChitChatterUtils.getCurrentAccount(this@MainActivity) ?: "",
            ChitChatterUtils.token ?: ""
        )
    }

    override fun onStop() {
        super.onStop()
        updateOnlineStatus(
            false,
            ChitChatterUtils.getCurrentAccount(this@MainActivity) ?: "",
            ChitChatterUtils.token ?: ""
        )
    }

    private fun updateOnlineStatus(isOnline: Boolean, email: String, token: String) {
        val accountsRef = firestore!!.collection("accounts").document(email)

        accountsRef.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {
                val tokens = documentSnapshot.get("tokens") as? List<Map<String, Any>>
                tokens?.let { tokenList ->
                    val updatedTokens = tokenList.map { tokenMap ->
                        if (tokenMap["token"] == token) {
                            tokenMap.toMutableMap().apply { put("isOnline", isOnline) }
                        } else {
                            tokenMap
                        }
                    }
                    accountsRef.update("tokens", updatedTokens).addOnSuccessListener {
                        Log.d("OnlineStatus", "Token status updated for email: $email")
                    }.addOnFailureListener { e ->
                        Log.w(
                            "OnlineStatus",
                            "Error updating token status for email: $email",
                            e
                        )
                    }
                }
            } else {
                Log.d("OnlineStatus", "Email not found: $email")
            }
        }.addOnFailureListener { e ->
            Log.w("OnlineStatus", "Error finding email: $email", e)
        }
    }

    private fun setupNavigation() {
        setSupportActionBar(binding.includeMain.toolbarMain)
        val navHostFragment: NavHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.home_fragment, R.id.callsFragment, R.id.contactsFragment),
            drawerLayout = binding.drawerLayout
        )

        setSupportActionBar(binding.includeMain.toolbarMain)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.includeMain.toolbarMain.setupWithNavController(navController, appBarConfiguration)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.message_fragment -> {
                    binding.includeMain.toolbarMain.setNavigationIcon(R.drawable.ic_back)
                }
                R.id.account_fragment, R.id.edit_profile_fragment -> {
                    binding.includeMain.toolbarMain.setNavigationIcon(R.drawable.ic_back_w)
                }
                R.id.home_fragment, R.id.callsFragment, R.id.contactsFragment -> {
                    binding.includeMain.toolbarMain.setNavigationIcon(R.drawable.ic_menu)
                }
            }
        }

        setupToolbar()
        binding.includeMain.bottomNav.setupWithNavController(navController)

    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setupToolbar() {
        cardView = CardView(this)

        val cardParams = Toolbar.LayoutParams(
            resources.getDimensionPixelSize(R.dimen.avatar_w_md),
            resources.getDimensionPixelSize(R.dimen.avatar_h_md)
        )
        cardParams.gravity = Gravity.END
        cardParams.marginEnd = resources.getDimensionPixelSize(R.dimen.spacing_lg)
        cardView.layoutParams = cardParams
        cardView.cardElevation = resources.getDimension(R.dimen.card_elevation)
        cardView.radius = resources.getDimension(R.dimen.card_corner_radius)

        val imageView = ImageView(this)
        imageView.id = R.id.iv_avatar_id

        val accountAvt = ChitChatterUtils.getCurrentAccountAvt(this)
        if (accountAvt != null) {
            val bucketUrl = "gs://chitchatter-b97bf.appspot.com/avatars/"

            val storage: FirebaseStorage = FirebaseStorage.getInstance()
            val storageRef: StorageReference = storage.getReferenceFromUrl(bucketUrl)
            val imageRef: StorageReference = storageRef.child(accountAvt)
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                Glide.with(imageView).load(uri).error(R.drawable.chitchatter)
                    .into(imageView)
            }.addOnFailureListener { exception ->
                Glide.with(imageView).load(R.drawable.chitchatter).error(R.drawable.chitchatter)
                    .into(imageView)
                // Xử lý lỗi nếu có
                Log.e("FirebaseStorage", "Failed to get download URL", exception)
            }
        } else {
            Glide.with(imageView).load(R.drawable.chitchatter).error(R.drawable.chitchatter)
                .into(imageView)
        }
//        imageView.setImageResource(R.drawable.android)
        imageView.layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT
        )
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP

        cardView.addView(imageView)
        cardView.setOnClickListener { _ -> setupMenu() }

        binding.includeMain.toolbarMain.addView(cardView)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setupMenu() {
        try {
            val mInflater = applicationContext
                .getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val layout: View = mInflater.inflate(R.layout.nav_account, null)
            layout.measure(
                View.MeasureSpec.UNSPECIFIED,
                View.MeasureSpec.UNSPECIFIED
            )
            val mDropdown = PopupWindow(
                layout, FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT, true
            )

            //If you want to add any listeners to your textviews, these are two //textviews.
            val itemLogout = layout.findViewById<View>(R.id.action_logout) as TextView
            val itemProfile = layout.findViewById<View>(R.id.action_profile) as TextView
            val itemNotification = layout.findViewById<View>(R.id.action_noti) as TextView

            itemLogout.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_logout, 0, 0, 0)
            itemProfile.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_user_pro, 0, 0, 0)
            itemNotification.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_noti, 0, 0, 0
            )

            itemLogout.setOnClickListener {
                logout()
            }
            itemProfile.setOnClickListener { _ ->
                val args = Bundle()
                args.putString("email", null)

                navController.currentDestination?.let {
                    navController.popBackStack(it.id, true)
                }
                navController.navigate(R.id.nav_contacts)
                navController.navigate(R.id.account_fragment, args)
                mDropdown.dismiss()

            }
            itemNotification.setOnClickListener {
                mDropdown.dismiss()
            }

            val background =
                resources.getDrawable(android.R.drawable.editbox_background_normal)
            mDropdown.setBackgroundDrawable(background)
            mDropdown.showAsDropDown(cardView, 5, 5)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun logout() {
        val sharedPref = getSharedPreferences(
            getString(R.string.preference_account_key), Context.MODE_PRIVATE
        )
        val emailKey = getString(R.string.preference_email_key)
        val currentEmail = sharedPref.getString(emailKey, null)
        if (currentEmail != null) {
            viewModel.removeToken(currentEmail)
        }

        sharedPref.edit().apply {
            clear()
            apply()
        }

        val intent = Intent(this, AuthenticationActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this, android.Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    postNotification()
                }

                ActivityCompat.shouldShowRequestPermissionRationale(
                    this, android.Manifest.permission.POST_NOTIFICATIONS
                ) -> {
                    showMessage(R.string.message_permission_prompt, Snackbar.LENGTH_LONG, true)
                }

                else -> {
                    requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    private fun postNotification() {
        // post notification
    }

    private fun showMessage(messageId: Int, duration: Int, showAction: Boolean = false) {
        val snackBar = Snackbar.make(binding.root, messageId, duration)
        if (showAction && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            snackBar.setAction("OK") {
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
            snackBar.setAction("No thank") {
                showMessage(R.string.message_permission_denied, Snackbar.LENGTH_LONG)
            }
        }
        snackBar.show()
    }

    fun hideNavigation() {
        val toolbar: Toolbar = findViewById(R.id.toolbar_main)
        toolbar.visibility = View.GONE
        val bottomNav: BottomNavigationView = findViewById(R.id.bottom_nav)
        bottomNav.visibility = View.GONE
    }

    fun showNavigation() {
        val toolbar: Toolbar = findViewById(R.id.toolbar_main)
        toolbar.visibility = View.VISIBLE
        val bottomNav: BottomNavigationView = findViewById(R.id.bottom_nav)
        bottomNav.visibility = View.VISIBLE
    }

    private fun listenForMessages() {
        Log.d("MessagePath", messagePath);
        val dbRef = database.getReference(messagePath)

        dbRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                // Xử lý dữ liệu khi có tin nhắn mới
                Log.d("onChildAdded", snapshot.toString());
                val newMessage: Message = ChitChatterUtils.convertSnapshotToMessage(
                    snapshot, ChitChatterUtils.getCurrentAccount(this@MainActivity) ?: ""
                )
                Log.d("newMessage", newMessage.toString())
//                val message = snapshot.getValue(Message::class.java)
//                message?.let {
//                    // Hiển thị tin nhắn lên giao diện người dùng (UI)
//                    displayMessage(it)
//                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                Log.d("onChildChanged", snapshot.toString());
                val newMessage: Message = ChitChatterUtils.convertSnapshotToMessage(
                    snapshot, ChitChatterUtils.getCurrentAccount(this@MainActivity) ?: ""
                )
                Log.d("newMessage", newMessage.toString())
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                Log.d("onChildRemoved", snapshot.toString());

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                Log.d("onChildMoved", snapshot.toString());

            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("onCancelled", "Failed to read value.", error.toException())
            }

            // Các phương thức khác của ChildEventListener có thể được bỏ qua
        })
    }

    private fun displayMessage(message: Message) {
        // Hiển thị tin nhắn lên giao diện người dùng (UI)
        // Bạn có thể cập nhật TextView, RecyclerView, v.v.
        Log.d("MainActivity", "Message: ${message.content} from ${message.sender}")
        // Ví dụ: textViewMessage.text = "${message.sender}: ${message.content}"
    }
}