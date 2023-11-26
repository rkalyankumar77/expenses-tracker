package com.example.expenses_tracker.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Expenses {
  /*
   @startuml
   !theme blueprint
   class Expenses {
     + Long id
     + String name
     + String description
     + Double amount
     + LocalDate date
   }
   @enduml
*/
  private Long id;
  private String name;
  private String description;
  private Double amount;
  private LocalDate date;
}
