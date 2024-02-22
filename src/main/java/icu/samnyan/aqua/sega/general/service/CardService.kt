package icu.samnyan.aqua.sega.general.service

import icu.samnyan.aqua.sega.general.dao.CardRepository
import icu.samnyan.aqua.sega.general.model.Card
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import kotlin.jvm.optionals.getOrNull

/**
 * @author samnyan (privateamusement@protonmail.com)
 */
@Service
class CardService(val cardRepo: CardRepository) {
    /**
     * Find a card by External ID
     * @param extId External ID
     * @return Optional of a Card
     */
    fun getCardByExtId(extId: String): Optional<Card> = cardRepo.findByExtId(extId.toLong())

    /**
     * Find a card by External ID
     *
     * @param extId External ID
     * @return Optional of a Card
     */
    fun getCardByExtId(extId: Long?): Optional<Card> = cardRepo.findByExtId(extId)

    /**
     * Find a card by its access code
     * @param accessCode String represent of an access code
     * @return Optional of a Card
     */
    fun getCardByAccessCode(accessCode: String?): Optional<Card> = cardRepo.findByLuid(accessCode)

    /**
     * Register a new card with access code
     * @param accessCode String represent of an access code
     * @return a new registered Card
     */
    fun registerByAccessCode(accessCode: String): Card = cardRepo.save(Card().apply {
        luid = accessCode
        extId = randExtID()
        registerTime = LocalDateTime.now()
        accessTime = registerTime
    })

    /**
     * Try to look up a card by a string ID. This ID could be one of the following:
     *
     * 1. An AIME access code (as a string stored directly in `luid` field)
     * 2. A Felica IDm (as a hex string 01:2E:XX:XX:XX:XX:XX:XX), converted to int64 and stored in `luid` field
     * 3. An NFC Serial (as a hex string XX:XX:XX:XX), converted to Felica by masking it with 01:2E:FF:FF:FF:FF:FF:FF
     * 4. Any of (2) or (3) converted to a decimal string, which is honestly the same as (1) so ignore this case
     */
    fun tryLookup(id: String): Card? {
        // First remove the colons and spaces
        val idm = id.replace(":", "").replace(" ", "")

        // Check case (1) and (4)
        cardRepo.findByLuid(idm)?.getOrNull()?.let { return it }
        cardRepo.findByLuid(idm.padStart(20, '0'))?.getOrNull()?.let { return it }

        // Check case (2)
        // Then convert to long, left pad zeros to make 20 digits, and look up
        idm.toLongOrNull(16)?.let { idmLong ->
            cardRepo.findByLuid("%020d".format(idmLong))?.getOrNull()?.let { return it }
        }

        // Check case (3)
        idm.padStart(16, '0').takeLast(12).let { "012E$it" }.let { idmMasked ->
            idmMasked.toLongOrNull(16)?.let { idmMaskedLong ->
                cardRepo.findByLuid("%020d".format(idmMaskedLong))?.getOrNull()?.let { return it }
            }
        }

        // If nothing is found, return null
        return null
    }

    fun randExtID(lower: Long = 0, upper: Long = 99999999): Long {
        var eid = ThreadLocalRandom.current().nextLong(lower, upper)
        while (cardRepo.findByExtId(eid).isPresent) {
            eid = ThreadLocalRandom.current().nextLong(lower, upper)
        }
        return eid
    }
}
