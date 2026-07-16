package com.brick.billing.model;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;


@Entity
public class ReportItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonBackReference
    private Report report;


    private String description;

    // Unit of Measure - free text, e.g. "BOX", "PACKET", "PIECE" (stored in caps)
    private String uom;

    private Double rate;
    private Double qty;

    // Per-row discount amount (flat rupee amount subtracted from rate*qty for this row)
    private Double discount = 0.0;

    private Double total;

    // getters & setters
    public void setReport(Report report) { this.report = report; }


    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getUom() { return uom; }
    public void setUom(String uom) { this.uom = uom; }

    public Double getRate() { return rate; }
    public void setRate(Double rate) { this.rate = rate; }

    public Double getQty() { return qty; }
    public void setQty(Double qty) { this.qty = qty; }

    public Double getDiscount() { return discount; }
    public void setDiscount(Double discount) { this.discount = discount; }

    public Double getTotal() { return total; }
    public void setTotal(Double total) { this.total = total; }
}
