package info.danielmartinez.fcontacts

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.ArrayList

class FContactsHandler (
    private val contentResolver: ContentResolver,
    private val context: Context
) {

    private val parentJob = Job()
    private val exceptionHandler : CoroutineExceptionHandler = CoroutineExceptionHandler {
        _, throwable ->
        GlobalScope.launch {
            println("CoRoutineException Caught: $throwable")
        }
    }
    private val scope = CoroutineScope( Dispatchers.Default + parentJob + exceptionHandler )

    fun list( query: String? = null, onSuccess: (contacts: List<Map<String,Any>>) -> Unit ) {
        scope.launch( Dispatchers.Main ) {
            val items = asyncList( query )
            onSuccess( items )
        }
    }

    private suspend fun asyncList( query: String? = null ) : List<Map<String,Any>> =
            scope.async {
                return@async filter( query )
            }.await()

    private fun filter( query: String? = null ) : List<Map<String,Any>> {
        val list = ArrayList<FContact>()
        val result = ArrayList<Map<String,Any>>()
        val resolver : ContentResolver = this.contentResolver
        val uri = ContactsContract.Contacts.CONTENT_URI

        val cursor = resolver.query(
            uri,
            null,
            null,
            null,
            null
        ) as Cursor
        if (cursor.count > 0) {
            while (cursor.moveToNext()) {
                val model = fromCursor( cursor )
                if ((model != null) && (!list.contains( model ))) {
                    if (contains( model, query )) {
                        list.add(model)
                        result.add(model.toMap())
                    }
                }
            }
        }
        cursor.close()
        return result
    }

    private fun fromCursor(cursor: Cursor): FContact? {
        val columnIndexID = cursor.getColumnIndex(
            ContactsContract.Contacts._ID
        )
        if (columnIndexID == -1) {
            return null
        }
        val identifier = cursor.getString( columnIndexID )
        val model = FContact( identifier )
        val columnIndexDisplayName = cursor.getColumnIndex(
                ContactsContract.Contacts.DISPLAY_NAME
        )
        val displayName = cursor.getString( columnIndexDisplayName )
        model.displayName = displayName
        model.contactType = "person"
        
        val columnIndexPhotoThumbnailUri = cursor.getColumnIndex(
                ContactsContract.Contacts.PHOTO_THUMBNAIL_URI
        )
        if (columnIndexPhotoThumbnailUri != -1) {
            val thumbnailString = cursor.getString( columnIndexPhotoThumbnailUri )
            if (thumbnailString != null) {
                val uri = Uri.parse( thumbnailString )
                val bytes = contentResolver.openInputStream( uri )?.buffered()?.use { it.readBytes() }
                model.thumbnailData = bytes
                model.imageDataAvailable = true
            }
        }
        val columnIndexHasPhoneNumber = cursor.getColumnIndex(
                ContactsContract.Contacts.HAS_PHONE_NUMBER
        )
        val hasPhoneNumber = cursor.getInt( columnIndexHasPhoneNumber )
        if (hasPhoneNumber == 1) {
            val cursorPhoneNumbers = contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + identifier,
                null,
                null
            )
            val listPhoneNumbers = ArrayList<FContactValueLabeled>()
            while( cursorPhoneNumbers!!.moveToNext() ) {
                val columnIndexNumber = cursorPhoneNumbers.getColumnIndex(
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                )
                if (columnIndexNumber != -1) {
                    val number = cursorPhoneNumbers.getString(columnIndexNumber)
                    val columnIndexType = cursorPhoneNumbers.getColumnIndex( ContactsContract.CommonDataKinds.Phone.TYPE )
                    val type = cursorPhoneNumbers.getInt( columnIndexType )
                    val columnIndexLabel = cursorPhoneNumbers.getColumnIndex( ContactsContract.CommonDataKinds.Phone.LABEL )
                    val label = cursorPhoneNumbers.getString( columnIndexLabel )
                    val formattedLabel = ContactsContract.CommonDataKinds.Phone.getTypeLabel(
                        context.resources,
                        type,
                        label
                    ) as String
                    listPhoneNumbers.add( FContactValueLabeled( formattedLabel, number ) )
                }
            }
            cursorPhoneNumbers.close()
            model.phoneNumbers = listPhoneNumbers
        }
        val emails = contentResolver.query(
            ContactsContract.CommonDataKinds.Email.CONTENT_URI,
            null,
            ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = " + identifier,
            null,
            null
        )
        val listEmails = ArrayList<FContactValueLabeled>()
        while( emails!!.moveToNext() ) {
            val address = emails.getString(emails.getColumnIndex(
                    ContactsContract.CommonDataKinds.Email.ADDRESS
            ))
            val type = emails.getInt( emails.getColumnIndex( ContactsContract.CommonDataKinds.Email.TYPE ) )
            val label = emails.getString( emails.getColumnIndex( ContactsContract.CommonDataKinds.Email.LABEL ) )
            val formattedLabel = ContactsContract.CommonDataKinds.Email.getTypeLabel(
                context.resources,
                type,
                label
            ).toString()
            listEmails.add( FContactValueLabeled( formattedLabel, address ) )
        }
        emails.close()
        model.emails = listEmails
        return model
    }

    private fun contains( model: FContact, query: String? = null ) : Boolean {
        if (query == null) {
            return true
        }
        val locale = Locale.getDefault()
        val tQuery = query.toLowerCase(locale)
        val b = (model.displayName?.toLowerCase(locale)?.contains( tQuery ))
        if ((b != null) && (b)) {
            return true
        }
        if (model.identifier.toLowerCase(locale).contains( tQuery )) {
            return true
        }
        
        model.emails?.forEach {
            val bValue = (it.value?.toLowerCase(locale)?.contains( tQuery ))
            if ((bValue != null) && (bValue)) {
                return true
            }
        }

        model.phoneNumbers?.forEach {
            val bValue = (it.value?.toLowerCase(locale)?.contains( tQuery ))
            if ((bValue != null) && (bValue)) {
                return true
            }
        }
        
        return false
    }

}
