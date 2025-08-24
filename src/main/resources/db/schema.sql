-- PDF存储系统数据库初始化脚本

-- 删除旧表（如果存在）
DROP TABLE IF EXISTS pdf_table_data;
DROP TABLE IF EXISTS pdf_image_data;
DROP TABLE IF EXISTS pdf_documents;

-- 创建PDF文档表
CREATE TABLE IF NOT EXISTS pdf_documents (
    pdf_id VARCHAR(36) PRIMARY KEY COMMENT 'PDF文档唯一标识符(UUID)',
    file_name VARCHAR(255) NOT NULL COMMENT '文件名',
    user_name VARCHAR(100) NOT NULL COMMENT '所属用户',
    info TEXT COMMENT '信息字段，默认为空',
    note TEXT COMMENT '笔记字段，默认为空',
    is_extracted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否已提取信息，默认为false',
    is_summarized BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否已总结，默认为false',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_file_name_user (file_name, user_name)
) COMMENT='PDF文档信息表';

-- 创建PDF图片表
CREATE TABLE IF NOT EXISTS pdf_images (
    image_name VARCHAR(255) PRIMARY KEY COMMENT '图片名称（唯一主键）',
    pdf_id VARCHAR(36) NOT NULL COMMENT '关联的PDF文档ID（外键）',
    caption TEXT COMMENT '图片标题，默认为空',
    footnote TEXT COMMENT '图片脚注，默认为空',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (pdf_id) REFERENCES pdf_documents(pdf_id) ON DELETE CASCADE
) COMMENT='PDF图片信息表';

-- 创建PDF表格表
CREATE TABLE IF NOT EXISTS pdf_tables (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '表格ID（自增主键）',
    pdf_id VARCHAR(36) NOT NULL COMMENT '关联的PDF文档ID（外键）',
    body TEXT NOT NULL COMMENT '表格内容',
    image_path VARCHAR(500) COMMENT '表格图片路径',
    caption TEXT COMMENT '表格标题，默认为空',
    footnote TEXT COMMENT '表格脚注，默认为空',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (pdf_id) REFERENCES pdf_documents(pdf_id) ON DELETE CASCADE
) COMMENT='PDF表格信息表';

-- 创建标签表
CREATE TABLE IF NOT EXISTS tags (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '自增主键',
    pdf_id VARCHAR(36) NOT NULL COMMENT '关联的PDF文档ID（外键）',
    file_name VARCHAR(255) NOT NULL COMMENT '文件名',
    tag VARCHAR(100) NOT NULL COMMENT '标签',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (pdf_id) REFERENCES pdf_documents(pdf_id) ON DELETE CASCADE,
    UNIQUE KEY uk_pdf_id_tag (pdf_id, tag)
) COMMENT='标签信息表';

-- 创建索引以提高查询性能
CREATE INDEX idx_pdf_documents_user ON pdf_documents(user_name);
CREATE INDEX idx_pdf_documents_extracted ON pdf_documents(is_extracted);
CREATE INDEX idx_pdf_images_pdf_id ON pdf_images(pdf_id);
CREATE INDEX idx_pdf_tables_pdf_id ON pdf_tables(pdf_id);
CREATE INDEX idx_tags_tag ON tags(tag);