create table oauth_client
(
    id                            varchar(255)            not null,
    client_id                     varchar(255)            not null,
    client_id_issued_at           timestamp default now() not null,
    client_secret                 varchar(255),
    client_secret_expires_at      timestamp,
    client_name                   varchar(255)            not null,
    client_authentication_methods varchar(1000)           not null,
    authorization_grant_types     varchar(1000)           not null,
    redirect_uris                 varchar(1000),
    post_logout_redirect_uris     varchar(1000),
    scopes                        varchar(1000)           not null,
    client_settings               varchar(2000)           not null,
    token_settings                varchar(2000)           not null,
    primary key (id)
);

create table oauth_authorization
(
    id                            varchar(255) not null,
    registered_client_id          varchar(255) not null,
    principal_name                varchar(255) not null,
    authorization_grant_type      varchar(255) not null,
    authorized_scopes             varchar(1000),
    attributes                    varchar(4000),
    state                         varchar(500),
    authorization_code_value      varchar(4000),
    authorization_code_issued_at  timestamp,
    authorization_code_expires_at timestamp,
    authorization_code_metadata   varchar(2000),
    access_token_value            varchar(4000),
    access_token_issued_at        timestamp,
    access_token_expires_at       timestamp,
    access_token_metadata         varchar(2000),
    access_token_type             varchar(255),
    access_token_scopes           varchar(1000),
    refresh_token_value           varchar(4000),
    refresh_token_issued_at       timestamp,
    refresh_token_expires_at      timestamp,
    refresh_token_metadata        varchar(2000),
    oidc_id_token_value           varchar(4000),
    oidc_id_token_issued_at       timestamp,
    oidc_id_token_expires_at      timestamp,
    oidc_id_token_metadata        varchar(2000),
    oidc_id_token_claims          varchar(2000),
    user_code_value               varchar(4000),
    user_code_issued_at           timestamp,
    user_code_expires_at          timestamp,
    user_code_metadata            varchar(2000),
    device_code_value             varchar(4000),
    device_code_issued_at         timestamp,
    device_code_expires_at        timestamp,
    device_code_metadata          varchar(2000),
    primary key (id)
);

create table oauth_authorization_consent
(
    registered_client_id varchar(255)  not null,
    principal_name       varchar(255)  not null,
    authorities          varchar(1000) not null,
    primary key (registered_client_id, principal_name)
);