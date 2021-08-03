CREATE TABLE category (
name varchar(32),
budgeted decimal(7, 2),
spent decimal(7, 2),
primary key (name));

CREATE TABLE expenditure (
datetime char(12), -- Dates will be recorded as mmddyyhhmmss
spending_category varchar(32),
action_amount decimal(7,2) NOT NULL,
memo varchar(64),
primary key (datetime),
foreign key (spending_category) references category(name),
CHECK (datetime LIKE "[0-1][0-9][0-3][0-9][0-9][0-9][0-9][0-9][0-5][0-9][0-5][0-9]"));

CREATE TABLE income (
datetime char(12), -- Dates will be recorded as mmddyyhhmmss
action_amount decimal(7,2) NOT NULL,
memo varchar(64),
primary key (datetime),
CHECK (datetime LIKE "[0-1][0-9][0-3][0-9][0-9][0-9][0-9][0-9][0-5][0-9][0-5][0-9]"));

CREATE TABLE monthly_expense (
memo varchar(32),
amount decimal(7,2),
months smallint DEFAULT -1,
primary key (memo));

CREATE TABLE monthly_income (
memo varchar(32),
amount decimal(7,2),
primary key (memo));

CREATE TABLE past_month (
date_code decimal(4) ZEROFILL, -- Recorded as mmyy
tot_income decimal(7,2),
tot_expense decimal(7,2),
primary key (date_code));

CREATE TABLE rollover_category (
name varchar(32),
baseline decimal(7,2),
primary key (name),
foreign key (name) references category(name));
