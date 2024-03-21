package icu.samnyan.aqua.net.games.mai2

import ext.*
import icu.samnyan.aqua.api.model.resp.sega.maimai2.external.Maimai2DataExport
import icu.samnyan.aqua.net.db.AquaNetUser
import icu.samnyan.aqua.net.db.AquaUserServices
import icu.samnyan.aqua.net.games.*
import icu.samnyan.aqua.net.utils.*
import icu.samnyan.aqua.sega.maimai2.model.*
import icu.samnyan.aqua.sega.maimai2.model.userdata.Mai2UserDetail
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import org.springframework.web.bind.annotation.RestController
import java.lang.reflect.Field
import java.time.LocalDateTime
import java.util.*
import kotlin.io.path.Path
import kotlin.io.path.writeText
import kotlin.reflect.full.declaredMembers

@RestController
@API("api/v2/game/mai2")
class Maimai2(
    override val us: AquaUserServices,
    override val playlogRepo: Mai2UserPlaylogRepo,
    override val userDataRepo: Mai2UserDataRepo,
    val userGeneralDataRepository: Mai2UserGeneralDataRepo,
    val repos: Mai2Repos,
    val netProps: AquaNetProps,
    transManager: PlatformTransactionManager
): GameApiController<Mai2UserDetail>("mai2", Mai2UserDetail::class) {
    val trans = TransactionTemplate(transManager)

    override suspend fun trend(@RP username: Str): List<TrendOut> = us.cardByName(username) { card ->
        findTrend(playlogRepo.findByUserCardExtId(card.extId)
            .map { TrendLog(it.playDate, it.afterRating) })
    }

    // Only show > S rank
    override val shownRanks = mai2Scores.filter { it.first >= 97 * 10000 }
    override val settableFields: Map<String, (Mai2UserDetail, String) -> Unit> by lazy { mapOf(
        "userName" to { u, v -> u.userName = v
            if (!v.all { it in USERNAME_CHARS }) { 400 - "Invalid character in username" }
        },
    ) }

    override suspend fun userSummary(@RP username: Str) = us.cardByName(username) { card ->
        val extra = userGeneralDataRepository.findByUser_Card_ExtId(card.extId)
            .associate { it.propertyKey to it.propertyValue }

        val ratingComposition = mapOf(
            "best35" to (extra["recent_rating"] ?: ""),
            "best15" to (extra["recent_rating_new"] ?: "")
        )

        genericUserSummary(card, ratingComposition)
    }

    // Use reflection to get all properties in Mai2Repos with matching names in Maimai2DataExport
    val exportFields: Map<Field, UserLinked<*>> = listOf(*Maimai2DataExport::class.java.declaredFields)
        .filter { f -> f.name !in setOf("gameId", "userData") }
        .associateWith { Mai2Repos::class.declaredMembers
            .filter { f -> f returns UserLinked::class }
            .firstOrNull { f -> f.name == it.name || f.name == it.name.replace("List", "") }
            ?.call(repos) as UserLinked<*>? ?: error("No matching field found for ${it.name}")
        }

    fun export(u: AquaNetUser) = Maimai2DataExport().apply {
        gameId = "SDEZ"
        userData = repos.userData.findByCard(u.ghostCard) ?: (404 - "User not found")
        exportFields.forEach { (f, u) ->
            f.set(this, if (f.type == List::class.java) u.findByUser(userData)
            else u.findSingleByUser(userData).orElse(null))
        }
    }

    @API("export")
    fun exportAllUserData(@RP token: Str) = us.jwt.auth(token) { u -> export(u) }

    @API("import")
    fun importUserData(@RP token: Str, @RP json: Str) = us.jwt.auth(token) { u ->
        val export = json.parseJson<Maimai2DataExport>()
        if (!export.gameId.equals("SDEZ", true)) 400 - "Invalid game ID"

        // Validate new user data
        // Check that all ids are 0 (this should be true since all ids are @JsonIgnore)
        if (export.userData.id != 0L) 400 - "User ID must be 0"
        exportFields.keys.forEach { it.gets<BaseEntity>(export).id.let { if (it != 0L) 400 - "ID must be 0" } }

        // Set user card
        export.userData.card = u.ghostCard
        // Set user of the remaining data
//        exportFields.values.forEach { it.setUser(export.userData) }

        // Check existing data
        val gu = repos.userData.findByCard(u.ghostCard)?.also { gu ->
            // Store a backup of the old data
            val fl = "mai2-backup-${u.auId}-${LocalDateTime.now().isoDateTime()}.json"
            (Path(netProps.importBackupPath) / fl).writeText(export(u).toJson())
        }

        trans.execute {
            gu?.let {
                // Delete the old data
                exportFields.values.forEach { it.deleteByUser(gu) }
                repos.userData.deleteByCard(u.ghostCard)
            }

            // Insert new data
        }

        TODO()
    }
}