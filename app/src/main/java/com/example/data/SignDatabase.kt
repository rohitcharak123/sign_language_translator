package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [SignEntity::class, TranslationHistoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class SignDatabase : RoomDatabase() {

    abstract fun signDao(): SignDao

    companion object {
        @Volatile
        private var INSTANCE: SignDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): SignDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SignDatabase::class.java,
                    "signbridge_database"
                )
                .addCallback(SignDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class SignDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialSigns(database.signDao())
                    }
                }
            }
        }

        suspend fun populateInitialSigns(dao: SignDao) {
            val initialSigns = listOf(
                // Emergency (Crucial for deaf/non-speaking users)
                SignEntity(
                    gloss = "HELP",
                    translation = "I need help",
                    category = "Emergency",
                    signLanguage = "ASL",
                    datasetSource = "WLASL",
                    description = "Closed fist with thumb up rests on open palm of non-dominant hand; both lift upward together.",
                    handShapeHint = "A-handshape on flat open palm, moving upwards",
                    usageCount = 12
                ),
                SignEntity(
                    gloss = "EMERGENCY",
                    translation = "Emergency / Urgent",
                    category = "Emergency",
                    signLanguage = "ASL",
                    datasetSource = "WLASL",
                    description = "E-handshape shaken side-to-side in front of chest with urgent facial expression.",
                    handShapeHint = "E-handshape shaken rapidly horizontally",
                    usageCount = 8
                ),
                SignEntity(
                    gloss = "DOCTOR",
                    translation = "Call a doctor",
                    category = "Emergency",
                    signLanguage = "ASL",
                    datasetSource = "WLASL",
                    description = "Bent B or M handshape taps wrist pulse twice.",
                    handShapeHint = "Fingertips tap wrist like checking a pulse",
                    usageCount = 5
                ),
                SignEntity(
                    gloss = "I AM DEAF",
                    translation = "I am deaf / non-speaking",
                    category = "Emergency",
                    signLanguage = "ASL",
                    datasetSource = "WLASL",
                    description = "Index finger touches ear then moves down to touch near corner of mouth.",
                    handShapeHint = "Index finger ear to mouth",
                    usageCount = 15
                ),
                SignEntity(
                    gloss = "CALL POLICE",
                    translation = "Call the police",
                    category = "Emergency",
                    signLanguage = "ASL",
                    datasetSource = "WLASL",
                    description = "C-handshape tapped on opposite upper chest badge area.",
                    handShapeHint = "C-hand tapped on chest like a badge",
                    usageCount = 4
                ),
                // Greetings & Essentials
                SignEntity(
                    gloss = "HELLO",
                    translation = "Hello! / Hi",
                    category = "Greetings",
                    signLanguage = "ASL",
                    datasetSource = "WLASL",
                    description = "Open hand with fingers together, thumb tucked, touches forehead temple then waves outward in salute.",
                    handShapeHint = "Salute outward from temple",
                    usageCount = 45
                ),
                SignEntity(
                    gloss = "THANK YOU",
                    translation = "Thank you very much",
                    category = "Greetings",
                    signLanguage = "ASL",
                    datasetSource = "WLASL",
                    description = "Flat hand fingers touch chin or lips and extend outward towards the conversation partner.",
                    handShapeHint = "Flat hand from chin outward",
                    usageCount = 38
                ),
                SignEntity(
                    gloss = "PLEASE",
                    translation = "Please",
                    category = "Greetings",
                    signLanguage = "ASL",
                    datasetSource = "WLASL",
                    description = "Flat open palm rubs in a circle over the center of the chest.",
                    handShapeHint = "Circular rub on chest with flat palm",
                    usageCount = 20
                ),
                SignEntity(
                    gloss = "YES",
                    translation = "Yes, agreed",
                    category = "Basics",
                    signLanguage = "ASL",
                    datasetSource = "WLASL",
                    description = "Fist (S-handshape) nods up and down repeatedly like a head nodding.",
                    handShapeHint = "S-fist nodding vertically",
                    usageCount = 30
                ),
                SignEntity(
                    gloss = "NO",
                    translation = "No / Negative",
                    category = "Basics",
                    signLanguage = "ASL",
                    datasetSource = "WLASL",
                    description = "Index and middle fingers snap down against thumb quickly.",
                    handShapeHint = "Index + middle snap to thumb",
                    usageCount = 28
                ),
                SignEntity(
                    gloss = "SORRY",
                    translation = "I am sorry / Excuse me",
                    category = "Greetings",
                    signLanguage = "ASL",
                    datasetSource = "WLASL",
                    description = "A-handshape fist rubs in a circle over heart / chest area.",
                    handShapeHint = "A-fist circular rub on chest",
                    usageCount = 14
                ),
                // Questions
                SignEntity(
                    gloss = "WHERE",
                    translation = "Where is it located?",
                    category = "Questions",
                    signLanguage = "ASL",
                    datasetSource = "WLASL",
                    description = "Index finger points up and wiggles side to side with furrowed brow (WH-question marker).",
                    handShapeHint = "Index finger side-to-side wag with furrowed brow",
                    usageCount = 22
                ),
                SignEntity(
                    gloss = "WHAT",
                    translation = "What happened?",
                    category = "Questions",
                    signLanguage = "ASL",
                    datasetSource = "WLASL",
                    description = "Both hands palms facing up in front, moving side to side in an open shrugging gesture.",
                    handShapeHint = "Both open palms up shaking gently",
                    usageCount = 19
                ),
                SignEntity(
                    gloss = "WATER",
                    translation = "I need water",
                    category = "Basics",
                    signLanguage = "ASL",
                    datasetSource = "WLASL",
                    description = "W-handshape (index, middle, ring extended) taps index finger against chin twice.",
                    handShapeHint = "W-hand taps chin twice",
                    usageCount = 16
                ),
                SignEntity(
                    gloss = "FOOD / EAT",
                    translation = "Food / I am hungry",
                    category = "Basics",
                    signLanguage = "ASL",
                    datasetSource = "WLASL",
                    description = "Squashed O-handshape tips fingers to lips repeatedly as if feeding oneself.",
                    handShapeHint = "O-fingertips tap mouth twice",
                    usageCount = 18
                ),
                // Indian Sign Language (ISL - INCLUDE Dataset)
                SignEntity(
                    gloss = "NAMASTE",
                    translation = "Hello / Greetings (ISL)",
                    category = "Greetings",
                    signLanguage = "ISL",
                    datasetSource = "INCLUDE",
                    description = "Both palms pressed together in front of chest in traditional respectful greeting with head nod.",
                    handShapeHint = "Prayer hands at heart center with respectful nod",
                    usageCount = 25
                ),
                SignEntity(
                    gloss = "HELP (ISL)",
                    translation = "Help me (Indian Sign Language)",
                    category = "Emergency",
                    signLanguage = "ISL",
                    datasetSource = "INCLUDE",
                    description = "Right hand with open palm cupped moves from signer chest towards recipient with pleading expression.",
                    handShapeHint = "Cupped open hand extending forward with eye contact",
                    usageCount = 11
                ),
                SignEntity(
                    gloss = "WATER (ISL)",
                    translation = "Drink water (ISL)",
                    category = "Basics",
                    signLanguage = "ISL",
                    datasetSource = "INCLUDE",
                    description = "C-shaped hand tilted towards mouth like holding a glass of water.",
                    handShapeHint = "Cupped hand tilting to mouth",
                    usageCount = 10
                ),
                SignEntity(
                    gloss = "HOSPITAL (ISL)",
                    translation = "Go to hospital (ISL)",
                    category = "Emergency",
                    signLanguage = "ISL",
                    datasetSource = "INCLUDE",
                    description = "Index finger forms a cross symbol on opposite shoulder or upper arm.",
                    handShapeHint = "Cross trace on upper arm",
                    usageCount = 9
                ),
                // British Sign Language (BSL)
                SignEntity(
                    gloss = "HELLO (BSL)",
                    translation = "Hello (BSL)",
                    category = "Greetings",
                    signLanguage = "BSL",
                    datasetSource = "BSL Corpus",
                    description = "Open dominant hand moves in wave gesture from chest height outward with bright smile.",
                    handShapeHint = "Open palm arc wave",
                    usageCount = 12
                ),
                SignEntity(
                    gloss = "THANK YOU (BSL)",
                    translation = "Thank you (BSL)",
                    category = "Greetings",
                    signLanguage = "BSL",
                    datasetSource = "BSL Corpus",
                    description = "Dominant hand fingers touch mouth and extend forward with slight bow.",
                    handShapeHint = "Fingertips from lips forward",
                    usageCount = 11
                )
            )
            dao.insertAll(initialSigns)
        }
    }
}
