package me.dhvakr.jpa.entity;

import jakarta.persistence.*;

@MappedSuperclass
public abstract class AbstractEntity {

    //~ Initializers =======================================================================================================================

    @Id
    @SequenceGenerator(name = "groot_id_seq", sequenceName = "groots_id_seq", allocationSize = 1, initialValue = 2)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "groot_id_seq") // The initial value is to account for data.sql data ids
    private int grootId;

    @Version
    private int version;

    //~ Methods ============================================================================================================================

    public int getId() {
        return grootId;
    }

    public void setId(int id) {
        this.grootId = id;
    }

    //~ ====================================================================================================================================

    public int getVersion() {
        return version;
    }
}
