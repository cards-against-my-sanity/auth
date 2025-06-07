create table users
(
    id         uuid primary key             default gen_random_uuid(),
    email      varchar(255) unique not null,
    password   varchar(128)         not null,
    nickname   varchar(16) unique  not null,
    confirmed  boolean             not null default false,
    banned     boolean             not null default false,
    ban_reason text
);

create table roles
(
    id   uuid primary key default gen_random_uuid(),
    name varchar(16) not null
);

insert into roles (name)
values ('admin'),
       ('moderator'),
       ('user');

create table user_roles
(
    user_id uuid,
    role_id uuid,
    primary key (user_id, role_id),
    foreign key (user_id) references users (id)
        on delete cascade
        on update cascade,
    foreign key (role_id) references roles (id)
        on delete cascade
        on update cascade
);
