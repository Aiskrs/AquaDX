package icu.samnyan.aqua.sega.wacca.model.db

import com.fasterxml.jackson.annotation.JsonIgnore
import ext.*
import icu.samnyan.aqua.net.games.BaseEntity
import icu.samnyan.aqua.sega.general.IntegerListConverter
import icu.samnyan.aqua.sega.wacca.WaccaItemType
import icu.samnyan.aqua.sega.wacca.WaccaItemType.*
import jakarta.persistence.*
import java.util.*

typealias UC = UniqueConstraint

/**
 * Base entity for all wacca user-related entities
 */
@MappedSuperclass
open class WaccaUserEntity : BaseEntity() {
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_id")
    open var user: WaccaUser = WaccaUser()
}

/**
 * In-game option key-value storage
 */
@Entity @Table(name = "wacca_user_option", uniqueConstraints = [UC("", ["user_id", "opt_id"])])
class WcUserOption(
    var optId: Int = 0,
    var value: Int = 0
) : WaccaUserEntity()

@Entity @Table(name = "wacca_user_bingo", uniqueConstraints = [UC("", ["user_id", "page_number"])])
class WcUserBingo : WaccaUserEntity() {
    var pageNumber = 0
    var pageProgress = ""
}

/**
 * The user here is the sender of the friend request.
 */
@Entity @Table(name = "wacca_friend", uniqueConstraints = [UC("", ["user_id", "with"])])
class WcUserFriend : WaccaUserEntity() {
    @ManyToOne @JoinColumn(name = "with")
    var with: WaccaUser = WaccaUser()
    var isAccepted = false
}

@Entity @Table(name = "wacca_user_gate", uniqueConstraints = [UC("", ["user_id", "gate_id"])])
class WcUserGate : WaccaUserEntity() {
    var gateId = 0
    var page = 1
    var progress = 0
    var loops = 0

    @Temporal(TemporalType.TIMESTAMP)
    var lastUsed = Date(0)
    var missionFlag = 0
    var totalPoints = 0

    fun ls() = ls(gateId, 1, page, progress, loops, lastUsed.sec, missionFlag)
}

@Entity @Table(name = "wacca_user_item", uniqueConstraints = [UC("", ["user_id", "item_id", "type"])])
class WcUserItem(
    var type: Int = 0,

    // Item prop represents different things based on the item type
    var itemId: Int = 0,
    var p1: Long = 0L,
    var p2: Long = 0L,
    var p3: Long = 0L,

    @Temporal(TemporalType.TIMESTAMP)
    var acquiredDate: Date = Date(),
) : WaccaUserEntity() {
    fun ls() = when (type) {
        MUSIC_UNLOCK() -> ls(itemId, p1, acquiredDate.sec, acquiredDate.sec) // songId, diff, acquireDate, unlockDate
        ICON() -> ls(itemId, 1, p1, acquiredDate.sec) // id, type, uses, acquiredDate
        TROPHY() -> ls(itemId, p1, p2, p3) // id, season, progress, badgeType
        SKILL() -> ls(itemId, p1, p2, p3) // skillType, level, flag, badge
        TICKET() -> ls(id, itemId, p1) // userTicketId, ticketId, expire
        NAVIGATOR() -> ls(itemId, 1, acquiredDate.sec, p1, p2) // id, type, acquiredDate, uses, usesToday

        // Generic: title, note colors, note sounds, plates, touch effects
        else -> ls(itemId, 1, acquiredDate.sec) // id, type, acquireDate
    }

    infix fun isType(t: WaccaItemType) = type == t()
}

@Entity @Table(name = "wacca_user_score", uniqueConstraints = [UC("", ["user_id", "song_id", "chart_id"])])
class WcUserScore : WaccaUserEntity() {
    var songId = 0
    var difficulty = 0 // aka difficulty
    var score = 0

    @Convert(converter = IntegerListConverter::class)
    var clears: MutableList<Int> = mutableListOf(0, 0, 0, 0, 0) // Played, Clear, Full Combo, Missless, All Marv

    @Convert(converter = IntegerListConverter::class)
    var grades: MutableList<Int> = (1..13).map { 0 }.toMutableList() // From D to SSS+
    var bestCombo = 0
    var lowestMissCt = Int.MAX_VALUE
    var rating = 0

    fun ls() = ls(songId, difficulty, clears, clears, grades, score, lowestMissCt, 0, 1, rating)
}

@Entity @Table(name = "wacca_user_playlog", uniqueConstraints = [UC("", ["user_id", "song_id", "chart_id", "date_scored"])])
class WcUserPlayLog : WaccaUserEntity() {
    var songId = 0
    var difficulty = 0
    var level = 0.0
    var score = 0
    @Convert(converter = IntegerListConverter::class)
    var judgements: MutableList<Int> = mutableListOf(0, 0, 0, 0) // Marv, Great, Good, Miss
    var maxCombo = 0
    var grade = 0
    var clear = false
    var missless = false
    var fullCombo = false
    var allMarv = false
    var giveUp = false
    var skillPt = 0
    var fastCt = 0
    var lateCt = 0
    var newRecord = false

    @Temporal(TemporalType.TIMESTAMP)
    var dateScored = Date()

    fun clears() = ls(1, +clear, +fullCombo, +missless, +allMarv)

    companion object {
        val keys = ls("songId", "difficulty", "level", "score", "judgements", "maxCombo", "grade", "clear", "missless", "fullCombo", "allMarv", "giveUp", "skillPt", "fastCt", "lateCt", "newRecord")
        fun parse(l: List<*>) = JACKSON.parse<WcUserPlayLog>(keys.zip(l).toMap())
    }
}

@Entity @Table(name = "wacca_user_stageup", uniqueConstraints = [UC("", ["user_id", "stage_id"])])
class WcUserStageUp : WaccaUserEntity() {
    var version = 0
    var stageId = 0
    var clearStatus = 0
    var clearSongCt = 0
    var song1Score = 0
    var song2Score = 0
    var song3Score = 0
    var playCt = 0
}
