package ivanmikhnevich.officebookings.api;

import ivanmikhnevich.officebookings.api.dto.BookingResponse;
import ivanmikhnevich.officebookings.api.dto.CreateBookingRequest;
import ivanmikhnevich.officebookings.model.Booking;
import ivanmikhnevich.officebookings.repository.BookingRepository;
import ivanmikhnevich.officebookings.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class BookingService {

    private final BookingRepository repo;

    public BookingService(BookingRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public BookingResponse create(CreateBookingRequest req, String userId) {
        Booking b = new Booking(req.roomId(), userId, req.startsAt(), req.endsAt());
        repo.save(b);
        return toDto(b);
    }

    @Transactional(readOnly = true)
    public Optional<BookingResponse> getVisibleTo(String id, CurrentUser user) {
        return repo.findById(id)
                .filter(b -> canView(user, b))
                .map(BookingService::toDto);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> listVisibleTo(String userId, boolean isAdmin) {
        return repo.findVisibleTo(userId, isAdmin).stream()
                .map(BookingService::toDto)
                .toList();
    }

    private boolean canView(CurrentUser user, Booking b) {
        if (b.getUserId().equals(user.userId())) return true;
        return user.hasAnyRole("admin", "manager");
    }

    private static BookingResponse toDto(Booking b) {
        return new BookingResponse(
                b.getId(), b.getRoomId(), b.getUserId(), b.getStartsAt(), b.getEndsAt());
    }

    public List<BookingResponse> findAll() {
        List<BookingResponse> responses = new ArrayList<>();
        return responses;
    }
}