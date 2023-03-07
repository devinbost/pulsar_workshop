package com.datastax.demo.logic.function;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@Builder
@NonNull
@AllArgsConstructor
@NoArgsConstructor
public class Product {
    int id;
    String name;
    String description;
    Double weight;
}
