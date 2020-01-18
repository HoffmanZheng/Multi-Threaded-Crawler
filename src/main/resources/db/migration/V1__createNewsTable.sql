

create table NEWS(
	ID bigint primary key auto_increment,
	TITLE text,
	CONTENT text,
	URL varchar(3000),
	CREATED_AT timestamp,
	MODIFIED_AT timestamp
);

create table LINKS_TO_BE_PROCESSED(link varchar(3000));

create table LINKS_ALREADY_PROCESSED(link varchar(3000));