package com.example.training.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "training_summaries")
@CompoundIndex(
        name = "trainer_name_surname_idx",
        def = "{'firstName': 1, 'lastName': 1}"
)
public class TrainerMonthlySummaryDocument
{
    @Id
    private String id;

    @Indexed(unique = true)
    private String username;

    private String firstName;

    private String lastName;

    private Boolean isActive;

    private List<YearSummary> years;

    public TrainerMonthlySummaryDocument() {}

    public String getUsername() {
        return username;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public List<YearSummary> getYears() {
        return years;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public void setYears(List<YearSummary> years) {
        this.years = years;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
}
