package converter

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import com.BookLibre.domain.*

@Converter(autoApply = false)
class BookTypeStrategyConverter : AttributeConverter<BookTypeStrategy, String> {

    override fun convertToDatabaseColumn(attribute: BookTypeStrategy?): String {
        if (attribute == null) return "Comun"

        return when (attribute) {
            is DedicationStrategy -> "Con Dedicatoria"
            is CollectableStrategy -> "Coleccionable"
            else -> "Comun"
        }
    }

    override fun convertToEntityAttribute(dbData: String?): BookTypeStrategy {
        if (dbData.isNullOrBlank()) return CommonStrategy() // Fallback de seguridad

        return BookTypeStrategy.fromString(dbData)
    }
}