create table if not exists t_token
(
	unique_id serial not null
		constraint t_token_pkey
			primary key,
	token_info jsonb not null,
	created_time timestamp default CURRENT_TIMESTAMP not null,
	updated_time timestamp default CURRENT_TIMESTAMP not null
);

alter table t_token owner to panmin;

create unique index if not exists t_token_unique_id_uindex
	on t_token (unique_id);

create table if not exists t_user
(
	unique_id serial not null
		constraint t_user_pkey
			primary key,
	user_info jsonb not null,
	created_time timestamp default CURRENT_TIMESTAMP not null,
	updated_time timestamp default CURRENT_TIMESTAMP not null
);

alter table t_user owner to panmin;

create unique index if not exists t_user_unique_id_uindex
	on t_user (unique_id);

create unique index if not exists email_idx
	on t_user ((user_info ->> 'email'::text));

create table if not exists t_account_activate
(
	unique_id serial not null
		constraint t_account_activate_pk
			primary key,
	activate_code varchar(20) not null,
	email varchar(50) not null,
	uuid varchar(50) not null
);

alter table t_account_activate owner to panmin;

create unique index if not exists t_account_activate_unique_id_uindex
	on t_account_activate (unique_id);

create unique index if not exists t_account_activate_email_uindex
	on t_account_activate (email);

create table if not exists t_user_pass
(
	unique_id serial not null
		constraint t_user_pass_pk
			primary key,
	uuid varchar(50) not null,
	weight integer default 0 not null,
	data jsonb not null,
	created_time timestamp default CURRENT_TIMESTAMP not null,
	updated_time timestamp default CURRENT_TIMESTAMP not null
);

alter table t_user_pass owner to panmin;

create unique index if not exists t_user_pass_unique_id_uindex
	on t_user_pass (unique_id);

