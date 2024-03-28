package icu.samnyan.aqua.sega.wacca.model.db

import ext.isoDate
import ext.ls
import ext.sec
import ext.toDate
import icu.samnyan.aqua.net.games.BaseEntity
import icu.samnyan.aqua.sega.general.model.Card
import jakarta.persistence.*
import java.util.*

/**
 * General user information
 */
@Entity @Table(name = "wacca_user")
class WaccaUser : BaseEntity() {
    @OneToOne
    @JoinColumn(name = "aime_card_id", unique = true)
    var card: Card = Card()

    @Column(length = 8)
    var username = ""

    var xp = 0
    var wp = 0
    var wpTotal = 0
    var wpSpent = 0
    var danType = 0
    var danLevel = 0
    var title0 = 0
    var title1 = 0
    var title2 = 0
    var rating = 0
    @Temporal(TemporalType.TIMESTAMP)
    var vipExpireTime: Date = "2077-01-01".isoDate().toDate()
    var alwaysVip = false
    var loginCount = 0
    var loginCountConsec = 0
    var loginCountDays = 0
    var loginCountDaysConsec = 0
    var loginCountToday = 0
    var playcountSingle = 0
    var playcountMultiVs = 0
    var playcountMultiCoop = 0
    var playcountStageup = 0
    var playcountTimeFree = 0
    var friendView1 = 0
    var friendView2 = 0
    var friendView3 = 0
    @Column(length = 50)
    var lastGameVer = "1.0.0"
    var lastSongId = 0
    var lastSongDifficulty = 0
    var lastFolderOrder = 0
    var lastFolderId = 0
    var lastSongOrder = 0
    @Temporal(TemporalType.TIMESTAMP)
    var lastConsecDate: Date = Date(0)
    @Temporal(TemporalType.TIMESTAMP)
    var lastLoginDate: Date = Date()
    var gateTutorialFlags: String = "[[1, 0], [2, 0], [3, 0], [4, 0], [5, 0]]"

    fun lStatus() = ls(card.extId, username, 1, xp, danLevel, danType, wp, ls(0, 0, 0), loginCount, loginCountDays,
        loginCountConsec, loginCountDaysConsec, vipExpireTime.sec, loginCountToday, rating)
}