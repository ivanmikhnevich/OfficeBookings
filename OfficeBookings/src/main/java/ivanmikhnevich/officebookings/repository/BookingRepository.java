package ivanmikhnevich.officebookings.repository;

import ivanmikhnevich.officebookings.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, String> {

    @Query("""
        select b from Booking b
        where (:isAdmin = true) or b.userId = :userId
        order by b.startsAt
    """)
    List<Booking> findVisibleTo(@Param("userId") String userId, @Param("isAdmin") boolean isAdmin);
}