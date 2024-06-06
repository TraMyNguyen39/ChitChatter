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
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.PopupMenu
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
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.midterm.chitchatter.ChitChatterApplication
import com.midterm.chitchatter.R
import com.midterm.chitchatter.data.model.DataUpdateStatus
import com.midterm.chitchatter.databinding.ActivityMainBinding
import com.midterm.chitchatter.ui.home.HomeViewModel
import com.midterm.chitchatter.ui.home.HomeViewModelFactory
import com.midterm.chitchatter.utils.ChitChatterUtils


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var cardView: CardView
    private lateinit var redDotView: View
    private lateinit var txtCountUnreadNoti: TextView

    private var backPressedTime: Long = 0
    private var connectedRef: DatabaseReference? = null
    private var firestore: FirebaseFirestore? = null
    private var auth: FirebaseAuth? = null
    private lateinit var messagePath: String
    private lateinit var receiver: String
    private var userEmail: String? = null

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
        userEmail = ChitChatterUtils.getCurrentAccount(this)
        if (userEmail == null) {
            finish()
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBackButton()
        setupNavigation()
        setupViewModel()
        askNotificationPermission()

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val repository = (this.application as ChitChatterApplication).repository

        viewModel = ViewModelProvider(
            this,
            HomeViewModelFactory(repository)
        )[HomeViewModel::class.java]


        // Theo dõi trạng thái kết nối mạng
        connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected")
        connectedRef!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val connected = snapshot.getValue(Boolean::class.java)!!
                viewModel.updateOnlineStatus(
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

    @SuppressLint("SetTextI18n")
    private fun setupViewModel() {
        val repository = (application as ChitChatterApplication).repository
        viewModel = ViewModelProvider(this, HomeViewModelFactory(repository))[HomeViewModel::class.java]

        viewModel.countUnreadNotifications(userEmail!!) {
            txtCountUnreadNoti.text = "($it)"
            showNotificationIndicator(it != 0)
        }
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
        val email = ChitChatterUtils.getCurrentAccount(this@MainActivity) ?: ""
        if (email.isNotBlank()) {
            viewModel.updateOnlineStatus(true, email, ChitChatterUtils.token ?: "")
        } else {
            Log.w("MainActivity", "Invalid email: $email")
        }
    }

    override fun onResume() {
        super.onResume()
        val email = ChitChatterUtils.getCurrentAccount(this@MainActivity) ?: ""
        if (email.isNotBlank()) {
            viewModel.updateOnlineStatus(false, email, ChitChatterUtils.token ?: "")
        } else {
            Log.w("MainActivity", "Invalid email: $email")
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
                R.id.account_fragment, R.id.edit_profile_fragment, R.id.contact_request_fragment-> {
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
        // Initialize FrameLayout to hold CardView and red dot
        val frameLayout = FrameLayout(this)
        val frameParams = Toolbar.LayoutParams(
            resources.getDimensionPixelSize(R.dimen.avatar_w_md),
            resources.getDimensionPixelSize(R.dimen.avatar_h_md)
        )
        frameParams.gravity = Gravity.END
        frameParams.marginEnd = resources.getDimensionPixelSize(R.dimen.spacing_lg)
        frameLayout.layoutParams = frameParams

        // Initialize CardView
        cardView = CardView(this)
        val cardParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        cardView.layoutParams = cardParams
        cardView.cardElevation = resources.getDimension(R.dimen.card_elevation)
        cardView.radius = resources.getDimension(R.dimen.card_corner_radius)

        // Initialize ImageView
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
                // Handle error if any
                Log.e("FirebaseStorage", "Failed to get download URL", exception)
            }
        } else {
            Glide.with(imageView).load(R.drawable.chitchatter).error(R.drawable.chitchatter)
                .into(imageView)
        }
        imageView.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT
        )
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP

        // Add ImageView to CardView
        cardView.addView(imageView)

        // Initialize red dot view
        redDotView = View(this)
        redDotView.setBackgroundResource(R.drawable.ic_red_dot) // Use a drawable resource for the red dot
        val dotSize = resources.getDimensionPixelSize(R.dimen.dot_size) // Define the size in dimens
        val dotParams = FrameLayout.LayoutParams(dotSize, dotSize)
        dotParams.gravity = Gravity.END or Gravity.TOP
        dotParams.marginEnd = resources.getDimensionPixelSize(R.dimen.dot_margin_end)
        dotParams.topMargin = resources.getDimensionPixelSize(R.dimen.dot_margin_top)
        redDotView.layoutParams = dotParams
        redDotView.visibility = View.GONE

        // Add red dot view to FrameLayout
        frameLayout.addView(redDotView)
        // Add CardView to FrameLayout
        frameLayout.addView(cardView)

        // Add FrameLayout to Toolbar
        binding.includeMain.toolbarMain.addView(frameLayout)

        val popupMenu = setupMenu()
        popupMenu?.dismiss()
        // Optionally set up a click listener for the CardView
        cardView.setOnClickListener {
            popupMenu?.showAsDropDown(cardView, 5, 5)
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setupMenu() : PopupWindow? {
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
            txtCountUnreadNoti = layout.findViewById(R.id.count_unread)

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
                navController.currentDestination?.let {
                    navController.popBackStack(it.id, true)
                }
                navController.navigate(R.id.nav_contacts)
                navController.navigate(R.id.contact_request_fragment)
                mDropdown.dismiss()
            }

            val background =
                resources.getDrawable(android.R.drawable.editbox_background_normal)
            mDropdown.setBackgroundDrawable(background)
            return mDropdown
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun setupRealTimeNotification() {
        // Set up Firebase Realtime Database listener
        val database = FirebaseDatabase.getInstance()
        val notificationsRef = database.getReference("contact_requests/${ChitChatterUtils}")

        notificationsRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                // A new notification is received
                showNotificationIndicator(true)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                // Notification changed
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                // Notification removed
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                // Notification moved
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle possible errors.
            }
        })
    }

    private fun showNotificationIndicator(show: Boolean) {
        redDotView.visibility = if (show) View.VISIBLE else View.GONE
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

}