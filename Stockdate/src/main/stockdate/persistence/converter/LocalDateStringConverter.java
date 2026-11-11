package stockdate.persistence.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.LocalDate;

@Converter
public class LocalDateStringConverter implements AttributeConverter<LocalDate, String> {

    @Override
    public String convertToDatabaseColumn(LocalDate date) {
        return date == null ? null : date.toString();
    }

    @Override
    public LocalDate convertToEntityAttribute(String value) {
        return value == null ? null : LocalDate.parse(value);
    }
}
