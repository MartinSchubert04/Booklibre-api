package com.BookLibre.service

import com.BookLibre.domain.*
import com.BookLibre.dto.*
import com.BookLibre.error.BusinessException
import com.BookLibre.error.NotFoundException
import com.BookLibre.repository.BookRepository
import com.BookLibre.repository.ClickLogRepository
import com.BookLibre.repository.UserRepository
import com.BookLibre.repository.ReservationRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.regex.Pattern

@Service
class BookService(
    val bookRepository: BookRepository,
    val userRepository: UserRepository,
    val reservationRepository: ReservationRepository,
    val reservationService: ReservationService,
    val clickLogRepository: ClickLogRepository,
    val mongoTemplate: MongoTemplate,
    val clickService: ClickService,
    val activitySseService: ActivitySseService
) {
    @Transactional(readOnly = true)
    fun getBooks(): List<Book> {
        return bookRepository.findAll().toList()
    }

    @Transactional(readOnly = true)
    fun search(request: SearchRequest): SearchResponse {
        val filter = request.filter

        val from = filter.dateRange.from ?: throw BusinessException("From date not provided")
        val until = filter.dateRange.until ?: throw BusinessException("Until date not provided")

        // Construye criterios de búsqueda MongoDB
        val criteria = mutableListOf<Criteria>()

        if (request.bookName.isNotBlank()) {
            criteria.add(Criteria.where("props.title").regex(request.bookName, "i"))
        }
        if (filter.isbn.isNotBlank()) {
            criteria.add(Criteria.where("props.isbn").regex(filter.isbn, "i"))
        }
        if (filter.genres.isNotEmpty()) {
            val patterns = filter.genres.map { Pattern.quote(it) }.joinToString("|")
            criteria.add(Criteria.where("props.genre").regex("^($patterns)$", "i"))
        }
        if (filter.pageRange.min > 0 || filter.pageRange.max < Int.MAX_VALUE) {
            criteria.add(Criteria.where("props.totalPages").gte(filter.pageRange.min).lte(filter.pageRange.max))
        }
        if (filter.owner.isNotBlank()) {
            criteria.add(Criteria.where("props.owner.props.fullName").regex(filter.owner, "i"))
        }

        // Función local que construye un Query fresco desde los criterios
        fun buildQuery(): Query = if (criteria.isNotEmpty())
            Query(Criteria().andOperator(*criteria.toTypedArray()))
        else
            Query()

        // Ordenamiento
        val sort = when (request.sortType?.lowercase()) {
            "author" -> Sort.by(Sort.Direction.ASC, "props.author")
            "user"   -> Sort.by(Sort.Direction.ASC, "props.owner.props.fullName")
            else     -> Sort.by(Sort.Direction.ASC, "props.title")
        }

        val pageable = PageRequest.of(request.page, request.quantity, sort)

        val total = mongoTemplate.count(buildQuery(), Book::class.java)
        val books = mongoTemplate.find(buildQuery().with(pageable), Book::class.java)
        val totalPages = ((total + request.quantity - 1) / request.quantity).toInt()

        val user = userRepository.findById(request.userId).orElse(null)
            ?: throw NotFoundException("User not found")

        val bookIds = books.mapNotNull { it.id }

        // El rating vive en el embed de cada Book (Mongo); se calcula con book.getRating().
        val ratingByBook: Map<String, Double?> = books
            .mapNotNull { b -> b.id?.let { it to b.getRating() } }
            .toMap()

        val reservedBookIds: Set<String> = if (bookIds.isNotEmpty()) {
            reservationRepository.getReservedBookIdsInRange(bookIds, from, until)
        } else emptySet()

        val bookDTOs = books.map { book ->
            book.toCardDTO().apply {
                bibliokarmas = book.getBiblioKarmas(user, from, until)
                reserved = reservedBookIds.contains(book.id)
                rating = ratingByBook[book.id]
            }
        }

        clickService.saveBooks(bookDTOs)

        return SearchResponse(bookDTOs, totalPages)
    }

    @Transactional
    fun delete(id: String) {
        val book = bookById(id)
        book.getOwner()?.removeBook(book) ?: throw BusinessException("Book to delete has no user")
        bookRepository.delete(book)
    }

    @Transactional
    fun create(request: BookCreateDTO, id: Int): Book {

        when {
            request.title.isBlank() -> throw BusinessException("Title not provided")
            request.type == null -> throw BusinessException("Type not provided")
            request.description.isBlank() -> throw BusinessException("Description not provided")
            request.genre.isBlank() -> throw BusinessException("Genre not provided")
            request.author.isBlank() -> throw BusinessException("Author not provided")
            request.totalPages < 1 -> throw BusinessException("Total pages cant be less than 1")
            request.isbn.isBlank() -> throw BusinessException("ISBN not provided")
            request.language.isBlank() -> throw BusinessException("Language not provided")
            request.publicationDate == null -> throw BusinessException("PublicationDate not provided")
            request.state == null -> throw BusinessException("State not provided")
            request.editorial.isBlank() -> throw BusinessException("Editorial not provided")
            request.imgUrl.isBlank() -> throw BusinessException("ImgUrl not provided")
        }

        val strategy = BookTypeStrategy.fromString(request.type!!)
        val user = userRepository.findById(id).orElse(null) ?: throw NotFoundException("User not found")

        val book = Book(
            BookProps(
                owner = user,
                title = request.title,
                type = strategy,
                description = request.description,
                genre = request.genre,
                author = request.author,
                totalPages = request.totalPages,
                isbn = request.isbn,
                language = request.language,
                publicationDate = request.publicationDate,
                state = request.state,
                editorial = request.editorial,
                imgUrl = request.imgUrl
            )
        )

        user.addBook(book)
        bookRepository.save(book)
        activitySseService.broadcast(
            ActivityEventDTO(
                fecha = book.createdAt,
                tipoEvento = "LIBRO_NUEVO",
                usuario = user.getFullName(),
                titulo = book.getTitle(),
                descripcion = "Nuevo libro: \"${book.getTitle()}\" agregado por ${user.getFullName()}"
            )
        )
        return book
    }

    @Transactional(readOnly = true)
    fun bookById(id: String): Book = bookRepository.findById(id).orElse(null)
        ?: throw NotFoundException("No se encontró el libro de id <$id>")

    @Transactional
    fun update(id: String, dto: BookUpdateDTO): BookUpdateDTO {
        val book = bookById(id)
        book.updateFromDTO(dto)
        bookRepository.save(book)
        return book.toGetInfoBookUpdateDTO()
    }

    @Transactional
    fun getBookDetail(id: String, userId: Int): BookDetailDTO {
        val book = bookById(id)
        val currentUser = userRepository.findById(userId).orElse(null)
            ?: throw NotFoundException("Usuario no encontrado")

        val bookId = book.id ?: ""
        val plusDelLibro = book.calculateBookKarmaPlus(currentUser)

        // Reviews y rating viven en el documento Book de MongoDB (única fuente de verdad).
        val recentReviews = book.getReservations()
            .filter { !it.comment.isNullOrBlank() }
            .sortedByDescending { it.id ?: 0 }
            .take(2)
            .let { buildReviewsFromEmbeds(it) }

        val rankingAvg = book.getRating() ?: 0.0

        val click = ClickLog(
            username = currentUser.getFullName(),
            bookId = bookId,
            bookTitle = book.getTitle()
        )
        clickService.registerClick(click, book)

        return book.toDetailDTO(plusDelLibro, recentReviews, rankingAvg)
    }

    @Transactional(readOnly = true)
    fun getClicksForBook(bookId: String): List<ClickLogDTO> =
        clickLogRepository.findByBookIdOrderByTimestampDesc(bookId).map { it.toDTO() }

    @Transactional(readOnly = true)
    fun getClickCountsForBooks(bookIds: List<String>): Map<String, Long> {
        val clicks = clickLogRepository.findByBookIdIn(bookIds)
        return clicks.groupBy { it.bookId }.mapValues { it.value.size.toLong() }
    }

    @Transactional
    fun createReservation(bookId: String, userId: Int, request: ReservationRequestDTO): Map<String, Any> {
        val book = bookById(bookId)

        val currentUser = userRepository.findById(userId).orElse(null)
            ?: throw NotFoundException("Usuario no encontrado")

        val inicio = try {
            java.time.LocalDate.parse(request.fechaDesde)
        } catch (e: java.time.format.DateTimeParseException) {
            throw BusinessException("Fecha de inicio inválida (formato esperado: YYYY-MM-DD)")
        }
        val fin = try {
            java.time.LocalDate.parse(request.fechaHasta)
        } catch (e: java.time.format.DateTimeParseException) {
            throw BusinessException("Fecha de fin inválida (formato esperado: YYYY-MM-DD)")
        }

        if (inicio.isAfter(fin)) {
            throw BusinessException("La fecha de inicio no puede ser posterior a la de fin.")
        }

        if (!book.isAvailableInRange(inicio, fin)) {
            throw BusinessException("El libro ya se encuentra reservado en esas fechas.")
        }

        val owner = book.getOwner() ?: throw NotFoundException("El libro no tiene un dueño valido.")

        reservationService.createReservation(owner, currentUser, book, inicio, fin)
        activitySseService.broadcast(
            ActivityEventDTO(
                fecha = LocalDateTime.now(),
                tipoEvento = "RESERVA_CONFIRMADA",
                usuario = currentUser.getFullName(),
                titulo = book.getTitle(),
                descripcion = "Reserva confirmada: \"${book.getTitle()}\" por ${currentUser.getFullName()}"
            )
        )

        return mapOf(
            "mensaje" to "Reserva confirmada exitosamente",
            "bibliokarmasActuales" to currentUser.getBiblioKarmas()
        )
    }

    @Transactional(readOnly = true)
    fun getReviews(bookId: String, page: Int): List<ReviewDTO> {
        val pageSize = 6
        val book = bookById(bookId)

        // Paginación manual sobre la lista embebida de reservas en MongoDB.
        val pageItems = book.getReservations()
            .filter { !it.comment.isNullOrBlank() }
            .sortedByDescending { it.id ?: 0 }
            .drop(page * pageSize)
            .take(pageSize)

        return buildReviewsFromEmbeds(pageItems)
    }

    // Construye los ReviewDTOs a partir de una lista de embeds ya filtrada/paginada.
    // Encapsula el lookup del nombre del reader en Postgres (por id de la reserva)
    private fun buildReviewsFromEmbeds(embeds: List<ReservationEmbed>): List<ReviewDTO> {
        if (embeds.isEmpty()) return emptyList()

        val readersByReservationId = embeds
            .mapNotNull { it.id }
            .let { reservationRepository.findAllById(it) }
            .associate { it.id!! to it.reader.getFullName() }

        return embeds.map { embed ->
            ReviewDTO(
                readersByReservationId[embed.id] ?: "",
                embed.comment ?: "",
                embed.rating ?: 0.0
            )
        }
    }

    @Transactional(readOnly = true)
    fun bookByIdAsRow(id: String): BookRowDTO {
        val book = bookRepository.findById(id).orElse(null)
            ?: throw NotFoundException("No se encontró el libro de id <$id>")
        return book.toRowDTO()
    }

    @Transactional(readOnly = true)
    fun getBooksWithCompletedReservations(): List<Book> {
        val today = LocalDate.now()
        return bookRepository.findBooksWithAllReservationsCompleted(today)
    }
}
