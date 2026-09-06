package com.agrishop.service;

import com.agrishop.dto.SupplierDTO;
import com.agrishop.dto.PageRequestDTO;
import com.agrishop.dto.PageResponseDTO;
import jakarta.ejb.Local;
import java.util.List;

@Local
public interface SupplierServiceLocal {
    List<SupplierDTO> getAllActiveSuppliers();
    PageResponseDTO<SupplierDTO> getSuppliersWithPagination(PageRequestDTO request);
    void createSupplier(SupplierDTO dto);
    void updateSupplier(SupplierDTO dto);
    void deleteSupplier(Long id);
}
