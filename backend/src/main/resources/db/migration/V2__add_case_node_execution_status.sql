ALTER TABLE case_node
    ADD COLUMN execution_status VARCHAR(32) NOT NULL DEFAULT 'PENDING' AFTER sort_order;
