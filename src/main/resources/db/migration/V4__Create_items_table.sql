create table if not exists items (
    id   uuid         not null primary key default uuid_generate_v4(),
    product_id   uuid not null references products(id),
    shipping_fee numeric not null,
    price numeric not null
    -- FOREIGN KEY (product_id) REFERENCES "products" (product_id) 
);
