package ru.yumeno.tamtambot.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "ERROR")
@Data
public class Task {
    @Id
    @Column(name = "ERROR_ID")
    private int taskId;
    @Column(name = "ERROR_TEXT")
    private String taskText;
}
