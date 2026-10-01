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

@WebFilter(filterName = "CustomerAuthFilter", urlPatterns = {"/*"})
public class CustomerAuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        HttpSession session = req.getSession(false);
        
        String path = req.getRequestURI().substring(req.getContextPath().length());
        
        // Bỏ qua các đường dẫn admin, resources, assets, và file tĩnh
        if (path.startsWith("/admin/") || path.startsWith("/resources/") || path.startsWith("/assets/") 
                || path.contains("jakarta.faces.resource")
                || path.endsWith(".css") || path.endsWith(".js") || path.endsWith(".png") 
                || path.endsWith(".jpg") || path.endsWith(".jpeg") || path.endsWith(".webp") 
                || path.endsWith(".svg") || path.endsWith(".ico")) {
            chain.doFilter(request, response);
            return;
        }

        boolean loggedIn = (session != null) && (session.getAttribute("user") != null);
        UserDTO user = null;
        if (loggedIn) {
            user = (UserDTO) session.getAttribute("user");
        }

        // Nếu đã đăng nhập (bất kể role) mà vào lại trang login/register thì đá về trang tương ứng
        if (loggedIn && (path.equals("/login.xhtml") || path.equals("/register.xhtml"))) {
            if ("ADMIN".equals(user.getRole())) {
                res.sendRedirect(req.getContextPath() + "/admin/dashboard.xhtml");
            } else {
                res.sendRedirect(req.getContextPath() + "/index.xhtml");
            }
            return;
        }

        // Nếu chưa đăng nhập mà cố vào các trang yêu cầu tài khoản khách hàng -> Chuyển về login
        if (!loggedIn && (path.equals("/checkout.xhtml") 
                || path.equals("/order-success.xhtml") || path.equals("/order-history.xhtml") 
                || path.equals("/profile.xhtml"))) {
            res.sendRedirect(req.getContextPath() + "/login.xhtml");
            return;
        }

        // Hợp lệ thì cho đi tiếp
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {}
}