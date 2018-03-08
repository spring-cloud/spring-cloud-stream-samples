DROP TABLE test;
create table test(
  id bigint,
  name varchar (2000),
  tag char(1)
);
insert into test values (1, 'Bob', NULL);
insert into test values (2, 'Jane', NULL);
insert into test values (3, 'John', NULL);