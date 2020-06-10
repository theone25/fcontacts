package info.danielmartinez.fcontacts

class FContact (
    val identifier: String,
    var displayName : String? = null,
    var contactType : String? = null,
    var thumbnailData : ByteArray? = null,
    var imageDataAvailable : Boolean = false,
    var emails : List<FContactValueLabeled>? = null,
    var phoneNumbers : List<FContactValueLabeled>? = null,
) : Comparable<FContact> {

    override fun compareTo( other: FContact ): Int {
        return other.identifier.compareTo( this.identifier )
    }

    fun toMap() : Map<String,Any> {
        val map = mutableMapOf<String,Any>(
            "identifier" to this.identifier,
            "displayName" to if (this.displayName != null) this.displayName as String else ""
        )
        if (this.contactType != null) {
            map["contactType"] = this.contactType as String
        }
        
        if (this.imageDataAvailable) {
            if (this.thumbnailData != null) {
                map["thumbnailData"] = this.thumbnailData as ByteArray
            }
        }
        if ((this.emails != null) && (this.emails!!.isNotEmpty())) {
            val list = ArrayList<Map<String,Any>>()
            this.emails!!.forEach { email ->
                list.add( email.toMap() )
            }
            map["emails"] = list
        }
        if ((this.phoneNumbers != null) && (this.phoneNumbers!!.isNotEmpty())) {
            val list = ArrayList<Map<String,Any>>()
            this.phoneNumbers!!.forEach { phoneNumber ->
                list.add( phoneNumber.toMap() )
            }
            map["phoneNumbers"] = list
        }
        
        return map
    }

}

class FContactValueLabeled (
    private val label: String,
    var value : String? = null
) {
    fun toMap() : Map<String,Any> {
        return mapOf( Pair(this.label, this.value as String) )
    }
}


