create table if not exists products (
    id   uuid         not null primary key default uuid_generate_v4(),
    name varchar(250) not null unique,
    category varchar(250) not null ,
    weight numeric not null ,
    price numeric not null,
    creationDate date not null
);
