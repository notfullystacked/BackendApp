package org.example.smartbiobackend.model;

import jakarta.persistence.*;

@Entity
public class Auditorium {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String auditoriumName;

    // Antal rækker og sæder pr. række. Sæderne oprettes ud fra disse (se AuditoriumService).
    // Hedder rowCount og ikke rows, fordi ROWS er et reserveret ord i MySQL
    private int rowCount;
    private int seatsPerRow;

    public Auditorium() {
    }

    public Auditorium(String auditoriumName) {
        this.auditoriumName = auditoriumName;
    }

    public Auditorium(String auditoriumName, int rowCount, int seatsPerRow) {
        this.auditoriumName = auditoriumName;
        this.rowCount = rowCount;
        this.seatsPerRow = seatsPerRow;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getAuditoriumName() { return auditoriumName; }
    public void setAuditoriumName(String auditoriumName) { this.auditoriumName = auditoriumName; }

    public int getRowCount() { return rowCount; }
    public void setRowCount(int rowCount) { this.rowCount = rowCount; }

    public int getSeatsPerRow() { return seatsPerRow; }
    public void setSeatsPerRow(int seatsPerRow) { this.seatsPerRow = seatsPerRow; }

    @Override
    public String toString() {
        return "Auditorium{id=" + id + ", auditoriumName='" + auditoriumName + "', rowCount=" + rowCount
                + ", seatsPerRow=" + seatsPerRow + '}';
    }

    public CleaningStatus getCleaningStatus() {
        return cleaningStatus;
    }

    public void setCleaningStatus(CleaningStatus cleaningStatus) {
        this.cleaningStatus = cleaningStatus;
    }

    @Enumerated(EnumType.STRING)
    private CleaningStatus cleaningStatus = CleaningStatus.CLEAN;
}

