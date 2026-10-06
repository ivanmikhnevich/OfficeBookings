package ivanmikhnevich.officebookings.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "bookings",
        indexes = {
                // Списочные ручки фильтруют по user_id → нужен индекс
                @Index(name = "ix_bookings_user_id", columnList = "user_id"),
                // Проверка пересечений по кабинету и времени
                @Index(name = "ix_bookings_room_time", columnList = "room_id, starts_at, ends_at")
        }
)
public class Booking {

    @Id
    @Column(name = "id", nullable = false, updatable = false, length = 36)
    private String id;

    @Column(name = "room_id", nullable = false, updatable = false, length = 64)
    private String roomId;

    /**
     * Внутренний идентификатор пользователя.
     * Заполняется ТОЛЬКО из проверенного токена (sub), никогда из тела запроса.
     */
    @Column(name = "user_id", nullable = false, updatable = false, length = 128)
    private String userId;

    @Column(name = "starts_at", nullable = false, updatable = false)
    private Instant startsAt;

    @Column(name = "ends_at", nullable = false, updatable = false)
    private Instant endsAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /** JPA требует no-arg конструктор (protected достаточно). */
    protected Booking() {}

    public Booking(String roomId, String userId, Instant startsAt, Instant endsAt) {
        this.roomId = roomId;
        this.userId = userId;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
    }

    @PrePersist
    void onCreate() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    // --- getters ---
    // Сеттеров на roomId/userId/startsAt/endsAt СОЗНАТЕЛЬНО нет:
    // бронь иммутабельна после создания (updatable = false тоже это гарантирует).
    // Если понадобится перенос — делайте отдельный метод/эндпоинт с явной логикой.

    public String getId() { return id; }
    public String getRoomId() { return roomId; }
    public String getUserId() { return userId; }
    public Instant getStartsAt() { return startsAt; }
    public Instant getEndsAt() { return endsAt; }
    public Instant getCreatedAt() { return createdAt; }

    // equals/hashCode по id — полезно, если сущность попадёт в Set/Map
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Booking other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id == null ? 0 : id.hashCode();
    }
}