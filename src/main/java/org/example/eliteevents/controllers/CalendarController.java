package org.example.eliteevents.controllers;

import javafx.beans.property.BooleanProperty;
import javafx.animation.PauseTransition;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.TextAlignment;
import java.time.Duration;     // ✔ correct duration class
import org.example.eliteevents.models.Booking;
import org.example.eliteevents.models.Client;
import org.example.eliteevents.models.Venue;
import org.example.eliteevents.services.DatabaseService;

import java.net.URL;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.time.Duration.*;

/**
 * CalendarController
 *
 * Loads bookings from DatabaseService and renders them into a grid.
 * - Week view (default) with hourly rows
 * - Month view (simplified) for overview
 * - Venue filter
 * - Today / Prev / Next navigation
 * - Multi-hour booking spans and tooltip details
 * - Color coding by booking status (CONFIRMED, PENDING, CONFLICT)
 */
public class CalendarController implements Initializable {

    @FXML private ComboBox<String> calendarVenueFilter;
    @FXML private ToggleButton btnWeek;
    @FXML private ToggleButton btnMonth;
    @FXML private Button btnPrev;
    @FXML private Button btnNext;
    @FXML private Button btnToday;
    @FXML private Label lblCalendarRange;
    @FXML private GridPane calendarGrid;

    private final DatabaseService db = DatabaseService.getInstance();

    // state
    private final ObjectProperty<LocalDate> currentDate = new SimpleObjectProperty<>(LocalDate.now());
    private final BooleanProperty isWeekView = new SimpleBooleanProperty(true);

    // live lists
    private final ObservableList<Booking> bookings = FXCollections.observableArrayList();
    private final ObservableList<Venue> venues = FXCollections.observableArrayList();

    // Hours to display in week view
    private final List<LocalTime> HOURS = IntStream.rangeClosed(8, 20)
            .mapToObj(h -> LocalTime.of(h, 0)).collect(Collectors.toList());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupControls();
        loadVenues();
        loadBookings();
        updateCalendar();
    }

    // ----------------------------- SETUP ----------------------------- //
    private void setupControls() {
        // Toggle buttons controlling view
        btnWeek.setSelected(true);
        btnWeek.setOnAction(e -> { isWeekView.set(true); updateCalendar(); });
        btnMonth.setOnAction(e -> { isWeekView.set(false); updateCalendar(); });

        // Navigation
        btnPrev.setOnAction(e -> navigate(-1));
        btnNext.setOnAction(e -> navigate(+1));
        btnToday.setOnAction(e -> {
            currentDate.set(LocalDate.now());
            updateCalendar();
        });

        // Venue filter with short debounce to avoid rapid DB calls
        calendarVenueFilter.setOnAction(e -> {
//            PauseTransition debounce = new PauseTransition(Duration.millis(150));
            PauseTransition debounce = new PauseTransition(javafx.util.Duration.millis(150));
            debounce.setOnFinished(ev -> updateCalendar());
            debounce.playFromStart();
        });

        // ensure grid styling padding/gap
        calendarGrid.setHgap(4);
        calendarGrid.setVgap(4);
        calendarGrid.setPadding(new Insets(6));
    }

    private void loadVenues() {
        try {
            List<Venue> fromDb = db.getAllVenues();
            venues.setAll(fromDb);
            List<String> names = new ArrayList<>();
            names.add("All Venues");
            for (Venue v : fromDb) names.add(v.getName());
            calendarVenueFilter.setItems(FXCollections.observableArrayList(names));
            calendarVenueFilter.getSelectionModel().selectFirst();
        } catch (Exception e) {
            System.err.println("Failed to load venues: " + e.getMessage());
            calendarVenueFilter.setItems(FXCollections.observableArrayList("All Venues"));
            calendarVenueFilter.getSelectionModel().selectFirst();
        }
    }

    private void loadBookings() {
        try {
            bookings.setAll(db.getAllBookings());
        } catch (Exception e) {
            System.err.println("Failed to load bookings: " + e.getMessage());
            bookings.clear();
        }
    }

    // ----------------------------- NAVIGATION ----------------------------- //
    private void navigate(int delta) {
        if (isWeekView.get()) currentDate.set(currentDate.get().plusWeeks(delta));
        else currentDate.set(currentDate.get().plusMonths(delta));
        updateCalendar();
    }

    // ----------------------------- RENDER ----------------------------- //
    private void updateCalendar() {
        // refresh data from DB each time (keeps view live)
        loadBookings();

        calendarGrid.getChildren().clear();
        calendarGrid.getColumnConstraints().clear();
        calendarGrid.getRowConstraints().clear();

        if (isWeekView.get()) renderWeekView();
        else renderMonthView();

        updateRangeLabel();
    }

    // ----------------------------- WEEK VIEW ----------------------------- //
    private void renderWeekView() {
        LocalDate start = currentDate.get().with(DayOfWeek.MONDAY);
        LocalDate end = start.plusDays(6);

        // 1st column = time labels, then 7 day columns
        int cols = 1 + 7;
        int rows = 1 + HOURS.size(); // header + hours

        // setup column constraints
        for (int c = 0; c < cols; c++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setHgrow(Priority.ALWAYS);
            cc.setPercentWidth(100.0 / cols);
            calendarGrid.getColumnConstraints().add(cc);
        }
        // rows
        for (int r = 0; r < rows; r++) {
            RowConstraints rc = new RowConstraints();
            rc.setVgrow(Priority.ALWAYS);
            rc.setPercentHeight(100.0 / rows);
            calendarGrid.getRowConstraints().add(rc);
        }

        // header: time, Mon..Sun
        addHeaderCell(0, 0, "Time");
        for (int d = 0; d < 7; d++) {
            LocalDate day = start.plusDays(d);
            String label = day.format(DateTimeFormatter.ofPattern("EEE\nMMM d"));
            boolean isToday = day.equals(LocalDate.now());
            addHeaderCell(0, d + 1, label, isToday);
        }

        // time column and booking cells
        for (int r = 0; r < HOURS.size(); r++) {
            LocalTime slotTime = HOURS.get(r);
            addTimeCell(r + 1, 0, slotTime.format(DateTimeFormatter.ofPattern("HH:mm")));

            for (int c = 0; c < 7; c++) {
                LocalDate cellDate = start.plusDays(c);
                addSlotCell(r + 1, c + 1, cellDate, slotTime);
            }
        }
    }

    private void addHeaderCell(int row, int col, String text) {
        addHeaderCell(row, col, text, false);
    }
    private void addHeaderCell(int row, int col, String text, boolean highlight) {
        StackPane p = new StackPane();
        p.setStyle("-fx-background-color: " + (highlight ? "#2563EB" : "#334155") +
                "; -fx-padding: 8; -fx-background-radius: 6;");
        Label L = new Label(text);
        L.setTextAlignment(TextAlignment.CENTER);
        L.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        p.getChildren().add(L);

        GridPane.setRowIndex(p, row);
        GridPane.setColumnIndex(p, col);
        calendarGrid.getChildren().add(p);
    }

    private void addTimeCell(int row, int col, String timeText) {
        StackPane p = new StackPane();
        p.setStyle("-fx-background-color: #f8fafc; -fx-padding: 6; -fx-border-color: #e6eef8;");
        Label L = new Label(timeText);
        L.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold; -fx-font-size: 11;");
        p.getChildren().add(L);

        GridPane.setRowIndex(p, row);
        GridPane.setColumnIndex(p, col);
        calendarGrid.getChildren().add(p);
    }

    private void addSlotCell(int row, int col, LocalDate date, LocalTime slotTime) {
        StackPane cell = new StackPane();
        cell.setStyle("-fx-background-color: white; -fx-border-color: #eef2f7;");
        cell.setMinHeight(48);

        // find bookings that overlap this slot (start <= slot < end)
        List<Booking> matches = bookings.stream()
                .filter(b -> b.getStartDateTime() != null && b.getEndDateTime() != null)
                .filter(b -> isBookingInDate(b, date))
                .filter(b -> isSlotWithinBooking(slotTime, b))
                .filter(b -> venueMatchesFilter(b))
                .collect(Collectors.toList());

        if (!matches.isEmpty()) {
            // Render first booking; also render stacked ones if multiple (show small chips)
            int placed = 0;
            for (Booking b : matches) {
                // Only add a main block for the first occurrence (we want spanning); avoid duplicate spanning
                if (placed == 0) {
                    Region bookingNode = createBookingNode(b);
                    // compute row span: how many hour-rows this booking covers
                    int span = computeRowSpan(b);
                    GridPane.setRowIndex(bookingNode, row);
                    GridPane.setColumnIndex(bookingNode, col);
                    GridPane.setRowSpan(bookingNode, Math.max(1, span));
                    calendarGrid.getChildren().add(bookingNode);
                } else {
                    // small badge/chip to indicate additional bookings in same slot
                    HBox chip = smallChip(matches.get(placed));
                    chip.setTranslateY(placed * 6); // slight offset
                    cell.getChildren().add(chip);
                }
                placed++;
                if (placed >= 3) break; // keep UI tidy
            }
        }

        // tooltip for the cell combining booking summaries
        if (!matches.isEmpty()) {
            String tooltipText = matches.stream()
                    .map(this::bookingSummary)
                    .collect(Collectors.joining("\n---\n"));
            Tooltip t = new Tooltip(tooltipText);
            Tooltip.install(cell, t);
        }

        GridPane.setRowIndex(cell, row);
        GridPane.setColumnIndex(cell, col);
        calendarGrid.getChildren().add(cell);
    }

    // ----------------------------- MONTH VIEW (simplified) ----------------------------- //
    private void renderMonthView() {
        YearMonth ym = YearMonth.from(currentDate.get());
        LocalDate first = ym.atDay(1);
        LocalDate start = first.with(DayOfWeek.MONDAY);

        int cols = 7;
        int rows = 1 + 6; // header + up to 6 weeks

        // columns & rows setup
        calendarGrid.getColumnConstraints().clear();
        calendarGrid.getRowConstraints().clear();
        for (int c = 0; c <= cols; c++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setHgrow(Priority.ALWAYS);
            cc.setPercentWidth(100.0 / (cols + 1));
            calendarGrid.getColumnConstraints().add(cc);
        }
        for (int r = 0; r < rows; r++) {
            RowConstraints rc = new RowConstraints();
            rc.setVgrow(Priority.ALWAYS);
            rc.setPercentHeight(100.0 / rows);
            calendarGrid.getRowConstraints().add(rc);
        }

        // headers
        addHeaderCell(0, 0, "Mon");
        for (int i = 1; i <= 7; i++) addHeaderCell(0, i, DayOfWeek.of(i % 7 + 1).toString().substring(0,3));

        // fill days
        int col = 0, row = 1;
        for (LocalDate date = start; date.isBefore(start.plusWeeks(6)); date = date.plusDays(1)) {
            VBox cell = new VBox(4);
            cell.setPadding(new Insets(6));
            cell.setStyle("-fx-background-color: white; -fx-border-color: #eef2f7;");

            Label dayLabel = new Label(String.valueOf(date.getDayOfMonth()));
            dayLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155;");
            cell.getChildren().add(dayLabel);

            // bookings for that day
            LocalDate finalDate = date;
            List<Booking> dayBookings = bookings.stream()
                    .filter(b -> isBookingInDate(b, finalDate))
                    .filter(this::venueMatchesFilter)
                    .collect(Collectors.toList());

            for (int i = 0; i < Math.min(3, dayBookings.size()); i++) {
                cell.getChildren().add(smallChip(dayBookings.get(i)));
            }
            if (dayBookings.size() > 3) {
                Label more = new Label("+" + (dayBookings.size() - 3) + " more");
                more.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");
                cell.getChildren().add(more);
            }

            GridPane.setRowIndex(cell, row);
            GridPane.setColumnIndex(cell, col);
            calendarGrid.getChildren().add(cell);

            col++;
            if (col >= 7) { col = 0; row++; }
            if (row > 6) break;
        }
    }

    // ----------------------------- BOOKING NODE ----------------------------- //
    private Region createBookingNode(Booking b) {
        String status = (b.getStatus() == null) ? "PENDING" : b.getStatus().toUpperCase();
        String color = switch (status) {
            case "CONFIRMED" -> "#3b82f6";
            case "CONFLICT"  -> "#ef4444";
            default -> "#f59e0b"; // PENDING or unknown
        };

        VBox box = new VBox(2);
        box.setPadding(new Insets(6));
        box.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 6;");
        box.setMaxWidth(Double.MAX_VALUE);

        String clientName = safeClientName(b);
        Label title = new Label((b.getEventType() != null ? b.getEventType() : "Booking") + " — " + clientName);
        title.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11;");
        title.setWrapText(true);

        Label time = new Label(formatTimeRange(b));
        time.setStyle("-fx-text-fill: white; -fx-font-size: 10;");

        box.getChildren().addAll(title, time);

        // Click to open booking details (if you have a details dialog)
        box.setOnMouseClicked(evt -> {
            if (evt.getButton() == MouseButton.PRIMARY) {
                showBookingDetails(b);
            }
        });

        return box;
    }

    private HBox smallChip(Booking b) {
        String status = (b.getStatus() == null) ? "PENDING" : b.getStatus().toUpperCase();
        String color = switch (status) {
            case "CONFIRMED" -> "#3b82f6";
            case "CONFLICT"  -> "#ef4444";
            default -> "#f59e0b";
        };
        Rectangle rect = new Rectangle(8, 8, Color.web(color));
        rect.setArcHeight(2); rect.setArcWidth(2);
        Label lbl = new Label(" " + (b.getEventType() != null ? b.getEventType() : "Booking"));
        lbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #334155;");
        HBox box = new HBox(6, rect, lbl);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    // ----------------------------- HELPERS ----------------------------- //
    private boolean isBookingInDate(Booking b, LocalDate date) {
        if (b.getStartDateTime() == null || b.getEndDateTime() == null) return false;
        LocalDate start = b.getStartDateTime().toLocalDate();
        LocalDate end = b.getEndDateTime().toLocalDate();
        // treat booking spanning multiple days as present on any overlapping date
        return !(date.isBefore(start) || date.isAfter(end));
    }

    private boolean isSlotWithinBooking(LocalTime slotTime, Booking b) {
        LocalTime start = b.getStartDateTime().toLocalTime();
        LocalTime end = b.getEndDateTime().toLocalTime();
        // consider slot included if slotTime >= floor(start hour) and slotTime < end
        return !slotTime.isBefore(start.truncatedTo(ChronoUnit.HOURS)) && slotTime.isBefore(end);
    }

    private int computeRowSpan(Booking b) {
        // compute number of hour-rows spanned by booking relative to HOURS list
        LocalTime start = b.getStartDateTime().toLocalTime().truncatedTo(ChronoUnit.HOURS);
        LocalTime end = b.getEndDateTime().toLocalTime();
        if (!end.isAfter(start)) return 1;
        long hours = between(start, end).toHours();
        return Math.max(1, (int) hours);
    }

    private boolean venueMatchesFilter(Booking b) {
        String sel = calendarVenueFilter.getValue();
        if (sel == null || sel.equals("All Venues")) return true;
        Venue v = b.getVenue();
        if (v == null) return false;
        return sel.equals(v.getName());
    }

    private String bookingSummary(Booking b) {
        String client = safeClientName(b);
        return String.format("%s (%s)\n%s - %s\nStatus: %s",
                (b.getEventType() != null ? b.getEventType() : "Booking"),
                client,
                b.getStartDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                b.getEndDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                b.getStatus());
    }

    private String safeClientName(Booking b) {
        Client c = b.getClient();
        if (c == null) return "Unknown";
        if (c.getName() != null && !c.getName().isBlank()) return c.getName();
        return "Client #" + c.getId();
    }

    private String formatTimeRange(Booking b) {
        return b.getStartDateTime().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")) +
                " - " + b.getEndDateTime().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    private void showBookingDetails(Booking b) {
        // simple details dialog — replace with your modal/dialog if desired
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Booking Details");
        a.setHeaderText(b.getEventType());
        String body = bookingSummary(b) + "\nVenue: " + (b.getVenue()!=null?b.getVenue().getName():"Unknown");
        a.setContentText(body);
        a.showAndWait();
    }

    private String formatRangeLabel(LocalDate start, LocalDate end) {
        return start.format(DateTimeFormatter.ofPattern("MMM d")) + " — " + end.format(DateTimeFormatter.ofPattern("MMM d, yyyy"));
    }

    private void updateRangeLabel() {
        if (isWeekView.get()) {
            LocalDate startOfWeek = currentDate.get().with(DayOfWeek.MONDAY);
            LocalDate endOfWeek = startOfWeek.plusDays(6);
            lblCalendarRange.setText(formatRangeLabel(startOfWeek, endOfWeek));
        } else {
            YearMonth ym = YearMonth.from(currentDate.get());
            lblCalendarRange.setText(ym.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        }
    }
}
