# AG R I S H O P - TECHNICAL STANDARD

## I. QUY TẮC KỸ THUẬT VÀ KIẾN TRÚC TỔNG QUAN

| ID | Module | Quy tắc | Mức độ | Vì sao |
|---|---|---|---|---|
| 1 | Architecture | Luồng kết nối chuẩn: JSF/Facelets -> CDI Bean -> Service -> Repository -> JPA -> JNDI DataSource -> SQL Server. | BẮT BUỘC | Tách biệt trách nhiệm, dễ bảo trì, đúng chuẩn Jakarta EE. |
| 2 | Architecture | JSF Bean chỉ xử lý UI state/orchestration; Business logic ở Service; Data access ở Repository. | BẮT BUỘC | Tránh code phình to ở UI, tái sử dụng logic. |
| 3 | Architecture | Transaction quản lý ở Service layer bằng JTA/Container Managed. | BẮT BUỘC | Đảm bảo data integrity khi thực hiện nghiệp vụ phức tạp. |
| 4 | Architecture | Không viết SQL/JPA query trực tiếp trong XHTML hoặc JSF Bean. | BẮT BUỘC | Bảo mật (ngăn Injection), dễ quản lý query. |
| 5 | Database | Sử dụng GlassFish quản lý JDBC Connection Pool và JNDI DataSource. | BẮT BUỘC | Hiệu năng, quản lý connection chuẩn của Application Server. |
| 6 | Database | Không hard-code database credentials hay JDBC URL trong source code. | BẮT BUỘC | Bảo mật cấu hình. |
| 7 | Database | Technical ID (BIGINT IDENTITY) tách biệt với Business Code. | BẮT BUỘC | Business Code có thể thay đổi/format lại, ID dùng cho FK. |
| 8 | Database | STT chỉ hiển thị, không lưu vào database. | BẮT BUỘC | Tránh dư thừa và sai lệch dữ liệu khi sort/filter. |
| 9 | Database | Sử dụng Soft Delete (`is_deleted`, `status`) thay vì Hard Delete với dữ liệu quan trọng. | BẮT BUỘC | Giữ lại lịch sử cho Order, Audit, Review. |
| 10 | Performance | Server-side Pagination, Search, Filter, Sorting. | BẮT BUỘC | Tránh crash server/browser khi dữ liệu lên tới hàng chục nghìn. |
| 11 | Performance | Dropdown > 100 records phải dùng Autocomplete/Server-side search. | BẮT BUỘC | Tối ưu UX và Memory. |
| 12 | UI/UX | Mọi trang dữ liệu phải có Loading, Empty, Error, Success state. | BẮT BUỘC | Tăng trải nghiệm người dùng, không gây hoang mang. |
| 13 | UI/UX | Xác thực dữ liệu (Validation) ở cả UI và Server-side. | BẮT BUỘC | Giao diện thân thiện, Server bảo vệ tính toàn vẹn. |
| 14 | Security | Phân quyền (Authorization) và xác thực (Authentication) kiểm tra ở Server. | BẮT BUỘC | Ẩn nút trên UI không có tác dụng chặn request trực tiếp. |
| 15 | Security | Customer chỉ được xem/sửa dữ liệu thuộc quyền của họ (tránh IDOR/BOLA). | BẮT BUỘC | Bảo mật dữ liệu người dùng. |
| 16 | Design | Tuân thủ Design System (Figma): Color, Typography, Component, Responsive. | KHUYẾN NGHỊ | Đảm bảo tính nhất quán, chuyên nghiệp (Premium/Trustworthy). |

## II. CHECKLIST CHỨC NĂNG TRỌNG TÂM

### A. Product Management (Admin)
- **CRUD:** Đầy đủ Create, Read (List/Detail), Update. Xóa dùng Soft Delete/Archive.
- **Search:** Theo Product Code, SKU, Name. Search partial match, không phân biệt hoa thường.
- **Filter:** Category, Status, Supplier (TÙY TRƯỜNG HỢP), Stock status.
- **Sort:** Name, Price, Stock, Created Date, Status (Default: Created Date DESC).
- **Pagination:** Bắt buộc Server-side (10/20/50).
- **Dropdown:** Category (Select - nếu ít), Supplier (Searchable Select - nếu nhiều).
- **Bắt buộc/Validation:** Price >= 0, Quantity >= 0, Name không rỗng, Code/SKU duy nhất.
- **Permission:** Quyền MANAGE_PRODUCT.
- **Soft Delete:** Bắt buộc (không xóa vật lý nếu đã có Order/Review).
- **Audit:** Có (Ai tạo, sửa, đổi giá, đổi stock).
- **State/UX:** Không để bảng trống vô nghĩa, hiển thị Empty State.

### B. Category Management (Admin)
- **CRUD:** Create, Read, Update, Soft Delete/Deactivate.
- **Search:** Tên danh mục, Code.
- **Filter:** Trạng thái (Active/Inactive).
- **Sort:** Name, Order/Priority.
- **Pagination:** Có nếu số lượng lớn.
- **Dropdown:** Parent Category (Select - chỉ lấy hợp lệ, không lấy chính nó).
- **Validation:** Không tạo circular hierarchy (A cha B, B cha A). Code duy nhất.
- **Permission:** Quyền MANAGE_CATEGORY.

### C. Inventory Management (Admin)
- **CRUD:** Read current stock, Create (Import/Export/Adjustment). Không tự sửa stock trên entity Product nếu tách Inventory.
- **Search:** Product Code, Name.
- **Filter:** Loại biến động (Import, Damaged...), Ngày tháng.
- **Sort:** Ngày biến động.
- **Pagination:** Server-side.
- **Field bắt buộc:** Reason, User, Timestamp, Lượng thay đổi.
- **Transaction & Concurrency:** BẮT BUỘC transaction, chống race condition.
- **Audit:** Bắt buộc (lưu trữ lịch sử mọi thay đổi).

### D. Order Management (Admin)
- **CRUD:** Read (List, Detail), Update (Status). KHÔNG Hard Delete, KHÔNG Update Item Giá/Tên (lấy Snapshot).
- **Search:** Order Code, Customer Name/Email/Phone.
- **Filter:** Order Status, Payment Status, Date range.
- **Sort:** Date (Default DESC).
- **Pagination:** Server-side.
- **Snapshot:** Lưu Tên SP, Giá SP tại thời điểm mua vào OrderItem.
- **Validation:** Chuyển trạng thái theo luồng (PENDING -> CONFIRMED -> PROCESSING -> SHIPPED -> DELIVERED).
- **Permission:** MANAGE_ORDER.
- **Audit:** Lưu lại ai đổi trạng thái, thời gian đổi.

### E. User/Customer Management (Admin)
- **CRUD:** Read (List, Detail), Update (Status, Role). KHÔNG tạo/sửa password trực tiếp (dùng luồng Reset).
- **Search:** Name, Email, Phone.
- **Filter:** Role, Status.
- **Sort:** Name, Created Date.
- **Pagination:** Server-side.
- **Dropdown:** Role, Status (Select).
- **Validation:** Không expose Password/Hash/Token.
- **Permission:** MANAGE_USER (User không thể tự nâng quyền).
- **Audit:** Đổi Role, Ban/Lock User.

### F. Review Management (Admin)
- **CRUD:** Read, Update (Hide/Restore - Moderation). KHÔNG Hard delete.
- **Search:** Tên sản phẩm, Người đánh giá.
- **Filter:** Rating (1-5 sao), Status.
- **Pagination:** Server-side.
- **Audit:** Lưu người ẩn bình luận.

### G. Coupon Management (Admin) (TÙY TRƯỜNG HỢP)
- **CRUD:** Đầy đủ.
- **Validation:** Code unique, Date hợp lệ (End > Start), Discount hợp lệ. Số lượng không vượt giới hạn.

### H. Customer Product Listing
- **CRUD:** Read Only.
- **Search:** Name, partial match.
- **Filter:** Category, Price Range, Availability.
- **Sort:** Relevance, Price (ASC/DESC), Newest.
- **Pagination:** Server-side (Có thể Load more hoặc Page numbers).
- **Performance:** Không load toàn bộ DB, optimize hình ảnh. Tránh N+1 khi lấy rating.

### I. Customer Product Detail
- **CRUD:** Read Only.
- **Hiển thị:** Tên, Giá, Ảnh, Tồn kho, Nguồn gốc, Đánh giá, SP liên quan.
- **Validation:** Selector số lượng không cho <= 0, không vượt tồn kho thực tế. Server kiểm tra lại khi thêm vào giỏ.

### J. Cart
- **CRUD:** Create, Read, Update (Quantity), Delete (Remove item).
- **Validation:** Server-side kiểm tra lại giá, trạng thái SP, tồn kho hiện tại mỗi khi mở Cart hoặc thanh toán. KHÔNG tin giá trị từ trình duyệt/Session.

### K. Checkout
- **CRUD:** Create Order.
- **Transaction:** BẮT BUỘC. Gồm trừ tiền, trừ tồn kho, tạo Order, tạo OrderItem, xóa Cart.
- **Concurrency:** BẮT BUỘC xử lý. Chặn trường hợp mua trùng/overselling khi còn 1 SP.
- **Validation:** Validate toàn bộ lại từ DB: giá hiện tại, tồn kho, fee, tổng tiền. Tuyệt đối không lấy Total do Client gửi lên làm chuẩn.

### L. Admin Dashboard
- **Load dữ liệu:** Chỉ load KPI (Doanh thu, Đơn mới, Khách hàng, SP sắp hết hàng).
- **Performance:** KHÔNG query hàng nghìn record để đếm. Dùng SQL COUNT/SUM tối ưu.
- **Filter:** Date range (Hôm nay, Tuần, Tháng).
- **Chart:** Trực quan (Tùy trường hợp).

## III. AG R I S H O P NON-NEGOTIABLE RULES

1. **Architecture:** Luôn tuân thủ luồng: JSF -> Service -> Repository -> JPA -> DB. Tuyệt đối không viết business logic khổng lồ trong JSF Bean.
2. **Architecture:** Không viết SQL hoặc JPA Query trực tiếp trên file giao diện `.xhtml` hay JSF Backing Bean.
3. **Database:** GlassFish quản lý JDBC Connection Pool (AgriShopPool). Code dùng JNDI. Không khởi tạo kết nối thủ công bằng `DriverManager`.
4. **Database:** Tuyệt đối không hard-code user/password/URL DB vào code.
5. **Database:** Dùng BIGINT IDENTITY làm Khóa chính (Technical ID). Business Code/SKU dành cho nghiệp vụ. STT chỉ dùng để hiển thị trên UI.
6. **Integrity:** Dùng Database Constraints (PK, FK, CHECK, UNIQUE, NOT NULL) để bảo vệ dữ liệu, không chỉ phụ thuộc vào validate của ứng dụng.
7. **Performance:** Mọi danh sách lớn BẮT BUỘC phải áp dụng Server-side Pagination, Filtering, Sorting, Searching.
8. **Performance:** Không load hàng nghìn hoặc hàng triệu record vào bộ nhớ (Memory/Browser) chỉ để phân trang hoặc đếm.
9. **UX:** Không dùng Dropdown/Select chứa toàn bộ dữ liệu nếu bảng có hàng nghìn dòng (Ví dụ: danh sách Sản phẩm). Phải dùng Server-side Searchable Autocomplete.
10. **Data Loss:** Tuyệt đối không dùng lệnh Hard Delete (DELETE FROM) với dữ liệu có lịch sử (Product, Order, Review, User). Bắt buộc dùng Soft Delete (`is_deleted` / `status`).
11. **Order Integrity:** OrderItem BẮT BUỘC phải lưu snapshot của tên sản phẩm và giá tại thời điểm mua. Không join trực tiếp ra giá hiện tại của bảng Product.
12. **Order Integrity:** Order KHÔNG BAO GIỜ được xóa vật lý (Hard delete).
13. **Concurrency:** Việc thay đổi số lượng tồn kho (Inventory) và Thanh toán (Checkout) phải được bọc trong Database Transaction và xử lý Concurrency an toàn.
14. **Validation:** UI Validation (JS, JSF Required) chỉ để tối ưu UX. BẮT BUỘC phải Validate lại mọi thứ tại Server-side trước khi lưu.
15. **Security:** Tuyệt đối không Expose SQL Exception, Stack Trace, Password, Hash, hay Secret Keys ra giao diện khách hàng.
16. **Security:** Ẩn một nút trên giao diện không được xem là Authorization. Server phải luôn kiểm tra quyền.
17. **Security:** Khách hàng (Customer) chỉ được phép thao tác/truy cập dữ liệu thuộc tài khoản của họ. Ngăn chặn IDOR (Sửa tham số ID trên URL để xem đơn hàng người khác).
18. **Trust:** Khi Checkout, tổng số tiền và phí phải được tính toán lại tại Backend. Tuyệt đối không lấy biến `Total` từ Browser gửi lên làm cơ sở thanh toán.
19. **UI States:** Bất kỳ trang hiển thị dữ liệu nào cũng phải xử lý các trạng thái: Loading, Loaded, Empty (hiển thị rõ nếu trống), Error.
20. **Complexity:** Ưu tiên giải pháp Modular Monolith có sẵn của Jakarta EE. Không áp dụng công nghệ rườm rà không cần thiết (Ví dụ: không nhồi nhét Microservices nếu chưa cần).
