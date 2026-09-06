package com.agrishop.service;

import com.agrishop.dto.SupplierDTO;
import com.agrishop.dto.PageRequestDTO;
import com.agrishop.dto.PageResponseDTO;
import com.agrishop.entity.Supplier;
import com.agrishop.repository.SupplierRepositoryLocal;
import com.agrishop.repository.ProductRepositoryLocal;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import java.util.List;
import java.util.stream.Collectors;

@Stateless
public class SupplierService implements SupplierServiceLocal {

    @EJB
    private SupplierRepositoryLocal supplierRepository;

    @EJB
    private ProductRepositoryLocal productRepository;

    @Override
    public List<SupplierDTO> getAllActiveSuppliers() {
        return supplierRepository.findActiveSuppliers().stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public PageResponseDTO<SupplierDTO> getSuppliersWithPagination(PageRequestDTO request) {
        List<Supplier> suppliers = supplierRepository.findWithPagination(request);
        long totalRecords = supplierRepository.countWithPagination(request);
        List<SupplierDTO> dtoList = suppliers.stream().map(this::convertToDTO).collect(Collectors.toList());
        return new PageResponseDTO<>(dtoList, totalRecords);
    }

    @Override
    public void createSupplier(SupplierDTO dto) {
        Supplier supplier = new Supplier();
        supplier.setSupplierCode(dto.getSupplierCode());
        supplier.setName(dto.getName());
        supplier.setAddress(dto.getAddress());
        supplier.setPhone(dto.getPhone());
        supplier.setIsDeleted(false);
        supplierRepository.create(supplier);
    }

    @Override
    public void updateSupplier(SupplierDTO dto) {
        Supplier supplier = supplierRepository.findById(dto.getId());
        if (supplier != null && !supplier.getIsDeleted()) {
            supplier.setSupplierCode(dto.getSupplierCode());
            supplier.setName(dto.getName());
            supplier.setAddress(dto.getAddress());
            supplier.setPhone(dto.getPhone());
            supplierRepository.update(supplier);
        }
    }

    @Override
    public void deleteSupplier(Long id) {
        if (productRepository.countBySupplierId(id) > 0) {
            throw new com.agrishop.exception.BusinessException("Không thể xóa nhà cung cấp vì vẫn còn sản phẩm thuộc nhà cung cấp này.");
        }
        Supplier supplier = supplierRepository.findById(id);
        if (supplier != null) {
            supplier.setIsDeleted(true);
            supplierRepository.update(supplier);
        }
    }

    private SupplierDTO convertToDTO(Supplier s) {
        SupplierDTO dto = new SupplierDTO();
        dto.setId(s.getId());
        dto.setSupplierCode(s.getSupplierCode());
        dto.setName(s.getName());
        dto.setAddress(s.getAddress());
        dto.setPhone(s.getPhone());
        return dto;
    }
}
