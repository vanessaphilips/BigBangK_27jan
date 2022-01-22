
package com.example.project_bigbangk.model;

import java.time.LocalDateTime;

/**
 * a price of an asset at a given dateTime
 * @author Pieter Jan Bleichrodt
 */
public class PriceDate implements Comparable<PriceDate> {


    private LocalDateTime dateTime;

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public double getPrice() {
        return price;
    }

    private double price;

    public PriceDate(LocalDateTime dateTime, double price) {
        this.price = price;
        this.dateTime = dateTime;
    }


    @Override
    public int compareTo(PriceDate o) {
        return this.getDateTime().compareTo(o.getDateTime());
    }

}