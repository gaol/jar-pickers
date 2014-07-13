
    alter table Artifact 
        drop 
        foreign key FK_f6mejfa67masq4an2vaffx8fm;

    alter table ProductVersion 
        drop 
        foreign key FK_hrp5ti9rpa4n243w1yw9ycuxm;

    alter table ProductVersion_Artifact 
        drop 
        foreign key FK_p5k511n6hj1uwqjqhkoh1x2jp;

    alter table ProductVersion_Artifact 
        drop 
        foreign key FK_k8w3ldfo4ryxde05s0q5uw7ah;

    drop table if exists Artifact;

    drop table if exists Component;

    drop table if exists Product;

    drop table if exists ProductVersion;

    drop table if exists ProductVersion_Artifact;

    create table Artifact (
        id bigint not null auto_increment,
        artifactId varchar(255),
        buildInfo varchar(255),
        groupId varchar(255),
        note varchar(255),
        type  varchar(10) DEFAULT 'jar',
        version varchar(255),
        component_id bigint,
        primary key (id)
    ) type=InnoDB;

    create table Component (
        id bigint not null auto_increment,
        description varchar(255),
        name varchar(255),
        scm varchar(255),
        version varchar(255),
        primary key (id)
    ) type=InnoDB;

    create table Product (
        id bigint not null auto_increment,
        description varchar(255),
        fullName varchar(255),
        name varchar(255),
        primary key (id)
    ) type=InnoDB;

    create table ProductVersion (
        id bigint not null auto_increment,
        note varchar(255),
        version varchar(255),
        product_id bigint,
        primary key (id)
    ) type=InnoDB;

    create table ProductVersion_Artifact (
        pvs_id bigint not null,
        artifacts_id bigint not null
    ) type=InnoDB;

    alter table Artifact 
        add constraint UK_gysit1otv61ud70f81oqw1obf unique (groupId, artifactId, version);

    alter table Component 
        add constraint UK_ke0n78ll6pncf9xtqw2bt2i0t unique (name, version);

    alter table Product 
        add constraint UK_gxubutkbk5o2a6aakbe7q9kww unique (name);

    alter table ProductVersion 
        add constraint UK_k4scqst1t4ju2fxah7f3h6y40 unique (product_id, version);

    alter table Artifact 
        add index FK_f6mejfa67masq4an2vaffx8fm (component_id), 
        add constraint FK_f6mejfa67masq4an2vaffx8fm 
        foreign key (component_id) 
        references Component (id);

    alter table ProductVersion 
        add index FK_hrp5ti9rpa4n243w1yw9ycuxm (product_id), 
        add constraint FK_hrp5ti9rpa4n243w1yw9ycuxm 
        foreign key (product_id) 
        references Product (id);

    alter table ProductVersion_Artifact 
        add index FK_p5k511n6hj1uwqjqhkoh1x2jp (artifacts_id), 
        add constraint FK_p5k511n6hj1uwqjqhkoh1x2jp 
        foreign key (artifacts_id) 
        references Artifact (id);

    alter table ProductVersion_Artifact 
        add index FK_k8w3ldfo4ryxde05s0q5uw7ah (pvs_id), 
        add constraint FK_k8w3ldfo4ryxde05s0q5uw7ah 
        foreign key (pvs_id) 
        references ProductVersion (id);
