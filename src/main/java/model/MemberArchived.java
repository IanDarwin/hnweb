package model;

import jakarta.persistence.*;
import org.apache.commons.beanutils.BeanUtils;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;

@Entity
@DiscriminatorValue("MemberArchived")
public class MemberArchived extends MemberBase {

    public String getArchivedReason() {
        return archivedReason;
    }

    public void setArchivedReason(String archivedReason) {
        this.archivedReason = archivedReason;
    }

    public LocalDate getArchivedDate() {
        return archivedDate;
    }

    public void setArchivedDate(LocalDate archivedDate) {
        this.archivedDate = archivedDate;
    }

    @Column(name = "archiveddate")
    private LocalDate archivedDate;

    @Column(name = "archivedreason")
    private String archivedReason;
    
    public MemberArchived(String reason) {
        archivedDate = LocalDate.now(); // Do this "now", avoid NPE later
        archivedReason = reason;
    }

    public MemberArchived() {
        this("Deleted");
    }
}
