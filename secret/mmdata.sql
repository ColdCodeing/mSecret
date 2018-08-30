create table t_token
(
	unique_id serial not null
		constraint t_token_pkey
			primary key,
	token_info jsonb not null,
	created_time timestamp default CURRENT_TIMESTAMP not null,
	updated_time timestamp default CURRENT_TIMESTAMP not null
)
;


create unique index t_token_unique_id_uindex
	on t_token (unique_id)
;

create table t_user
(
	unique_id serial not null
		constraint t_user_pkey
			primary key,
	user_info jsonb not null,
	created_time timestamp default CURRENT_TIMESTAMP not null,
	updated_time timestamp default CURRENT_TIMESTAMP not null
)

create unique index t_user_unique_id_uindex
	on t_user (unique_id)
;

create unique index email_idx
	on t_user ((user_info ->> 'email'::text))
;

create table t_account_activate
(
	unique_id serial not null
		constraint t_account_activate_pk
			primary key,
	activate_code varchar(20) not null,
	email varchar(50) not null,
	uuid varchar(50) not null
)
;

create unique index t_account_activate_unique_id_uindex
	on t_account_activate (unique_id)
;

create unique index t_account_activate_email_uindex
	on t_account_activate (email)
;

