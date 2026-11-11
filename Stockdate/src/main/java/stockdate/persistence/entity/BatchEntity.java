package stockdate.persistence.entity;

import stockdate.ExpirationDateType;
import stockdate.persistence.converter.LocalDateStringConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "batch")
public class BatchEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "storage_location", nullable = false)
    private String storageLocation;

    @Column(name = "expiration_date", nullable = false)
    @Convert(converter = LocalDateStringConverter.class)
    private LocalDate expirationDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "expiration_date_type", nullable = false)
    private ExpirationDateType expirationDateType;

    protected BatchEntity() {
    }

    public BatchEntity(String productName, String category, int quantity, String storageLocation,
                       LocalDate expirationDate, ExpirationDateType expirationDateType) {
        this.productName = productName;
        this.category = category;
        this.quantity = quantity;
        this.storageLocation = storageLocation;
        this.expirationDate = expirationDate;
        this.expirationDateType = expirationDateType;
    }

    public void update(String productName, String category, int quantity, String storageLocation,
                       LocalDate expirationDate, ExpirationDateType expirationDateType) {
        this.productName = productName;
        this.category = category;
        this.quantity = quantity;
        this.storageLocation = storageLocation;
        this.expirationDate = expirationDate;
        this.expirationDateType = expirationDateType;
    }

    public Long getId() {
        return id;
    }

    public String getProductName() {
        return productName;
    }

    public String getCategory() {
        return category;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getStorageLocation() {
        return storageLocation;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public ExpirationDateType getExpirationDateType() {
        return expirationDateType;
    }
}
