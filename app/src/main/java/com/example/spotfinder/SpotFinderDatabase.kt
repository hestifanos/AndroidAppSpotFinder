

package com.example.spotfinder

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class SpotFinderDatabase private constructor(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "spotfinder_db.sqlite"
        private const val DATABASE_VERSION = 2

        const val TABLE_LOCATION = "location"
        const val COL_ID = "id"
        const val COL_ADDRESS = "address"
        const val COL_LAT = "latitude"
        const val COL_LNG = "longitude"

        @Volatile
        private var INSTANCE: SpotFinderDatabase? = null


        fun getInstance(context: Context): SpotFinderDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = SpotFinderDatabase(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_LOCATION (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_ADDRESS TEXT NOT NULL UNIQUE,
                $COL_LAT REAL NOT NULL,
                $COL_LNG REAL NOT NULL
            )
        """.trimIndent()
        db.execSQL(createTable)

        // Only called the very first time DB is created
        insertInitialLocations(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_LOCATION")
        onCreate(db)
    }

    // the CRUD helpers used by MainActivity
    fun getByAddress(address: String): LocationEntity? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_LOCATION,
            null,
            "$COL_ADDRESS = ?",
            arrayOf(address),
            null,
            null,
            null,
            "1"
        )

        cursor.use {
            if (it.moveToFirst()) {
                val id = it.getLong(it.getColumnIndexOrThrow(COL_ID))
                val addr = it.getString(it.getColumnIndexOrThrow(COL_ADDRESS))
                val lat = it.getDouble(it.getColumnIndexOrThrow(COL_LAT))
                val lng = it.getDouble(it.getColumnIndexOrThrow(COL_LNG))
                return LocationEntity(id, addr, lat, lng)
            }
        }
        return null
    }

    fun insert(location: LocationEntity): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_ADDRESS, location.address)
            put(COL_LAT, location.latitude)
            put(COL_LNG, location.longitude)
        }
        return db.insert(TABLE_LOCATION, null, values)
    }

    fun updateByAddress(address: String, lat: Double, lng: Double): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_LAT, lat)
            put(COL_LNG, lng)
        }
        return db.update(
            TABLE_LOCATION,
            values,
            "$COL_ADDRESS = ?",
            arrayOf(address)
        )
    }

    fun deleteByAddress(address: String): Int {
        val db = writableDatabase
        return db.delete(
            TABLE_LOCATION,
            "$COL_ADDRESS = ?",
            arrayOf(address)
        )
    }


    private fun insertInitialLocations(db: SQLiteDatabase) {
        fun insert(addr: String, lat: Double, lng: Double) {
            val values = ContentValues().apply {
                put(COL_ADDRESS, addr)
                put(COL_LAT, lat)
                put(COL_LNG, lng)
            }
            db.insert(TABLE_LOCATION, null, values)
        }

        insert("Downtown Toronto", 43.6532, -79.3832)
        insert("Scarborough", 43.7764, -79.2318)
        insert("North York", 43.7615, -79.4111)
        insert("Etobicoke", 43.6289, -79.5200)
        insert("Mississauga", 43.5890, -79.6441)
        insert("Brampton", 43.7315, -79.7624)
        insert("Markham", 43.8561, -79.3370)
        insert("Oshawa", 43.8971, -78.8658)
        insert("Pickering", 43.8355, -79.0890)
        insert("Ajax", 43.8509, -79.0204)
        insert("Whitby", 43.8975, -78.9420)
        insert("Vaughan", 43.8361, -79.4983)
        insert("Richmond Hill", 43.8828, -79.4403)
        insert("Aurora", 44.0065, -79.4504)
        insert("Newmarket", 44.0592, -79.4613)
        insert("King City", 43.9283, -79.5287)
        insert("Bolton", 43.8744, -79.7356)
        insert("Caledon", 43.8571, -79.8821)
        insert("Milton", 43.5183, -79.8774)
        insert("Georgetown", 43.6497, -79.9040)
        insert("Oakville", 43.4675, -79.6877)
        insert("Burlington", 43.3255, -79.7990)
        insert("Hamilton", 43.2557, -79.8711)
        insert("Stoney Creek", 43.2176, -79.7653)
        insert("Grimsby", 43.2000, -79.5667)
        insert("Niagara Falls", 43.0896, -79.0849)
        insert("Port Credit", 43.5520, -79.5889)
        insert("Meadowvale", 43.5975, -79.7557)
        insert("Erin Mills", 43.5481, -79.6925)
        insert("Cooksville", 43.5765, -79.6144)
        insert("Dixie", 43.6092, -79.5961)
        insert("Malton", 43.7009, -79.6346)
        insert("Rexdale", 43.7161, -79.5881)
        insert("Weston", 43.7011, -79.5129)
        insert("York", 43.6890, -79.4537)
        insert("Midtown Toronto", 43.6997, -79.3981)
        insert("East York", 43.7061, -79.3272)
        insert("Leslieville", 43.6667, -79.3312)
        insert("The Beaches", 43.6711, -79.2960)
        insert("Riverdale", 43.6761, -79.3485)
        insert("Cabbagetown", 43.6655, -79.3698)
        insert("Liberty Village", 43.6387, -79.4223)
        insert("High Park", 43.6465, -79.4637)
        insert("The Junction", 43.6675, -79.4741)
        insert("Roncesvalles", 43.6414, -79.4481)
        insert("Little Italy", 43.6550, -79.4180)
        insert("Kensington Market", 43.6543, -79.4001)
        insert("Chinatown", 43.6530, -79.3989)
        insert("Financial District", 43.6481, -79.3810)
        insert("Harbourfront", 43.6381, -79.3793)
        insert("Distillery District", 43.6505, -79.3596)
        insert("St. Lawrence Market", 43.6486, -79.3716)
        insert("Yorkville", 43.6715, -79.3930)
        insert("Rosedale", 43.6827, -79.3793)
        insert("Forest Hill", 43.6936, -79.4156)
        insert("Lawrence Park", 43.7220, -79.3989)
        insert("Leaside", 43.7080, -79.3630)
        insert("Don Mills", 43.7392, -79.3437)
        insert("Bayview Village", 43.7717, -79.3856)
        insert("Willowdale", 43.7706, -79.4144)
        insert("Thornhill", 43.8133, -79.4296)
        insert("Concord", 43.8009, -79.5074)
        insert("Maple", 43.8505, -79.5178)
        insert("Kleinburg", 43.8430, -79.6282)
        insert("Woodbridge", 43.7904, -79.6057)
        insert("Weston Downs", 43.7891, -79.5483)
        insert("Malvern", 43.8072, -79.2141)
        insert("Rouge", 43.8079, -79.1533)
        insert("Guildwood", 43.7436, -79.2023)
        insert("Woburn", 43.7710, -79.2387)
        insert("Morningside", 43.7768, -79.1907)
        insert("Agincourt", 43.7872, -79.2772)
        insert("Wexford", 43.7488, -79.2837)
        insert("Kennedy Park", 43.7163, -79.2664)
        insert("Birch Cliff", 43.6921, -79.2657)
        insert("Clairlea", 43.7141, -79.2925)
        insert("Victoria Village", 43.7278, -79.3071)
        insert("Flemingdon Park", 43.7052, -79.3368)
        insert("Banbury-Don Mills", 43.7495, -79.3451)
        insert("Bayview Glen", 43.8384, -79.3965)
        insert("Unionville", 43.8615, -79.3125)
        insert("Cornell", 43.8830, -79.2292)
        insert("Greensborough", 43.9014, -79.2453)
        insert("Bur Oak", 43.8785, -79.2682)
        insert("Mount Joy", 43.9168, -79.2702)
        insert("Stouffville", 43.9709, -79.2493)
        insert("Ballantrae", 44.0052, -79.3169)
        insert("Goodwood", 44.0433, -79.1867)
        insert("Uxbridge", 44.1005, -79.1169)
        insert("Brooklin", 43.9617, -78.9444)
        insert("Port Perry", 44.1051, -78.9445)
        insert("Courtice", 43.9168, -78.7892)
        insert("Bowmanville", 43.9126, -78.6878)
        insert("Newcastle", 43.9056, -78.5881)
        insert("Clarington", 43.9337, -78.6880)
        insert("Whitby Shores", 43.8559, -78.9426)
        insert("Brookfield", 43.8335, -79.3755)
        insert("York University Heights", 43.7672, -79.4935)
        insert("Downsview", 43.7353, -79.4727)
        insert("Jane and Finch", 43.7620, -79.5153)
        insert("Finch West", 43.7635, -79.5068)
        insert("Steeles", 43.8120, -79.3245)
        insert("L’Amoreaux", 43.7994, -79.3120)
        insert("Hillcrest Village", 43.7963, -79.3556)
        insert("Pleasant View", 43.7787, -79.3415)
        insert("Scarborough Town Centre", 43.7764, -79.2579)
        insert("Centennial College", 43.7842, -79.2261)
        insert("UTSC Campus", 43.7841, -79.1861)
        insert("Toronto Pearson Airport", 43.6777, -79.6248)
        insert("Toronto Islands", 43.6205, -79.3784)
        insert("Exhibition Place", 43.6330, -79.4183)
        insert("Yonge-Dundas Square", 43.6561, -79.3802)
        insert("Casa Loma", 43.6780, -79.4094)
        insert("Toronto Zoo", 43.8177, -79.1859)
        insert("CN Tower", 43.6426, -79.3871)
        insert("Rogers Centre", 43.6415, -79.3893)
        insert("High Park Zoo", 43.6465, -79.4637)
        insert("Ontario Place", 43.6280, -79.4185)
        insert("Union Station", 43.6452, -79.3806)


    }
}






















