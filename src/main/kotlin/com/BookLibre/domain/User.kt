package com.BookLibre.domain

import com.BookLibre.error.BusinessException
import jakarta.persistence.*
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.LocalDate

@Embeddable
data class UserProps (
    @Column(nullable = false) var fullName: String = "",
    @Column(length = 500) var description: String = "",
    @Column(unique = true, nullable = false) var email: String = "",
    @Column var imgUrl: String = "",
    @Column var phone: String = "",
    @Column var city: String = "",
    @Column var biblioKarmas: Int = 0,
    @Column var password: String = ""
)

@Entity
@Table(name = "usuarios")
class User(
    @Embedded
    private val props: UserProps
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null

    @Column
    private var registerDate: LocalDate = LocalDate.now()

    // Book ya no es @Entity JPA (vive en MongoDB), no puede mapearse con @OneToMany.
    // @Transient de JPA lo excluye del schema; @Transient de Spring Data lo excluye
    // de la serialización MongoDB cuando User se embebe dentro de un documento Book.
    @jakarta.persistence.Transient
    @org.springframework.data.annotation.Transient
    private var ownedBooks: MutableList<Book> = mutableListOf()

    // @Transient de Spring Data evita ciclos cuando User se embebe en el documento Book
    @OneToMany(mappedBy = "reader", cascade = [CascadeType.ALL])
    @org.springframework.data.annotation.Transient
    private var reservations: MutableList<Reservation> = mutableListOf()

    fun update(
        fullName: String,
        email: String,
        description: String,
        phone: String,
        city: String,
        imgUrl: String
    ) {
        if (fullName.isBlank()) throw BusinessException("El nombre es requerido")
        if (email.isBlank()) throw BusinessException("El email es requerido")
        if (phone.isBlank()) throw BusinessException("El teléfono es requerido")
        if (city.isBlank()) throw BusinessException("La ciudad es requerida")
        if (description.isBlank()) throw BusinessException("La descripcion esta vacia")

        setName(fullName)
        setEmail(email)
        setPhone(phone)
        setCity(city)
        setImgUrl(imgUrl)
        setDescription(description)
    }

    fun getAmoutReservations(): Int {
        return getReservations().size
    }

    fun getLentBooks() = getOwnedBooks().filter { book -> book.getReservations().isNotEmpty() }

    fun getLentBooksAmout() = getLentBooks().size

    fun getReadedBooksAmount() = getReservations()
        .filter { res -> res.reader == this }
        .distinctBy { it.bookId }
        .size

    fun getBooksAvailableNow() = getOwnedBooks().filter { book -> book.isAvailableNow() }

    fun getReservedBooks() = getReservations().filter { it.reader == this }

    fun getReservationsFromMyBooks(): List<ReservationEmbed> {
        return getOwnedBooks().flatMap { it.getReservations() }
    }

    // Setters
    fun addReservation(reservation: Reservation) {
        reservations.add(reservation)
    }
    fun addBook(book: Book) {
        getOwnedBooks().add(book)
    }

    fun addBiblioKarmas(biblioKarmas: Int) {
        props.biblioKarmas += biblioKarmas
    }

    fun setPassword(passwordHash: String) {
        props.password = passwordHash
    }

    fun removeBook(book: Book) = getOwnedBooks().remove(book)

    fun setName(name: String) { props.fullName = name }
    fun setEmail(email: String) { props.email = email }
    fun setDescription(description: String) { props.description = description }
    fun setCity(city: String) { props.city = city }
    fun setPhone(phone: String) { props.phone = phone }
    fun setImgUrl(imgUrl: String) { props.imgUrl = imgUrl }

    fun setOwnedBooks(books: MutableList<Book>) { this.ownedBooks = books }
    fun setReservations(reservations: MutableList<Reservation>) { this.reservations = reservations }

    // Getters
    fun getId(): Int { return id ?: throw RuntimeException("User has no ID") }
    fun getFullName() = props.fullName
    fun getEmail() = props.email
    fun getCity() = props.city
    fun getDescription() = props.description
    fun getPhone() = props.phone
    fun getRoles(): List<String> = roles.map { it.name }
    fun getImgUrl() = props.imgUrl
    fun getPassword(): String { return props.password }
    fun getBiblioKarmas(): Int { return props.biblioKarmas }
    fun getRegisterDate() = registerDate

    // El constructor no-arg de JPA no inicializa campos con valores por defecto,
    // por lo que pueden quedar null en tiempo de ejecución.
    fun getReservations(): MutableList<Reservation> = reservations
    fun getOwnedBooks(): MutableList<Book> {
        if ((ownedBooks as? MutableList<Book>) == null) ownedBooks = mutableListOf()
        return ownedBooks
    }

    // ROLES

    @ManyToMany
    @JoinTable(
        name = "user_types",
        joinColumns = [JoinColumn(name = "user_id", referencedColumnName = "id")],
        inverseJoinColumns = [JoinColumn(name = "role_id", referencedColumnName = "id")]
    )
    private var roles: MutableSet<Role> = mutableSetOf()

    fun addRole(role: Role) = roles.add(role)

    fun setRoles(roles: MutableSet<Role>) {
        if (roles.isEmpty()) throw BusinessException("User must have roles")
        this.roles.clear()
        this.roles.addAll(roles)
    }

    private fun getDefaultEncoder(): PasswordEncoder {
        return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8()!!
    }

    fun validatePassword(password: String) =
        getDefaultEncoder().matches(password, getPassword())


    fun createPassword(rawPassword: String) {
        props.password = getDefaultEncoder().encode(rawPassword)
    }

    // el tipo de User lo provee Spring
    fun builUser() = User(getEmail(), getPassword(), roles.map { SimpleGrantedAuthority(it.name) })
}

@Entity
@Table(name = "roles")
class Role(
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
    var name: String = ""
}


enum class ROLES (val roleName: String) {
    PUBLISHER("PUBLISHER"), READER("READER")
}
