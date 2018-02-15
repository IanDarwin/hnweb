-- Archived Member
drop table if exists member_archived;
create table member_archived as select * from member where id = -1;
-- alter table member_archived add column archive_date date default now();
-- alter table member_archived add column archive_reason varchar default 'Deleted';
