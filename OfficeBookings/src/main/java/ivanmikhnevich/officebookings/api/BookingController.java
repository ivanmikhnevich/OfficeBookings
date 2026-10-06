package ivanmikhnevich.officebookings.api;

import ivanmikhnevich.officebookings.api.dto.BookingResponse;
import ivanmikhnevich.officebookings.api.dto.CreateBookingRequest;
import ivanmikhnevich.officebookings.security.CurrentUser;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService service;

    public BookingController(BookingService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse create(@RequestBody CreateBookingRequest req,
                                  CurrentUser user) {
        return service.create(req, user.userId());
    }

    @GetMapping("/{id}")
    public BookingResponse get(@PathVariable String id, CurrentUser user) {
        // Сервис сам решает 200 vs 404 (чужое = 404, чтобы не раскрывать существование)
        return service.getVisibleTo(id, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
    }

    // Пример: эндпоинт только для админов → 403 при нехватке прав
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('admin')")
    public List<BookingResponse> allForAdmin() {
        return service.findAll();
    }
}