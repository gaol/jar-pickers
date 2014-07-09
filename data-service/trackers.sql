-- DDL for Postgresql server

    alter table Artifact 
        drop constraint FK_f6mejfa67masq4an2vaffx8fm;

    alter table ProductVersion 
        drop constraint FK_hrp5ti9rpa4n243w1yw9ycuxm;

    alter table ProductVersion_Artifact 
        drop constraint FK_p5k511n6hj1uwqjqhkoh1x2jp;

    alter table ProductVersion_Artifact 
        drop constraint FK_k8w3ldfo4ryxde05s0q5uw7ah;

    drop table if exists Artifact cascade;

    drop table if exists Component cascade;

    drop table if exists Product cascade;

    drop table if exists ProductVersion cascade;

    drop table if exists ProductVersion_Artifact cascade;

    drop sequence hibernate_sequence;

    drop sequence trackerseq;

    create table Artifact (
        id int8 not null,
        artifactId varchar(255),
        buildInfo varchar(255),
        groupId varchar(255),
        note varchar(255),
        type  varchar(10) DEFAULT 'jar',
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

    create table ProductVersion_Artifact (
        pvs_id int8 not null,
        artifacts_id int8 not null
    );

    alter table Artifact 
        add constraint UK_gysit1otv61ud70f81oqw1obf unique (groupId, artifactId, version);

    alter table Component 
        add constraint UK_ke0n78ll6pncf9xtqw2bt2i0t unique (name, version);

    alter table Product 
        add constraint UK_gxubutkbk5o2a6aakbe7q9kww unique (name);

    alter table ProductVersion 
        add constraint UK_k4scqst1t4ju2fxah7f3h6y40 unique (product_id, version);

    alter table Artifact 
        add constraint FK_f6mejfa67masq4an2vaffx8fm 
        foreign key (component_id) 
        references Component;

    alter table ProductVersion 
        add constraint FK_hrp5ti9rpa4n243w1yw9ycuxm 
        foreign key (product_id) 
        references Product;

    alter table ProductVersion_Artifact 
        add constraint FK_p5k511n6hj1uwqjqhkoh1x2jp 
        foreign key (artifacts_id) 
        references Artifact;

    alter table ProductVersion_Artifact 
        add constraint FK_k8w3ldfo4ryxde05s0q5uw7ah 
        foreign key (pvs_id) 
        references ProductVersion;

    create sequence hibernate_sequence minvalue 100;
    create sequence trackerseq minvalue 100;
