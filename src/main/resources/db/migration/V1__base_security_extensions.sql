create table tb_auth
(
    uuid                       varchar(36)  not null
        primary key,
    created_at                 timestamp(6) not null,
    deleted_at                 timestamp(6),
    updated_at                 timestamp(6),
    email                      varchar(255),
    nickname                   varchar(36)  not null,
    password                   text         not null,
    salt                       varchar(32)  not null,
    status                     varchar(50)  not null
        constraint tb_auth_status_check
            check ((status)::text = ANY
        ((ARRAY ['ACTIVE'::character varying, 'LOCKED'::character varying, 'SUSPENDED'::character varying, 'WITHDRAWN'::character varying])::text[])),
    type                       varchar(50)  not null
        constraint tb_auth_type_check
            check ((type)::text = ANY
        ((ARRAY ['GENERAL'::character varying, 'INTERNAL'::character varying, 'EXTERNAL'::character varying])::text[])),
    username                   varchar(24)  not null
);

alter table tb_auth
    owner to root;

create table tb_auth_login_history
(
    id         bigserial
        primary key,
    created_at timestamp(6) not null,
    deleted_at timestamp(6),
    updated_at timestamp(6),
    user_uuid  varchar(36)
        constraint fk_auth_login_history_user_uuid
            references tb_auth,
    username   varchar(24),
    client_ip  varchar(64),
    user_agent varchar(512)
);

alter table tb_auth_login_history
    owner to root;

create index idx_tb_auth_login_history_user_uuid
    on tb_auth_login_history (user_uuid);

create table tb_auth_login_failure
(
    id                 bigserial
        primary key,
    created_at         timestamp(6) not null,
    deleted_at         timestamp(6),
    updated_at         timestamp(6),
    user_uuid          varchar(36)  not null
        constraint fk_auth_login_failure_user_uuid
            references tb_auth,
    failed_login_count integer      not null,
    locked_until       timestamp(6)
);

alter table tb_auth_login_failure
    owner to root;

create index idx_tb_auth_login_failure_user_uuid
    on tb_auth_login_failure (user_uuid);

create table tb_auth_mfa
(
    id         bigserial
        primary key,
    created_at timestamp(6) not null,
    deleted_at timestamp(6),
    updated_at timestamp(6),
    user_uuid  varchar(36)  not null
        constraint fk_auth_mfa_user_uuid
            references tb_auth,
    secret     text,
    enabled    boolean      not null
);

alter table tb_auth_mfa
    owner to root;

create index idx_tb_auth_mfa_user_uuid
    on tb_auth_mfa (user_uuid);

create table tb_auth_permission
(
    id          bigserial
        primary key,
    created_at  timestamp(6) not null,
    deleted_at  timestamp(6),
    updated_at  timestamp(6),
    code        varchar(120) not null,
    name        varchar(120) not null,
    depth       integer      not null,
    parent_id   bigint
        constraint fk_auth_permission_parent_id
            references tb_auth_permission,
    sort_order  integer      not null,
    description varchar(255)
);

alter table tb_auth_permission
    owner to root;

create index idx_tb_auth_permission_code
    on tb_auth_permission (code);

create index idx_tb_auth_permission_parent_id
    on tb_auth_permission (parent_id);

insert into tb_auth_permission (created_at, code, name, depth, parent_id, sort_order, description)
values (current_timestamp, 'SYSTEM_ADMIN', '시스템 관리자', 1, null, 0, '운영/관리자 API 전체 권한');

insert into tb_auth_permission (created_at, code, name, depth, parent_id, sort_order, description)
values (current_timestamp, 'SAMPLE_MENU', '샘플 대메뉴', 1, null, 10, '베이스 프로젝트 샘플 대메뉴');

insert into tb_auth_permission (created_at, code, name, depth, parent_id, sort_order, description)
values (current_timestamp, 'SAMPLE_MENU_SECTION', '샘플 중메뉴', 2,
        (select id from tb_auth_permission where code = 'SAMPLE_MENU' and deleted_at is null order by id desc limit 1),
        10, '베이스 프로젝트 샘플 중메뉴');

insert into tb_auth_permission (created_at, code, name, depth, parent_id, sort_order, description)
values (current_timestamp, 'SAMPLE_MENU_SECTION_UPDATE', '샘플 수정', 3,
        (select id from tb_auth_permission where code = 'SAMPLE_MENU_SECTION' and deleted_at is null order by id desc limit 1),
        10, '샘플 수정 권한');

insert into tb_auth_permission (created_at, code, name, depth, parent_id, sort_order, description)
values (current_timestamp, 'SAMPLE_MENU_SECTION_CREATE', '샘플 생성', 3,
        (select id from tb_auth_permission where code = 'SAMPLE_MENU_SECTION' and deleted_at is null order by id desc limit 1),
        20, '샘플 생성 권한');

insert into tb_auth_permission (created_at, code, name, depth, parent_id, sort_order, description)
values (current_timestamp, 'SAMPLE_POLICY_SECTION', '샘플 정책 중메뉴', 2,
        (select id from tb_auth_permission where code = 'SAMPLE_MENU' and deleted_at is null order by id desc limit 1),
        20, '베이스 프로젝트 샘플 정책 중메뉴');

insert into tb_auth_permission (created_at, code, name, depth, parent_id, sort_order, description)
values (current_timestamp, 'SAMPLE_POLICY_SECTION_APPLY', '샘플 정책 적용', 3,
        (select id from tb_auth_permission where code = 'SAMPLE_POLICY_SECTION' and deleted_at is null order by id desc limit 1),
        10, '샘플 정책 적용 권한');

insert into tb_auth_permission (created_at, code, name, depth, parent_id, sort_order, description)
values (current_timestamp, 'SAMPLE_POLICY_SECTION_VIEW', '샘플 정책 조회', 3,
        (select id from tb_auth_permission where code = 'SAMPLE_POLICY_SECTION' and deleted_at is null order by id desc limit 1),
        20, '샘플 정책 조회 권한');

insert into tb_auth_permission (created_at, code, name, depth, parent_id, sort_order, description)
values (current_timestamp, 'SAMPLE_POLICY_SECTION_MANAGE', '샘플 정책 관리', 3,
        (select id from tb_auth_permission where code = 'SAMPLE_POLICY_SECTION' and deleted_at is null order by id desc limit 1),
        30, '샘플 정책 관리 권한');

create table tb_auth_permission_grant
(
    id              bigserial
        primary key,
    created_at      timestamp(6) not null,
    deleted_at      timestamp(6),
    updated_at      timestamp(6),
    user_uuid       varchar(36)  not null
        constraint fk_auth_permission_grant_user_uuid
            references tb_auth,
    permission_id   bigint       not null
        constraint fk_auth_permission_grant_permission_id
            references tb_auth_permission,
    granted_by_uuid varchar(36)
        constraint fk_auth_permission_grant_granted_by_uuid
            references tb_auth
);

alter table tb_auth_permission_grant
    owner to root;

create index idx_tb_auth_permission_grant_user_uuid
    on tb_auth_permission_grant (user_uuid);

create index idx_tb_auth_permission_grant_permission_id
    on tb_auth_permission_grant (permission_id);

create table tb_auth_verification
(
    id                bigserial
        primary key,
    created_at        timestamp(6) not null,
    deleted_at        timestamp(6),
    updated_at        timestamp(6),
    expires_at        timestamp(6) not null,
    metadata          varchar(120),
    target            varchar(255) not null,
    type              varchar(50)  not null
        constraint tb_auth_verification_type_check
            check ((type)::text = ANY
        ((ARRAY ['PASSWORD_RESET'::character varying, 'EMAIL_VERIFICATION'::character varying, 'LOGIN_CHALLENGE'::character varying])::text[])),
    user_uuid         varchar(36)  not null
        constraint fkt64l5ajr6r0umlxxe86udwkbc
            references tb_auth,
    verification_hash varchar(255) not null,
    verified          boolean      not null
);

alter table tb_auth_verification
    owner to root;

create table tb_fcm_device_token
(
    id              bigserial
        primary key,
    created_at      timestamp(6) not null,
    deleted_at      timestamp(6),
    updated_at      timestamp(6),
    device_label    varchar(120),
    device_token    varchar(512) not null,
    enabled         boolean      not null,
    last_ip_address varchar(64),
    last_used_at    timestamp(6),
    user_agent      varchar(512),
    user_uuid       varchar(36)  not null
        constraint fkh5mqsrcueqqpmpy3h10kspxot
            references tb_auth
);

alter table tb_fcm_device_token
    owner to root;

create table tb_fcm_notification
(
    id         bigserial
        primary key,
    created_at timestamp(6) not null,
    deleted_at timestamp(6),
    updated_at timestamp(6),
    body       text         not null,
    data       text,
    message_id varchar(255),
    read       boolean      not null,
    read_at    timestamp(6),
    title      varchar(255) not null,
    user_uuid  varchar(36)  not null
        constraint fk54b9qelegeflu3bwn7f2huytl
            references tb_auth
);

alter table tb_fcm_notification
    owner to root;

create table tb_files
(
    id                 bigserial
        primary key,
    content_type       varchar(255),
    created_at         timestamp(6),
    deleted            boolean      not null,
    dir_name           varchar(255) not null,
    file_path          varchar(255) not null,
    file_size          bigint,
    original_file_name varchar(255) not null,
    owner_uuid         varchar(36)
        constraint fkgls6l9y2rie2sust48pn4bagr
            references tb_auth,
    storage_type       varchar(20)  not null
        constraint tb_files_storage_type_check
            check ((storage_type)::text = ANY ((ARRAY ['LOCAL'::character varying, 'S3'::character varying])::text[])),
    url                varchar(255) not null
);

alter table tb_files
    owner to root;

create table tb_refresh_token
(
    id                   bigserial
        primary key,
    created_at           timestamp(6)  not null,
    deleted_at           timestamp(6),
    updated_at           timestamp(6),
    device_label         varchar(128),
    expiry_date          timestamp(6)  not null,
    family_id            varchar(64)   not null,
    ip_address           varchar(64),
    last_used_at         timestamp(6),
    replaced_by_token_id varchar(64),
    revoked              boolean       not null,
    token                varchar(1024) not null,
    token_hash           varchar(1024) not null,
    token_id             varchar(64)   not null,
    user_agent           varchar(512),
    user_uuid            varchar(36)   not null
        constraint fkes59al21mee9i14oy9310kpas
            references tb_auth
);

alter table tb_refresh_token
    owner to root;
