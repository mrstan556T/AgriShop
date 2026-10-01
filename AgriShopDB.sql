-- =========================================================================================
-- AGRISHOP DATABASE — FILE TỔNG HỢP FINAL
-- Chạy file này để khởi tạo toàn bộ AgriShopDB từ đầu
-- (Bao gồm: Schema 18 bảng + Dữ liệu mẫu đầy đủ)
--
-- Hoặc chạy riêng từng phần:
--   Phần 1 - Schema : AgriShopDB_SCHEMA.sql
--   Phần 2 - Data   : AgriShopDB_DATA.sql
-- =========================================================================================

-- ▼ PHẦN 1: TẠO DB VÀ 18 BẢNG ▼ ──────────────────────────────────────────────────────────

IF EXISTS (SELECT name FROM sys.databases WHERE name = 'AgriShopDB')
BEGIN
    ALTER DATABASE AgriShopDB SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE AgriShopDB;
END
GO

CREATE DATABASE AgriShopDB COLLATE Vietnamese_CI_AS;
GO

USE AgriShopDB;
GO

-- ── 1.1 Users ─────────────────────────────────────────────────────────────────────────────
CREATE TABLE Users (
    id                  BIGINT          IDENTITY(1,1) PRIMARY KEY,
    user_code           VARCHAR(20)     UNIQUE NOT NULL,
    username            VARCHAR(50)     UNIQUE NOT NULL,
    password_hash       VARCHAR(255)    NOT NULL,
    full_name           NVARCHAR(100)   NOT NULL,
    email               VARCHAR(100)    UNIQUE NOT NULL,
    role                VARCHAR(20)     NOT NULL DEFAULT 'CUSTOMER',
    status              VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    phone               VARCHAR(20)     NULL,
    address             NVARCHAR(255)   NULL,
    avatar_url          VARCHAR(255)    NULL,
    is_email_verified   BIT             NOT NULL DEFAULT 1,
    totp_secret         VARCHAR(64)     NULL,
    totp_enabled        BIT             NOT NULL DEFAULT 0,
    created_at          DATETIME        NOT NULL DEFAULT GETDATE(),
    updated_at          DATETIME        NULL
);
CREATE INDEX IX_Users_Username ON Users(username);
CREATE INDEX IX_Users_Email    ON Users(email);
GO

-- ── 1.2 UserTokens ────────────────────────────────────────────────────────────────────────
CREATE TABLE UserTokens (
    id          BIGINT          IDENTITY(1,1) PRIMARY KEY,
    user_id     BIGINT          NOT NULL,
    token       VARCHAR(100)    UNIQUE NOT NULL,
    token_type  VARCHAR(50)     NOT NULL,
    expires_at  DATETIME        NOT NULL,
    is_used     BIT             NOT NULL DEFAULT 0,
    created_at  DATETIME        NOT NULL DEFAULT GETDATE(),
    CONSTRAINT FK_UserTokens_Users FOREIGN KEY (user_id) REFERENCES Users(id) ON DELETE CASCADE
);
CREATE INDEX IX_UserTokens_Token ON UserTokens(token);
GO

-- ── 1.3 LoginAttempts ─────────────────────────────────────────────────────────────────────
CREATE TABLE LoginAttempts (
    id              BIGINT          IDENTITY(1,1) PRIMARY KEY,
    identifier      VARCHAR(150)    UNIQUE NOT NULL,
    attempt_count   INT             NOT NULL DEFAULT 1,
    last_attempt_at DATETIME        NOT NULL DEFAULT GETDATE(),
    locked_until    DATETIME        NULL
);
CREATE INDEX IX_LoginAttempts_Identifier ON LoginAttempts(identifier);
GO

-- ── 1.4 Categories ────────────────────────────────────────────────────────────────────────
CREATE TABLE Categories (
    id          BIGINT          IDENTITY(1,1) PRIMARY KEY,
    code        VARCHAR(20)     UNIQUE NOT NULL,
    name        NVARCHAR(100)   NOT NULL,
    description NVARCHAR(MAX)   NULL,
    is_deleted  BIT             NOT NULL DEFAULT 0,
    created_at  DATETIME        NOT NULL DEFAULT GETDATE(),
    updated_at  DATETIME        NULL
);
GO

-- ── 1.5 Suppliers ─────────────────────────────────────────────────────────────────────────
CREATE TABLE Suppliers (
    id              BIGINT          IDENTITY(1,1) PRIMARY KEY,
    supplier_code   VARCHAR(20)     UNIQUE NOT NULL,
    name            NVARCHAR(150)   NOT NULL,
    address         NVARCHAR(255)   NULL,
    phone           VARCHAR(20)     NULL,
    is_deleted      BIT             NOT NULL DEFAULT 0,
    created_at      DATETIME        NOT NULL DEFAULT GETDATE(),
    updated_at      DATETIME        NULL
);
GO

-- ── 1.6 Products ──────────────────────────────────────────────────────────────────────────
CREATE TABLE Products (
    id                  BIGINT          IDENTITY(1,1) PRIMARY KEY,
    product_code        VARCHAR(20)     UNIQUE NOT NULL,
    category_id         BIGINT          NOT NULL,
    supplier_id         BIGINT          NULL,
    name                NVARCHAR(150)   NOT NULL,
    unit                NVARCHAR(50)    NOT NULL,
    unit_type           VARCHAR(20)     NOT NULL DEFAULT 'COUNT',
    price               DECIMAL(18,2)   NOT NULL CHECK (price >= 0),
    cost_price          DECIMAL(18,2)   NOT NULL DEFAULT 0 CHECK (cost_price >= 0),
    stock_quantity      DECIMAL(18,2)   NOT NULL DEFAULT 0 CHECK (stock_quantity >= 0),
    reserved_quantity   DECIMAL(18,2)   NOT NULL DEFAULT 0 CHECK (reserved_quantity >= 0),
    min_stock           DECIMAL(18,2)   NOT NULL DEFAULT 10 CHECK (min_stock >= 0),
    total_imported      DECIMAL(18,2)   NOT NULL DEFAULT 0 CHECK (total_imported >= 0),
    total_sold          DECIMAL(18,2)   NOT NULL DEFAULT 0 CHECK (total_sold >= 0),
    description         NVARCHAR(MAX)   NULL,
    image_url           VARCHAR(255)    NULL,
    is_deleted          BIT             NOT NULL DEFAULT 0,
    created_at          DATETIME        NOT NULL DEFAULT GETDATE(),
    updated_at          DATETIME        NULL,
    CONSTRAINT FK_Products_Categories FOREIGN KEY (category_id) REFERENCES Categories(id),
    CONSTRAINT FK_Products_Suppliers  FOREIGN KEY (supplier_id) REFERENCES Suppliers(id)
);
CREATE INDEX IX_Products_Category ON Products(category_id);
CREATE INDEX IX_Products_Supplier ON Products(supplier_id);
CREATE INDEX IX_Products_Code     ON Products(product_code);
GO

-- ── 1.7 ProductImages ─────────────────────────────────────────────────────────────────────
CREATE TABLE ProductImages (
    id              BIGINT          IDENTITY(1,1) PRIMARY KEY,
    product_id      BIGINT          NOT NULL,
    image_url       VARCHAR(255)    NOT NULL,
    display_order   INT             NOT NULL DEFAULT 0,
    is_primary      BIT             NOT NULL DEFAULT 0,
    created_at      DATETIME        NOT NULL DEFAULT GETDATE(),
    CONSTRAINT FK_ProductImages_Products FOREIGN KEY (product_id) REFERENCES Products(id) ON DELETE CASCADE
);
CREATE INDEX IX_ProductImages_Product ON ProductImages(product_id);
GO

-- ── 1.8 Wishlists ─────────────────────────────────────────────────────────────────────────
CREATE TABLE Wishlists (
    id          BIGINT          IDENTITY(1,1) PRIMARY KEY,
    user_id     BIGINT          NOT NULL,
    product_id  BIGINT          NOT NULL,
    created_at  DATETIME        NOT NULL DEFAULT GETDATE(),
    CONSTRAINT UQ_Wishlists_User_Product UNIQUE (user_id, product_id),
    CONSTRAINT FK_Wishlists_Users    FOREIGN KEY (user_id)    REFERENCES Users(id)    ON DELETE CASCADE,
    CONSTRAINT FK_Wishlists_Products FOREIGN KEY (product_id) REFERENCES Products(id) ON DELETE NO ACTION
);
GO

-- ── 1.9 Coupons ───────────────────────────────────────────────────────────────────────────
CREATE TABLE Coupons (
    id              BIGINT          IDENTITY(1,1) PRIMARY KEY,
    code            VARCHAR(50)     UNIQUE NOT NULL,
    discount_type   VARCHAR(20)     NOT NULL,
    discount_value  DECIMAL(18,2)   NOT NULL CHECK (discount_value > 0),
    min_order_value DECIMAL(18,2)   NOT NULL DEFAULT 0 CHECK (min_order_value >= 0),
    start_date      DATETIME        NOT NULL,
    end_date        DATETIME        NOT NULL,
    usage_limit     INT             NULL CHECK (usage_limit IS NULL OR usage_limit > 0),
    used_count      INT             NOT NULL DEFAULT 0 CHECK (used_count >= 0),
    status          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    created_at      DATETIME        NOT NULL DEFAULT GETDATE(),
    updated_at      DATETIME        NULL
);
CREATE INDEX IX_Coupons_Code ON Coupons(code);
GO

-- ── 1.10 Orders ───────────────────────────────────────────────────────────────────────────
CREATE TABLE Orders (
    id                  BIGINT          IDENTITY(1,1) PRIMARY KEY,
    order_code          VARCHAR(50)     UNIQUE NOT NULL,
    user_id             BIGINT          NOT NULL,
    subtotal            DECIMAL(18,2)   NULL,
    discount_amount     DECIMAL(18,2)   NOT NULL DEFAULT 0,
    shipping_fee        DECIMAL(18,2)   NOT NULL DEFAULT 0,
    coupon_code         VARCHAR(50)     NULL,
    payment_status      VARCHAR(30)     NOT NULL DEFAULT 'UNPAID',
    payment_method      VARCHAR(50)     NOT NULL DEFAULT 'COD',
    total_amount        DECIMAL(18,2)   NOT NULL CHECK (total_amount >= 0),
    shipping_address    NVARCHAR(255)   NOT NULL,
    phone               VARCHAR(20)     NOT NULL,
    note                NVARCHAR(500)   NULL,
    status              VARCHAR(50)     NOT NULL,
    created_at          DATETIME        NOT NULL DEFAULT GETDATE(),
    updated_at          DATETIME        NULL,
    CONSTRAINT FK_Orders_Users FOREIGN KEY (user_id) REFERENCES Users(id)
);
CREATE INDEX IX_Orders_User   ON Orders(user_id);
CREATE INDEX IX_Orders_Status ON Orders(status);
CREATE INDEX IX_Orders_Code   ON Orders(order_code);
GO

-- ── 1.11 OrderDetails (SNAPSHOT BẤT BIẾN) ────────────────────────────────────────────────
CREATE TABLE OrderDetails (
    id              BIGINT          IDENTITY(1,1) PRIMARY KEY,
    order_id        BIGINT          NOT NULL,
    product_id      BIGINT          NOT NULL,
    product_code    VARCHAR(50)     NULL,
    product_name    NVARCHAR(150)   NOT NULL,
    unit            NVARCHAR(50)    NULL,
    unit_price      DECIMAL(18,2)   NOT NULL CHECK (unit_price >= 0),
    cost_price      DECIMAL(18,2)   NOT NULL DEFAULT 0 CHECK (cost_price >= 0),
    quantity        DECIMAL(18,2)   NOT NULL CHECK (quantity > 0),
    CONSTRAINT FK_OrderDetails_Orders   FOREIGN KEY (order_id)   REFERENCES Orders(id)   ON DELETE CASCADE,
    CONSTRAINT FK_OrderDetails_Products FOREIGN KEY (product_id) REFERENCES Products(id) ON DELETE NO ACTION
);
CREATE INDEX IX_OrderDetails_Order   ON OrderDetails(order_id);
CREATE INDEX IX_OrderDetails_Product ON OrderDetails(product_id);
GO

-- ── 1.12 CancellationRequests ─────────────────────────────────────────────────────────────
CREATE TABLE CancellationRequests (
    id              BIGINT          IDENTITY(1,1) PRIMARY KEY,
    order_id        BIGINT          NOT NULL,
    user_id         BIGINT          NOT NULL,
    reason          NVARCHAR(500)   NOT NULL,
    status          VARCHAR(30)     NOT NULL DEFAULT 'PENDING',
    admin_note      NVARCHAR(500)   NULL,
    created_at      DATETIME        NOT NULL DEFAULT GETDATE(),
    processed_at    DATETIME        NULL,
    CONSTRAINT FK_CancellationRequests_Orders FOREIGN KEY (order_id) REFERENCES Orders(id),
    CONSTRAINT FK_CancellationRequests_Users  FOREIGN KEY (user_id)  REFERENCES Users(id)
);
CREATE INDEX IX_CancellationRequests_Order ON CancellationRequests(order_id);
GO

-- ── 1.13 InventoryTransactions ────────────────────────────────────────────────────────────
CREATE TABLE InventoryTransactions (
    id                  BIGINT          IDENTITY(1,1) PRIMARY KEY,
    product_id          BIGINT          NOT NULL,
    user_id             BIGINT          NOT NULL,
    supplier_id         BIGINT          NULL,
    quantity_changed    DECIMAL(18,2)   NOT NULL,
    unit_cost           DECIMAL(18,2)   NULL,
    transaction_type    VARCHAR(20)     NOT NULL,
    reason              NVARCHAR(255)   NULL,
    created_at          DATETIME        NOT NULL DEFAULT GETDATE(),
    CONSTRAINT FK_InvTrans_Products  FOREIGN KEY (product_id)  REFERENCES Products(id) ON DELETE NO ACTION,
    CONSTRAINT FK_InvTrans_Users     FOREIGN KEY (user_id)     REFERENCES Users(id),
    CONSTRAINT FK_InvTrans_Suppliers FOREIGN KEY (supplier_id) REFERENCES Suppliers(id)
);
CREATE INDEX IX_InvTrans_Product ON InventoryTransactions(product_id);
CREATE INDEX IX_InvTrans_Type    ON InventoryTransactions(transaction_type);
GO

-- ── 1.14 Reviews ──────────────────────────────────────────────────────────────────────────
CREATE TABLE Reviews (
    id              BIGINT          IDENTITY(1,1) PRIMARY KEY,
    product_id      BIGINT          NOT NULL,
    user_id         BIGINT          NOT NULL,
    rating          INT             NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment         NVARCHAR(MAX)   NULL,
    status          VARCHAR(20)     NOT NULL DEFAULT 'VISIBLE',
    admin_reply     NVARCHAR(1000)  NULL,
    admin_reply_at  DATETIME        NULL,
    created_at      DATETIME        NOT NULL DEFAULT GETDATE(),
    updated_at      DATETIME        NULL,
    CONSTRAINT FK_Reviews_Products FOREIGN KEY (product_id) REFERENCES Products(id) ON DELETE NO ACTION,
    CONSTRAINT FK_Reviews_Users    FOREIGN KEY (user_id)    REFERENCES Users(id)
);
CREATE INDEX IX_Reviews_Product ON Reviews(product_id);
GO

-- ── 1.15 Carts ────────────────────────────────────────────────────────────────────────────
CREATE TABLE Carts (
    id          BIGINT          IDENTITY(1,1) PRIMARY KEY,
    user_id     BIGINT          NOT NULL UNIQUE,
    created_at  DATETIME        NOT NULL DEFAULT GETDATE(),
    updated_at  DATETIME        NULL,
    CONSTRAINT FK_Carts_Users FOREIGN KEY (user_id) REFERENCES Users(id) ON DELETE CASCADE
);
GO

-- ── 1.16 CartItems ────────────────────────────────────────────────────────────────────────
CREATE TABLE CartItems (
    id          BIGINT          IDENTITY(1,1) PRIMARY KEY,
    cart_id     BIGINT          NOT NULL,
    product_id  BIGINT          NOT NULL,
    quantity    DECIMAL(18,2)   NOT NULL CHECK (quantity > 0),
    CONSTRAINT FK_CartItems_Carts    FOREIGN KEY (cart_id)    REFERENCES Carts(id)    ON DELETE CASCADE,
    CONSTRAINT FK_CartItems_Products FOREIGN KEY (product_id) REFERENCES Products(id) ON DELETE NO ACTION
);
CREATE INDEX IX_CartItems_Cart ON CartItems(cart_id);
GO

-- ── 1.17 AuditLogs ────────────────────────────────────────────────────────────────────────
CREATE TABLE AuditLogs (
    id          BIGINT          IDENTITY(1,1) PRIMARY KEY,
    user_id     BIGINT          NULL,
    action      VARCHAR(100)    NOT NULL,
    entity_name VARCHAR(50)     NOT NULL,
    entity_id   BIGINT          NOT NULL,
    old_value   NVARCHAR(MAX)   NULL,
    new_value   NVARCHAR(MAX)   NULL,
    created_at  DATETIME        NOT NULL DEFAULT GETDATE(),
    CONSTRAINT FK_AuditLogs_Users FOREIGN KEY (user_id) REFERENCES Users(id)
);
CREATE INDEX IX_AuditLogs_Action ON AuditLogs(action);
CREATE INDEX IX_AuditLogs_Entity ON AuditLogs(entity_name, entity_id);
GO

-- ── 1.18 SystemSettings ───────────────────────────────────────────────────────────────────
CREATE TABLE SystemSettings (
    id              BIGINT          IDENTITY(1,1) PRIMARY KEY,
    setting_key     VARCHAR(100)    UNIQUE NOT NULL,
    setting_value   NVARCHAR(500)   NOT NULL,
    description     NVARCHAR(255)   NULL,
    updated_at      DATETIME        NOT NULL DEFAULT GETDATE()
);
CREATE INDEX IX_SystemSettings_Key ON SystemSettings(setting_key);
GO

-- ▼ PHẦN 2: DỮ LIỆU MẪU ▼ ────────────────────────────────────────────────────────────────
-- Mật khẩu tất cả tài khoản: 123456
-- SHA-256('123456') = 8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92

-- 2.1 Users
INSERT INTO Users (user_code, username, password_hash, full_name, email, role, status, phone, address, is_email_verified, totp_enabled) VALUES
('USR-0001','admin',  '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92',N'Quản Trị Viên Hệ Thống','admin@agrishop.vn',    'ADMIN',   'ACTIVE','0901000001',N'AgriShop HQ, Cần Thơ',1,0),
('USR-0002','cust1',  '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92',N'Nguyễn Văn Khách',     'cust1@example.com','CUSTOMER','ACTIVE','0912345678',N'123 Đường Nông Nghiệp Xanh, An Khánh, Ninh Kiều, Cần Thơ',1,0),
('USR-0003','cust2',  '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92',N'Trần Thị Lan',          'cust2@example.com','CUSTOMER','ACTIVE','0923456789',N'45 Nguyễn Văn Cừ, Bình Thủy, Cần Thơ',1,0),
('USR-0004','cust3',  '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92',N'Lê Hoàng Nam',          'cust3@example.com','CUSTOMER','ACTIVE','0934567890',N'88 Lê Lợi, Vĩnh Long',1,0),
('USR-0005','cust4',  '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92',N'Phạm Thị Mai',          'cust4@example.com','CUSTOMER','PENDING','0945678901',N'12 Trần Hưng Đạo, Sóc Trăng',0,0),
('USR-0006','cust5',  '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92',N'Võ Thanh Tuấn',         'cust5@example.com','CUSTOMER','BANNED', '0956789012',N'99 Hùng Vương, Vĩnh Long',1,0);
GO

-- 2.2 SystemSettings
INSERT INTO SystemSettings (setting_key, setting_value, description, updated_at) VALUES
('FREE_SHIPPING_THRESHOLD','300000',N'Ngưỡng miễn phí vận chuyển (VNĐ)',GETDATE()),
('SHIPPING_FEE',           '30000', N'Phí vận chuyển dưới ngưỡng (VNĐ)', GETDATE()),
('RETURN_WINDOW_DAYS',     '7',     N'Số ngày được đổi trả sau nhận hàng',GETDATE()),
('MAX_CART_ITEMS',         '20',    N'Số loại SP tối đa trong giỏ hàng', GETDATE()),
('LOW_STOCK_THRESHOLD',    '10',    N'Ngưỡng cảnh báo sắp hết hàng',     GETDATE()),
('VIETQR_PAYMENT_ENABLED', 'false', N'Bật cổng thanh toán tự động VietQR/SePay', GETDATE());
GO

-- 2.3 Categories
INSERT INTO Categories (code, name, description) VALUES
('CAT-VEG',N'Rau Củ Hữu Cơ',        N'Rau củ quả sạch đạt chuẩn VietGAP thu hoạch tươi mỗi ngày.'),
('CAT-FRU',N'Trái Cây Đặc Sản',      N'Trái cây tươi ngon bốn mùa theo chuẩn chỉ dẫn địa lý.'),
('CAT-SEE',N'Hạt Giống & Cây Trồng', N'Hạt giống rau củ thuần chủng F1 năng suất cao.'),
('CAT-DRY',N'Nông Sản Sấy Khô',      N'Nông sản sấy thăng hoa giữ trọn hương vị và dưỡng chất.'),
('CAT-ORG',N'Nấm & Rau Mầm',         N'Nấm linh chi, nấm bào ngư và các loại rau mầm dinh dưỡng cao.');
GO

-- 2.4 Suppliers
INSERT INTO Suppliers (supplier_code, name, address, phone) VALUES
('SUP-DL',     N'Hợp Tác Xã Nông Nghiệp Đà Lạt', N'Phường 8, Đà Lạt, Lâm Đồng',    '02633888999'),
('SUP-BV',     N'Nông Trại Hữu Cơ Ba Vì',         N'Yên Bài, Ba Vì, Hà Nội',         '02433777888'),
('SUP-MT',     N'Viện Cây Giống Miền Tây',        N'Cái Mơn, Chợ Lách, Bến Tre',     '02753666555'),
('SUP-ORGANIC',N'HTX Nông Nghiệp Xanh An Nhiên',  N'Phong Điền, Cần Thơ',            '02923555444');
GO

-- 2.5 Products (10 SP: 5 hàng cân WEIGHT + 5 hàng đếm COUNT)
INSERT INTO Products(product_code,category_id,supplier_id,name,unit,unit_type,price,cost_price,stock_quantity,reserved_quantity,min_stock,total_imported,total_sold,description,image_url) VALUES
('PRD-CA-CHUA', 1,1,N'Cà Chua Beef Hữu Cơ Đà Lạt',   N'kg',  'WEIGHT',45000, 28000,120.50,0,20,350,229.50,N'Cà chua Beef trái to, thịt dày, ngọt thanh, trồng tự nhiên trong nhà kính theo chuẩn VietGAP.',NULL),
('PRD-XA-LACH', 1,1,N'Xà Lách Mỡ Thủy Canh',          N'kg',  'WEIGHT',38000, 22000, 85.00,0,15,250,165.00,N'Xà lách tươi giòn, không dùng thuốc bảo vệ thực vật, thích hợp làm salad.',NULL),
('PRD-XOAI-CAT',2,3,N'Xoài Cát Hòa Lộc Loại 1',       N'kg',  'WEIGHT',95000, 65000, 50.00,0,10,200,150.00,N'Xoài cát Hòa Lộc chính gốc Tiền Giang, thơm lừng ngọt đậm, cơm dày không xơ.',NULL),
('PRD-NHO-XANH',2,1,N'Nho Xanh Ninh Thuận VietGAP',   N'kg',  'WEIGHT',85000, 55000, 60.00,0,10,150, 90.00,N'Nho xanh không hạt Ninh Thuận, vị ngọt thanh, chùm mẫm, đạt chuẩn VietGAP xuất khẩu.',NULL),
('PRD-CAI-XANH',1,4,N'Cải Xanh Hữu Cơ Cần Thơ',       N'kg',  'WEIGHT',25000, 14000,200.00,0,30,500,300.00,N'Cải xanh hữu cơ không thuốc trừ sâu, thu hoạch sáng sớm giao tận nơi.',NULL),
('PRD-HAT-GIONG',3,3,N'Gói Hạt Giống Ớt Hiểm F1',     N'gói', 'COUNT', 25000, 12000,298,   0,30,500,202,   N'Hạt giống ớt hiểm lai F1, kháng sâu bệnh tốt, tỷ lệ nảy mầm >90%, sai trái quanh năm.',NULL),
('PRD-CHUOI-SAY',4,2,N'Chuối Sấy Dẻo Năng Lượng MT',  N'hộp', 'COUNT', 55000, 32000,139,   0,25,400,261,   N'Chuối xiêm chín cây sấy dẻo tự nhiên bằng năng lượng mặt trời, không thêm đường.',NULL),
('PRD-NAM-LINH', 5,2,N'Nấm Linh Chi Đỏ Sấy Khô',      N'hộp', 'COUNT',185000,110000,80,    0,10,200,120,   N'Nấm linh chi đỏ Hàn Quốc nuôi cấy tại Ba Vì, sấy khô giữ nguyên hoạt chất Ganoderic Acid.',NULL),
('PRD-RAU-MAM',  5,4,N'Rau Mầm Hỗn Hợp 5 Loại',       N'khay','COUNT', 35000, 18000,150,   0,20,300,150,   N'Hỗn hợp 5 loại rau mầm: đậu xanh, hướng dương, cải đỏ, lúa mạch, đậu Hà Lan.',NULL),
('PRD-HAT-DIEU', 4,3,N'Hạt Điều Rang Muối Bình Phước', N'túi', 'COUNT', 95000, 62000,180,   0,20,300,120,   N'Điều Bình Phước loại 1 rang muối thủ công, hút chân không 500g, hạt mẩy giòn thơm.',NULL);
GO

-- 2.6 ProductImages
INSERT INTO ProductImages (product_id, image_url, display_order, is_primary) VALUES
(1,'/resources/images/products/ca-chua-1.jpg',1,1),(1,'/resources/images/products/ca-chua-2.jpg',2,0),(1,'/resources/images/products/ca-chua-3.jpg',3,0),
(2,'/resources/images/products/xa-lach-1.jpg',1,1),(2,'/resources/images/products/xa-lach-2.jpg',2,0),
(3,'/resources/images/products/xoai-cat-1.jpg',1,1),(3,'/resources/images/products/xoai-cat-2.jpg',2,0),
(4,'/resources/images/products/nho-xanh-1.jpg',1,1),
(5,'/resources/images/products/cai-xanh-1.jpg',1,1),
(6,'/resources/images/products/hat-giong-1.jpg',1,1),
(7,'/resources/images/products/chuoi-say-1.jpg',1,1),(7,'/resources/images/products/chuoi-say-2.jpg',2,0),
(8,'/resources/images/products/nam-linh-1.jpg',1,1),
(9,'/resources/images/products/rau-mam-1.jpg',1,1),
(10,'/resources/images/products/hat-dieu-1.jpg',1,1),(10,'/resources/images/products/hat-dieu-2.jpg',2,0);
GO

-- 2.7 Coupons
INSERT INTO Coupons (code,discount_type,discount_value,min_order_value,start_date,end_date,usage_limit,used_count,status) VALUES
('AGRIFREESHIP','FIXED',     30000,250000,GETDATE(),DATEADD(day, 90,GETDATE()),500,3,'ACTIVE'),
('WELCOME10',   'PERCENTAGE',10,   100000,GETDATE(),DATEADD(day,120,GETDATE()),1000,1,'ACTIVE'),
('AGRI50K',     'FIXED',     50000,500000,GETDATE(),DATEADD(day, 30,GETDATE()),100,0,'ACTIVE'),
('TETHOLIDAY',  'PERCENTAGE',15,   300000,GETDATE(),DATEADD(day, 15,GETDATE()),200,0,'ACTIVE'),
('OLDCODE2025', 'FIXED',     20000,150000,DATEADD(year,-1,GETDATE()),DATEADD(day,-1,GETDATE()),100,0,'INACTIVE');
GO

-- 2.8 Carts & CartItems
INSERT INTO Carts (user_id) VALUES (2),(3);
GO
INSERT INTO CartItems (cart_id,product_id,quantity) VALUES
(1,3,2.00),(1,7,1.00),
(2,1,1.50),(2,9,2.00);
GO

-- 2.9 InventoryTransactions
INSERT INTO InventoryTransactions(product_id,user_id,supplier_id,quantity_changed,unit_cost,transaction_type,reason,created_at) VALUES
(1,1,1,+350.00,28000,'IMPORT',N'Nhập cà chua Beef đợt 1 từ HTX Đà Lạt',         DATEADD(day,-30,GETDATE())),
(2,1,1,+250.00,22000,'IMPORT',N'Nhập xà lách mỡ thủy canh từ HTX Đà Lạt',       DATEADD(day,-28,GETDATE())),
(3,1,3,+200.00,65000,'IMPORT',N'Nhập xoài Cát Hòa Lộc loại 1',                   DATEADD(day,-25,GETDATE())),
(4,1,1,+150.00,55000,'IMPORT',N'Nhập nho xanh Ninh Thuận đợt 1',                 DATEADD(day,-22,GETDATE())),
(5,1,4,+500.00,14000,'IMPORT',N'Nhập cải xanh hữu cơ từ HTX An Nhiên',           DATEADD(day,-20,GETDATE())),
(6,1,3,+500.00,12000,'IMPORT',N'Nhập hạt giống ớt hiểm F1',                      DATEADD(day,-35,GETDATE())),
(7,1,2,+400.00,32000,'IMPORT',N'Nhập chuối sấy dẻo từ Nông Trại Ba Vì',          DATEADD(day,-18,GETDATE())),
(8,1,2,+200.00,110000,'IMPORT',N'Nhập nấm linh chi đỏ sấy khô',                  DATEADD(day,-15,GETDATE())),
(9,1,4,+300.00,18000,'IMPORT',N'Nhập rau mầm hỗn hợp 5 loại',                    DATEADD(day,-10,GETDATE())),
(10,1,3,+300.00,62000,'IMPORT',N'Nhập hạt điều rang muối Bình Phước',             DATEADD(day,-12,GETDATE())),
(1,2,NULL,-5.00,28000,'SALE',N'Xuất bán đơn ORD-20260901',                        DATEADD(day,-20,GETDATE())),
(7,2,NULL,-3.00,32000,'SALE',N'Xuất bán đơn ORD-20260901',                        DATEADD(day,-20,GETDATE())),
(3,2,NULL,-3.00,65000,'SALE',N'Xuất bán đơn ORD-20260905',                        DATEADD(day,-15,GETDATE())),
(1,3,NULL,-3.50,28000,'SALE',N'Xuất bán đơn ORD-20260908',                        DATEADD(day,-2, GETDATE())),
(9,3,NULL,-5.00,18000,'SALE',N'Xuất bán đơn ORD-20260908',                        DATEADD(day,-2, GETDATE())),
(2,3,NULL,-2.00,22000,'SALE',N'Xuất bán đơn ORD-20260910',                        DATEADD(day,-1, GETDATE())),
(9,3,NULL,-3.00,18000,'SALE',N'Xuất bán đơn ORD-20260910',                        DATEADD(day,-1, GETDATE())),
(8,2,NULL,-2.00,110000,'SALE',N'Xuất bán đơn ORD-20260915',                       DATEADD(hour,-18,GETDATE())),
(10,2,NULL,-1.00,62000,'SALE',N'Xuất bán đơn ORD-20260915',                       DATEADD(hour,-18,GETDATE())),
(4,2,NULL,-2.00,55000,'SALE',N'Xuất bán đơn ORD-20260915',                        DATEADD(hour,-18,GETDATE())),
(5,1,NULL,-3.00,14000,'ADJUSTMENT',N'Loại bỏ hàng hư hỏng do vận chuyển',        DATEADD(day,-8, GETDATE()));
GO

-- 2.10 Orders (6 đơn hàng — đủ các trạng thái)
INSERT INTO Orders(order_code,user_id,subtotal,discount_amount,shipping_fee,coupon_code,payment_status,payment_method,total_amount,shipping_address,phone,note,status,created_at) VALUES
('ORD-20260901081011-001',2,330000,0,    0,     NULL,           'PAID',  'COD',  330000,N'123 Đường Nông Nghiệp Xanh, An Khánh, Ninh Kiều, Cần Thơ','0912345678',NULL,         'DELIVERED', DATEADD(day,-20,GETDATE())),
('ORD-20260905123045-002',2,285000,30000,0,     'AGRIFREESHIP', 'PAID',  'VNPAY',255000,N'123 Đường Nông Nghiệp Xanh, An Khánh, Ninh Kiều, Cần Thơ','0912345678',N'Giao sáng trước 9h','DELIVERED', DATEADD(day,-15,GETDATE())),
('ORD-20260908091530-003',3,382500,0,    0,     NULL,           'UNPAID','COD',  382500,N'45 Nguyễn Văn Cừ, Bình Thủy, Cần Thơ',                    '0923456789',NULL,         'PROCESSING',DATEADD(day,-2, GETDATE())),
('ORD-20260910150000-004',3,146000,0,    30000, NULL,           'UNPAID','COD',  176000,N'45 Nguyễn Văn Cừ, Bình Thủy, Cần Thơ',                    '0923456789',NULL,         'PENDING',   DATEADD(day,-1, GETDATE())),
('ORD-20260906173000-005',4,95000, 0,    30000, NULL,           'UNPAID','COD',  125000,N'88 Lê Lợi, Vĩnh Long',                                     '0934567890',NULL,         'CANCELLED', DATEADD(day,-14,GETDATE())),
('ORD-20260915090000-006',2,555000,50000,0,     'AGRI50K',      'PAID',  'VNPAY',505000,N'123 Đường Nông Nghiệp Xanh, An Khánh, Ninh Kiều, Cần Thơ','0912345678',N'Gọi trước 30 phút','SHIPPED',   DATEADD(hour,-18,GETDATE()));
GO

INSERT INTO OrderDetails(order_id,product_id,product_code,product_name,unit,unit_price,cost_price,quantity) VALUES
(1,1,'PRD-CA-CHUA',  N'Cà Chua Beef Hữu Cơ Đà Lạt',        N'kg',   45000,28000,5.00),
(1,7,'PRD-CHUOI-SAY',N'Chuối Sấy Dẻo Năng Lượng Mặt Trời', N'hộp',  55000,32000,3.00),
(2,3,'PRD-XOAI-CAT', N'Xoài Cát Hòa Lộc Loại 1',           N'kg',   95000,65000,3.00),
(3,1,'PRD-CA-CHUA',  N'Cà Chua Beef Hữu Cơ Đà Lạt',        N'kg',   45000,28000,3.50),
(3,9,'PRD-RAU-MAM',  N'Rau Mầm Hỗn Hợp 5 Loại',            N'khay', 35000,18000,5.00),
(4,2,'PRD-XA-LACH',  N'Xà Lách Mỡ Thủy Canh',              N'kg',   38000,22000,2.00),
(4,9,'PRD-RAU-MAM',  N'Rau Mầm Hỗn Hợp 5 Loại',            N'khay', 35000,18000,2.00),
(5,3,'PRD-XOAI-CAT', N'Xoài Cát Hòa Lộc Loại 1',           N'kg',   95000,65000,1.00),
(6,8,'PRD-NAM-LINH', N'Nấm Linh Chi Đỏ Sấy Khô',           N'hộp', 185000,110000,2.00),
(6,10,'PRD-HAT-DIEU',N'Hạt Điều Rang Muối Bình Phước',      N'túi',  95000,62000,1.00),
(6,4,'PRD-NHO-XANH', N'Nho Xanh Ninh Thuận VietGAP',        N'kg',   85000,55000,2.00);
GO

-- 2.11 CancellationRequests
INSERT INTO CancellationRequests (order_id,user_id,reason,status,created_at) VALUES
(3,3,N'Tôi đặt nhầm số lượng, muốn hủy để đặt lại đúng hơn.','PENDING',GETDATE());
GO

-- 2.12 Wishlists
INSERT INTO Wishlists (user_id,product_id) VALUES
(2,4),(2,8),(3,3),(3,7),(4,1),(4,10);
GO

-- 2.13 Reviews
INSERT INTO Reviews(product_id,user_id,rating,comment,status,admin_reply,admin_reply_at,created_at) VALUES
(1,2,5,N'Cà chua rất tươi, vị ngọt tự nhiên, đóng gói kỹ càng!','VISIBLE',
 N'Cảm ơn bạn đã tin tưởng! Chúng tôi luôn cam kết giao hàng tươi nhất trong ngày.',
 DATEADD(day,-14,GETDATE()),DATEADD(day,-15,GETDATE())),
(3,2,4,N'Xoài ngon, tuy nhiên một trái hơi dập. Vẫn hài lòng tổng thể.','VISIBLE',
 N'Xin lỗi! Lần sau bạn chụp ảnh gửi hotline để đổi trả miễn phí nhé.',
 DATEADD(day,-9,GETDATE()),DATEADD(day,-10,GETDATE())),
(7,3,5,N'Chuối sấy ngon, dẻo vừa, không ngọt quá. Mua lần 3 rồi!','VISIBLE',NULL,NULL,DATEADD(day,-5,GETDATE())),
(1,3,5,N'Cà chua tươi, sáng sớm đã giao đến nhà. Rất uy tín!','VISIBLE',NULL,NULL,DATEADD(day,-2,GETDATE())),
(9,3,4,N'Rau mầm tươi xanh, nhưng khay hơi nhỏ hơn tôi nghĩ.','VISIBLE',NULL,NULL,DATEADD(day,-1,GETDATE()));
GO

-- 2.14 AuditLogs
INSERT INTO AuditLogs(user_id,action,entity_name,entity_id,old_value,new_value,created_at) VALUES
(1,'UPDATE_ORDER_STATUS','Orders',1,N'{"status":"PENDING"}',    N'{"status":"CONFIRMED"}',  DATEADD(day,-20,GETDATE())),
(1,'UPDATE_ORDER_STATUS','Orders',1,N'{"status":"CONFIRMED"}',  N'{"status":"PROCESSING"}', DATEADD(day,-19,GETDATE())),
(1,'UPDATE_ORDER_STATUS','Orders',1,N'{"status":"PROCESSING"}', N'{"status":"SHIPPED"}',    DATEADD(day,-18,GETDATE())),
(1,'UPDATE_ORDER_STATUS','Orders',1,N'{"status":"SHIPPED"}',    N'{"status":"DELIVERED"}',  DATEADD(day,-17,GETDATE())),
(1,'REPLY_REVIEW','Reviews',1,NULL,N'{"admin_reply":"Cam on ban da tin tuong!"}',           DATEADD(day,-14,GETDATE())),
(1,'UPDATE_SETTING','SystemSettings',1,N'{"FREE_SHIPPING_THRESHOLD":"200000"}',N'{"FREE_SHIPPING_THRESHOLD":"300000"}',DATEADD(day,-10,GETDATE())),
(1,'UPDATE_ORDER_STATUS','Orders',5,N'{"status":"PENDING"}',    N'{"status":"CANCELLED"}',  DATEADD(day,-14,GETDATE())),
(1,'UPDATE_ORDER_STATUS','Orders',2,N'{"status":"PENDING"}',    N'{"status":"CONFIRMED"}',  DATEADD(day,-15,GETDATE())),
(1,'UPDATE_ORDER_STATUS','Orders',2,N'{"status":"SHIPPED"}',    N'{"status":"DELIVERED"}',  DATEADD(day,-12,GETDATE())),
(1,'LOCK_USER','Users',6,            N'{"status":"ACTIVE"}',     N'{"status":"BANNED"}',     DATEADD(day,-5,GETDATE()));
GO

-- ▼ PHẦN 3: KIỂM TRA ▼ ────────────────────────────────────────────────────────────────────
SELECT [Bang]=N'Users',               [Ban ghi]=COUNT(*) FROM Users               UNION ALL
SELECT N'Categories',                  COUNT(*) FROM Categories                    UNION ALL
SELECT N'Suppliers',                   COUNT(*) FROM Suppliers                     UNION ALL
SELECT N'Products',                    COUNT(*) FROM Products                      UNION ALL
SELECT N'ProductImages',               COUNT(*) FROM ProductImages                 UNION ALL
SELECT N'Wishlists',                   COUNT(*) FROM Wishlists                     UNION ALL
SELECT N'Coupons',                     COUNT(*) FROM Coupons                       UNION ALL
SELECT N'Carts',                       COUNT(*) FROM Carts                         UNION ALL
SELECT N'CartItems',                   COUNT(*) FROM CartItems                     UNION ALL
SELECT N'Orders',                      COUNT(*) FROM Orders                        UNION ALL
SELECT N'OrderDetails',                COUNT(*) FROM OrderDetails                  UNION ALL
SELECT N'CancellationRequests',        COUNT(*) FROM CancellationRequests          UNION ALL
SELECT N'InventoryTransactions',       COUNT(*) FROM InventoryTransactions         UNION ALL
SELECT N'Reviews',                     COUNT(*) FROM Reviews                       UNION ALL
SELECT N'AuditLogs',                   COUNT(*) FROM AuditLogs                     UNION ALL
SELECT N'SystemSettings',              COUNT(*) FROM SystemSettings                UNION ALL
SELECT N'UserTokens',                  COUNT(*) FROM UserTokens                    UNION ALL
SELECT N'LoginAttempts',               COUNT(*) FROM LoginAttempts;
GO

PRINT '=========================================================================================';
PRINT '  AgriShopDB — KHOI TAO HOAN TAT! San sang su dung.';
PRINT '=========================================================================================';
PRINT '  TAI KHOAN TEST (tat ca mat khau: 123456)';
PRINT '  +---------+---------+----------+-----------------------------------------------+';
PRINT '  | Role    | User    | Status   | Mo ta                                         |';
PRINT '  +---------+---------+----------+-----------------------------------------------+';
PRINT '  | ADMIN   | admin   | ACTIVE   | Quan tri vien he thong                        |';
PRINT '  | CUST    | cust1   | ACTIVE   | Co don hang, wishlist, gio hang               |';
PRINT '  | CUST    | cust2   | ACTIVE   | Don PROCESSING + yeu cau huy dang cho admin   |';
PRINT '  | CUST    | cust3   | ACTIVE   | Co don da huy (CANCELLED)                     |';
PRINT '  | CUST    | cust4   | PENDING  | Chua xac thuc email (test activate-account)   |';
PRINT '  | CUST    | cust5   | BANNED   | Bi khoa tai khoan (test admin lock/unlock)    |';
PRINT '  +---------+---------+----------+-----------------------------------------------+';
PRINT '  COUPON TEST: AGRIFREESHIP(-30K>=250K) | WELCOME10(-10%>=100K) | AGRI50K(-50K>=500K)';
PRINT '  COUPON HET HAN: OLDCODE2025 (test validate coupon expired)';
PRINT '=========================================================================================';
GO