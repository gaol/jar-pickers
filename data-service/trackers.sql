
    alter table Artifact 
        drop constraint FKBA9C69F29BC9089D;

    alter table ProductVersion 
        drop constraint FKD1421909276FD01D;

    drop table if exists Artifact cascade;

    drop table if exists Component cascade;

    drop table if exists Product cascade;

    drop table if exists ProductVersion cascade;

    drop sequence hibernate_sequence;

    create table Artifact (
        id int8 not null,
        artifactId varchar(255),
        buildInfo varchar(255),
        groupId varchar(255),
        note varchar(255),
        version varchar(255),
        component_id int8,
        primary key (id)
    );

    create table Component (
        id int8 not null,
        description varchar(255),
        name varchar(255),
        scm varchar(255),
        version varchar(255),
        primary key (id)
    );

    create table Product (
        id int8 not null,
        description varchar(255),
        fullName varchar(255),
        name varchar(255),
        primary key (id)
    );

    create table ProductVersion (
        id int8 not null,
        note varchar(255),
        version varchar(255),
        product_id int8,
        primary key (id)
    );

    alter table Artifact 
        add constraint FKBA9C69F29BC9089D 
        foreign key (component_id) 
        references Component;

    alter table ProductVersion 
        add constraint FKD1421909276FD01D 
        foreign key (product_id) 
        references Product;

    create sequence hibernate_sequence;
