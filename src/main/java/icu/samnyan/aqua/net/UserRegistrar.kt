package icu.samnyan.aqua.net

import ext.*
import icu.samnyan.aqua.net.db.AquaNetUser
import icu.samnyan.aqua.net.db.AquaNetUserRepo
import icu.samnyan.aqua.net.utils.GeoIP
import icu.samnyan.aqua.net.utils.TurnstileService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v2/user")
class UserRegistrar(
    val userRepo: AquaNetUserRepo,
    val hasher: PasswordEncoder,
    val turnstileService: TurnstileService,
    val geoIP: GeoIP
) {
    /**
     * Register a new user
     */
    @PostMapping("/register")
    suspend fun register(@RP username: Str, @RP email: Str, @RP password: Str,
                         @RP turnstile: Str?, request: HttpServletRequest) {
        val ip = geoIP.getIP(request)

        // Check captcha
        if (!turnstileService.validate(turnstile, ip)) 400 > "Invalid captcha"

        // Check if email is valid
        if (!email.isValidEmail()) 400 > "Invalid email"

        // Check if user with the same email exists
        if (async { userRepo.existsByEmail(email) }) 400 > "User already exists"

        // Check if username is valid
        if (username.length < 2) 400 > "Username too short (min 2 letters)"
        if (username.length > 48) 400 > "Username too long (max 48 letters)"
        if (username.contains(" ")) 400 > "Username cannot contain spaces"

        // Check if username is within A-Za-z0-9_-~.
        username.find { !it.isLetterOrDigit() && it != '_' && it != '-' && it != '~' && it != '.' }?.let {
            400 > "Username cannot contain `$it`. Please only use letters (A-Z), numbers (0-9), and `_-~.` characters. " +
                "You can set a display name later."
        }

        // Validate password
        if (password.length < 8) 400 > "Password too short"

        // GeoIP check to infer country
        val country = geoIP.getCountry(ip)

        val u = AquaNetUser(username = username, email = email, pwHash = hasher.encode(password),
            regTime = millis(), lastLogin = millis(), country = country)
        async { userRepo.save(u) }

        // TODO: Send confirmation email

        200 > "User created"
    }
}