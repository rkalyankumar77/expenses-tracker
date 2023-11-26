package com.example.expenses_tracker.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Expenses {
  private Long id;
  private String name;
  private String description;
  private Double amount;
  private LocalDate date;
}
