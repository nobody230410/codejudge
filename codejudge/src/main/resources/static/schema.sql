drop database if exists code_judge;
create database code_judge CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
use code_judge;

create table user
(
    id         int auto_increment
        primary key,
    username   varchar(255)                       not null,
    password   varchar(255)                       not null,
    email      varchar(100)                       null,
    created_at datetime default CURRENT_TIMESTAMP null,
    constraint uk_user_name
        unique (username)
);

create table admin
(
    id         int auto_increment
        primary key,
    username   varchar(255)                       not null,
    password   varchar(255)                       not null,
    email      varchar(100)                       null,
    created_at datetime default CURRENT_TIMESTAMP null,
    constraint uk_admin_name
        unique (username)
);

create table problem
(
    id          int auto_increment
        primary key,
    title       varchar(255)                       not null,
    description mediumtext                         not null,
    difficulty  enum ('easy', 'middle', 'hard')    not null,
    created_by  int                                null,
    created_at  datetime default CURRENT_TIMESTAMP null,
    constraint uk_problem_title
        unique (title)
);

create table submission
(
    id          int auto_increment
        primary key,
    user_id     int                                null,
    problem_id  int                                null,
    code        mediumtext                         not null,
    status      varchar(32)                        not null,
    exec_time   int                                null,
    exec_memory int                                null,
    submit_time datetime default CURRENT_TIMESTAMP null,
    language    varchar(32)                        not null,
    message     mediumtext                         null,
    constraint submission_ibfk_1
        foreign key (user_id) references user (id),
    constraint submission_ibfk_2
        foreign key (problem_id) references problem (id)
);

create index idx_submission_user_id
    on submission (user_id);

create index idx_submission_user_problem_id
    on submission (user_id, problem_id, id);

create table test_case
(
    id         int auto_increment
        primary key,
    problem_id int         null,
    input      mediumtext  not null,
    output     mediumtext  not null,
    input_hash varchar(32) not null,
    constraint uk_input_hash
        unique (input_hash),
    constraint test_case_ibfk_1
        foreign key (problem_id) references problem (id)
);

create index problem_id
    on test_case (problem_id);