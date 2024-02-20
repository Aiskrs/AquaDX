package icu.samnyan.aqua.net.db

import ext.Str
import ext.isValidEmail
import ext.minus
import icu.samnyan.aqua.sega.general.model.Card
import jakarta.persistence.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import java.io.Serializable
import kotlin.reflect.KFunction
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.functions

@Entity(name = "AquaNetUser")
@Table(name = "aqua_net_user")
class AquaNetUser(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var auId: Long = 0,

    @Column(nullable = false, unique = true, length = 32)
    var username: String = "",

    // Login credentials
    @Column(nullable = false, unique = true)
    var email: String = "",
    @Column(nullable = false)
    var pwHash: String = "",

    @Column(nullable = true, length = 32)
    var displayName: String = "",

    // Country code at most 3 characters
    @Column(length = 3)
    var country: String = "",

    // Last login time
    var lastLogin: Long = 0L,

    // Registration time
    var regTime: Long = 0L,

    // Profile fields
    var profileLocation: String = "",
    var profileBio: String = "",

    // Email confirmation
    var emailConfirmed: Boolean = false,

    @OneToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "ghostCard", unique = true, nullable = false)
    var ghostCard: Card = Card(),

    // One user can have multiple cards
    @OneToMany(mappedBy = "aquaUser", cascade = [CascadeType.ALL])
    var cards: MutableList<Card> = mutableListOf()
) : Serializable {
    val computedName get() = displayName.ifEmpty { username }
}

@Repository("AquaNetUserRepository")
interface AquaNetUserRepo : JpaRepository<AquaNetUser, Long> {
    fun findByAuId(auId: Long): AquaNetUser?
    fun findByEmailIgnoreCase(email: String): AquaNetUser?
    fun findByUsernameIgnoreCase(username: String): AquaNetUser?
}

data class SettingField(
    val name: Str,
    val checker: KFunction<*>,
    val setter: KMutableProperty.Setter<*>,
)

/**
 * This class is a validator for user fields. It will return the parsed value if the field is valid, or
 * throw an ApiException if the field is invalid.
 */
@Service
class AquaUserValidator(
    val userRepo: AquaNetUserRepo,
    val hasher: PasswordEncoder,
) {
    companion object {
        val SETTING_FIELDS = AquaUserValidator::class.functions
            .filter { it.name.startsWith("check") }
            .map {
                val name = it.name.removePrefix("check").replaceFirstChar { c -> c.lowercase() }
                val prop = AquaNetUser::class.members.find { m -> m.name == name } as KMutableProperty<*>
                SettingField(name, it, prop.setter)
            }
    }

    fun checkUsername(username: Str) = username.apply {
        // Check if username is valid
        if (length < 2) 400 - "Username must be at least 2 letters"
        if (length > 32) 400 - "Username too long (max 32 letters)"
        if (contains(" ")) 400 - "Username cannot contain spaces"

        // Check if username is within A-Za-z0-9_-~.
        find { !it.isLetterOrDigit() && it != '_' && it != '-' && it != '~' && it != '.' }?.let {
            400 - "Username cannot contain `$it`. Please only use letters (A-Z), numbers (0-9), and `_-~.` characters. You can set a display name later."
        }

        // Check if user with the same username exists
        if (userRepo.findByUsernameIgnoreCase(this) != null)
            400 - "User with username `$this` already exists"
    }

    fun checkEmail(email: Str) = email.apply {
        // Check if email is valid
        if (!isValidEmail()) 400 - "Invalid email"

        // Check if user with the same email exists
        if (userRepo.findByEmailIgnoreCase(email) != null)
            400 - "User with email `$email` already exists"
    }

    fun checkPwHash(password: Str) = password.run {
        // Validate password
        if (length < 8) 400 - "Password must be at least 8 characters"

        hasher.encode(this)
    }

    fun checkDisplayName(displayName: Str) = displayName.apply {
        // Check if display name is valid
        if (length > 32) 400 - "Display name too long (max 32 letters)"
    }

    fun checkProfileLocation(profileLocation: Str) = profileLocation.apply {
        // Check if profile location is valid
        if (length > 64) 400 - "Profile location too long (max 64 letters)"
    }

    fun checkProfileBio(profileBio: Str) = profileBio.apply {
        // Check if profile bio is valid
        if (length > 255) 400 - "Profile bio too long (max 255 letters)"
    }
}