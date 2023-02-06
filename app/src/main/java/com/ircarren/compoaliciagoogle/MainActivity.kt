package com.ircarren.compoaliciagoogle

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
//import coil.compose.AsyncImage

import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.ircarren.compoaliciagoogle.ui.theme.CompoAliciaGoogleTheme

class MainActivity : ComponentActivity() {

    companion object {
        const val RC_SIGN_IN = 100
    }

    private lateinit var mAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // firebase auth instance
        mAuth = FirebaseAuth.getInstance()

        // configure Google SignIn
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        setContent {
            CompoAliciaGoogleTheme() {

                if (mAuth.currentUser == null) {
                    GoogleSignInButton {
                        signIn()
                    }
                } else {
                    val user: FirebaseUser = mAuth.currentUser!!
                    ProfileScreen(
                        profileImage = user.photoUrl!!,
                        name = user.displayName!!,
                        email = user.email!!,
                        signOutClicked = {
                            signOut()
                        }
                    )
                }

            }
        }
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // result returned from launching the intent from GoogleSignInApi.getSignInIntent()
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val exception = task.exception
            if (task.isSuccessful) {
                try {
                    // Google SignIn was successful, authenticate with Firebase
                    val account = task.getResult(ApiException::class.java)!!
                    firebaseAuthWithGoogle(account.idToken!!)
                } catch (e: Exception) {
                    // Google SignIn Failed
                    Log.d("SignIn", "Google SignIn Failed")
                }
            } else {
                Log.d("SignIn", exception.toString())
            }
        }

    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // SignIn Successful
                    Toast.makeText(this, "SignIn Successful", Toast.LENGTH_SHORT).show()
                    setContent {
                        CompoAliciaGoogleTheme {
                            val user: FirebaseUser = mAuth.currentUser!!
                            ProfileScreen(
                                profileImage = user.photoUrl!!,
                                name = user.displayName!!,
                                email = user.email!!,
                                signOutClicked = {
                                    signOut()
                                }
                            )
                        }
                    }
                } else {
                    // SignIn Failed
                    Toast.makeText(this, "SignIn Failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun signOut() {
        // get the google account
        val googleSignInClient: GoogleSignInClient

        // configure Google SignIn
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Sign Out of all accounts
        mAuth.signOut()
        googleSignInClient.signOut()
            .addOnSuccessListener {
                Toast.makeText(this, "Sign Out Successful", Toast.LENGTH_SHORT).show()
                setContent {
                    CompoAliciaGoogleTheme() {
                        GoogleSignInButton {
                            signIn()
                        }
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Sign Out Failed", Toast.LENGTH_SHORT).show()
            }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoogleSignInButton(
    signInClicked: () -> Unit
) {

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .padding(start = 30.dp, end = 30.dp)
                .height(55.dp)
                .fillMaxWidth()
                .clickable {
                    signInClicked()
                },
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(width = 1.5.dp, color = Color.Black),
        ) {
            Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                Image(
                    modifier = Modifier
                        .padding(start = 15.dp)
                        .size(32.dp)
                        .padding(0.dp)
                        .align(Alignment.CenterVertically),
                    painter = painterResource(id = R.drawable.ic_launcher_background),
                    contentDescription = "google_logo"
                )
                Text(
                    modifier = Modifier
                        .padding(start = 20.dp)
                        .align(Alignment.CenterVertically),
                    text = "Sign In With Google",
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    profileImage: Uri,
    name: String,
    email: String,
    signOutClicked: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .size(150.dp)
                .fillMaxHeight(0.4f),
            shape = RoundedCornerShape(125.dp),
        ) {

        }

        Column(
            modifier = Modifier
                .fillMaxHeight(0.6f)
                .padding(top = 60.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = {},
                readOnly = true,
                label = {
                    Text(text = "Name")
                },
            )

            OutlinedTextField(
                modifier = Modifier.padding(top = 20.dp),
                value = email,
                onValueChange = {},
                readOnly = true,
                label = {
                    Text(text = "Email")
                },
            )

            Button(
                modifier = Modifier
                    .align(CenterHorizontally)
                    .padding(top = 100.dp),
                onClick = { signOutClicked() }
            ) {
                Text(text = "LogOut")
            }
        }

    }
}