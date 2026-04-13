-- V9: MVP cleanup — remove empty non-Java categories
-- Only Java category remains for the initial release

DELETE FROM category WHERE slug IN ('cpp', 'ai', 'python', 'spring-boot');
