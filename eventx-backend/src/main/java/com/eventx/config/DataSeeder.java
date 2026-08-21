package com.eventx.config;

import com.eventx.entity.*;
import com.eventx.entity.enums.*;
import com.eventx.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final VenueRepository venueRepository;
    private final SeatRepository seatRepository;
    private final EventRepository eventRepository;
    private final TicketCategoryRepository ticketCategoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        try {
            if (userRepository.count() == 0) {
                log.info("Seeding data...");
                seedData();
                log.info("Data seeding completed!");
            } else {
                ensureOrganizerAccount();
                ensureEventsOrganizerColumn();
            }
        } catch (Exception ex) {
            log.error("Data seeding failed; API will still start.", ex);
        }
    }

    private void seedData() {
        // Users
        User admin = new User();
        admin.setName("Admin User");
        admin.setEmail("admin@eventx.com");
        admin.setPassword(passwordEncoder.encode("Admin@123"));
        admin.setRole(Role.ROLE_ADMIN);
        admin.setActive(true);
        userRepository.save(admin);

        User organizer = new User();
        organizer.setName("Priya Sharma");
        organizer.setEmail("organizer@eventx.com");
        organizer.setPassword(passwordEncoder.encode("Organizer@123"));
        organizer.setRole(Role.ROLE_ORGANIZER);
        organizer.setActive(true);
        userRepository.save(organizer);

        List<User> users = Arrays.asList(
                createUser("John Doe", "john@example.com", "User@123"),
                createUser("Jane Smith", "jane@example.com", "User@123"),
                createUser("Bob Wilson", "bob@example.com", "User@123")
        );
        userRepository.saveAll(users);

        // Venues
        Venue grandArena = createVenue("Grand Arena", "Mumbai", "Maharashtra", 500, "123 Marine Drive");
        Venue cityCenter = createVenue("City Convention Center", "Bangalore", "Karnataka", 300, "456 MG Road");
        Venue royalTheater = createVenue("Royal Theater", "Delhi", "Delhi", 200, "789 Connaught Place");
        venueRepository.saveAll(Arrays.asList(grandArena, cityCenter, royalTheater));

        // Create seats (mocked logic)
        createSeats(grandArena, 10, 15, 20);
        createSeats(cityCenter, 10, 10, 15);
        createSeats(royalTheater, 10, 10, 10);

        // Events
        Event event1 = createEvent("Arijit Singh Live", EventCategory.CONCERT, grandArena, 30, "https://images.unsplash.com/photo-1540039155733-d7696e8aba98", organizer);
        Event event2 = createEvent("Tech Summit 2025", EventCategory.CONFERENCE, cityCenter, 45, "https://images.unsplash.com/photo-1540575467063-178a50c2df87", organizer);
        Event event3 = createEvent("IPL Final", EventCategory.SPORTS, grandArena, 60, "https://images.unsplash.com/photo-1540747913346-19e32dc3e97e", organizer);
        Event event4 = createEvent("Shakespeare Festival", EventCategory.THEATER, royalTheater, 20, "https://images.unsplash.com/photo-1507676184212-d03305a555b4", organizer);
        Event event5 = createEvent("Stand-Up Night with Zakir Khan", EventCategory.COMEDY, cityCenter, 15, "https://images.unsplash.com/photo-1585699324551-f6c309eedeca", organizer);
        
        eventRepository.saveAll(Arrays.asList(event1, event2, event3, event4, event5));

        // Categories
        createCategories(event1, 5000, 3000, 1500, 100, 150, 250);
        createCategories(event2, 2000, 1200, 800, 50, 100, 150);
        createCategories(event3, 8000, 5000, 2500, 100, 150, 250);
        createCategories(event4, 1500, 1000, 500, 20, 30, 50);
        createCategories(event5, 2500, 1500, 800, 30, 40, 70);

        log.info("Seeded Users: {}", userRepository.count());
        log.info("Seeded Venues: {}", venueRepository.count());
        log.info("Seeded Events: {}", eventRepository.count());
    }

    private User createUser(String name, String email, String password) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(Role.ROLE_USER);
        user.setActive(true);
        return user;
    }

    /** H2 ddl-auto=update often skips adding this column on existing local DBs. */
    private void ensureEventsOrganizerColumn() {
        try {
            jdbcTemplate.execute("ALTER TABLE \"EVENTS\" ADD COLUMN IF NOT EXISTS \"ORGANIZER_ID\" BIGINT");
            jdbcTemplate.update("""
                    UPDATE "EVENTS" SET "ORGANIZER_ID" = (
                        SELECT "ID" FROM "USERS" WHERE "EMAIL" = 'organizer@eventx.com' LIMIT 1
                    ) WHERE "ORGANIZER_ID" IS NULL
                    """);
        } catch (Exception ex) {
            log.warn("Could not patch EVENTS.ORGANIZER_ID: {}", ex.getMessage());
        }
    }

    private void ensureOrganizerAccount() {
        if (userRepository.findByEmail("organizer@eventx.com").isPresent()) {
            return;
        }
        User organizer = new User();
        organizer.setName("Priya Sharma");
        organizer.setEmail("organizer@eventx.com");
        organizer.setPassword(passwordEncoder.encode("Organizer@123"));
        organizer.setRole(Role.ROLE_ORGANIZER);
        organizer.setActive(true);
        userRepository.save(organizer);
        log.info("Created organizer account organizer@eventx.com");
    }

    private Venue createVenue(String name, String city, String state, int capacity, String address) {
        Venue venue = new Venue();
        venue.setName(name);
        venue.setCity(city);
        venue.setState(state);
        venue.setCapacity(capacity);
        venue.setAddress(address);
        return venue;
    }

    private void createSeats(Venue venue, int vip, int premium, int regular) {
        // Just mock seat entries for one section as an example
        for (int i = 1; i <= vip; i++) {
            Seat seat = new Seat();
            seat.setVenue(venue);
            seat.setSeatNumber("V" + i);
            seat.setRow("A");
            seat.setSection("VIP");
            seat.setCategory(SeatCategory.VIP);
            seatRepository.save(seat);
        }
        for (int i = 1; i <= premium; i++) {
            Seat seat = new Seat();
            seat.setVenue(venue);
            seat.setSeatNumber("P" + i);
            seat.setRow("B");
            seat.setSection("PREMIUM");
            seat.setCategory(SeatCategory.PREMIUM);
            seatRepository.save(seat);
        }
        for (int i = 1; i <= regular; i++) {
            Seat seat = new Seat();
            seat.setVenue(venue);
            seat.setSeatNumber("R" + i);
            seat.setRow("C");
            seat.setSection("REGULAR");
            seat.setCategory(SeatCategory.REGULAR);
            seatRepository.save(seat);
        }
    }

    private Event createEvent(String title, EventCategory category, Venue venue, int daysAhead, String image, User organizer) {
        Event event = new Event();
        event.setTitle(title);
        event.setDescription("Join us for " + title);
        event.setCategory(category);
        event.setVenue(venue);
        event.setOrganizer(organizer);
        event.setEventDate(LocalDate.now().plusDays(daysAhead));
        event.setStartTime(LocalTime.of(19, 0));
        event.setEndTime(LocalTime.of(22, 0));
        event.setBookingOpenTime(LocalDateTime.now());
        event.setBookingCloseTime(LocalDateTime.now().plusDays(daysAhead - 1));
        event.setStatus(EventStatus.PUBLISHED);
        event.setImageUrl(image);
        return event;
    }

    private void createCategories(Event event, int vipP, int premP, int regP, int vipCap, int premCap, int regCap) {
        saveCategory(event, "VIP", new BigDecimal(vipP), vipCap, SeatCategory.VIP);
        saveCategory(event, "PREMIUM", new BigDecimal(premP), premCap, SeatCategory.PREMIUM);
        saveCategory(event, "REGULAR", new BigDecimal(regP), regCap, SeatCategory.REGULAR);
    }

    private void saveCategory(Event event, String name, BigDecimal price, int capacity, SeatCategory seatCategory) {
        TicketCategory cat = new TicketCategory();
        cat.setEvent(event);
        cat.setName(name);
        cat.setPrice(price);
        cat.setTotalSeats(capacity);
        cat.setAvailableSeats(capacity);
        cat.setSeatCategory(seatCategory);
        ticketCategoryRepository.save(cat);
    }
}

