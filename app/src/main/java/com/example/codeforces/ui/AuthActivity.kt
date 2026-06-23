package com.example.codeforces.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.codeforces.R
import com.example.codeforces.databinding.ActivityAuthBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    // ─── Google Sign-In result launcher ──────────────────────────────────────

    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            firebaseAuthWithGoogle(account.idToken!!)
        } catch (e: ApiException) {
            showError("Google Sign-In failed: ${e.message}")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.statusBarColor = Color.parseColor("#0A0A0A")
        window.navigationBarColor = Color.parseColor("#0A0A0A")

        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // If already signed in — check if handle is bound
        if (auth.currentUser != null) {
            navigateIfHandleBound()
            return
        }

        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Buttons
        binding.btnGoogleSignIn.setOnClickListener {
            setLoading(true)
            val signInIntent = googleSignInClient.signInIntent
            signInLauncher.launch(signInIntent)
        }

        binding.btnSkipAuth.setOnClickListener {
            // Skip auth — go directly to the anonymous handle entry
            startActivity(Intent(this, EnterUsernameActivity::class.java))
            finish()
        }
    }

    // ─── Firebase credential exchange ────────────────────────────────────────

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        lifecycleScope.launch {
            try {
                auth.signInWithCredential(credential).await()
                setLoading(false)
                // Signed in — now check if handle is already bound in Firestore
                navigateIfHandleBound()
            } catch (e: Exception) {
                setLoading(false)
                showError("Authentication failed: ${e.message}")
            }
        }
    }

    // ─── Routing ─────────────────────────────────────────────────────────────

    private fun navigateIfHandleBound() {
        val uid = auth.currentUser?.uid ?: return
        lifecycleScope.launch {
            val prefs = getSharedPreferences("CodeforcesPrefs", MODE_PRIVATE)
            // Check local prefs first (fast path)
            val localHandle = prefs.getString("HANDLE", null)
            if (!localHandle.isNullOrBlank()) {
                goToMain(localHandle)
                return@launch
            }
            // Check Firestore
            val repo = com.example.codeforces.repository.UserRepository
            val firestoreHandle = repo.getHandle(uid)
            if (!firestoreHandle.isNullOrBlank()) {
                // Save locally for offline use
                prefs.edit().putString("HANDLE", firestoreHandle).apply()
                goToMain(firestoreHandle)
            } else {
                // No handle yet — go to binding screen
                goToHandleBinding()
            }
        }
    }

    private fun goToMain(handle: String) {
        startActivity(Intent(this, MainActivity::class.java).also {
            it.putExtra("HANDLE", handle)
        })
        finish()
    }

    private fun goToHandleBinding() {
        startActivity(Intent(this, HandleBindingActivity::class.java))
        finish()
    }

    // ─── UI helpers ──────────────────────────────────────────────────────────

    private fun setLoading(loading: Boolean) {
        binding.progressAuth.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnGoogleSignIn.isEnabled = !loading
        binding.btnSkipAuth.isEnabled = !loading
        binding.btnGoogleSignIn.text = if (loading) "SIGNING IN..." else "CONTINUE WITH GOOGLE"
    }

    private fun showError(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }
}
