-- Dummy Data

insert into member(id, firstname, lastname, email, expiryyear, version) values(-1, 'TEST', 'PERSON', 'p1@x.com', '2016', 0);

-- Archived
create table member_archived as select * from member where id = -1;
alter table member_archived add column archive_date date default now();
alter table member_archived add column archive_reason varchar default 'Deleted';
