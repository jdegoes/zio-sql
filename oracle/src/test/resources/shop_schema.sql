create table customers
(
    id varchar(36) not null primary key,
    first_name varchar(100) not null,
    last_name varchar(100) not null,
    verified smallint not null,
    dob date not null
);

create table orders
(
    id varchar(36) not null primary key,
    customer_id varchar(36) not null,
    order_date date not null
);

create table products
(
    id varchar(36) not null primary key,
    name varchar(100),
    description varchar(500) not null,
    image_url varchar(500)
);

create table product_prices
(
    product_id varchar(36) not null,
    effective date not null,
    price Number(15,2) not null
);

create table order_details
(
    order_id varchar(36) not null,
    product_id varchar(36) not null,
    quantity integer not null,
    unit_price Number(15,2) not null
);

insert all
	into customers (id, first_name, last_name, verified, dob) values ('60b01fc9-c902-4468-8d49-3c0f989def37', 'Ronald', 'Russell', 1, TO_DATE('1983-01-05','YYYY-MM-DD'))   
	into customers (id, first_name, last_name, verified, dob) values ('f76c9ace-be07-4bf3-bd4c-4a9c62882e64', 'Terrence', 'Noel', 1, TO_DATE('1999-11-02','YYYY-MM-DD'))
	into customers (id, first_name, last_name, verified, dob) values ('784426a5-b90a-4759-afbb-571b7a0ba35e', 'Mila', 'Paterso', 1, TO_DATE('1990-11-16','YYYY-MM-DD'))
	into customers (id, first_name, last_name, verified, dob) values ('df8215a2-d5fd-4c6c-9984-801a1b3a2a0b', 'Alana', 'Murray', 1, TO_DATE('1995-11-12','YYYY-MM-DD'))
	into customers (id, first_name, last_name, verified, dob) values ('636ae137-5b1a-4c8c-b11f-c47c624d9cdc', 'Jose', 'Wiggins', 0, TO_DATE('1987-03-23','YYYY-MM-DD'))
select * from dual;

insert all
	into products (id, name, description, image_url) values ('7368ABF4-AED2-421F-B426-1725DE756895', 'Thermometer', 'Make sure you don''t have a fever (could be covid!)', 'https://images.pexels.com/photos/3987152/pexels-photo-3987152.jpeg?auto=compress&cs=tinysrgb&h=750&w=1260')
	into products (id, name, description, image_url) values ('4C770002-4C8F-455A-96FF-36A8186D5290', 'Slippers', 'Keep your feet warm this winter', 'https://images.pexels.com/photos/1989843/pexels-photo-1989843.jpeg?auto=compress&cs=tinysrgb&h=750&w=1260')
	into products (id, name, description, image_url) values ('05182725-F5C8-4FD6-9C43-6671E179BF55', 'Mouse Pad', 'Who uses these anyway?', 'https://images.pexels.com/photos/3944396/pexels-photo-3944396.jpeg?auto=compress&cs=tinysrgb&h=750&w=1260')
	into products (id, name, description, image_url) values ('105A2701-EF93-4E25-81AB-8952CC7D9DAA', 'Pants', 'Avoid a lawsuit, wear pants to work today!', 'https://images.pexels.com/photos/52518/jeans-pants-blue-shop-52518.jpeg?cs=srgb&dl=blue-jeans-clothes-shopping-52518.jpg&fm=jpg')
	into products (id, name, description, image_url) values ('F35B0053-855B-4145-ABE1-DC62BC1FDB96', 'Nail File', 'Keep those nails looking good', 'https://images.pexels.com/photos/3997373/pexels-photo-3997373.jpeg?auto=compress&cs=tinysrgb&h=750&w=1260')
	into products (id, name, description, image_url) values ('D5137D3A-894A-4109-9986-E982541B434F', 'Teddy Bear', 'Because sometimes you just need something to hug', 'https://images.pexels.com/photos/1019471/stuffed-bear-teddy-child-girl-1019471.jpeg?cs=srgb&dl=closeup-photography-of-brown-teddy-bear-1019471.jpg&fm=jpg')
select * from dual;

insert all
    into product_prices
        (product_id, effective, price)
    values('7368ABF4-AED2-421F-B426-1725DE756895', TO_DATE('2018-01-01', 'YYYY-MM-DD'), 10.00)
    into product_prices
        (product_id, effective, price)
    values('7368ABF4-AED2-421F-B426-1725DE756895', TO_DATE('2019-01-01', 'YYYY-MM-DD'), 11.00)
    into product_prices
        (product_id, effective, price)
    values('7368ABF4-AED2-421F-B426-1725DE756895', TO_DATE('2020-01-01', 'YYYY-MM-DD'), 12.00)
    into product_prices
        (product_id, effective, price)
    values('4C770002-4C8F-455A-96FF-36A8186D5290', TO_DATE('2018-01-01', 'YYYY-MM-DD'), 20.00)
    into product_prices
        (product_id, effective, price)
    values('4C770002-4C8F-455A-96FF-36A8186D5290', TO_DATE('2019-01-01', 'YYYY-MM-DD'), 22.00)
    into product_prices
        (product_id, effective, price)
    values('4C770002-4C8F-455A-96FF-36A8186D5290', TO_DATE('2020-01-01', 'YYYY-MM-DD'), 22.00)
    into product_prices
        (product_id, effective, price)
    values('05182725-F5C8-4FD6-9C43-6671E179BF55', TO_DATE('2018-01-01', 'YYYY-MM-DD'), 2.00)
    into product_prices
        (product_id, effective, price)
    values('105A2701-EF93-4E25-81AB-8952CC7D9DAA', TO_DATE('2018-01-01', 'YYYY-MM-DD'), 70.00)
    into product_prices
        (product_id, effective, price)
    values('105A2701-EF93-4E25-81AB-8952CC7D9DAA', TO_DATE('2019-01-01', 'YYYY-MM-DD'), 74.00)
    into product_prices
        (product_id, effective, price)
    values('105A2701-EF93-4E25-81AB-8952CC7D9DAA', TO_DATE('2020-01-01', 'YYYY-MM-DD'), 80.00)
    into product_prices
        (product_id, effective, price)
    values('F35B0053-855B-4145-ABE1-DC62BC1FDB96', TO_DATE('2018-01-01', 'YYYY-MM-DD'), 5.00)
    into product_prices
        (product_id, effective, price)
    values('F35B0053-855B-4145-ABE1-DC62BC1FDB96', TO_DATE('2019-01-01', 'YYYY-MM-DD'), 6.00)
    into product_prices
        (product_id, effective, price)
    values('D5137D3A-894A-4109-9986-E982541B434F', TO_DATE('2018-01-01', 'YYYY-MM-DD'), 50.00)
    into product_prices
        (product_id, effective, price)
    values('D5137D3A-894A-4109-9986-E982541B434F', TO_DATE('2020-01-01', 'YYYY-MM-DD'), 55.00)
select * from dual;

insert all
	into orders
	    (id, customer_id, order_date)
	values
	    ('04912093-cc2e-46ac-b64c-1bd7bb7758c3', '60b01fc9-c902-4468-8d49-3c0f989def37', TO_DATE('2019-03-25', 'YYYY-MM-DD'))
	into orders
	    (id, customer_id, order_date)
	values   
	    ('a243fa42-817a-44ec-8b67-22193d212d82', '60b01fc9-c902-4468-8d49-3c0f989def37', TO_DATE('2018-06-04', 'YYYY-MM-DD'))
	into orders
	    (id, customer_id, order_date)
	values
	    ('9022dd0d-06d6-4a43-9121-2993fc7712a1', 'df8215a2-d5fd-4c6c-9984-801a1b3a2a0b', TO_DATE('2019-08-19', 'YYYY-MM-DD'))
    into orders
            (id, customer_id, order_date)
        values
            ('38d66d44-3cfa-488a-ac77-30277751418f', '636ae137-5b1a-4c8c-b11f-c47c624d9cdc', TO_DATE('2019-08-30', 'YYYY-MM-DD'))
    into orders
            (id, customer_id, order_date)
        values
            ('7b2627d5-0150-44df-9171-3462e20797ee', '636ae137-5b1a-4c8c-b11f-c47c624d9cdc', TO_DATE('2019-03-07', 'YYYY-MM-DD'))
    into orders
            (id, customer_id, order_date)
        values
            ('62cd4109-3e5d-40cc-8188-3899fc1f8bdf', '60b01fc9-c902-4468-8d49-3c0f989def37', TO_DATE('2020-03-19', 'YYYY-MM-DD'))
    into orders
            (id, customer_id, order_date)
        values
            ('9473a0bc-396a-4936-96b0-3eea922af36b', 'df8215a2-d5fd-4c6c-9984-801a1b3a2a0b', TO_DATE('2020-05-11', 'YYYY-MM-DD'))
    into orders
            (id, customer_id, order_date)
        values
            ('b8bac18d-769f-48ed-809d-4b6c0e4d1795', 'df8215a2-d5fd-4c6c-9984-801a1b3a2a0b', TO_DATE('2019-02-21', 'YYYY-MM-DD'))
    into orders
            (id, customer_id, order_date)
        values
            ('852e2dc9-4ec3-4225-a6f7-4f42f8ff728e', '60b01fc9-c902-4468-8d49-3c0f989def37', TO_DATE('2018-05-06', 'YYYY-MM-DD'))
    into orders
            (id, customer_id, order_date)
        values
            ('bebbfe4d-4ec3-4389-bdc2-50e9eac2b15b', '784426a5-b90a-4759-afbb-571b7a0ba35e', TO_DATE('2019-02-11', 'YYYY-MM-DD'))
    into orders
            (id, customer_id, order_date)
        values
            ('742d45a0-e81a-41ce-95ad-55b4cabba258', 'f76c9ace-be07-4bf3-bd4c-4a9c62882e64', TO_DATE('2019-10-12', 'YYYY-MM-DD'))
    into orders
            (id, customer_id, order_date)
        values
            ('618aa21f-700b-4ca7-933c-67066cf4cd97', '60b01fc9-c902-4468-8d49-3c0f989def37', TO_DATE('2019-01-29', 'YYYY-MM-DD'))
    into orders
            (id, customer_id, order_date)
        values
            ('606da090-dd33-4a77-8746-6ed0e8443ab2', 'f76c9ace-be07-4bf3-bd4c-4a9c62882e64', TO_DATE('2019-02-10', 'YYYY-MM-DD'))
    into orders
            (id, customer_id, order_date)
        values
            ('4914028d-2e28-4033-a5f2-8f4fcdee8206', '60b01fc9-c902-4468-8d49-3c0f989def37', TO_DATE('2019-09-27', 'YYYY-MM-DD'))
    into orders
            (id, customer_id, order_date)
        values
            ('d4e77298-d829-4e36-a6a0-902403f4b7d3', 'df8215a2-d5fd-4c6c-9984-801a1b3a2a0b', TO_DATE('2018-11-13', 'YYYY-MM-DD'))
    into orders
            (id, customer_id, order_date)
        values
            ('fd0fa8d4-e1a0-4369-be07-945450db5d36', '636ae137-5b1a-4c8c-b11f-c47c624d9cdc', TO_DATE('2020-01-15', 'YYYY-MM-DD'))
    into orders
            (id, customer_id, order_date)
        values
            ('d6d8dddc-4b0b-4d74-8edc-a54e9b7f35f7', 'f76c9ace-be07-4bf3-bd4c-4a9c62882e64', TO_DATE('2018-07-10', 'YYYY-MM-DD'))
    into orders
            (id, customer_id, order_date)
        values
            ('876b6034-b33c-4497-81ee-b4e8742164c2', '784426a5-b90a-4759-afbb-571b7a0ba35e', TO_DATE('2019-08-01', 'YYYY-MM-DD'))
        into orders
            (id, customer_id, order_date)
        values
            ('91caa28a-a5fe-40d7-979c-bd6a128d0418', 'df8215a2-d5fd-4c6c-9984-801a1b3a2a0b', TO_DATE('2019-12-08', 'YYYY-MM-DD'))
    into orders
            (id, customer_id, order_date)
        values
            ('401c7ab1-41cf-4756-8af5-be25cf2ae67b', '784426a5-b90a-4759-afbb-571b7a0ba35e', TO_DATE('2019-11-04', 'YYYY-MM-DD'))
    into orders
            (id, customer_id, order_date)
        values
            ('c3fc180-d0df-4d7b-a271-e6ccd2440393', '784426a5-b90a-4759-afbb-571b7a0ba35e', TO_DATE('2018-10-14', 'YYYY-MM-DD'))
    into orders
            (id, customer_id, order_date)
        values
            ('763a7c39-833f-4ee8-9939-e80dfdbfc0fc', 'f76c9ace-be07-4bf3-bd4c-4a9c62882e64', TO_DATE('2020-04-05', 'YYYY-MM-DD'))
        into orders
            (id, customer_id, order_date)
        values
            ('5011d206-8eff-42c4-868e-f1a625e1f186', '636ae137-5b1a-4c8c-b11f-c47c624d9cdc', TO_DATE('2019-01-23', 'YYYY-MM-DD'))
        into orders
            (id, customer_id, order_date)
        values
            ('0a48ffb0-ec61-4147-af56-fc4dbca8de0a', 'f76c9ace-be07-4bf3-bd4c-4a9c62882e64', TO_DATE('2019-05-14', 'YYYY-MM-DD'))
        into orders
            (id, customer_id, order_date)
        values
            ('5883cb62-d792-4ee3-acbc-fe85b6baa998', '784426a5-b90a-4759-afbb-571b7a0ba35e', TO_DATE('2020-04-30', 'YYYY-MM-DD'))
select * from dual;


insert all
    into order_details(order_id, product_id, quantity, unit_price)
    values('9022DD0D-06D6-4A43-9121-2993FC7712A1', '7368ABF4-AED2-421F-B426-1725DE756895', 4, 11.00)
    into order_details(order_id, product_id, quantity, unit_price)
    values
        ('38D66D44-3CFA-488A-AC77-30277751418F', '7368ABF4-AED2-421F-B426-1725DE756895', 1, 11.00)
    into order_details(order_id, product_id, quantity, unit_price)
    values
        ('7B2627D5-0150-44DF-9171-3462E20797EE', '7368ABF4-AED2-421F-B426-1725DE756895', 1, 11.00)
    into order_details(order_id, product_id, quantity, unit_price)
    values
        ('62CD4109-3E5D-40CC-8188-3899FC1F8BDF', '7368ABF4-AED2-421F-B426-1725DE756895', 2, 10.90)
    into order_details(order_id, product_id, quantity, unit_price)
    values
        ('9473A0BC-396A-4936-96B0-3EEA922AF36B', '7368ABF4-AED2-421F-B426-1725DE756895', 1, 12.00)
    into order_details(order_id, product_id, quantity, unit_price)
    values
        ('852E2DC9-4EC3-4225-A6F7-4F42F8FF728E', '7368ABF4-AED2-421F-B426-1725DE756895', 1, 9.09)
    into order_details(order_id, product_id, quantity, unit_price)
    values
        ('742D45A0-E81A-41CE-95AD-55B4CABBA258', '7368ABF4-AED2-421F-B426-1725DE756895', 2, 10.00)
    into order_details(order_id, product_id, quantity, unit_price)
    values
        ('618AA21F-700B-4CA7-933C-67066CF4CD97', '7368ABF4-AED2-421F-B426-1725DE756895', 2, 11.00)
    into order_details(order_id, product_id, quantity, unit_price)
    values
        ('606DA090-DD33-4A77-8746-6ED0E8443AB2', '7368ABF4-AED2-421F-B426-1725DE756895', 2, 10.00)
    into order_details(order_id, product_id, quantity, unit_price)
    values
        ('4914028D-2E28-4033-A5F2-8F4FCDEE8206', '7368ABF4-AED2-421F-B426-1725DE756895', 1, 10.00)
    into order_details(order_id, product_id, quantity, unit_price)
    values
        ('D4E77298-D829-4E36-A6A0-902403F4B7D3', '7368ABF4-AED2-421F-B426-1725DE756895', 2, 10.00)
    into order_details(order_id, product_id, quantity, unit_price)
    values
        ('FD0FA8D4-E1A0-4369-BE07-945450DB5D36', '7368ABF4-AED2-421F-B426-1725DE756895', 1, 12.00)
    into order_details(order_id, product_id, quantity, unit_price)
    values
        ('D6D8DDDC-4B0B-4D74-8EDC-A54E9B7F35F7', '7368ABF4-AED2-421F-B426-1725DE756895', 2, 10.00)
    into order_details(order_id, product_id, quantity, unit_price)
    values
        ('876B6034-B33C-4497-81EE-B4E8742164C2', '7368ABF4-AED2-421F-B426-1725DE756895', 1, 11.00)
    into order_details(order_id, product_id, quantity, unit_price)
    values
        ('91CAA28A-A5FE-40D7-979C-BD6A128D0418', '7368ABF4-AED2-421F-B426-1725DE756895', 3, 11.00)
    into order_details(order_id, product_id, quantity, unit_price)
    values
        ('2C3FC180-D0DF-4D7B-A271-E6CCD2440393', '7368ABF4-AED2-421F-B426-1725DE756895', 2, 10.00)
    into order_details(order_id, product_id, quantity, unit_price)
    values
        ('763A7C39-833F-4EE8-9939-E80DFDBFC0FC', '7368ABF4-AED2-421F-B426-1725DE756895', 4, 12.00)
    into order_details(order_id, product_id, quantity, unit_price)
    values
        ('5011D206-8EFF-42C4-868E-F1A625E1F186', '7368ABF4-AED2-421F-B426-1725DE756895', 4, 11.00)
    into order_details(order_id, product_id, quantity, unit_price)
    values
        ('0A48FFB0-EC61-4147-AF56-FC4DBCA8DE0A', '7368ABF4-AED2-421F-B426-1725DE756895', 1, 11.00)
    into order_details(order_id, product_id, quantity, unit_price)
    values
        ('5883CB62-D792-4EE3-ACBC-FE85B6BAA998', '7368ABF4-AED2-421F-B426-1725DE756895', 3, 12.00)
    into order_details(order_id, product_id, quantity, unit_price)
    values
        ('04912093-CC2E-46AC-B64C-1BD7BB7758C3', '4C770002-4C8F-455A-96FF-36A8186D5290', 2, 22.00)
    into order_details(order_id, product_id, quantity, unit_price)
    values
        ('A243FA42-817A-44EC-8B67-22193D212D82', '4C770002-4C8F-455A-96FF-36A8186D5290', 5, 18.18)
    into order_details(order_id, product_id, quantity, unit_price)
    values
        ('9022DD0D-06D6-4A43-9121-2993FC7712A1', '4C770002-4C8F-455A-96FF-36A8186D5290', 2, 22.00)
    into order_details(order_id, product_id, quantity, unit_price)
    values
        ('38D66D44-3CFA-488A-AC77-30277751418F', '4C770002-4C8F-455A-96FF-36A8186D5290', 1, 22.00)
    into order_details(order_id, product_id, quantity, unit_price)
    values
        ('62CD4109-3E5D-40CC-8188-3899FC1F8BDF', '4C770002-4C8F-455A-96FF-36A8186D5290', 1, 20.00)
    into order_details(order_id, product_id, quantity, unit_price)
    values
        ('852E2DC9-4EC3-4225-A6F7-4F42F8FF728E', '4C770002-4C8F-455A-96FF-36A8186D5290', 1, 18.18)
    into order_details(order_id, product_id, quantity, unit_price)
    values
        ('BEBBFE4D-4EC3-4389-BDC2-50E9EAC2B15B', '4C770002-4C8F-455A-96FF-36A8186D5290', 1, 20.00)
    into order_details(order_id, product_id, quantity, unit_price)
    values
        ('618AA21F-700B-4CA7-933C-67066CF4CD97', '4C770002-4C8F-455A-96FF-36A8186D5290', 1, 22.00)
    into order_details(order_id, product_id, quantity, unit_price)
    values
        ('606DA090-DD33-4A77-8746-6ED0E8443AB2', '4C770002-4C8F-455A-96FF-36A8186D5290', 3, 20.00)
    into order_details(order_id, product_id, quantity, unit_price)
    values
        ('4914028D-2E28-4033-A5F2-8F4FCDEE8206', '4C770002-4C8F-455A-96FF-36A8186D5290', 1, 20.00)
    into order_details(order_id, product_id, quantity, unit_price)
    values
        ('FD0FA8D4-E1A0-4369-BE07-945450DB5D36', '4C770002-4C8F-455A-96FF-36A8186D5290', 1, 22.00)
    into order_details(order_id, product_id, quantity, unit_price)
    values
        ('D6D8DDDC-4B0B-4D74-8EDC-A54E9B7F35F7', '4C770002-4C8F-455A-96FF-36A8186D5290', 1, 20.00)
    into order_details(order_id, product_id, quantity, unit_price)
    values
        ('876B6034-B33C-4497-81EE-B4E8742164C2', '4C770002-4C8F-455A-96FF-36A8186D5290', 2, 22.00)
    into order_details(order_id, product_id, quantity, unit_price)
    values
        ('91CAA28A-A5FE-40D7-979C-BD6A128D0418', '4C770002-4C8F-455A-96FF-36A8186D5290', 1, 22.00)
    into order_details(order_id, product_id, quantity, unit_price)
    values
        ('2C3FC180-D0DF-4D7B-A271-E6CCD2440393', '4C770002-4C8F-455A-96FF-36A8186D5290', 1, 20.00)
    into order_details(order_id, product_id, quantity, unit_price)
    values
        ('763A7C39-833F-4EE8-9939-E80DFDBFC0FC', '4C770002-4C8F-455A-96FF-36A8186D5290', 1, 22.00)
    into order_details(order_id, product_id, quantity, unit_price)
    values
        ('5011D206-8EFF-42C4-868E-F1A625E1F186', '4C770002-4C8F-455A-96FF-36A8186D5290', 1, 22.00)
    into order_details(order_id, product_id, quantity, unit_price)
    values
        ('0A48FFB0-EC61-4147-AF56-FC4DBCA8DE0A', '4C770002-4C8F-455A-96FF-36A8186D5290', 3, 22.00)
    into order_details(order_id, product_id, quantity, unit_price)
    values
        ('5883CB62-D792-4EE3-ACBC-FE85B6BAA998', '4C770002-4C8F-455A-96FF-36A8186D5290', 3, 22.00)
    into order_details(order_id, product_id, quantity, unit_price)
    values
        ('A243FA42-817A-44EC-8B67-22193D212D82', '05182725-F5C8-4FD6-9C43-6671E179BF55', 1, 1.81)
    into order_details(order_id, product_id, quantity, unit_price)
    values
        ('852E2DC9-4EC3-4225-A6F7-4F42F8FF728E', '05182725-F5C8-4FD6-9C43-6671E179BF55', 1, 1.81)
    into order_details(order_id, product_id, quantity, unit_price)
    values
        ('D4E77298-D829-4E36-A6A0-902403F4B7D3', '05182725-F5C8-4FD6-9C43-6671E179BF55', 1, 2.00)
    into order_details(order_id, product_id, quantity, unit_price)
    values
        ('D6D8DDDC-4B0B-4D74-8EDC-A54E9B7F35F7', '05182725-F5C8-4FD6-9C43-6671E179BF55', 3, 2.00)
    into order_details(order_id, product_id, quantity, unit_price)
    values
        ('04912093-CC2E-46AC-B64C-1BD7BB7758C3', '105A2701-EF93-4E25-81AB-8952CC7D9DAA', 1, 74.00)
    into order_details(order_id, product_id, quantity, unit_price)
    values
        ('9022DD0D-06D6-4A43-9121-2993FC7712A1', '105A2701-EF93-4E25-81AB-8952CC7D9DAA', 4, 74.00)
    into order_details(order_id, product_id, quantity, unit_price)
    values
        ('38D66D44-3CFA-488A-AC77-30277751418F', '105A2701-EF93-4E25-81AB-8952CC7D9DAA', 2, 74.00)
    into order_details(order_id, product_id, quantity, unit_price)
    values
        ('7B2627D5-0150-44DF-9171-3462E20797EE', '105A2701-EF93-4E25-81AB-8952CC7D9DAA', 1, 74.00)
    into order_details(order_id, product_id, quantity, unit_price)
    values
        ('62CD4109-3E5D-40CC-8188-3899FC1F8BDF', '105A2701-EF93-4E25-81AB-8952CC7D9DAA', 1, 72.72)
    into order_details(order_id, product_id, quantity, unit_price)
    values
        ('9473A0BC-396A-4936-96B0-3EEA922AF36B', '105A2701-EF93-4E25-81AB-8952CC7D9DAA', 2, 80.00)
    into order_details(order_id, product_id, quantity, unit_price)
    values
        ('B8BAC18D-769F-48ED-809D-4B6C0E4D1795', '105A2701-EF93-4E25-81AB-8952CC7D9DAA', 3, 67.27)
    into order_details(order_id, product_id, quantity, unit_price)
    values
        ('BEBBFE4D-4EC3-4389-BDC2-50E9EAC2B15B', '105A2701-EF93-4E25-81AB-8952CC7D9DAA', 2, 67.27)
    into order_details(order_id, product_id, quantity, unit_price)
    values
        ('742D45A0-E81A-41CE-95AD-55B4CABBA258', '105A2701-EF93-4E25-81AB-8952CC7D9DAA', 1, 67.27)
    into order_details(order_id, product_id, quantity, unit_price)
    values
        ('618AA21F-700B-4CA7-933C-67066CF4CD97', '105A2701-EF93-4E25-81AB-8952CC7D9DAA', 2, 74.00)
    into order_details(order_id, product_id, quantity, unit_price)
    values
        ('606DA090-DD33-4A77-8746-6ED0E8443AB2', '105A2701-EF93-4E25-81AB-8952CC7D9DAA', 1, 67.27)
    into order_details(order_id, product_id, quantity, unit_price)
    values
        ('FD0FA8D4-E1A0-4369-BE07-945450DB5D36', '105A2701-EF93-4E25-81AB-8952CC7D9DAA', 1, 80.00)
    into order_details(order_id, product_id, quantity, unit_price)
    values
        ('876B6034-B33C-4497-81EE-B4E8742164C2', '105A2701-EF93-4E25-81AB-8952CC7D9DAA', 2, 74.00)
    into order_details(order_id, product_id, quantity, unit_price)
    values
        ('91CAA28A-A5FE-40D7-979C-BD6A128D0418', '105A2701-EF93-4E25-81AB-8952CC7D9DAA', 5, 74.00)
    into order_details(order_id, product_id, quantity, unit_price)
    values
        ('2C3FC180-D0DF-4D7B-A271-E6CCD2440393', '105A2701-EF93-4E25-81AB-8952CC7D9DAA', 1, 70.00)
    into order_details(order_id, product_id, quantity, unit_price)
    values
        ('763A7C39-833F-4EE8-9939-E80DFDBFC0FC', '105A2701-EF93-4E25-81AB-8952CC7D9DAA', 3, 80.00)
    into order_details(order_id, product_id, quantity, unit_price)
    values
        ('5011D206-8EFF-42C4-868E-F1A625E1F186', '105A2701-EF93-4E25-81AB-8952CC7D9DAA', 6, 74.00)
    into order_details(order_id, product_id, quantity, unit_price)
    values
        ('0A48FFB0-EC61-4147-AF56-FC4DBCA8DE0A', '105A2701-EF93-4E25-81AB-8952CC7D9DAA', 2, 74.00)
    into order_details(order_id, product_id, quantity, unit_price)
    values
        ('04912093-CC2E-46AC-B64C-1BD7BB7758C3', 'F35B0053-855B-4145-ABE1-DC62BC1FDB96', 1, 6.00)
    into order_details(order_id, product_id, quantity, unit_price)
    values
        ('A243FA42-817A-44EC-8B67-22193D212D82', 'F35B0053-855B-4145-ABE1-DC62BC1FDB96', 4, 4.54)
    into order_details(order_id, product_id, quantity, unit_price)
    values
        ('9022DD0D-06D6-4A43-9121-2993FC7712A1', 'F35B0053-855B-4145-ABE1-DC62BC1FDB96', 1, 6.00)
    into order_details(order_id, product_id, quantity, unit_price)
    values
        ('38D66D44-3CFA-488A-AC77-30277751418F', 'F35B0053-855B-4145-ABE1-DC62BC1FDB96', 1, 6.00)
    into order_details(order_id, product_id, quantity, unit_price)
    values
        ('7B2627D5-0150-44DF-9171-3462E20797EE', 'F35B0053-855B-4145-ABE1-DC62BC1FDB96', 2, 6.00)
    into order_details(order_id, product_id, quantity, unit_price)
    values
        ('B8BAC18D-769F-48ED-809D-4B6C0E4D1795', 'F35B0053-855B-4145-ABE1-DC62BC1FDB96', 1, 5.45)
    into order_details(order_id, product_id, quantity, unit_price)
    values
        ('BEBBFE4D-4EC3-4389-BDC2-50E9EAC2B15B', 'F35B0053-855B-4145-ABE1-DC62BC1FDB96', 1, 5.45)
    into order_details(order_id, product_id, quantity, unit_price)
    values
        ('742D45A0-E81A-41CE-95AD-55B4CABBA258', 'F35B0053-855B-4145-ABE1-DC62BC1FDB96', 1, 5.45)
    into order_details(order_id, product_id, quantity, unit_price)
    values
        ('606DA090-DD33-4A77-8746-6ED0E8443AB2', 'F35B0053-855B-4145-ABE1-DC62BC1FDB96', 1, 5.45)
    into order_details(order_id, product_id, quantity, unit_price)
    values
        ('4914028D-2E28-4033-A5F2-8F4FCDEE8206', 'F35B0053-855B-4145-ABE1-DC62BC1FDB96', 1, 5.45)
    into order_details(order_id, product_id, quantity, unit_price)
    values
        ('D6D8DDDC-4B0B-4D74-8EDC-A54E9B7F35F7', 'F35B0053-855B-4145-ABE1-DC62BC1FDB96', 1, 5.00)
    into order_details(order_id, product_id, quantity, unit_price)
    values
        ('91CAA28A-A5FE-40D7-979C-BD6A128D0418', 'F35B0053-855B-4145-ABE1-DC62BC1FDB96', 1, 6.00)
    into order_details(order_id, product_id, quantity, unit_price)
    values
        ('401C7AB1-41CF-4756-8AF5-BE25CF2AE67B', 'F35B0053-855B-4145-ABE1-DC62BC1FDB96', 2, 5.45)
    into order_details(order_id, product_id, quantity, unit_price)
    values
        ('5011D206-8EFF-42C4-868E-F1A625E1F186', 'F35B0053-855B-4145-ABE1-DC62BC1FDB96', 1, 6.00)
    into order_details(order_id, product_id, quantity, unit_price)
    values
        ('0A48FFB0-EC61-4147-AF56-FC4DBCA8DE0A', 'F35B0053-855B-4145-ABE1-DC62BC1FDB96', 5, 6.00)
    into order_details(order_id, product_id, quantity, unit_price)
    values
        ('A243FA42-817A-44EC-8B67-22193D212D82', 'D5137D3A-894A-4109-9986-E982541B434F', 1, 45.45)
    into order_details(order_id, product_id, quantity, unit_price)
    values
        ('62CD4109-3E5D-40CC-8188-3899FC1F8BDF', 'D5137D3A-894A-4109-9986-E982541B434F', 4, 50.00)
    into order_details(order_id, product_id, quantity, unit_price)
    values
        ('9473A0BC-396A-4936-96B0-3EEA922AF36B', 'D5137D3A-894A-4109-9986-E982541B434F', 2, 55.00)
    into order_details(order_id, product_id, quantity, unit_price)
    values
        ('852E2DC9-4EC3-4225-A6F7-4F42F8FF728E', 'D5137D3A-894A-4109-9986-E982541B434F', 1, 45.45)
    into order_details(order_id, product_id, quantity, unit_price)
    values
        ('D6D8DDDC-4B0B-4D74-8EDC-A54E9B7F35F7', 'D5137D3A-894A-4109-9986-E982541B434F', 2, 50.00)
    into order_details(order_id, product_id, quantity, unit_price)
    values
        ('2C3FC180-D0DF-4D7B-A271-E6CCD2440393', 'D5137D3A-894A-4109-9986-E982541B434F', 2, 50.00)
    into order_details(order_id, product_id, quantity, unit_price)
    values
        ('5883CB62-D792-4EE3-ACBC-FE85B6BAA998', 'D5137D3A-894A-4109-9986-E982541B434F', 1, 55.00)
select * from dual;