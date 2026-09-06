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

@WebFilter(filterName = "AdminAuthFilter", urlPatterns = {"/admin/*"})
public class AdminAuthFilter implements Filter {

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
            if ("ADMIN".equals(user.getRole())) {
                chain.doFilter(request, response); // Cấp phép truy cập
            } else {
                res.sendError(HttpServletResponse.SC_FORBIDDEN, "Không có quyền truy cập"); // HTTP 403
            }
        } else {
            res.sendRedirect(req.getContextPath() + "/login.xhtml"); // Chuyển hướng về trang đăng nhập
        }
    }

    @Override
    public void destroy() {}
}