package com.agrishop.web.filter;

import com.agrishop.dto.UserDTO;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@WebFilter(filterName = "AdminAuthFilter", urlPatterns = {"/admin/*"})
public class AdminAuthFilter implements Filter {

    // Pages restricted strictly to ADMIN only
    private static final Set<String> ADMIN_ONLY_PAGES = new HashSet<>(Arrays.asList(
        "user.xhtml",
        "audit-log.xhtml",
        "coupon.xhtml"
    ));

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        HttpSession session = req.getSession(false);
        
        boolean loggedIn = (session != null) && (session.getAttribute("user") != null);
        
        if (loggedIn) {
            UserDTO user = (UserDTO) session.getAttribute("user");
            String role = user.getRole();

            if ("ADMIN".equalsIgnoreCase(role)) {
                // Bắt buộc xác thực 2FA TOTP cho ADMIN khi truy cập /admin/*
                Boolean admin2faVerified = (Boolean) session.getAttribute("admin2faVerified");
                if (admin2faVerified == null || !admin2faVerified) {
                    res.sendRedirect(req.getContextPath() + "/admin-2fa.xhtml");
                    return;
                }
                // ADMIN has full unrestricted access to all management features
                chain.doFilter(request, response);
                return;
            } 
            
            if ("STAFF".equalsIgnoreCase(role)) {
                // STAFF has operational access (products, inventory, orders, suppliers, reviews)
                String uri = req.getRequestURI();
                String pageName = uri.substring(uri.lastIndexOf("/") + 1);

                if (ADMIN_ONLY_PAGES.contains(pageName.toLowerCase())) {
                    res.sendError(HttpServletResponse.SC_FORBIDDEN, "Nhân viên (STAFF) không có quyền truy cập chức năng quản trị hệ thống này.");
                    return;
                }

                chain.doFilter(request, response);
                return;
            }

            // Other roles (e.g. CUSTOMER) attempting to access admin portal
            res.sendError(HttpServletResponse.SC_FORBIDDEN, "Tài khoản không có quyền truy cập khu vực quản trị.");
        } else {
            res.sendRedirect(req.getContextPath() + "/login.xhtml");
        }
    }

    @Override
    public void destroy() {}
}