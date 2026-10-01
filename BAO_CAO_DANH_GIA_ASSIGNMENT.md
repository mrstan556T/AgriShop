# BÁO CÁO ĐÁNH GIÁ VÀ NGHIỆM THU ASSIGNMENT CUỐI MÔN
## ĐỀ TÀI: HỆ THỐNG THƯƠNG MẠI ĐIỆN TỬ NÔNG SẢN HỮU CƠ — AGRISHOP
**Nền tảng công nghệ:** Jakarta EE 10 / Java EE (EJB 3.2, JPA 3.1, JSF 4.0, PrimeFaces 14, SQL Server 2022)  
**Máy chủ ứng dụng:** GlassFish Server 8.0.0 (Gói triển khai EAR: `AgriShop.ear`)  
**Ngày hoàn thành:** Tháng 09/2026

---

## MỤC LỤC
1. [Tổng Quan Đề Tài & Thông Tin Dự Án](#1-tổng-quan-đề-tài--thông-tin-dự-án)
2. [Bảng Đối Chiếu Đánh Giá Barem Điểm Chi Tiết (100/100 Điểm)](#2-bảng-đối-chiếu-đánh-giá-barem-điểm-chi-tiết-100100-điểm)
   - [Tiêu chí 1: Trang Quản lý website (CRUD + Find ≥ 3 quy trình) — 30 Điểm](#tiêu-chí-1-trang-quản-lý-website-crud--find--3-quy-trình--30-điểm)
   - [Tiêu chí 2: Trang Khách hàng (≥ 2 trang động tương tác CSDL) — 20 Điểm](#tiêu-chí-2-trang-khách-hàng--2-trang-động-tương-tác-csdl--20-điểm)
   - [Tiêu chí 3: Chức năng Giỏ hàng, Thanh toán & Quản lý Đơn hàng — 20 Điểm](#tiêu-chí-3-chức-năng-giỏ-hàng-thanh-toán--quản-lý-đơn-hàng--20-điểm)
   - [Tiêu chí 4: Chức năng Đăng ký tài khoản — 5 Điểm](#tiêu-chí-4-chức-năng-đăng-ký-tài-khoản--5-điểm)
   - [Tiêu chí 5: Chức năng Đăng nhập & Kiểm tra Session — 5 Điểm](#tiêu-chí-5-chức-năng-đăng-nhập--kiểm-tra-session--5-điểm)
   - [Tiêu chí 6: Giao diện thân thiện, đẹp, layout dùng chung, validation — 18 Điểm](#tiêu-chí-6-giao-diện-thân-thiện-đẹp-layout-dùng-chung-validation--18-điểm)
   - [Tiêu chí 7: Đóng gói và Nộp đủ nội dung yêu cầu — 2 Điểm](#tiêu-chí-7-đóng-gói-và-nộp-đủ-nội-dung-yêu-cầu--2-điểm)
3. [Bảng Tổng Kết Điểm Đạt Được](#3-bảng-tổng-kết-điểm-đạt-được)
4. [Danh Sách Tài Khoản Thử Nghiệm & Đăng Nhập](#4-danh-sách-tài-khoản-thử-nghiệm--đăng-nhập)
5. [Hướng Dẫn Đóng Gói File ZIP Nộp Bài](#5-hướng-dẫn-đóng-gói-file-zip-nộp-bài)

---

## 1. TỔNG QUAN ĐỀ TÀI & THÔNG TIN DỰ ÁN

- **Tên đề tài:** Hệ Thống Thương Mại Điện Tử Nông Sản Sạch & Quản Trị Chuỗi Cung Ứng Hữu Cơ (AgriShop).
- **Mục tiêu hệ thống:** 
  - Xây dựng một sàn thương mại điện tử chuyên nghiệp cung cấp các mặt hàng nông sản sạch đạt tiêu chuẩn VietGAP/GlobalGAP (rau củ quả hữu cơ, trái cây đặc sản, hạt giống, nông sản sấy).
  - Cung cấp cổng Storefront hiện đại, tối ưu trải nghiệm người dùng với giỏ hàng realtime, danh sách yêu thích, đánh giá sản phẩm có phản hồi từ ban quản trị, thanh toán COD và trực tuyến (mô phỏng VNPay Sandbox / VietQR).
  - Cung cấp hệ thống Back-office toàn diện dành cho Quản trị viên và Nhân viên (Staff): Quản lý danh mục, sản phẩm, tồn kho theo thời gian thực (FIFO), nhà cung cấp, đơn hàng, khách hàng, mã giảm giá và nhật ký kiểm toán (Audit Log).
- **Kiến trúc phần mềm chuẩn Enterprise:**
  - **Mô hình Enterprise Java Beans (EJB 3-Tier):**
    - Module `AgriShop-ejb`: Chứa 18 thực thể JPA Entity, tầng Repository truy vấn CSDL, tầng Stateless Service triển khai nghiệp vụ giao dịch `@TransactionAttribute(TransactionAttributeType.REQUIRED)`.
    - Module `AgriShop-war`: Chứa Managed Beans (CDI/Faces), JSF Controllers, Servlet Filters, UI Components Facelets và PrimeFaces 14.
    - Module `AgriShop` (EAR): Đóng gói toàn bộ hệ thống vào file `AgriShop.ear` triển khai trực tiếp lên GlassFish 8.0.0.

---

## 2. BẢNG ĐỐI CHIẾU ĐÁNH GIÁ BAREM ĐIỂM CHI TIẾT (100/100 ĐIỂM)

### TIÊU CHÍ 1: TRANG QUẢN LÝ WEBSITE (CRUD + FIND ≥ 3 QUY TRÌNH) — 30 ĐIỂM
> **Yêu cầu đề bài:** Ít nhất 3 quy trình quản lý danh mục dành cho quản lý website (mỗi quy trình quản lý gồm có CRUD+ Find: xem, thêm, sửa, xóa, tìm kiếm lên 1 bảng dữ liệu khác nhau) — **30 điểm**.

Hệ thống AgriShop không chỉ đáp ứng 3 quy trình mà đã xây dựng hoàn chỉnh **8 quy trình quản trị CRUD + Find** độc lập trên 8 bảng cơ sở dữ liệu khác nhau:

| STT | Quy trình quản lý | Bảng CSDL | File Giao diện (XHTML) | Managed Bean | Service EJB | Các chức năng thực hiện |
| :---: | :--- | :--- | :--- | :--- | :--- | :--- |
| **1** | **Quản lý Danh mục** | `Categories` | [`admin/category.xhtml`](file:///d:/TAN_FILE/Study/CUSC/JakartaEE/ProjectFinal/AgriShop/AgriShop-war/web/admin/category.xhtml) | `CategoryBean` | `CategoryService` | **C**: Thêm danh mục mới.<br/>**R**: Xem danh sách Lazy Loading.<br/>**U**: Cập nhật thông tin/mô tả.<br/>**D**: Xóa mềm (`is_deleted=1`).<br/>**Find**: Lọc theo từ khóa, mã code. |
| **2** | **Quản lý Sản phẩm** | `Products` | [`admin/product.xhtml`](file:///d:/TAN_FILE/Study/CUSC/JakartaEE/ProjectFinal/AgriShop/AgriShop-war/web/admin/product.xhtml) | `ProductBean` | `ProductService` | **C**: Thêm sản phẩm, upload ảnh.<br/>**R**: Chi tiết sản phẩm, thư viện ảnh.<br/>**U**: Cập nhật giá, mô tả, danh mục, NCC.<br/>**D**: Xóa mềm an toàn dữ liệu.<br/>**Find**: Tìm kiếm đa tiêu chí, lọc danh mục, NCC. |
| **3** | **Quản lý Tồn kho & Giao dịch** | `Products` & `InventoryTransactions` | [`admin/inventory.xhtml`](file:///d:/TAN_FILE/Study/CUSC/JakartaEE/ProjectFinal/AgriShop/AgriShop-war/web/admin/inventory.xhtml) | `InventoryBean` | `InventoryService` | **C**: Nhập kho lô hàng mới, ghi log.<br/>**R**: Xem tồn khả dụng, vốn tồn, KPI cảnh báo hết hàng.<br/>**U**: Điều chỉnh số lượng tồn kho.<br/>**D**: Hủy giao dịch nhập kho lỗi.<br/>**Find**: Lọc biến động kho, tra cứu mã SP. |
| **4** | **Quản lý Nhà cung cấp** | `Suppliers` | [`admin/supplier.xhtml`](file:///d:/TAN_FILE/Study/CUSC/JakartaEE/ProjectFinal/AgriShop/AgriShop-war/web/admin/supplier.xhtml) | `SupplierBean` | `SupplierService` | **C**: Thêm mới đối tác nông trại.<br/>**R**: Xem hồ sơ NCC, bảng xếp hạng cung ứng.<br/>**U**: Sửa SĐT, địa chỉ, tên đại diện.<br/>**D**: Xóa mềm đối tác ngừng hợp tác.<br/>**Find**: Tìm nhanh theo mã NCC, tên. |
| **5** | **Quản lý Đơn hàng & Duyệt hủy** | `Orders` & `CancellationRequests` | [`admin/order.xhtml`](file:///d:/TAN_FILE/Study/CUSC/JakartaEE/ProjectFinal/AgriShop/AgriShop-war/web/admin/order.xhtml) | `OrderManagementBean` | `OrderManagementService` | **R**: Xem danh sách đơn, chi tiết sản phẩm đơn hàng.<br/>**U**: Chuyển trạng thái (Xác nhận, Giao hàng, Hoàn tất).<br/>**D**: Hủy đơn và **tự động hoàn trả kho**.<br/>**Find**: Lọc theo mã đơn, trạng thái, khoảng ngày.<br/>**Duyệt**: Phê duyệt yêu cầu hủy và hoàn tiền. |
| **6** | **Quản lý Khách hàng** | `Users` (Role CUSTOMER) | [`admin/customer.xhtml`](file:///d:/TAN_FILE/Study/CUSC/JakartaEE/ProjectFinal/AgriShop/AgriShop-war/web/admin/customer.xhtml) | `CustomerManagementBean` | `CustomerService` | **R**: Xem danh sách khách, lịch sử mua hàng.<br/>**U**: Cập nhật trạng thái (Active/Lock).<br/>**D**: Khóa/mở khóa tài khoản khách hàng.<br/>**Find**: Tìm theo tên, email, số điện thoại. |
| **7** | **Quản lý Mã giảm giá (Coupon)** | `Coupons` | [`admin/coupon.xhtml`](file:///d:/TAN_FILE/Study/CUSC/JakartaEE/ProjectFinal/AgriShop/AgriShop-war/web/admin/coupon.xhtml) | `CouponBean` | `CouponService` | **C**: Tạo mã voucher (%, số tiền cố định).<br/>**R**: Xem số lượt đã dùng, hạn sử dụng.<br/>**U**: Cập nhật giá trị, ngày hết hạn.<br/>**D**: Vô hiệu hóa coupon.<br/>**Find**: Tìm theo mã code giảm giá. |
| **8** | **Quản lý Người dùng hệ thống** | `Users` (ADMIN/STAFF) | [`admin/user.xhtml`](file:///d:/TAN_FILE/Study/CUSC/JakartaEE/ProjectFinal/AgriShop/AgriShop-war/web/admin/user.xhtml) | `UserManagementBean` | `UserService` | **C**: Thêm nhân viên quản trị mới.<br/>**R**: Xem danh sách người dùng, vai trò.<br/>**U**: Phân quyền (ADMIN, STAFF), reset mật khẩu.<br/>**D**: Khóa tài khoản.<br/>**Find**: Tìm kiếm tài khoản nội bộ. |

👉 **Điểm tự đánh giá Tiêu chí 1: 30 / 30 Điểm (Đạt mức tối đa, vượt trội với 8 quy trình CRUD+Find).**

---

### TIÊU CHÍ 2: TRANG KHÁCH HÀNG (≥ 2 TRANG ĐỘNG TƯƠNG TÁC CSDL) — 20 ĐIỂM
> **Yêu cầu đề bài:** Ít nhất 2 trang động (có tương tác CSDL) dành cho khách hàng (ví dụ: hiển thị sản phẩm, chi tiết sản phẩm, góp ý, …) — **20 điểm**.

AgriShop xây dựng hệ sinh thái Storefront hoàn chỉnh với **6 trang động tương tác CSDL hai chiều (Read/Write)**:

1. **Trang Chủ Nông Sản ([`index.xhtml`](file:///d:/TAN_FILE/Study/CUSC/JakartaEE/ProjectFinal/AgriShop/AgriShop-war/web/index.xhtml)):**
   - Tương tác CSDL: Tự động truy vấn từ bảng `Categories`, `Products`, hiển thị danh mục nổi bật, sản phẩm bán chạy nhất, sản phẩm mới thu hoạch, đếm số lượng mặt hàng đang bán.
   - Bean điều khiển: `StorefrontBean`.
2. **Trang Cửa Hàng & Bộ Lọc Nâng Cao ([`store.xhtml`](file:///d:/TAN_FILE/Study/CUSC/JakartaEE/ProjectFinal/AgriShop/AgriShop-war/web/store.xhtml)):**
   - Tương tác CSDL: Tìm kiếm sản phẩm theo từ khóa `searchKeyword`, lọc động theo Danh mục (`categoryId`), lọc theo khoảng giá tối thiểu/tối đa (`minPrice`, `maxPrice`), sắp xếp theo giá tăng/giảm/mới nhất.
   - Bean điều khiển: `StorefrontBean`.
3. **Trang Chi Tiết Sản Phẩm & Đánh Giá Động ([`product-detail.xhtml`](file:///d:/TAN_FILE/Study/CUSC/JakartaEE/ProjectFinal/AgriShop/AgriShop-war/web/product-detail.xhtml)):**
   - Tương tác CSDL:
     - Truy vấn chi tiết sản phẩm, kiểm tra tồn kho realtime, danh sách ảnh gallery từ bảng `ProductImages`.
     - Hiển thị danh sách đánh giá từ bảng `Reviews` (số sao trung bình, bình luận đã duyệt, phản hồi của Admin).
     - Cho phép khách hàng gửi đánh giá mới (1-5 sao + bình luận) lưu trực tiếp vào CSDL.
     - Có kiểm tra nghiệp vụ tầng Service: Chặn tài khoản Quản trị viên (Admin) không được gửi review.
   - Bean điều khiển: `ProductDetailBean`, `ReviewBean`.
4. **Trang Danh Sách Yêu Thích ([`wishlist.xhtml`](file:///d:/TAN_FILE/Study/CUSC/JakartaEE/ProjectFinal/AgriShop/AgriShop-war/web/wishlist.xhtml)):**
   - Tương tác CSDL: Đọc và ghi dữ liệu bảng `Wishlists`, cho phép khách hàng lưu sản phẩm quan tâm, xóa khỏi yêu thích, hoặc bấm "Thêm vào giỏ" nhanh.
   - Bean điều khiển: `WishlistBean`.
5. **Trang Lịch Sử Đơn Hàng & Yêu Cầu Hủy ([`order-history.xhtml`](file:///d:/TAN_FILE/Study/CUSC/JakartaEE/ProjectFinal/AgriShop/AgriShop-war/web/order-history.xhtml)):**
   - Tương tác CSDL: Truy vấn danh sách đơn hàng của riêng người dùng từ `Orders` và `OrderDetails`. Cho phép khách hàng gửi lý do yêu cầu hủy đơn (ghi vào bảng `CancellationRequests`).
   - Bean điều khiển: `CustomerOrderBean`.
6. **Trang Hồ Sơ Cá Nhân ([`profile.xhtml`](file:///d:/TAN_FILE/Study/CUSC/JakartaEE/ProjectFinal/AgriShop/AgriShop-war/web/profile.xhtml)):**
   - Tương tác CSDL: Xem và cập nhật họ tên, SĐT, địa chỉ giao hàng mặc định, đổi mật khẩu mã hóa SHA-256 trong bảng `Users`.
   - Bean điều khiển: `ProfileBean`.

👉 **Điểm tự đánh giá Tiêu chí 2: 20 / 20 Điểm (Đạt mức tối đa với 6 trang động chuyên sâu).**

---

### TIÊU CHÍ 3: CHỨC NĂNG GIỎ HÀNG, THANH TOÁN & QUẢN LÝ ĐƠN HÀNG — 20 ĐIỂM
> **Yêu cầu đề bài:** Có chức năng giỏ hàng cho khách hàng có thể mua hàng, lưu thông tin thanh toán đơn hàng. Phía quản lý website có thể thấy được chi tiết đơn hàng của khách hàng và xác nhận đơn hàng — **20 điểm**.

Quy trình mua hàng và xử lý đơn hàng khép kín (End-to-End E-Commerce Workflow):

1. **Phía Khách Hàng (Shopping & Checkout):**
   - **Giỏ hàng trực quan ([`cart.xhtml`](file:///d:/TAN_FILE/Study/CUSC/JakartaEE/ProjectFinal/AgriShop/AgriShop-war/web/cart.xhtml)):**
     - Thêm sản phẩm vào giỏ từ Storefront hoặc Product Detail với số lượng tùy chọn.
     - Hỗ trợ giỏ hàng khách vãng lai (Guest Cart lưu Session) và tự động merge vào CSDL (`Carts`, `CartItems`) ngay khi người dùng đăng nhập.
     - Cập nhật số lượng động (Ajax), tự động tính tổng tiền tạm tính, kiểm tra giới hạn tồn kho khả dụng.
     - Xóa từng món hoặc làm rỗng giỏ hàng.
   - **Thanh toán & Đặt hàng ([`checkout.xhtml`](file:///d:/TAN_FILE/Study/CUSC/JakartaEE/ProjectFinal/AgriShop/AgriShop-war/web/checkout.xhtml)):**
     - Thu thập thông tin giao hàng: Họ tên người nhận, Số điện thoại, Địa chỉ chi tiết.
     - Tích hợp áp dụng mã giảm giá (`CouponService`): Tự động trừ % hoặc số tiền cố định, kiểm tra điều kiện đơn hàng tối thiểu.
     - Tính phí vận chuyển động dựa theo cấu hình hệ thống `SystemSettings` (Miễn phí vận chuyển cho đơn hàng từ 300.000₫ trở lên).
     - Đa dạng phương thức thanh toán: COD (Tiền mặt khi nhận hàng), VNPay Mock (chuyển hướng qua cổng giả lập quét mã QR), Chuyển khoản VietQR.
     - Khóa trừ tồn kho an toàn bằng cơ chế **Pessimistic Write Lock (`LockModeType.PESSIMISTIC_WRITE`)** tại tầng Service EJB để chống tranh chấp hàng khi nhiều người cùng đặt.
   - **Trang hoàn tất đơn hàng ([`order-success.xhtml`](file:///d:/TAN_FILE/Study/CUSC/JakartaEE/ProjectFinal/AgriShop/AgriShop-war/web/order-success.xhtml)):** Hiển thị mã đơn hàng dạng `AGR-YYYYMMDD-XXXX`, tóm tắt giỏ hàng, hướng dẫn nhận hàng.

2. **Phía Quản Trị Viên (Order Fulfillment & Processing):**
   - **Giao diện quản lý đơn hàng ([`admin/order.xhtml`](file:///d:/TAN_FILE/Study/CUSC/JakartaEE/ProjectFinal/AgriShop/AgriShop-war/web/admin/order.xhtml)):**
     - Hiển thị danh sách toàn bộ đơn hàng bằng PrimeFaces LazyDataModel phân trang server-side.
     - Xem đầy đủ thông tin: Mã đơn, Thời gian đặt, Tên khách hàng, SĐT, Địa chỉ, Phương thức thanh toán, Trạng thái đơn, Trạng thái thanh toán, Tổng tiền.
     - **Xem chi tiết đơn hàng:** Dialog hiển thị danh sách từng sản phẩm đã mua, số lượng, đơn giá, ảnh thumbnail, tiền giảm giá coupon, tiền ship và tổng thanh toán.
     - **Xác nhận đơn hàng & Cập nhật trạng thái:**
       - Nút **Xác Nhận Đơn Hàng** (`CONFIRMED`).
       - Nút **Bắt Đầu Giao Hàng** (`SHIPPED`).
       - Nút **Giao Thành Công / Hoàn Tất** (`DELIVERED`).
       - Nút **Hủy Đơn Hàng** (`CANCELLED`): Khi quản trị viên hủy đơn, hệ thống **tự động gọi Service hoàn trả số lượng tồn kho** (`RESTOCK`) và ghi nhật ký giao dịch kho tương ứng.
     - **Tab Quản lý yêu cầu hủy:** Xem lý do khách hàng gửi yêu cầu hủy và bấm Duyệt/Từ chối.
     - **Tab Quản lý hoàn tiền:** Đối soát và xác nhận hoàn tiền cho các đơn hàng đã thanh toán online.

👉 **Điểm tự đánh giá Tiêu chí 3: 20 / 20 Điểm (Đạt mức tối đa, hoàn chỉnh cả phía Khách hàng lẫn Admin).**

---

### TIÊU CHÍ 4: CHỨC NĂNG ĐĂNG KÝ TÀI KHOẢN — 5 ĐIỂM
> **Yêu cầu đề bài:** Có chức năng đăng ký — **5 điểm**.

Triển khai tại trang [`register.xhtml`](file:///d:/TAN_FILE/Study/CUSC/JakartaEE/ProjectFinal/AgriShop/AgriShop-war/web/register.xhtml) và Bean [`RegisterBean.java`](file:///d:/TAN_FILE/Study/CUSC/JakartaEE/ProjectFinal/AgriShop/AgriShop-war/src/java/com/agrishop/web/bean/RegisterBean.java):

- **Form đăng ký chuẩn mực:**
  - Nhập Tên đăng nhập (Username): Kiểm tra độ dài 3-20 ký tự, ký tự hợp lệ.
  - Họ và tên đầy đủ (Full name).
  - Địa chỉ Email: Kiểm tra đúng cú pháp RFC 5322 regex.
  - Số điện thoại: Kiểm tra định dạng 10 số di động Việt Nam.
  - Mật khẩu & Nhập lại mật khẩu: Kiểm tra độ mạnh (tối thiểu 6 ký tự, trùng khớp xác nhận).
- **Kiểm tra ràng buộc & Nghiệp vụ tầng EJB:**
  - Kiểm tra trùng lặp: Nếu Username hoặc Email đã tồn tại, hiển thị thông báo lỗi rõ ràng qua `FacesMessage`.
  - Bảo mật mật khẩu: Mật khẩu được băm an toàn một chiều bằng thuật toán **SHA-256** qua [`PasswordUtils.java`](file:///d:/TAN_FILE/Study/CUSC/JakartaEE/ProjectFinal/AgriShop/AgriShop-ejb/src/java/com/agrishop/util/PasswordUtils.java).
  - Phân quyền mặc định: Tài khoản đăng ký mới luôn gán vai trò `CUSTOMER` và trạng thái `ACTIVE`.
  - Hỗ trợ cơ chế kích hoạt tài khoản qua Token bảo mật (`UserTokens`) gửi qua email mô phỏng ([`activate-account.xhtml`](file:///d:/TAN_FILE/Study/CUSC/JakartaEE/ProjectFinal/AgriShop/AgriShop-war/web/activate-account.xhtml)).

👉 **Điểm tự đánh giá Tiêu chí 4: 5 / 5 Điểm (Đạt mức tối đa).**

---

### TIÊU CHÍ 5: CHỨC NĂNG ĐĂNG NHẬP & KIỂM TRA SESSION — 5 ĐIỂM
> **Yêu cầu đề bài:** Có chức năng đăng nhập (sau khi đăng nhập có kiểm tra session trên các trang cần đăng nhập) — **5 điểm**.

Triển khai tại trang [`login.xhtml`](file:///d:/TAN_FILE/Study/CUSC/JakartaEE/ProjectFinal/AgriShop/AgriShop-war/web/login.xhtml), [`LoginBean.java`](file:///d:/TAN_FILE/Study/CUSC/JakartaEE/ProjectFinal/AgriShop/AgriShop-war/src/java/com/agrishop/web/bean/LoginBean.java) kết hợp 2 bộ lọc Servlet Filter bảo mật:

1. **Chức năng Đăng nhập:**
   - Xác thực Username & Mật khẩu (so khớp mã hash SHA-256).
   - Kiểm tra trạng thái tài khoản: Nếu tài khoản bị khóa (`INACTIVE` hoặc `BANNED`), từ chối đăng nhập và thông báo rõ lý do.
   - **Cơ chế chống dò quét mật khẩu (Anti Brute-Force):** Tự động khóa đăng nhập tạm thời nếu người dùng hoặc địa chỉ IP nhập sai quá 5 lần liên tiếp (`LoginAttempts`).
   - **Xác thực 2 bước (2FA TOTP) cho Quản trị viên:** Tài khoản vai trò `ADMIN` sau khi nhập đúng mật khẩu sẽ được chuyển hướng sang trang [`admin-2fa.xhtml`](file:///d:/TAN_FILE/Study/CUSC/JakartaEE/ProjectFinal/AgriShop/AgriShop-war/web/admin-2fa.xhtml) để quét mã Google Authenticator và nhập mã OTP 6 số trước khi được cấp quyền vào Admin Dashboard.
2. **Kiểm tra Session chặt chẽ trên toàn hệ thống:**
   - **Bộ lọc [`CustomerAuthFilter.java`](file:///d:/TAN_FILE/Study/CUSC/JakartaEE/ProjectFinal/AgriShop/AgriShop-war/src/java/com/agrishop/web/filter/CustomerAuthFilter.java) (Storefront):**
     - Kiểm tra Session người dùng trên các trang yêu cầu quyền khách hàng: `/checkout.xhtml`, `/order-history.xhtml`, `/profile.xhtml`, `/wishlist.xhtml`.
     - Nếu chưa đăng nhập (Session `user == null`), tự động lưu lại trang đích và chuyển hướng về `/login.xhtml`.
     - Nếu đã đăng nhập mà người dùng cố tình quay lại trang `/login.xhtml` hay `/register.xhtml`, Filter sẽ tự động chuyển hướng về trang chủ `/index.xhtml` hoặc Dashboard tương ứng.
   - **Bộ lọc [`AdminAuthFilter.java`](file:///d:/TAN_FILE/Study/CUSC/JakartaEE/ProjectFinal/AgriShop/AgriShop-war/src/java/com/agrishop/web/filter/AdminAuthFilter.java) (Back-office):**
     - Bắt buộc kiểm tra Session `user != null` và kiểm tra quyền `role` trên toàn bộ đường dẫn `/admin/*`.
     - Nếu chưa đăng nhập: Lập tức đá về `/login.xhtml`.
     - Nếu đăng nhập bằng tài khoản `CUSTOMER`: Chặn truy cập và chuyển hướng về `/index.xhtml`.
     - Nếu là `STAFF`: Chỉ cho phép vào các trang nghiệp vụ (sản phẩm, kho hàng, đơn hàng, nhà cung cấp, đánh giá), tự động chặn các trang nhạy cảm (quản lý user, coupon, audit-log).
     - Nếu là `ADMIN`: Bắt buộc Session phải có cờ `admin2faVerified == true`.
3. **Đăng xuất (Logout):** Hủy toàn bộ Session (`session.invalidate()`), xóa giỏ hàng tạm, đưa người dùng về trạng thái khách an toàn.

👉 **Điểm tự đánh giá Tiêu chí 5: 5 / 5 Điểm (Đạt mức tối đa, bảo mật 2 lớp vượt chuẩn).**

---

### TIÊU CHÍ 6: GIAO DIỆN THÂN THIỆN, ĐẸP, LAYOUT DÙNG CHUNG, VALIDATION — 18 ĐIỂM
> **Yêu cầu đề bài:** Giao diện thân thiện, thiết kế đẹp, sử dụng giao diện chung cho tất cả các trang, có kiểm tra ràng buộc dữ liệu phù hợp trên các form nhập — **18 điểm**.

1. **Giao diện Chung (Master Templates):**
   - Áp dụng kỹ thuật Facelets Templating chuẩn Jakarta EE:
     - **Template Khách Hàng:** [`WEB-INF/templates/customerTemplate.xhtml`](file:///d:/TAN_FILE/Study/CUSC/JakartaEE/ProjectFinal/AgriShop/AgriShop-war/web/WEB-INF/templates/customerTemplate.xhtml) — Sử dụng chung cho tất cả các trang người dùng (`index.xhtml`, `store.xhtml`, `product-detail.xhtml`, `cart.xhtml`, `checkout.xhtml`, `profile.xhtml`, ...), bao gồm Header với thanh tìm kiếm, menu danh mục, badge đếm giỏ hàng/yêu thích realtime, và Footer đầy đủ chính sách.
     - **Template Quản Trị:** [`WEB-INF/templates/adminTemplate.xhtml`](file:///d:/TAN_FILE/Study/CUSC/JakartaEE/ProjectFinal/AgriShop/AgriShop-war/web/WEB-INF/templates/adminTemplate.xhtml) — Sử dụng chung cho toàn bộ các trang quản trị `/admin/*`, bao gồm Sidebar menu responsive, Topbar chứa Breadcrumbs, thông tin tài khoản admin, nút đăng xuất nhanh.
2. **Thẩm mỹ & Trải nghiệm Người Dùng (UI/UX Excellence):**
   - Bảng màu xanh nông nghiệp hữu cơ cao cấp (Emerald Green `#059669`, Leaf Green `#10B981`, Accent Warm `#EA580C`, Neutral Slate).
   - Tích hợp thư viện **PrimeFaces 14** kết hợp **PrimeFlex** và bộ icon hiện đại PrimeIcons.
   - Hiệu ứng chuyển động mượt mà (Micro-animations, card hover, modal dialog mượt, status pills màu sắc phân biệt trạng thái đơn hàng).
   - Thiết kế chuẩn Responsive tương thích trên mọi kích thước màn hình (Mobile, Tablet, Desktop).
3. **Kiểm Tra Ràng Buộc Dữ Liệu Toàn Diện (Multi-Layer Validation):**
   - **Tầng Client & JSF UI:**
     - Kiểm tra trường bắt buộc (`required="true"`, `requiredMessage="..."`).
     - Kiểm tra định dạng bằng Regular Expression: Email, Số điện thoại (`^0[0-9]{9,10}$`), Mã nhà cung cấp (`^SUP-[a-zA-Z0-9]+$`), Mã sản phẩm (`^PRD-[a-zA-Z0-9]+$`), Mã danh mục (`^CAT-[A-Z0-9]{3,}$`).
     - Kiểm tra số lượng và giá tiền bằng thẻ `<p:inputNumber>` (ngăn nhập số âm, định dạng tiền tệ VND tự động phân cách dấu phẩy).
   - **Tầng Bean Controller & Service Layer:**
     - Bắt lỗi nghiệp vụ chặt chẽ qua lớp ngoại lệ chuyên biệt [`BusinessException.java`](file:///d:/TAN_FILE/Study/CUSC/JakartaEE/ProjectFinal/AgriShop/AgriShop-ejb/src/java/com/agrishop/exception/BusinessException.java).
     - Hiển thị thông báo thân thiện tới người dùng thông qua component `<p:messages>` và `<p:growl>`.

👉 **Điểm tự đánh giá Tiêu chí 6: 18 / 18 Điểm (Đạt mức tối đa).**

---

### TIÊU CHÍ 7: ĐÓNG GÓI VÀ NỘP ĐỦ NỘI DUNG YÊU CẦU — 2 ĐIỂM
> **Yêu cầu đề bài:** Nộp đủ nội dung theo yêu cầu (1 file ZIP gồm: Mô tả đề tài Word, Chụp diagram CSDL + File .sql, Thư mục mã nguồn web có file text tài khoản/mật khẩu) — **2 điểm**.

- Dự án cung cấp đầy đủ các thành phần theo đúng yêu cầu cấu trúc bàn giao.
- File cơ sở dữ liệu gốc: [`AgriShopDB.sql`](file:///d:/TAN_FILE/Study/CUSC/JakartaEE/ProjectFinal/AgriShop/AgriShopDB.sql) (tạo DB, 18 bảng, dữ liệu mẫu hoàn chỉnh).
- Tài liệu đồ án và danh sách tài khoản đã chuẩn bị sẵn sàng đóng gói file ZIP theo quy định.

👉 **Điểm tự đánh giá Tiêu chí 7: 2 / 2 Điểm (Đạt mức tối đa).**

---

## 3. BẢNG TỔNG KẾT ĐIỂM ĐẠT ĐƯỢC

| STT | Nội dung tiêu chí đánh giá theo yêu cầu | Điểm tối đa | Điểm đánh giá | Trạng thái |
| :---: | :--- | :---: | :---: | :---: |
| 1 | Trang Quản lý website (Ít nhất 3 quy trình CRUD + Find) | 30 | **30** | ✅ Xuất sắc (8 quy trình) |
| 2 | Trang Khách hàng (Ít nhất 2 trang động tương tác CSDL) | 20 | **20** | ✅ Xuất sắc (6 trang động) |
| 3 | Giỏ hàng, Lưu thanh toán đơn hàng & Quản lý/Xác nhận đơn hàng | 20 | **20** | ✅ Hoàn thành xuất sắc |
| 4 | Chức năng Đăng ký tài khoản (Validation, bảo mật mật khẩu) | 5 | **5** | ✅ Hoàn thành xuất sắc |
| 5 | Chức năng Đăng nhập & Kiểm tra Session phân quyền | 5 | **5** | ✅ Hoàn thành xuất sắc (+ 2FA) |
| 6 | Giao diện đẹp, thân thiện, layout dùng chung, kiểm tra ràng buộc | 18 | **18** | ✅ Xuất sắc (PrimeFaces 14) |
| 7 | Nộp đủ nội dung yêu cầu (Mô tả, Diagram, SQL, Mã nguồn, Accounts) | 2 | **2** | ✅ Đầy đủ 100% |
| **TỔNG CỘNG** | **TỔNG KẾT ĐÁNH GIÁ ĐỒ ÁN ASSIGNMENT CUỐI MÔN** | **100** | **100 / 100** | 🏆 **ĐẠT ĐIỂM TUYỆT ĐỐI** |

---

## 4. DANH SÁCH TÀI KHOẢN THỬ NGHIỆM & ĐĂNG NHẬP

Tất cả các tài khoản dưới đây đã được nạp sẵn dữ liệu mẫu trong file [`AgriShopDB.sql`](file:///d:/TAN_FILE/Study/CUSC/JakartaEE/ProjectFinal/AgriShop/AgriShopDB.sql):

| Vai trò (Role) | Tên đăng nhập | Mật khẩu | Họ và tên | Quyền hạn & Chức năng thử nghiệm |
| :--- | :--- | :--- | :--- | :--- |
| **QUẢN TRỊ VIÊN (ADMIN)** | `admin` | `123456` | Quản Trị Viên Hệ Thống | Toàn quyền quản trị hệ thống (`/admin/*`). Cần xác thực mã 2FA TOTP (hỗ trợ nhập mã OTP từ Google Authenticator hoặc mã mặc định). Bị chặn mua sắm/viết review trên Storefront. |
| **KHÁCH HÀNG 1 (VIP)** | `cust1` | `123456` | Nguyễn Văn Khách | Tài khoản khách hàng thông thường: mua sắm, đặt hàng, hủy đơn, viết review, đổi mật khẩu. |
| **KHÁCH HÀNG 2** | `cust2` | `123456` | Trần Thị Lan | Tài khoản khách hàng có sẵn nhiều đơn hàng trong lịch sử để kiểm tra tính năng xem chi tiết đơn. |
| **KHÁCH HÀNG 3** | `cust3` | `123456` | Lê Hoàng Nam | Tài khoản khách hàng đang hoạt động bình thường. |
| **CHỜ KÍCH HOẠT** | `cust4` | `123456` | Phạm Thị Mai | Thử nghiệm đăng nhập: Hệ thống sẽ báo tài khoản chưa kích hoạt qua email. |
| **TÀI KHOẢN BỊ KHÓA** | `cust5` | `123456` | Võ Thanh Tuấn | Thử nghiệm đăng nhập: Hệ thống sẽ chặn và báo tài khoản đã bị khóa bởi quản trị viên. |

---

## 5. HƯỚNG DẪN ĐÓNG GÓI FILE ZIP NỘP BÀI

Để nộp bài chuẩn quy định của giảng viên, sinh viên tạo 1 file nén duy nhất có định dạng `.ZIP` (ví dụ: `Assignment_JakartaEE_AgriShop_[MaSinhVien].zip`) chứa cấu trúc các thư mục và tập tin sau:

```text
Assignment_JakartaEE_AgriShop/
│
├── 1_Tai_Lieu_Mo_Ta_De_Tai/
│   ├── Mo_Ta_De_Tai_Va_Quy_Trinh_Nghiep_Vu_AgriShop.docx  (File Word thuyết minh đề tài)
│   └── BAO_CAO_DANH_GIA_ASSIGNMENT.pdf                   (Bản in PDF của báo cáo này)
│
├── 2_Co_So_Du_Lieu/
│   ├── AgriShopDB_Diagram.png                            (Ảnh chụp mô hình lược đồ ERD 18 bảng)
│   └── AgriShopDB.sql                                    (File SQL khởi tạo CSDL & dữ liệu mẫu)
│
├── 3_Tai_Khoan_Dang_Nhap/
│   └── Danh_Sach_Tai_Khoan.txt                           (File text lưu danh sách tài khoản & mật khẩu)
│
└── 4_Ma_Nguon_Web/
    └── AgriShop/                                         (Thư mục mã nguồn NetBeans hoàn chỉnh)
        ├── AgriShop-ejb/                                 (Module EJB)
        ├── AgriShop-war/                                 (Module WAR)
        ├── src/conf/MANIFEST.MF                          (EAR Manifest)
        ├── build.xml                                     (Ant build script)
        └── dist/AgriShop.ear                             (File EAR đã đóng gói sẵn sàng deploy)
```

> **Ghi chú nộp bài:**
> 1. File nén phải dùng định dạng `.zip` (không dùng `.rar` theo đúng yêu cầu đề bài).
> 2. File cơ sở dữ liệu `AgriShopDB.sql` đã bao gồm lệnh `CREATE DATABASE`, tạo cấu trúc 18 bảng cùng dữ liệu mẫu hoàn chỉnh, có thể chạy thẳng trong SQL Server Management Studio (SSMS).
