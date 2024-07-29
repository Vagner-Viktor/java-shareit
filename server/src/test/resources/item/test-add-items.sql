delete from users;
insert into users ( name, email)
values( 'TestUserNameInBD', 'TestUserEmail@in.BD');
insert into users ( name, email)
values( 'TestUserNameInBD2', 'TestUserEmail2@in.BD');

delete from requests;
insert into requests ( description, requestor_id, created)
values ( 'TestDescriptionInDB', 1, NOW());

delete from items;
insert into items ( name, description, is_available, owner_id, request_id)
values ('TestNameInBD', 'TestDescriptionInDB', true, 1, 1);