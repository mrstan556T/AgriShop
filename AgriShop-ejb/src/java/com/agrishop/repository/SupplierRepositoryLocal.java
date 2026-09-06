package com.agrishop.repository;

import com.agrishop.entity.Supplier;
import com.agrishop.dto.PageRequestDTO;
import jakarta.ejb.Local;
import java.util.List;

@Local
public interface SupplierRepositoryLocal {
    Supplier findById(Long id);
    List<Supplier> findActiveSuppliers();
    void create(Supplier supplier);
    void update(Supplier supplier);
    List<Supplier> findWithPagination(PageRequestDTO request);
    long countWithPagination(PageRequestDTO request);
}
