package com.agrishop.web.servlet;

import com.agrishop.dto.CategoryDTO;
import com.agrishop.dto.ProductDTO;
import com.agrishop.service.CategoryServiceLocal;
import com.agrishop.service.ProductServiceLocal;
import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@WebServlet(name = "SitemapServlet", urlPatterns = {"/sitemap.xml"})
public class SitemapServlet extends HttpServlet {

    @EJB
    private CategoryServiceLocal categoryService;

    @EJB
    private ProductServiceLocal productService;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/xml; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int port = request.getServerPort();
        String contextPath = request.getContextPath();
        String baseUrl = ((scheme.equals("http") && port == 80) || (scheme.equals("https") && port == 443))
                ? (scheme + "://" + serverName + contextPath)
                : (scheme + "://" + serverName + ":" + port + contextPath);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String today = sdf.format(new Date());

        try (PrintWriter out = response.getWriter()) {
            out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            out.println("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">");

            // 1. Static Pages
            writeUrl(out, baseUrl + "/index.xhtml", today, "daily", "1.0");
            writeUrl(out, baseUrl + "/store.xhtml", today, "daily", "0.9");
            writeUrl(out, baseUrl + "/about.xhtml", today, "monthly", "0.6");
            writeUrl(out, baseUrl + "/contact.xhtml", today, "monthly", "0.6");
            writeUrl(out, baseUrl + "/policy-returns.xhtml", today, "monthly", "0.5");
            writeUrl(out, baseUrl + "/policy-shipping.xhtml", today, "monthly", "0.5");
            writeUrl(out, baseUrl + "/policy-privacy.xhtml", today, "monthly", "0.5");

            // 2. Active Categories
            if (categoryService != null) {
                List<CategoryDTO> categories = categoryService.getAllActiveCategories();
                if (categories != null) {
                    for (CategoryDTO cat : categories) {
                        writeUrl(out, baseUrl + "/store.xhtml?catId=" + cat.getId(), today, "weekly", "0.8");
                    }
                }
            }

            // 3. Active Products
            if (productService != null) {
                List<ProductDTO> products = productService.getAllActiveProducts();
                if (products != null) {
                    for (ProductDTO prod : products) {
                        writeUrl(out, baseUrl + "/product-detail.xhtml?id=" + prod.getId(), today, "daily", "0.8");
                    }
                }
            }

            out.println("</urlset>");
        }
    }

    private void writeUrl(PrintWriter out, String loc, String lastMod, String changeFreq, String priority) {
        out.println("  <url>");
        out.println("    <loc>" + escapeXml(loc) + "</loc>");
        out.println("    <lastmod>" + lastMod + "</lastmod>");
        out.println("    <changefreq>" + changeFreq + "</changefreq>");
        out.println("    <priority>" + priority + "</priority>");
        out.println("  </url>");
    }

    private String escapeXml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&apos;");
    }
}
