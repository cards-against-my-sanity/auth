create table users
(
    id         char(36) primary key         default uuid(),
    email      varchar(255) unique not null,
    password   varchar(64)         not null,
    nickname   varchar(16) unique  not null,
    confirmed  bool                not null default false,
    banned     bool                not null default false,
    ban_reason text null default null
);

create table roles
(
    id   char(36) primary key default uuid(),
    name varchar(16) not null
);

insert into roles (name)
values ('admin'),
       ('moderator'),
       ('user');

create table user_roles
(
    user_id char(36),
    role_id char(36),
    primary key (user_id, role_id),
    foreign key (user_id) references users (id)
        on delete cascade
        on update cascade,
    foreign key (role_id) references roles (id)
        on delete cascade
        on update cascade
);
