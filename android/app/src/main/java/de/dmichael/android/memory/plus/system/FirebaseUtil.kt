package de.dmichael.android.memory.plus.system

import android.content.Context
import android.net.Uri
import android.os.SystemClock
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import kotlin.random.Random

object FirebaseUtil {

    private val random = Random(SystemClock.elapsedRealtime())

    fun initialize(context: Context) {
        // Initialize Firebase
        val app = FirebaseApp.initializeApp(context)
        Log.v(Game.TAG, "Firebase connection initialized: $app")

        // Sign In Anonymously
        val auth = Firebase.auth
        auth.signInAnonymously()
            .addOnSuccessListener { result ->
                Log.v(Game.TAG, "Anonymous sign in to firebase successful: ${result.user?.uid}")
            }
            .addOnFailureListener { ex ->
                Log.e(Game.TAG, "Failed to sign in anonymously to firebase: $ex", ex)
            }
    }

    fun createUniqueIdentifier(
        name: String,
        callback: (identifier: String) -> Unit,
        error: (ex: Exception) -> Unit
    ) {
        val idName = name.replace(' ', '_')
        val number = random.nextInt(100000, 999999)
        val identifier = "$idName#$number"

        val firestore = Firebase.firestore
        val document = firestore.collection("profiles").document(identifier)
        // Check, if the document already exists
        document.get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.data == null) {
                    // identifier is unique
                    Log.v(Game.TAG, "identifier '$identifier' is unique")
                    callback(identifier)
                } else {
                    // identifier is not unique
                    Log.v(Game.TAG, "identifier '$identifier' is not unique, try again ...")
                    createUniqueIdentifier(name, callback, error)
                }
            }
            .addOnFailureListener { ex ->
                Log.e(Game.TAG, "Failed to get document ${document.path} from firebase: $ex", ex)
                error(ex)
            }
    }

    fun createOrUpdateDocument(
        collectionName: String,
        documentName: String,
        properties: Map<String, Any?>,
        update: Boolean,
        callback: () -> Unit,
        error: (ex: Exception) -> Unit
    ) {
        val firestore = Firebase.firestore

        val document = firestore.collection(collectionName).document(documentName)
        if (!update) {
            document.set(properties)
                .addOnSuccessListener {
                    Log.v(Game.TAG, "Document '${document.path} successfully created")
                    callback()
                }
                .addOnFailureListener { ex ->
                    Log.e(Game.TAG, "Failed to create document '${document.path}: $ex", ex)
                    error(ex)
                }
        } else {
            document.update(properties)
                .addOnSuccessListener {
                    Log.v(Game.TAG, "Document '${document.path} successfully updated")
                    callback()
                }
                .addOnFailureListener { ex ->
                    Log.e(Game.TAG, "Failed to update document '${document.path}: $ex", ex)
                    error(ex)
                }
        }
    }


    fun removeDocument(
        collectionName: String,
        documentName: String,
        callback: () -> Unit,
        error: (ex: Exception) -> Unit
    ) {
        val firestore = Firebase.firestore

        val document = firestore.collection(collectionName).document(documentName)
        document.delete()
            .addOnSuccessListener {
                Log.v(Game.TAG, "Document '${document.path}' successfully deleted")
                callback()
            }
            .addOnFailureListener { ex ->
                Log.e(Game.TAG, "Failed to delete document '${document.path}: $ex", ex)
                error(ex)
            }
    }

    fun uploadFile(
        uri: Uri?,
        path: String,
        callback: (uploaded: Boolean) -> Unit,
        error: (ex: Exception) -> Unit
    ) {
        if (uri != null) {
            val storage = Firebase.storage
            val filename = uri.lastPathSegment
            val reference = storage.reference.child("$path/$filename")
            // Check if image is already uploaded
            reference.downloadUrl
                .addOnSuccessListener {
                    // Image already uploaded
                    Log.v(Game.TAG, "Image '$filename' already uploaded")
                    callback(false)
                }
                .addOnFailureListener { ex ->
                    if (ex is StorageException && ex.httpResultCode == 404) {
                        // Upload image
                        reference.putFile(uri)
                            .addOnSuccessListener {
                                Log.v(
                                    Game.TAG,
                                    "Image '$filename' successfully uploaded to firebase"
                                )
                                callback(true)
                            }
                            .addOnFailureListener { ex2 ->
                                Log.e(
                                    Game.TAG,
                                    "Failed to upload image '$filename' to firebase: $ex2",
                                    ex2
                                )
                            }
                    } else {
                        Log.e(Game.TAG, "Failed to get download URL for ${reference.path}: $ex", ex)
                        error(ex)
                    }
                }
        } else {
            callback(false)
        }
    }

    fun iterateDirectory(
        path: String,
        it: (reference: StorageReference) -> Unit,
        callback: () -> Unit,
        error: (ex: Exception) -> Unit
    ) {
        val storage = Firebase.storage
        val reference = storage.reference.child(path)
        iterateDirectory(0, reference, it, callback, error)
    }

    fun removeDirectory(
        path: String,
        callback: () -> Unit,
        error: (ex: Exception) -> Unit
    ) {
        iterateDirectory(path, { removeReference(it) }, callback, error)
    }

    private fun removeReference(
        reference: StorageReference
    ) {
        reference.delete()
            .addOnSuccessListener {
                Log.v(Game.TAG, "Reference '${reference.path}' successfully deleted")
            }
            .addOnFailureListener { ex ->
                Log.e(Game.TAG, "Failed to delete reference '${reference.path}: $ex", ex)
            }
    }

    private fun iterateDirectory(
        level: Int,
        reference: StorageReference,
        it: (reference: StorageReference) -> Unit,
        callback: () -> Unit,
        error: (ex: Exception) -> Unit
    ) {
        reference.listAll()
            .addOnSuccessListener { result ->
                for (childRef in result.prefixes) {
                    Log.v(Game.TAG, "* ${childRef.path}")
                    iterateDirectory(level + 1, childRef, it, callback, error)
                }
                for (childRef in result.items) {
                    Log.v(Game.TAG, "* ${childRef.path}")
                    it(childRef)
                }
                if (level == 0) {
                    callback()
                }
            }
            .addOnFailureListener { ex ->
                Log.e(Game.TAG, "Failed to list files in directory '${reference.path}': $ex", ex)
                error(ex)
            }
    }
}