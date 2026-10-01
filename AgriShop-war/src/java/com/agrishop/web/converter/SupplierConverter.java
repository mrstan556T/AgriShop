package com.agrishop.web.converter;

import com.agrishop.dto.SupplierDTO;
import com.agrishop.service.SupplierServiceLocal;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.List;

@Named
@RequestScoped
@FacesConverter(value = "supplierConverter", managed = true)
public class SupplierConverter implements Converter<SupplierDTO> {

    @Inject
    private SupplierServiceLocal supplierService;

    @Override
    public SupplierDTO getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        Long id = null;
        try {
            id = Long.valueOf(value.trim());
        } catch (NumberFormatException ignored) {}

        List<SupplierDTO> all = supplierService.getAllActiveSuppliers();
        for (SupplierDTO dto : all) {
            if (id != null && dto.getId().equals(id)) {
                return dto;
            }
            if (dto.getName() != null && dto.getName().trim().equalsIgnoreCase(value.trim())) {
                return dto;
            }
            String formatted = (dto.getName() + " (" + dto.getSupplierCode() + ")").trim();
            if (formatted.equalsIgnoreCase(value.trim())) {
                return dto;
            }
        }
        return null;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, SupplierDTO value) {
        if (value == null || value.getId() == null) {
            return "";
        }
        return String.valueOf(value.getId());
    }
}
