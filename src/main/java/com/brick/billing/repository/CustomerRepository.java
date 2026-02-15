package com.brick.billing.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.brick.billing.model.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
}
