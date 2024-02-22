package icu.samnyan.aqua.net

import ext.*
import icu.samnyan.aqua.net.components.JWT
import icu.samnyan.aqua.net.utils.AquaNetProps
import icu.samnyan.aqua.net.utils.SUCCESS
import icu.samnyan.aqua.sega.general.dao.CardRepository
import icu.samnyan.aqua.sega.general.model.Card
import icu.samnyan.aqua.sega.general.service.CardService
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.RestController
import kotlin.jvm.optionals.getOrNull

@RestController
@API("/api/v2/card")
class CardController(
    val jwt: JWT,
    val cardService: CardService,
    val cardGameService: CardGameService,
    val cardRepository: CardRepository,
    val props: AquaNetProps
) {
    @API("/summary")
    suspend fun summary(@RP cardId: Str): Any
    {
        // DO NOT CHANGE THIS ERROR MESSAGE - The frontend uses it to detect if the card is not found
        val card = cardService.tryLookup(cardId) ?: (404 - "Card not found")

        // Lookup data for each game
        return mapOf(
            "card" to card,
            "summary" to cardGameService.getSummary(card),
        )
    }

    /**
     * Bind a card to the user. This action will migrate selected data from the card to the user's ghost card.
     *
     * Non-migrated data will not be lost, but will be inaccessible from the card until the card is unbound.
     *
     * @param token JWT token
     * @param cardId Card ID
     * @param migrate Things to migrate, stored as a comma-separated list of game IDs (e.g. "maimai2,chusan")
     */
    @API("/link")
    suspend fun link(@RP token: Str, @RP cardId: Str, @RP migrate: Str) = jwt.auth(token) { u ->
        // Check if the user's card limit is reached
        if (u.cards.size >= props.linkCardLimit) 400 - "Card limit reached"

        // Try to look up the card
        val card = cardService.tryLookup(cardId)

        // If no card is found, create a new card
        if (card == null) {
            // Ensure the format of the card ID is correct
            val id = cardService.sanitizeCardId(cardId)

            // Create a new card
            cardService.registerByAccessCode(id, u)

            return SUCCESS
        }

        // If card is already bound
        if (card.aquaUser != null) 400 - "Card already bound to another user"

        // Bind the card
        card.aquaUser = u
        async { cardRepository.save(card) }

        // Migrate selected data to the new user
        val games = migrate.split(',')
        cardGameService.migrate(card, games)

        SUCCESS
    }
}

@Service
class CardGameService(
    val maimai: icu.samnyan.aqua.sega.maimai.dao.userdata.UserDataRepository,
    val maimai2: icu.samnyan.aqua.sega.maimai2.dao.userdata.UserDataRepository,
    val chusan: icu.samnyan.aqua.sega.chusan.dao.userdata.UserDataRepository,
    val chunithm: icu.samnyan.aqua.sega.chunithm.dao.userdata.UserDataRepository,
    val ongeki: icu.samnyan.aqua.sega.ongeki.dao.userdata.UserDataRepository,
    val diva: icu.samnyan.aqua.sega.diva.dao.userdata.PlayerProfileRepository,
) {
    suspend fun migrate(crd: Card, games: List<String>) = async {
        // Migrate data from the card to the user's ghost card
        // An easy migration is to change the UserData card field to the user's ghost card
        games.forEach { game ->
            when (game) {
                "maimai" -> maimai.findByCard_ExtId(crd.extId).getOrNull()?.let {
                    maimai.save(it.apply { card = crd.aquaUser!!.ghostCard })
                }
                "maimai2" -> maimai2.findByCard_ExtId(crd.extId).getOrNull()?.let {
                    maimai2.save(it.apply { card = crd.aquaUser!!.ghostCard })
                }
                "chusan" -> chusan.findByCard_ExtId(crd.extId).getOrNull()?.let {
                    chusan.save(it.apply { card = crd.aquaUser!!.ghostCard })
                }
                "chunithm" -> chunithm.findByCard_ExtId(crd.extId).getOrNull()?.let {
                    chunithm.save(it.apply { card = crd.aquaUser!!.ghostCard })
                }
                "ongeki" -> ongeki.findByCard_ExtId(crd.extId).getOrNull()?.let {
                    ongeki.save(it.apply { card = crd.aquaUser!!.ghostCard })
                }
                // TODO: diva
//                "diva" -> diva.findByPdId(card.extId.toInt()).getOrNull()?.let {
//                    it.pdId = card.aquaUser!!.ghostCard
//                }
            }
        }
    }

    suspend fun getSummary(card: Card) = async { mapOf(
        "maimai" to maimai.findByCard_ExtId(card.extId).getOrNull()?.let {
            mapOf(
                "name" to it.userName,
                "rating" to it.playerRating,
                "lastLogin" to it.lastPlayDate,
            )
        },
        "maimai2" to maimai2.findByCard_ExtId(card.extId).getOrNull()?.let {
            mapOf(
                "name" to it.userName,
                "rating" to it.playerRating,
                "lastLogin" to it.lastPlayDate,
            )
        },
        "chusan" to chusan.findByCard_ExtId(card.extId).getOrNull()?.let {
            mapOf(
                "name" to it.userName,
                "rating" to it.playerRating,
                "lastLogin" to it.lastPlayDate,
            )
        },
        "chunithm" to chunithm.findByCard_ExtId(card.extId).getOrNull()?.let {
            mapOf(
                "name" to it.userName,
                "rating" to it.playerRating,
                "lastLogin" to it.lastPlayDate,
            )
        },
        "ongeki" to ongeki.findByCard_ExtId(card.extId).getOrNull()?.let {
            mapOf(
                "name" to it.userName,
                "rating" to it.playerRating,
                "lastLogin" to it.lastPlayDate,
            )
        },
        "diva" to diva.findByPdId(card.extId.toInt()).getOrNull()?.let {
            mapOf(
                "name" to it.playerName,
                "rating" to it.level,
            )
        },
    ) }
}