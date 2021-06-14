CREATE TABLE category (
name varchar(32),
budgeted decimal(7, 2),
spent decimal(7, 2),
primary key (name));

CREATE TABLE transaction (
datetime char(12), -- Dates will be recorded as mmddyyhhmmss
action_type char(8) NOT NULL,
spending_category varchar(32),
action_amount decimal(7,2) NOT NULL,
memo varchar(64),
primary key (datetime),
foreign key (spending_category) references category(name),
CHECK (datetime LIKE "[0-1][0-9][0-3][0-9][0-9][0-9][0-9][0-9][0-5][0-9][0-5][0-9]"));
