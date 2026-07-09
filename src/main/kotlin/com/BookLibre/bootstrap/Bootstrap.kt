package com.BookLibre.bootstrap

import com.BookLibre.domain.*
import com.BookLibre.repository.BookRepository
import com.BookLibre.repository.ReservationRepository
import com.BookLibre.repository.RoleRepository
import com.BookLibre.repository.UserRepository
import com.BookLibre.service.ClickService
import com.BookLibre.service.ReservationService
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.time.LocalDate
import java.time.LocalDateTime


@Configuration
class Bootstrap(private val roleRepository: RoleRepository) {

    @Bean
    fun initData(
        userRepository: UserRepository,
        bookRepository: BookRepository,
        reservationRepository: ReservationRepository,
        reservationService: ReservationService,
        roleRepository: RoleRepository,
        clickService: ClickService
    ) = CommandLineRunner {

        val reader = crearRolSiNoExiste("READER")
        val publisher = crearRolSiNoExiste("PUBLISHER")

        when {
            userRepository.count() > 0 -> return@CommandLineRunner
            bookRepository.count() > 0 -> return@CommandLineRunner
            reservationRepository.count() > 0 -> return@CommandLineRunner
        }

        val passwordEncoder = BCryptPasswordEncoder()

        // =========================
        // 👤 USUARIOS
        // =========================

        val alice = User(UserProps().apply {
            fullName = "Alice Reader"
            description = "Lectora apasionada de clásicos"
            email = "alice@example.com"
            password = passwordEncoder.encode("admin")
            phone = "+123456789"
            city = "Buenos Aires"
            biblioKarmas = 100
        }).apply { setRoles(mutableSetOf(reader))}

        val bob = User(UserProps().apply {
            fullName = "Bob Publisher"
            description = "Publica libros de ficción y distopía"
            email = "bob@example.com"
            password = passwordEncoder.encode("admin")
            phone = "+987654321"
            city = "Córdoba"
            biblioKarmas = 50
        }).apply { setRoles(mutableSetOf(publisher))}

        val charlie = User(UserProps().apply {
            fullName = "Charlie Owner"
            description = "Coleccionista de libros de fantasía"
            email = "charlie@example.com"
            password = passwordEncoder.encode("admin")
            phone = "+555666777"
            city = "Rosario"
            biblioKarmas = 75
        }).apply { setRoles(mutableSetOf(publisher))}

        val juan = User(UserProps().apply {
            fullName = "Juan Pérez"
            description = "Lector apasionado de novelas"
            email = "juan.perez@email.com"
            phone = "1112345678"
            city = "Buenos Aires"
            biblioKarmas = 10
            password = passwordEncoder.encode("VivoenArg")
        }).apply { setRoles(mutableSetOf(publisher, reader))}

        val maria = User(UserProps().apply {
            fullName = "María López"
            description = "Publica libros de ciencia ficción"
            email = "maria.lopez@email.com"
            phone = "1198765432"
            city = "Mendoza"
            biblioKarmas = 50
            password = passwordEncoder.encode("Messielmejor")
        }).apply { setRoles(mutableSetOf(publisher))}

        val carlos = User(UserProps().apply {
            fullName = "Carlos Gómez"
            description = "Lector y escritor amateur"
            email = "carlos.gomez@email.com"
            phone = "1177778888"
            city = "Rosario"
            biblioKarmas = 25
            password = passwordEncoder.encode("admin")
        }).apply { setRoles(mutableSetOf(reader))}

        listOf(alice, bob, charlie, juan, maria, carlos).forEach { userRepository.save(it) }
        /*listOf(alice, bob, charlie, juan, maria, carlos).forEach { userRepository.create(it) }*/

        // =========================
        // 📚 LIBROS — CommonStrategy (8)
        // =========================

        val commonBooks = listOf(
            Book(BookProps().apply {
                title = "El Aleph"
                genre = "Ficción"
                author = "Jorge Luis Borges"
                description = "Libro de cuentos fantásticos"
                language = "Español"
                publicationDate = LocalDate.parse("1949-09-15")
                editorial = "Sur"
                state = BookState.BUENO
                totalPages = 146
                isbn = "9780307476463"
                imgUrl = "https://images.cdn2.buscalibre.com/fit-in/360x360/5f/ef/5feffdeb93f4de626b37dcf36d8389f0.jpg"
                type = CommonStrategy()
                owner = bob
            }),
            Book(BookProps().apply {
                title = "Cien años de soledad"
                genre = "Realismo mágico"
                author = "Gabriel García Márquez"
                description = "Novela emblemática del realismo mágico"
                language = "Español"
                publicationDate = LocalDate.parse("1967-05-30")
                editorial = "Sudamericana"
                state = BookState.EXCELENTE
                totalPages = 417
                isbn = "9780307474728"
                imgUrl = "https://www.edicontinente.com.ar/image/titulos/9788466379717.jpg"
                type = CommonStrategy()
                owner = charlie
            }),
            Book(BookProps().apply {
                title = "The Eye of the World"
                genre = "Fantasía"
                author = "Robert Jordan"
                description = "Primera entrega de La Rueda del Tiempo"
                language = "Inglés"
                publicationDate = LocalDate.parse("1990-01-15")
                editorial = "Tor Books"
                state = BookState.EXCELENTE
                totalPages = 900
                isbn = "9780765305343"
                imgUrl = "https://upload.wikimedia.org/wikipedia/en/0/00/WoT01_TheEyeOfTheWorld.jpg"
                type = CommonStrategy()
                owner = charlie
            }),
            Book(BookProps().apply {
                title = "Don Quijote de la Mancha"
                genre = "Clásico"
                author = "Miguel de Cervantes"
                description = "La novela más influyente en lengua española"
                language = "Español"
                publicationDate = LocalDate.parse("1605-01-16")
                editorial = "Francisco de Robles"
                state = BookState.REGULAR
                totalPages = 863
                isbn = "9788420412146"
                imgUrl = "https://images.cdn2.buscalibre.com/fit-in/360x360/73/b6/73b6fd96c31d26e2b6a3531808c1188c.jpg"
                type = CommonStrategy()
                owner = juan
            }),
            Book(BookProps().apply {
                title = "Ficciones"
                genre = "Ficción"
                author = "Jorge Luis Borges"
                description = "Colección de relatos filosóficos y fantásticos"
                language = "Español"
                publicationDate = LocalDate.parse("1944-01-01")
                editorial = "Sur"
                state = BookState.BUENO
                totalPages = 174
                isbn = "9788420633138"
                imgUrl = "https://acdn-us.mitiendanube.com/stores/001/542/126/products/9789875666474-9169a61b8a651143a816944491431237-640-0.webp"
                type = CommonStrategy()
                owner = bob
            }),
            Book(BookProps().apply {
                title = "Rayuela"
                genre = "Literatura latinoamericana"
                author = "Julio Cortázar"
                description = "Novela experimental y vanguardista"
                language = "Español"
                publicationDate = LocalDate.parse("1963-06-28")
                editorial = "Sudamericana"
                state = BookState.MUY_BUENO
                totalPages = 635
                isbn = "9788437604572"
                imgUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQxLpM2_MpC07MgDNZF7wxTjg7sIFmgUXOiMQ&s"
                type = CommonStrategy()
                owner = maria
            }),
            Book(BookProps().apply {
                title = "El nombre de la rosa"
                genre = "Misterio histórico"
                author = "Umberto Eco"
                description = "Thriller medieval ambientado en una abadía"
                language = "Italiano"
                publicationDate = LocalDate.parse("1980-10-01")
                editorial = "Bompiani"
                state = BookState.BUENO
                totalPages = 502
                isbn = "9780151446474"
                imgUrl = "https://m.media-amazon.com/images/S/pv-target-images/a56a3906b4862a1b9e4ace451a3d0e38f7e6a47a4f0154c873477fae5dd1c0eb.jpg"
                type = CommonStrategy()
                owner = charlie
            }),
            Book(BookProps().apply {
                title = "El túnel"
                genre = "Psicológico"
                author = "Ernesto Sabato"
                description = "Novela corta sobre la obsesión y la soledad"
                language = "Español"
                publicationDate = LocalDate.parse("1948-01-01")
                editorial = "Sur"
                state = BookState.MUY_BUENO
                totalPages = 128
                isbn = "9788420633374"
                imgUrl = "https://contentv2.tap-commerce.com/cover/large/9788432248368_1.jpg?id_com=1156"
                type = CommonStrategy()
                owner = juan
            })
        )

        // =========================
        // 📚 LIBROS — DedicationStrategy (8)
        // =========================

        val dedicationBooks = listOf(
            Book(BookProps().apply {
                title = "1984"
                genre = "Distopía"
                author = "George Orwell"
                description = "Novela distópica sobre el totalitarismo"
                language = "Inglés"
                publicationDate = LocalDate.parse("1949-06-08")
                editorial = "Secker & Warburg"
                state = BookState.MUY_BUENO
                totalPages = 328
                isbn = "9780451524935"
                imgUrl = "https://images.cdn1.buscalibre.com/fit-in/360x360/67/c9/67c9014aec833f3b2ef2952e6e6cd56a.jpg"
                type = DedicationStrategy()
                owner = bob
            }),
            Book(BookProps().apply {
                title = "Beloved"
                genre = "Drama histórico"
                author = "Toni Morrison"
                description = "Historia sobre la esclavitud y sus secuelas"
                language = "Inglés"
                publicationDate = LocalDate.parse("1987-09-16")
                editorial = "Alfred A. Knopf"
                state = BookState.EXCELENTE
                totalPages = 321
                isbn = "9781400033416"
                imgUrl = "https://nidodelibros.com/wp-content/uploads/2023/02/9781400033416.jpeg"
                type = DedicationStrategy()
                owner = maria
            }),
            Book(BookProps().apply {
                title = "El amor en los tiempos del cólera"
                genre = "Romance"
                author = "Gabriel García Márquez"
                description = "Historia de amor que trasciende el tiempo"
                language = "Español"
                publicationDate = LocalDate.parse("1985-10-01")
                editorial = "Oveja Negra"
                state = BookState.BUENO
                totalPages = 348
                isbn = "9780307389732"
                imgUrl = "https://upload.wikimedia.org/wikipedia/commons/e/e9/El_amor_en_los_tiempos_del_c%C3%B3lera.png"
                type = DedicationStrategy()
                owner = charlie
            }),
            Book(BookProps().apply {
                title = "Brave New World"
                genre = "Ciencia ficción distópica"
                author = "Aldous Huxley"
                description = "Futuro controlado por el placer y el consumo"
                language = "Inglés"
                publicationDate = LocalDate.parse("1932-08-01")
                editorial = "Chatto & Windus"
                state = BookState.MUY_BUENO
                totalPages = 311
                isbn = "9780060850524"
                imgUrl = "https://images.cdn3.buscalibre.com/fit-in/360x360/a9/5f/a95f66e0851fceae573cc3bbe12b930f.jpg"
                type = DedicationStrategy()
                owner = juan
            }),
            Book(BookProps().apply {
                title = "El gran Gatsby"
                genre = "Drama"
                author = "F. Scott Fitzgerald"
                description = "Crítica del sueño americano en los años 20"
                language = "Inglés"
                publicationDate = LocalDate.parse("1925-04-10")
                editorial = "Scribner"
                state = BookState.REGULAR
                totalPages = 180
                isbn = "9780743273565"
                imgUrl = "https://cms.anagrama-ed.es/uploads/media/portadas/0001/15/b2834bc4ea71357c8b549dfccdd16d611c6586ea.jpeg"
                type = DedicationStrategy()
                owner = bob
            }),
            Book(BookProps().apply {
                title = "Fahrenheit 451"
                genre = "Ciencia ficción"
                author = "Ray Bradbury"
                description = "Un mundo donde los libros están prohibidos"
                language = "Inglés"
                publicationDate = LocalDate.parse("1953-10-19")
                editorial = "Ballantine Books"
                state = BookState.BUENO
                totalPages = 158
                isbn = "9781451673319"
                imgUrl = "https://images.cdn1.buscalibre.com/fit-in/360x360/39/0c/390cf389c0c83ef393d8a0b763e856c0.jpg"
                type = DedicationStrategy()
                owner = charlie
            }),
            Book(BookProps().apply {
                title = "La insoportable levedad del ser"
                genre = "Filosófico"
                author = "Milan Kundera"
                description = "Reflexión sobre el amor y la existencia"
                language = "Checo"
                publicationDate = LocalDate.parse("1984-04-01")
                editorial = "Gallimard"
                state = BookState.EXCELENTE
                totalPages = 314
                isbn = "9780061148521"
                imgUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQPOoHZjvNvKPv6dXln1babYkuzbdIlCswImg&s"
                type = DedicationStrategy()
                owner = maria
            }),
            Book(BookProps().apply {
                title = "Orgullo y prejuicio"
                genre = "Romance clásico"
                author = "Jane Austen"
                description = "Comentario social sobre el matrimonio y la clase"
                language = "Inglés"
                publicationDate = LocalDate.parse("1813-01-28")
                editorial = "T. Egerton"
                state = BookState.MUY_BUENO
                totalPages = 432
                isbn = "9780141439518"
                imgUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSjykXuoU-pVtZYP4C91G9YNTdOKIf6_KapOg&s"
                type = DedicationStrategy()
                owner = juan
            })
        )

        // =========================
        // 📚 LIBROS — CollectableStrategy / tercer tipo (8)
        // =========================

        val exchangeBooks = listOf(
            Book(BookProps().apply {
                title = "Harry Potter y la piedra filosofal"
                genre = "Fantasía juvenil"
                author = "J.K. Rowling"
                description = "Inicio de la saga del mago más famoso del mundo"
                language = "Español"
                publicationDate = LocalDate.parse("1997-06-26")
                editorial = "Bloomsbury"
                state = BookState.BUENO
                totalPages = 309
                isbn = "9788478884452"
                imgUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQGDT64bDRNQ0IOTK6ysRFFFBstgyXRvmy2Zw&s"
                type = CollectableStrategy()
                owner = bob
            }),
            Book(BookProps().apply {
                title = "El señor de los anillos: La comunidad del anillo"
                genre = "Fantasía épica"
                author = "J.R.R. Tolkien"
                description = "El inicio del viaje para destruir el Anillo Único"
                language = "Español"
                publicationDate = LocalDate.parse("1954-07-29")
                editorial = "Allen & Unwin"
                state = BookState.EXCELENTE
                totalPages = 479
                isbn = "9788445071526"
                imgUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSeRTiUfWHMRxuDMACFW4URTVdEWb-uz8PahA&s"
                type = CollectableStrategy()
                owner = charlie
            }),
            Book(BookProps().apply {
                title = "Dune"
                genre = "Ciencia ficción"
                author = "Frank Herbert"
                description = "Épica galáctica sobre política, religión y ecología"
                language = "Inglés"
                publicationDate = LocalDate.parse("1965-08-01")
                editorial = "Chilton Books"
                state = BookState.MUY_BUENO
                totalPages = 688
                isbn = "9780441013593"
                imgUrl = "https://images.cdn1.buscalibre.com/fit-in/360x360/86/9f/869fa8cf3c087c954b6a3b7293c31469.jpg"
                type = CollectableStrategy()
                owner = juan
            }),
            Book(BookProps().apply {
                title = "Sapiens"
                genre = "Historia"
                author = "Yuval Noah Harari"
                description = "Breve historia de la humanidad"
                language = "Español"
                publicationDate = LocalDate.parse("2011-01-01")
                editorial = "Debate"
                state = BookState.BUENO
                totalPages = 496
                isbn = "9788499924212"
                imgUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSyAJxSA62_Z8k-pqxGp8x3Zp1bWoAtBjkZow&s"
                type = CollectableStrategy()
                owner = maria
            }),
            Book(BookProps().apply {
                title = "El principito"
                genre = "Fábula"
                author = "Antoine de Saint-Exupéry"
                description = "Cuento filosófico sobre la infancia y la vida"
                language = "Español"
                publicationDate = LocalDate.parse("1943-04-06")
                editorial = "Reynal & Hitchcock"
                state = BookState.REGULAR
                totalPages = 96
                isbn = "9788467036763"
                imgUrl = "https://acdn-us.mitiendanube.com/stores/001/640/786/products/681b63dd7d9dbb4c4ce5ae76_wjlunxlgnrzqh3hn_u7wmentvs1tv0qkwtukvxj2jtk-60cc3c300fd82ac29417570028547914-1024-1024.webp"
                type = CollectableStrategy()
                owner = alice
            }),
            Book(BookProps().apply {
                title = "Crimen y castigo"
                genre = "Drama psicológico"
                author = "Fyodor Dostoevsky"
                description = "El dilema moral de un estudiante que comete un crimen"
                language = "Ruso"
                publicationDate = LocalDate.parse("1866-01-01")
                editorial = "The Russian Messenger"
                state = BookState.BUENO
                totalPages = 545
                isbn = "9780140449136"
                imgUrl = "https://editorialverbum.es/wp-content/uploads/2019/11/Crimen-y-castigo-1.webp"
                type = CollectableStrategy()
                owner = carlos
            }),
            Book(BookProps().apply {
                title = "Matar a un ruiseñor"
                genre = "Drama social"
                author = "Harper Lee"
                description = "Injusticia racial en el sur de Estados Unidos"
                language = "Inglés"
                publicationDate = LocalDate.parse("1960-07-11")
                editorial = "J. B. Lippincott & Co."
                state = BookState.MUY_BUENO
                totalPages = 281
                isbn = "9780061743528"
                imgUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTs9w3RrPG5lzupUrpTOOS10qFZyJjJWUPOOQ&s"
                type = CollectableStrategy()
                owner = bob
            }),
            Book(BookProps().apply {
                title = "El retrato de Dorian Gray"
                genre = "Gótico"
                author = "Oscar Wilde"
                description = "La corrupción de un joven a través de la belleza eterna"
                language = "Inglés"
                publicationDate = LocalDate.parse("1890-07-01")
                editorial = "Ward, Lock and Company"
                state = BookState.EXCELENTE
                totalPages = 254
                isbn = "9780141439570"
                imgUrl = "https://images.cdn2.buscalibre.com/fit-in/360x360/91/18/9118645bef1e527a3e1f14e7187ac89e.jpg"
                type = CollectableStrategy()
                owner = charlie
            })
        )
        // Guardar todos los libros (CAMBIAR CREATE POR SAVE)
        (commonBooks + dedicationBooks + exchangeBooks).forEach { book ->
            bookRepository.save(book)
        }

        // Asignar libros a sus dueños
        val userBooks = mapOf(
            alice   to (commonBooks + dedicationBooks + exchangeBooks).filter { it.getOwner() == alice },
            bob     to (commonBooks + dedicationBooks + exchangeBooks).filter { it.getOwner() == bob },
            charlie to (commonBooks + dedicationBooks + exchangeBooks).filter { it.getOwner() == charlie },
            juan    to (commonBooks + dedicationBooks + exchangeBooks).filter { it.getOwner() == juan },
            maria   to (commonBooks + dedicationBooks + exchangeBooks).filter { it.getOwner() == maria },
            carlos  to (commonBooks + dedicationBooks + exchangeBooks).filter { it.getOwner() == carlos }
        )

        userBooks.forEach { (user, books) ->
            books.forEach { user.addBook(it) }
            userRepository.save(user) // Sincronizamos las relaciones en Postgres
        }

        // =========================
        // 🔥 CLICKS EN REDIS (top 10)
        // =========================

        data class BookClicks(val book: Book, val username: String, val times: Int)

        val clickData = listOf(
            BookClicks(commonBooks[1],    "alice@example.com",       18), // Cien años de soledad
            BookClicks(exchangeBooks[0],  "juan.perez@email.com",    15), // Harry Potter
            BookClicks(dedicationBooks[0],"carlos.gomez@email.com",  14), // 1984
            BookClicks(exchangeBooks[1],  "alice@example.com",       12), // El señor de los anillos
            BookClicks(commonBooks[0],    "maria.lopez@email.com",   11), // El Aleph
            BookClicks(exchangeBooks[2],  "bob@example.com",         10), // Dune
            BookClicks(dedicationBooks[5],"charlie@example.com",      9), // Fahrenheit 451
            BookClicks(commonBooks[3],    "juan.perez@email.com",     8), // Don Quijote
            BookClicks(exchangeBooks[4],  "carlos.gomez@email.com",   7), // El principito
            BookClicks(dedicationBooks[4],"alice@example.com",        6), // El gran Gatsby
            BookClicks(dedicationBooks[7],"maria.lopez@email.com",    5), // Orgullo y prejuicio
            BookClicks(commonBooks[5],    "bob@example.com",          4)  // Rayuela
        )

        clickData.forEach { (book, username, times) ->
            repeat(times) {
                clickService.registerClick(
                    ClickLog(username = username, bookId = book.id!!, bookTitle = book.getTitle(), timestamp = LocalDateTime.now()),
                    book
                )
            }
        }

/*        // Guardar todos los libros
        (commonBooks + dedicationBooks + exchangeBooks).forEach { book ->
            bookRepository.create(book)
        }

        // Asignar libros a sus dueños
        val userBooks = mapOf(
            alice   to (commonBooks + dedicationBooks + exchangeBooks).filter { it.getOwner() == alice },
            bob     to (commonBooks + dedicationBooks + exchangeBooks).filter { it.getOwner() == bob },
            charlie to (commonBooks + dedicationBooks + exchangeBooks).filter { it.getOwner() == charlie },
            juan    to (commonBooks + dedicationBooks + exchangeBooks).filter { it.getOwner() == juan },
            maria   to (commonBooks + dedicationBooks + exchangeBooks).filter { it.getOwner() == maria },
            carlos  to (commonBooks + dedicationBooks + exchangeBooks).filter { it.getOwner() == carlos }
        )
        userBooks.forEach { (user, books) -> books.forEach { user.addBook(it) } }*/

        // =========================
        // 📖 RESERVAS (≥2 por usuario)
        // =========================

        // Acceso rápido a libros por tipo y owner para armar reservas variadas
        val cb = commonBooks
        val db = dedicationBooks
        val eb = exchangeBooks

        // --- ALICE (reader): reserva libros de bob y charlie ---
        reservationService.createReservation(bob,     alice,  cb[0], LocalDate.now().plusDays(1),  LocalDate.now().plusDays(8),  "Un clásico atemporal.",                    5.0)
        reservationService.createReservation(bob,     alice,  db[0], LocalDate.now().plusDays(10), LocalDate.now().plusDays(17), "Escalofriante y brillante.",               4.5)
        reservationService.createReservation(charlie, alice,  eb[1], LocalDate.now().plusDays(20), LocalDate.now().plusDays(27), "Épico e inolvidable.",                     5.0)

        // --- BOB (publisher): reserva libros de charlie y juan ---
        reservationService.createReservation(charlie, bob,    cb[1], LocalDate.now().plusDays(2),  LocalDate.now().plusDays(9),  "El realismo mágico en su máxima expresión.", 4.8)
        reservationService.createReservation(juan,    bob,    cb[3], LocalDate.now().plusDays(12), LocalDate.now().plusDays(19))

        // --- CHARLIE (publisher): reserva libros de bob y maria ---
        reservationService.createReservation(bob,     charlie, db[4], LocalDate.now().plusDays(3),  LocalDate.now().plusDays(10), "Una joya de la literatura.",               4.0)
        reservationService.createReservation(maria,   charlie, db[1], LocalDate.now().plusDays(14), LocalDate.now().plusDays(21))

        // --- JUAN (reader+publisher): reserva libros de bob y charlie ---
        reservationService.createReservation(bob,     juan,   cb[4], LocalDate.now().plusDays(5),  LocalDate.now().plusDays(12), "Borges nunca decepciona.",                 4.7)
        reservationService.createReservation(charlie, juan,   eb[2], LocalDate.now().plusDays(15), LocalDate.now().plusDays(22))

        // --- MARIA (publisher): reserva libros de charlie y alice ---
        reservationService.createReservation(charlie, maria,  db[2], LocalDate.now().plusDays(6),  LocalDate.now().plusDays(13), "Romance que trasciende el tiempo.",        5.0)
        reservationService.createReservation(alice,   maria,  eb[4], LocalDate.now().plusDays(18), LocalDate.now().plusDays(25))

        // --- CARLOS (reader): reserva libros de bob y charlie ---
        reservationService.createReservation(bob,     carlos, eb[0], LocalDate.now().plusDays(4),  LocalDate.now().plusDays(11), "¡Mágico desde la primera página!",        5.0)
        reservationService.createReservation(charlie, carlos, cb[2], LocalDate.now().plusDays(16), LocalDate.now().plusDays(23))
    }



    fun crearRolSiNoExiste(roleName: String): Role {
        return roleRepository.findByName(roleName)
            ?: roleRepository.save(Role().apply { name = roleName })
    }
}